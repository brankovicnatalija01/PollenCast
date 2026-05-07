(ns pollen-cast.cli
  (:require [pollen-cast.data :as data]
            [pollen-cast.api :as api]
            [pollen-cast.allergens :as allergens]
            [pollen-cast.model :as model]
            [pollen-cast.forecast :as forecast]
            [pollen-cast.preprocess :as prep]
            [pollen-cast.advice :as advice]
            [pollen-cast.cities :as cities]
            [pollen-cast.calendar :as calendar] 
            [clojure.string :as str]
            [pollen-cast.allergen-info :as allergen-info]))

;; ---- HELPERS ----

(defn print-header []
  (println "======================================")
  (println "         POLLENCAST ")
  (println "   Pollen Forecast Monitor for Serbia")
  (println "======================================"))

(defn get-input [prompt]
  (print prompt)
  (flush)
  (read-line))

(defn read-int [prompt valid?]
  (loop []
    (let [input (get-input prompt)
          n     (try (Integer/parseInt input)
                     (catch Exception _ nil))]
      (if (and n (valid? n))
        n
        (do (println "Invalid input, try again.")
            (recur))))))

;; ---- CITY SELECTION ----

(defn select-city []
  (println "\nSelect your city:")
  (let [cities  model/supported-cities
        indexed (map-indexed vector cities)]
    (doseq [[i city] indexed]
      (println (str (inc i) ". " (get model/city-api->display city city))))
    (let [choice (read-int "\n> " #(and (>= % 1) (<= % (count cities))))]
      (nth cities (dec choice)))))

;; ---- ALLERGY PROFILE SELECTION ----

(defn select-allergy-profile []
  (println "\nSelect your allergens:")
  (println "(enter numbers separated by space, or 0 for none)")
  (println "")
  (let [species-list (vec (keys allergens/allergen-info))
        indexed      (map-indexed vector species-list)]
    (doseq [[i species] indexed]
      (let [info (get allergens/allergen-info species)]
        (println (str (inc i) ". " (:name-en info) " | " (:name-sr info)))))
    (println "\n0. No allergies")
    (print "\n> ")
    (flush)
    (let [input (read-line)
          nums  (if (= input "0")
                  []
                  (map #(Integer/parseInt %)
                       (str/split (str/trim input) #"\s+")))]
      (mapv (fn [n] (nth species-list (dec n))) nums))))

;; ---- REGISTER ----

(defn register []
  (println "\n--- REGISTER ---")
  (let [username (get-input "Choose a username: ")]
    (if (data/get-user username)
      (do (println "Username already exists! Please try a different username.")
          nil)
      (let [city    (select-city)
            profile (select-allergy-profile)]
        (data/create-user! username city profile)
        (println (str "\nWelcome, " username "! Your account has been created."))
        (data/get-user username)))))

;; ---- LOGIN ----

(defn login []
  (println "\n--- LOGIN ---")
  (let [username (get-input "Username: ")
        user     (data/get-user username)]
    (if user
      (do (println (str "Welcome back, " username "!"))
          user)
      (do (println "User not found. Please register first.")
          nil))))

;; ---- AUTH MENU ----

(defn auth-menu []
  (loop []
    (println "\n1. Login")
    (println "2. Register")
    (println "0. Exit")
    (case (read-int "\n> " #{0 1 2})
      1 (let [user (login)]
          (if user user (recur)))
      2 (let [user (register)]
          (if user user (recur)))
      0 (do (println "Goodbye!") (System/exit 0)))))

;; ---- POLLEN DISPLAY ----

(defn pollen-level [value]
  (cond
    (< value 0.1) "NONE"
    (< value 10)  "LOW"
    (< value 60)  "MEDIUM"
    (< value 100) "HIGH"
    :else         "VERY HIGH"))

(defn show-today-pollen [user]
  (println "\n======================================")
  (println (str "  TODAY'S POLLEN — "
                (get model/city-api->display (:city user) (:city user))))
  (println "======================================")
  (let [latest (api/get-latest-pollen (:city user))]
    (when latest
      (println (str "Last measurement: " (:date latest)))))
  (let [forecast (forecast/get-forecast (:city user))]
    (if (nil? forecast)
      (println "Forecast unavailable.")
      (let [formatter  (java.time.format.DateTimeFormatter/ofPattern "yyyy-MM-dd")
            first-ld   (java.time.LocalDate/parse (:date (first forecast)) formatter)
            last-meas  (.minusDays first-ld 1)
            today-ld   (java.time.LocalDate/now)
            lag        (.between java.time.temporal.ChronoUnit/DAYS last-meas today-ld)
            idx        (int (min (max (dec lag) 0) 20))
            today-data (nth forecast idx)
            values     (:values today-data)
            entry      (zipmap prep/pollen-species values)]
        (println (str "Predicted for: " (:date today-data)))
        (println "")
        (println (format "%-25s   %-20s %-8s %s" "Species (EN)" "Species (SR)" "Value" "Level"))
        (println (apply str (repeat 65 "-")))
        ;; Show all species with value > 5
        (let [all-species (filter (fn [species]
                                    (> (get entry species 0) 5.0))
                                  prep/pollen-species)
              sorted      (sort-by #(get entry % 0) > all-species)]
          (if (empty? sorted)
            (println "No significant pollen today.")
            (doseq [species sorted]
              (let [value (get entry species 0)
                    info  (get allergens/allergen-info species)]
                (if info
                  (println (format "%-25s | %-20s %-8.1f %s"
                                   (:name-en info) (:name-sr info)
                                   value (pollen-level value)))
                  (println (format "%-48s %-8.1f %s"
                                   (name species) value (pollen-level value))))))))
        (println "")
        (println "Your allergens:")
        (println (apply str (repeat 65 "-")))
        ;; Show ALL user allergens with their values
        (let [allergen-data (for [allergen (:allergy-profile user)]
                              (let [allergen-kw (if (keyword? allergen)
                                                  allergen
                                                  (keyword (name allergen)))
                                    value (get entry allergen-kw 0)
                                    info  (get allergens/allergen-info allergen-kw)]
                                {:info info :value value}))
              sorted (sort-by :value > allergen-data)]
          (doseq [{:keys [info value]} sorted]
            (when info
              (println (format "%-25s | %-20s %-8.1f %s"
                               (:name-en info) (:name-sr info)
                               value (pollen-level value))))))))))

;; ---- EDIT PROFILE ----

(defn edit-profile [user]
  (println "\n--- EDIT PROFILE ---")
  (println "1. Change city")
  (println "2. Change allergy profile")
  (println "0. Back")
  (case (read-int "\n> " #{0 1 2})
    1 (let [new-city (select-city)]
        (data/update-user! (:username user) {:city new-city})
        (println (str "\nCity updated to: "
                      (get model/city-api->display new-city new-city)))
        (assoc user :city new-city))
    2 (let [new-profile (select-allergy-profile)]
        (data/update-user! (:username user) {:allergy-profile new-profile})
        (println "\nAllergy profile updated!")
        (assoc user :allergy-profile new-profile))
    0 user))

;; ---- MAIN MENU ----

(defn main-menu [user]
  (loop [user user]
    (println "\n======================================")
    (println (str "  POLLENCAST | "
                  (get model/city-api->display (:city user) (:city user))
                  " | " (:username user)))
    (println "======================================")
    (println "1. Today's pollen levels")
    (println "2. 7-day forecast")
    (println "3. Should I go outside today?")
    (println "4. Top 5 cities lowest pollen")
    (println "5. My allergy calendar")
    (println "6. Allergen information")
    (println "7. Edit profile")
    (println "8. Logout")
    (println "0. Exit")
    (case (read-int "\n> " #{0 1 2 3 4 5 6 7 8})
      1 (do (show-today-pollen user) (recur user))
      2 (do (forecast/show-forecast (:city user) (:allergy-profile user)) (recur user))
      3 (do (advice/show-advice user) (recur user))
      4 (do (cities/show-top5-lowest user) (recur user))
      5 (do (calendar/show-calendar user) (recur user))
      6 (do (allergen-info/show-allergen-info user) (recur user))
      7 (recur (edit-profile user))
      8 (do (println "\nLogged out successfully!") nil)
      0 (do (println "Goodbye!") (System/exit 0)))))