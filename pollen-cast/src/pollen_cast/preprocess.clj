(ns pollen-cast.preprocess
  (:require [pollen-cast.api :as api]))

;; All 26 pollen species in fixed order
(def pollen-species
  [:ACER :ALNUS :AMBROSIA :ARTEMISIA :BETULA
   :CANNABACEAE :CARPINUS :CELTIS :CHENOP/AMAR.
   :CORYLUS :CUPRESS/TAXA. :FAGUS :FRAXINUS
   :JUGLANS :MORACEAE :PINACEAE :PLANTAGO
   :PLATANUS :POACEAE :POPULUS :QUERCUS
   :RUMEX :SALIX :TILIA :ULMACEAE :URTICACEAE])

;; 26 pollen + 6 date features
(def features-per-day 32)

;; Global stats computed from training data
(def stats (atom nil))

;; Z-score normalization
(defn compute-stats [records]
  (let [all-pollen (mapcat (fn [r]
                             (take 26 (:values r)))
                           records)
        n     (count all-pollen)
        mean  (float (/ (reduce + all-pollen) n))
        std   (float (Math/sqrt
                       (/ (reduce + (map #(Math/pow (- % mean) 2)
                                         all-pollen))
                          n)))
        std   (float (max std 1.0))]
    {:mean mean :std std}))

(defn normalize [v mean std]
  (float (/ (- v mean) std)))

(defn denormalize [v mean std]
  (float (+ (* v std) mean)))

;; AI je korišćen kao pomoć za pisanje formula kod cikličnog kodiranja datuma.

;; Cyclic encoding for seasonal patterns
(defn cyclic-encode [value max-value]
  [(float (Math/sin (* 2 Math/PI (/ value max-value))))
   (float (Math/cos (* 2 Math/PI (/ value max-value))))])

;; Extract 6 date features from a date string "yyyy-MM-dd"
(defn date-features [date-str]
  (let [local-date  (java.time.LocalDate/parse date-str)
        month       (.getMonthValue local-date)
        day-of-year (.getDayOfYear local-date)
        day-of-week (.getValue (.getDayOfWeek local-date))]
    (vec (concat
          (cyclic-encode month 12)
          (cyclic-encode day-of-year 365)
          (cyclic-encode day-of-week 7)))))

;; Convert one API record to raw (unnormalized) pollen values + date features
(defn record->raw [record]
  (let [pollen-vals (mapv (fn [species]
                            (float (get record species 0)))
                          pollen-species)
        date-vals   (date-features (:date record))]
    (vec (concat pollen-vals date-vals))))

;; Normalize a record's pollen values using z-score
(defn normalize-record [record mean std]
  (update record :values
          (fn [vals]
            (vec (concat
                  (mapv #(normalize % mean std) (take 26 vals))
                  (drop 26 vals))))))

;; Load all data for one location and year
(defn load-location-year [location year]
  (let [data (api/get-pollen-for-location year location)]
    (mapv (fn [record]
            {:date     (:date record)
             :location location
             :values   (record->raw record)})
          data)))

;; Load training data for one location across all years
(defn load-training-data [location]
  (println (str "Loading data for " location "..."))
  (vec (mapcat (fn [year]
                 (load-location-year location year))
               [2019 2020 2021 2022 2023 2024])))

;; AI je korišćen kao pomoć za logiku sečenja vektora i ispravno računanje indeksa kod kreiranja kliznih prozora. 

;; Create sliding windows preserving dates
(defn create-windows [records window-size pred-size]
  (let [n (count records)]
    (vec (for [i (range (- n window-size pred-size))]
           (let [input-records  (subvec records i (+ i window-size))
                 output-records (subvec records
                                        (+ i window-size)
                                        (+ i window-size pred-size))]
             {:input-dates  (mapv :date input-records)
              :output-dates (mapv :date output-records)
              :input        (mapv :values input-records)
              ;; Output: only 26 pollen values (normalized)
              :output       (mapv (fn [r]
                                    (vec (take 26 (:values r))))
                                  output-records)})))))

(defn active-season? [record]
  (let [month (-> (:date record)
                  (java.time.LocalDate/parse)
                  (.getMonthValue))]
    (and (>= month 3) (<= month 10))))

(defn prepare-training-data
  ([location] (prepare-training-data location 30 21))
  ([location window-size pred-size]
   (let [data    (load-training-data location)
         active  (vec (filter active-season? data))
         s       (compute-stats active)
         _       (reset! stats s)
         _       (println (str "Stats - mean: " (format "%.4f" (double (:mean s)))
                               " std: " (format "%.4f" (double (:std s)))))
         normed  (mapv #(normalize-record % (:mean s) (:std s)) active)
         windows (create-windows normed window-size pred-size)]
     (println (str "Active season records: " (count active)))
     (println (str "Prepared " (count windows)
                   " training windows for " location))
     windows)))

;; Generate next n dates after a given date string
(defn next-dates [last-date n]
  (let [formatter (java.time.format.DateTimeFormatter/ofPattern "yyyy-MM-dd")
        start     (java.time.LocalDate/parse last-date formatter)]
    (mapv (fn [i]
            (.toString (.plusDays start (inc i))))
          (range n))))