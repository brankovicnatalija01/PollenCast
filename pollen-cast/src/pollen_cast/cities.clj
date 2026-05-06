(ns pollen-cast.cities
  (:require [pollen-cast.api :as api]
            [pollen-cast.preprocess :as prep]
            [pollen-cast.allergens :as allergens]
            [pollen-cast.model :as model]
            [pollen-cast.forecast :as forecast]))

(defn pollen-level [value]
  (cond
    (< value 0.1)  "NONE"
    (< value 10)   "LOW"
    (< value 60)   "MEDIUM"
    (< value 100)  "HIGH"
    :else          "VERY HIGH"))

(defn city-today-pollen [city]
  (let [fc (forecast/get-forecast city)]
    (if (nil? fc)
      {:city city :total 0 :entry nil}
      (let [formatter  (java.time.format.DateTimeFormatter/ofPattern "yyyy-MM-dd")
            first-ld   (java.time.LocalDate/parse (:date (first fc)) formatter)
            last-meas  (.minusDays first-ld 1)
            today-ld   (java.time.LocalDate/now)
            lag        (.between java.time.temporal.ChronoUnit/DAYS last-meas today-ld)
            idx        (int (min (max (dec lag) 0) 20))
            today-data (nth fc idx)
            values     (:values today-data)
            entry      (zipmap prep/pollen-species values)
            total      (reduce + values)]
        {:city  city
         :total total
         :entry entry}))))

(defn show-top5-lowest [user]
  (println "\n======================================")
  (println "  TOP 5 CITIES — LOWEST POLLEN")
  (println "======================================")
  (println "Fetching data for all cities...")
  (let [allergy-profile (:allergy-profile user)
        city-data (mapv city-today-pollen model/supported-cities)
        sorted    (sort-by :total city-data)
        top5      (take 5 sorted)]
    (println "")
    (doseq [[rank {:keys [city total entry]}] (map-indexed vector top5)]
      (println (str (inc rank) ". "
                    (get model/city-api->display city city)
                    " — Total pollen: " (format "%.0f" (double total))))
      (when (and (seq allergy-profile) (not (nil? entry)))
        (let [allergen-vals (for [allergen allergy-profile]
                              (let [allergen-kw (if (keyword? allergen)
                                                  allergen
                                                  (keyword (name allergen)))
                                    value (get entry allergen-kw 0)
                                    info  (get allergens/allergen-info allergen-kw)]
                                {:info info :value value}))
              with-pollen (filter #(> (:value %) 0.1) allergen-vals)]
          (when (seq with-pollen)
            (doseq [{:keys [info value]} (sort-by :value > with-pollen)]
              (println (format "   %-25s %6.1f  %s"
                               (:name-en info) (double value)
                               (pollen-level value)))))))
      (println ""))))