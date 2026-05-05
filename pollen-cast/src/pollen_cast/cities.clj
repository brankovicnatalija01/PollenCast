(ns pollen-cast.cities
  (:require [pollen-cast.api :as api]
            [pollen-cast.preprocess :as prep]
            [pollen-cast.allergens :as allergens]
            [pollen-cast.model :as model]))

(defn pollen-level [value]
  (cond
    (= value 0)   "NONE"
    (< value 10)  "LOW"
    (< value 60)  "MEDIUM"
    (< value 100) "HIGH"
    :else         "VERY HIGH"))

(defn city-total-pollen [city]
  (let [entry (api/get-latest-pollen city)]
    (if (nil? entry)
      {:city city :total 0 :entry nil}
      (let [total (reduce + (map #(get entry % 0) prep/pollen-species))]
        {:city  city
         :total total
         :entry entry}))))

(defn show-top5-lowest [user]
  (println "\n======================================")
  (println "  TOP 5 CITIES — LOWEST POLLEN")
  (println "======================================")
  (println "Fetching data for all cities...")
  (let [allergy-profile (:allergy-profile user)
        city-data (mapv city-total-pollen model/supported-cities)
        sorted    (sort-by :total city-data)
        top5      (take 5 sorted)]
    (println "")
    (doseq [[rank {:keys [city total entry]}] (map-indexed vector top5)]
      (println (str (inc rank) ". "
                    (get model/city-api->display city city)
                    " — Total pollen: " (format "%.0f" (double total))))
      ;; Show user's allergens for this city
      (when (and (seq allergy-profile) (not (nil? entry)))
        (let [allergen-vals (for [allergen allergy-profile]
                              (let [allergen-kw (if (keyword? allergen)
                                                  allergen
                                                  (keyword (name allergen)))
                                    value (get entry allergen-kw 0)
                                    info  (get allergens/allergen-info allergen-kw)]
                                {:info info :value value}))
              with-pollen (filter #(> (:value %) 0) allergen-vals)]
          (when (seq with-pollen)
            (doseq [{:keys [info value]} (sort-by :value > with-pollen)]
              (println (format "   %-25s %6.0f  %s"
                               (:name-en info) (double value)
                               (pollen-level value)))))))
      (println ""))))