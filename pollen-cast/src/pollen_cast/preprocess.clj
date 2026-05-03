(ns pollen-cast.preprocess
  (:require [pollen-cast.api :as api]))

;; All 26 pollen species in order
(def pollen-species
  [:ACER :ALNUS :AMBROSIA :ARTEMISIA :BETULA
   :CANNABACEAE :CARPINUS :CELTIS :CHENOP/AMAR.
   :CORYLUS :CUPRESS/TAXA. :FAGUS :FRAXINUS
   :JUGLANS :MORACEAE :PINACEAE :PLANTAGO
   :PLATANUS :POACEAE :POPULUS :QUERCUS
   :RUMEX :SALIX :TILIA :ULMACEAE :URTICACEAE])

;; Convert one API record to a vector of 26 floats
(defn record->vector [record]
  (mapv (fn [species]
          (float (get record species 0)))
        pollen-species))

;; Load all data for one location and year
(defn load-location-year [location year]
  (let [data (api/get-pollen-for-location year location)]
    (mapv (fn [record]
            {:date     (:date record)
             :location location
             :values   (record->vector record)})
          data)))

;; Load training data for one location across all years
(defn load-training-data [location]
  (println (str "Loading data for " location "..."))
  (mapcat (fn [year]
            (load-location-year location year))
          [2019 2020 2021 2022 2023 2024]))

;; Create sliding windows for training
;; Input: last 14 days, Output: next 7 days
(defn create-windows [records window-size pred-size]
  (let [values (mapv :values records)
        n      (count values)]
    (for [i (range (- n window-size pred-size))]
      {:input  (subvec values i (+ i window-size))
       :output (subvec values (+ i window-size)
                              (+ i window-size pred-size))})))

;; Max pollen value for normalization
(def max-pollen-value 800.0)

(defn normalize [v]
  (float (/ v max-pollen-value)))

(defn denormalize [v]
  (float (* v max-pollen-value)))

(defn normalize-record [record]
  (update record :values #(mapv normalize %)))

;; Load and normalize all training data for one location
(defn load-normalized-data [location]
  (map normalize-record (load-training-data location)))

;; Create normalized windows ready for training
(defn prepare-training-data [location]
  (let [data    (load-normalized-data location)
        windows (create-windows (vec data) 14 7)]
    (println (str "Prepared " (count windows) " training windows for " location))
    windows))