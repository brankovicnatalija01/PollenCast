(ns pollen-cast.cli
  (:require [pollen-cast.data :as data]
            [pollen-cast.api :as api]))

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
  (let [locations (api/get-locations)
        indexed   (map-indexed vector locations)]
    (doseq [[i loc] indexed]
      (println (str (inc i) ". " (:name loc))))
    (let [choice (read-int "\n> " #(and (>= % 1) (<= % (count locations))))]
      (:name (nth locations (dec choice))))))

;; ---- ALLERGY PROFILE SELECTION ----

(defn select-allergy-profile []
  (println "\nSelect your allergy profile:")
  (println "1. No allergies")
  (println "2. Allergic to grass (POACEAE)")
  (println "3. Allergic to ambrosia (AMBROSIA)")
  (println "4. Allergic to birch (BETULA)")
  (println "5. Multiple - high allergenicity plants")
  (case (read-int "\n> " #{1 2 3 4 5})
    1 []
    2 [:POACEAE]
    3 [:AMBROSIA]
    4 [:BETULA]
    5 [:POACEAE :AMBROSIA :BETULA :URTICACEAE :ALNUS]))

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
          (if user
            user
            (recur)))
      2 (register)
      0 (do (println "Goodbye!") (System/exit 0)))))

;; ---- MAIN MENU ----

(defn main-menu [user]
  (loop []
    (println (str "\n======================================"))
    (println (str "  POLLENCAST | " (:city user) " | " (:username user)))
    (println "======================================")
    (println "1. Today's pollen levels")
    (println "2. 7-day forecast")
    (println "3. Should I go outside today?")
    (println "4. Top 5 cities lowest pollen")
    (println "5. My allergy calendar")
    (println "6. Edit profile")
    (println "0. Exit")
    (case (read-int "\n> " #{0 1 2 3 4 5 6})
      1 (do (println "Coming soon...") (recur))
      2 (do (println "Coming soon...") (recur))
      3 (do (println "Coming soon...") (recur))
      4 (do (println "Coming soon...") (recur))
      5 (do (println "Coming soon...") (recur))
      6 (do (println "Coming soon...") (recur))
      0 (println "Goodbye!"))))