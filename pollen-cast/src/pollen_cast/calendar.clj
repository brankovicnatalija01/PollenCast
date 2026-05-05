(ns pollen-cast.calendar
  (:require [pollen-cast.allergens :as allergens]
            [clojure.string :as str]))

(def months
  {1  "January"   2  "February"  3  "March"
   4  "April"     5  "May"       6  "June"
   7  "July"      8  "August"    9  "September"
   10 "October"   11 "November"  12 "December"})

(defn current-month []
  (.getMonthValue (java.time.LocalDate/now)))

(defn potency-rank [potency]
  (case potency
    :very-high        6
    :high             5
    :moderate-to-high 4
    :moderate         3
    :low-to-moderate  2
    :low              1
    :very-low         0
    0))

(defn potency-circle [potency]
  (case potency
    :very-high        "🔴 Very High"
    :high             "🟠 High"
    :moderate-to-high "🟡 Moderate to High"
    :moderate         "🟡 Moderate"
    :low-to-moderate  "🟢 Low to Moderate"
    :low              "🟢 Low"
    :very-low         "🟢 Very Low"
    "⚪ Unknown"))

(defn parse-season-months [season-str]
  (let [month-map {"January" 1 "February" 2 "March" 3
                   "April" 4 "May" 5 "June" 6
                   "July" 7 "August" 8 "September" 9
                   "October" 10 "November" 11 "December" 12
                   "Late January" 1 "Early February" 2 "Late February" 2
                   "Early March" 3 "Late March" 3 "Mid March" 3
                   "Early April" 4 "Late April" 4 "Mid April" 4
                   "Early May" 5 "Late May" 5 "Mid May" 5
                   "Early June" 6 "Late June" 6 "Mid June" 6
                   "Early July" 7 "Late July" 7
                   "Early August" 8 "Late August" 8
                   "Early September" 9 "Late September" 9
                   "Early October" 10 "Late October" 10}
        parts     (clojure.string/split season-str #" - ")
        start     (get month-map (first parts) 1)
        end       (get month-map (last parts) 12)]
    {:start start :end end}))

(defn active-this-month? [season-str month]
  (let [{:keys [start end]} (parse-season-months season-str)]
    (and (>= month start) (<= month end))))

(defn sort-by-potency [allergens]
  (sort-by (fn [a]
             (let [allergen-kw (if (keyword? a) a (keyword (name a)))
                   info (get allergens/allergen-info allergen-kw)]
               (- (potency-rank (:potency info)))))
           allergens))

(defn show-calendar [user]
  (println "\n======================================")
  (println "  MY ALLERGY CALENDAR")
  (println "======================================")
  (let [profile   (:allergy-profile user)
        cur-month (current-month)]
    (if (empty? profile)
      (do
        (println "\nNo allergy profile set.")
        (println "Go to Edit Profile to add your allergens."))
      (do
        ;; Current month active allergens
        (println (str "\n  Active allergens this month ("
                      (get months cur-month) "):"))
        (println (apply str (repeat 70 "-")))
        (let [active (filter (fn [allergen]
                               (let [allergen-kw (if (keyword? allergen)
                                                   allergen
                                                   (keyword (name allergen)))
                                     info (get allergens/allergen-info allergen-kw)]
                                 (when info
                                   (active-this-month? (:season info) cur-month))))
                             profile)
              sorted (sort-by-potency active)]
          (if (empty? sorted)
            (println "  No allergens active this month!")
            (doseq [allergen sorted]
              (let [allergen-kw (if (keyword? allergen)
                                  allergen
                                  (keyword (name allergen)))
                    info (get allergens/allergen-info allergen-kw)]
                (println (format "  %-25s | %-20s | %-25s | %s"
                                 (:name-en info)
                                 (:name-sr info)
                                 (:season info)
                                 (potency-circle (:potency info))))))))

        ;; Full year calendar
        (println "\n")
        (println "  Full year overview:")
        (println (apply str (repeat 70 "-")))
        (doseq [month (range 1 13)]
          (let [active (filter (fn [allergen]
                                 (let [allergen-kw (if (keyword? allergen)
                                                     allergen
                                                     (keyword (name allergen)))
                                       info (get allergens/allergen-info allergen-kw)]
                                   (when info
                                     (active-this-month? (:season info) month))))
                               profile)
                sorted (sort-by-potency active)]
            (println "")
            (println (str (if (= month cur-month) "  ▶ " "    ")
                          (format "%-12s" (get months month))))
            (if (seq sorted)
              (doseq [allergen sorted]
                (let [allergen-kw (if (keyword? allergen)
                                    allergen
                                    (keyword (name allergen)))
                      info (get allergens/allergen-info allergen-kw)]
                  (println (format "      %-25s | %-20s | %s"
                                   (:name-en info)
                                   (:name-sr info)
                                   (potency-circle (:potency info))))))
              (println "      No active allergens"))))))))