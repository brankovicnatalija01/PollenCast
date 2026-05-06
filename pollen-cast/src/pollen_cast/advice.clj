(ns pollen-cast.advice
  (:require [pollen-cast.preprocess :as prep]
            [pollen-cast.allergens :as allergens]
            [pollen-cast.model :as model]
            [pollen-cast.forecast :as forecast]))

(defn potency-weight [potency]
  (case potency
    :very-high        5.0
    :high             4.0
    :moderate-to-high 3.0
    :moderate         2.0
    :low-to-moderate  1.5
    :low              1.0
    :very-low         0.5
    1.0))

(defn pollen-category [value potency]
  (let [low-threshold (if (= potency :very-high) 30.0 60.0)]
    (cond
      (>= value 100.0) :high
      (>= value low-threshold) :medium
      (> value 0.0)   :low
      :else           :none)))

(defn individual-risk [value potency]
  (if (< value 0.1)
    :none
    (let [category (pollen-category value potency)
          weight   (potency-weight potency)]
      (case category
        :high   (cond (>= weight 4.0) :very-high
                      (>= weight 2.0) :high
                      :else           :medium)
        :medium (cond (>= weight 4.0) :high
                      (>= weight 2.0) :medium
                      :else           :low)
        :low    (cond (>= weight 4.0) :medium
                      :else           :low)
        :none   :none))))

(defn risk-rank [level]
  (case level
    :very-high 4
    :high      3
    :medium    2
    :low       1
    :none      0
    0))

(defn risk-emoji [level]
  (case level
    :very-high "🔴 Very High"
    :high      "🟠 High"
    :medium    "🟡 Medium"
    :low       "🟢 Low"
    :none      "✅ None"
    "✅ None"))

(defn overall-advice [level]
  (case level
    :very-high "Stay indoors if possible. Keep windows closed.\n     Take antihistamines and consult your doctor."
    :high      "Limit time outdoors. Take antihistamines beforehand.\n     Shower after coming inside."
    :medium    "Consider taking antihistamines before going out.\n     Avoid peak hours (5am-10am)."
    :low       "Generally safe to go outside.\n     Monitor how you feel."
    "No significant pollen from your allergens today."))

(defn calculate-risk [entry allergy-profile]
  (if (empty? allergy-profile)
    {:overall :none :details []}
    (let [details (for [allergen allergy-profile]
                    (let [allergen-kw (if (keyword? allergen)
                                        allergen
                                        (keyword (name allergen)))
                          value    (double (get entry allergen-kw 0))
                          info     (get allergens/allergen-info allergen-kw)
                          potency  (:potency info)
                          risk     (individual-risk value potency)]
                      {:allergen  allergen-kw
                       :name-en   (:name-en info)
                       :name-sr   (:name-sr info)
                       :value     value
                       :potency   potency
                       :risk      risk}))
          sorted  (sort-by #(risk-rank (:risk %)) > details)
          worst   (first sorted)]
      {:overall (:risk worst)
       :details sorted})))

(defn show-advice [user]
  (println "\n======================================")
  (println (str "  SHOULD I GO OUTSIDE? — "
                (get model/city-api->display (:city user) (:city user))))
  (println "======================================")
  (let [fc (forecast/get-forecast (:city user))]
    (if (nil? fc)
      (println "Forecast unavailable.")
      (let [formatter  (java.time.format.DateTimeFormatter/ofPattern "yyyy-MM-dd")
            first-ld   (java.time.LocalDate/parse (:date (first fc)) formatter)
            last-meas  (.minusDays first-ld 1)
            today-ld   (java.time.LocalDate/now)
            lag        (.between java.time.temporal.ChronoUnit/DAYS last-meas today-ld)
            idx        (int (min (max (dec lag) 0) 20))
            day        (nth fc idx)
            date       (:date day)
            values     (:values day)
            entry      (zipmap prep/pollen-species values)
            profile    (:allergy-profile user)]
        (println (str "\nForecast for: " date
                      (when (> lag 21) " (max available)")))
        (if (empty? profile)
          (do
            (println "\nNo allergy profile set.")
            (println "Go to Edit Profile to add your allergens."))
          (let [{:keys [overall details]} (calculate-risk entry profile)]
            (println (str "Overall Risk: " (risk-emoji overall)))
            (println (str "\nAdvice: " (overall-advice overall)))
            (println "\nYour allergens breakdown:")
            (println (apply str (repeat 75 "-")))
            (println (format "%-25s | %-20s %-8s %-18s %s"
                             "Species (EN)" "Species (SR)"
                             "Value" "Potency" "Risk"))
            (println (apply str (repeat 75 "-")))
            (doseq [{:keys [name-en name-sr value potency risk]} details]
              (println (format "%-25s | %-20s %-8.1f %-18s %s"
                               name-en name-sr value
                               (clojure.string/replace (name potency) "-" " ")
                               (risk-emoji risk))))
            (println "\nTip: Peak pollen hours are 5am-10am.")
            (println "     Consider going out in the afternoon or after rain.")))))))