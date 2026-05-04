(ns pollen-cast.forecast
  (:require [uncomplicate.diamond.dnn :refer [infer!]]
            [uncomplicate.diamond.tensor :refer [tensor]]
            [uncomplicate.neanderthal.core :refer [transfer!]]
            [pollen-cast.api :as api]
            [pollen-cast.preprocess :as prep]
            [pollen-cast.model :as model]
            [pollen-cast.persistence :as persist]
            [pollen-cast.allergens :as allergens]))

(def loaded-models (atom {}))

(defn get-model [city]
  (or (get @loaded-models city)
      (when (persist/model-exists? city)
        (let [net (persist/load-model! city)]
          (swap! loaded-models assoc city net)
          net))))

(defn pollen-level [value]
  (cond
    (< value 10)  "LOW"
    (< value 50)  "MEDIUM"
    (< value 100) "HIGH"
    :else         "VERY HIGH"))

(defn prepare-input [city]
  (let [raw-data (api/get-last-14-days city)]
    (when (= 14 (count raw-data))
      (let [stats     @prep/stats
            mean      (:mean stats)
            std       (:std stats)
            records   (mapv (fn [record]
                              {:date   (:date record)
                               :values (prep/record->raw record)})
                            raw-data)
            normed    (mapv #(prep/normalize-record % mean std) records)
            flat-in   (model/flatten-input (mapv :values normed))
            x-data    (float-array (* model/batch-size model/input-size))
            _         (doseq [[i v] (map-indexed vector flat-in)]
                        (aset x-data i (float v)))
            x-tz      (tensor [model/batch-size model/input-size] :float :nc)]
        (transfer! x-data x-tz)
        {:tensor    x-tz
         :last-date (:date (last records))}))))

(defn parse-predictions [raw-result last-date stats]
  (let [mean  (double (:mean stats))
        std   (double (:std stats))
        dates (prep/next-dates last-date 7)]
    (mapv (fn [day]
            (let [offset (* day 26)
                  values (mapv (fn [species-idx]
                                 (max 0.0 (prep/denormalize
                                            (double (nth raw-result
                                                         (+ offset species-idx)))
                                            mean std)))
                               (range 26))]
              {:date   (nth dates day)
               :values values}))
          (range 7))))

(defn get-forecast [city]
  (let [net (get-model city)]
    (if (nil? net)
      (do (println (str "No model for " city ". Train first."))
          nil)
      (when-let [input (prepare-input city)]
        (let [out-tz   (infer! net (:tensor input))
              raw      (float-array (* model/batch-size model/output-size))
              _        (transfer! out-tz raw)
              result   (vec raw)
              forecast (parse-predictions result (:last-date input) @prep/stats)]
          forecast)))))

(defn show-forecast [city allergy-profile]
  (println "\n======================================")
  (println (str "  7-DAY FORECAST — "
                (get model/city-api->display city city)))
  (println "======================================")
  (if-let [forecast (get-forecast city)]
    (do
      (doseq [{:keys [date values]} forecast]
        (println (str "\n  " date))
        (println (apply str (repeat 55 "-")))
        (let [nonzero (filter (fn [[_ val]] (> val 5.0))
                              (map-indexed vector values))]
          (if (empty? nonzero)
            (println "  No significant pollen expected.")
            (doseq [[idx val] (sort-by second > nonzero)]
              (let [species (nth prep/pollen-species idx)
                    info    (get allergens/allergen-info species)]
                (if info
                  (println (format "  %-25s | %-20s %6.1f  %s"
                                   (:name-en info) (:name-sr info)
                                   val (pollen-level val)))
                  (println (format "  %-48s %6.1f  %s"
                                   (name species) val (pollen-level val))))))))
        (when (seq allergy-profile)
          (println "  --- Your allergens ---")
          (let [allergen-data (for [allergen allergy-profile]
                                (let [allergen-kw (if (keyword? allergen)
                                                    allergen
                                                    (keyword (name allergen)))
                                      idx  (first (keep-indexed
                                                    (fn [i s] (when (= s allergen-kw) i))
                                                    prep/pollen-species))
                                      val  (if idx (nth values idx) 0.0)
                                      info (get allergens/allergen-info allergen-kw)]
                                  {:info info :val val}))
                sorted (sort-by :val > allergen-data)]
            (doseq [{:keys [info val]} sorted]
              (when info
                (println (format "  %-25s | %-20s %6.1f  %s"
                                 (:name-en info) (:name-sr info)
                                 val (pollen-level val)))))))))
    (println "Forecast unavailable.")))