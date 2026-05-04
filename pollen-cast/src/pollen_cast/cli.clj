(ns pollen-cast.cli
  (:require [pollen-cast.data :as data]
            [pollen-cast.api :as api]
            [pollen-cast.allergens :as allergens]
            [pollen-cast.model :as model]))

;; ---- HELPERS ----

(defn print-header []
  (println "======================================")
  (println "         POLLENCAST v1.0")
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
  (let [indexed (map-indexed vector (keys allergens/allergen-info))]
    (doseq [[i species] indexed]
      (let [info (get allergens/allergen-info species)]
        (println (str (inc i) ". "
                      (:name-sr info)
                      " / " (:name-en info)
                      " — " (allergens/potency-label (:potency info))
                      " — " (:season info)))))
    (println "\n0. No allergies")
    (print "\n> ")
    (flush)
    (let [input (read-line)
          nums  (if (= input "0")
                  []
                  (map #(Integer/parseInt %)
                       (clojure.string/split (clojure.string/trim input) #"\s+")))]
      (mapv (fn [n] (first (nth indexed (dec n)))) nums))))

;; ---- REGISTER ----

(defn register []
  (println "\n--- REGISTER ---")
  (let [username (get-input "Choose a username: ")
        _        (when (data/get-user username)
                   (println "Username already exists!")
                   (System/exit 1))
        city     (select-city)
        profile  (select-allergy-profile)]
    (data/create-user! username city profile)
    (println (str "\nWelcome, " username "! Your account has been created."))
    (data/get-user username)))

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
      2 (register)
      0 (do (println "Goodbye!") (System/exit 0)))))

;; ---- POLLEN DISPLAY ----

(defn pollen-level [value]
  (cond
    (= value 0)   "NONE"
    (< value 10)  "LOW"
    (< value 50)  "MEDIUM"
    (< value 100) "HIGH"
    :else         "VERY HIGH"))

(defn show-today-pollen [user]
  (println "\n======================================")
  (println (str "  TODAY'S POLLEN — "
                (get model/city-api->display (:city user) (:city user))))
  (println "======================================")
  (let [entry (api/get-latest-pollen (:city user))]
    (if (nil? entry)
      (println "No data available.")
      (do
        (println (str "Last measurement: " (:date entry)))
        (println "")
        (println (format "%-20s %-8s %s" "Species" "Value" "Level"))
        (println (apply str (repeat 45 "-")))
        (doseq [species [:POACEAE :AMBROSIA :BETULA :URTICACEAE
                         :ALNUS :CORYLUS :FRAXINUS :QUERCUS]]
          (let [value (get entry species 0)]
            (when (> value 0)
              (println (format "%-20s %-8s %s"
                               (name species)
                               value
                               (pollen-level value))))))
        (println "")
        (println "Your allergens:")
        (doseq [allergen (:allergy-profile user)]
          (let [value  (get entry allergen 0)
                info   (get allergens/allergen-info allergen)]
            (println (str "  " (:name-en info) ": "
                          value " -> " (pollen-level value)))))))))

;; ---- MAIN MENU ----

(defn main-menu [user]
  (loop []
    (println (str "\n======================================"))
    (println (str "  POLLENCAST | "
                  (get model/city-api->display (:city user) (:city user))
                  " | " (:username user)))
    (println "======================================")
    (println "1. Today's pollen levels")
    (println "2. 7-day forecast")
    (println "3. Should I go outside today?")
    (println "4. Top 5 cities lowest pollen")
    (println "5. My allergy calendar")
    (println "6. Edit profile")
    (println "0. Exit")
    (case (read-int "\n> " #{0 1 2 3 4 5 6})
      1 (do (show-today-pollen user) (recur))
      2 (do (println "Coming soon...") (recur))
      3 (do (println "Coming soon...") (recur))
      4 (do (println "Coming soon...") (recur))
      5 (do (println "Coming soon...") (recur))
      6 (do (println "Coming soon...") (recur))
      0 (println "Goodbye!"))))