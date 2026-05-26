(ns pollen-cast.train
  (:require [uncomplicate.commons.core :refer [with-release let-release]]
            [uncomplicate.neanderthal.core :refer [transfer!]]
            [uncomplicate.diamond.tensor :refer [tensor]]
            [uncomplicate.diamond.dnn :refer [network init! train!
                                              fully-connected dropout]]
            [uncomplicate.diamond.native :refer :all]
            [pollen-cast.preprocess :as prep]
            [pollen-cast.model :as model] 
            [pollen-cast.persistence :as persist]))

;; AI je korišćen kao pomoć za logiku spajanja prozora i računanje pozicija

;; Convert windows to flat float arrays for training
(defn windows->matrices [windows]
  (let [n      (count windows)
        x-data (float-array (* n model/input-size))
        y-data (float-array (* n model/output-size))]
    (doseq [[i w] (map-indexed vector windows)]
      (let [flat-in  (model/flatten-input (:input w))
            flat-out (model/flatten-output (:output w))]
        (doseq [[j v] (map-indexed vector flat-in)]
          (aset x-data (+ (* i model/input-size) j) (float v)))
        (doseq [[j v] (map-indexed vector flat-out)]
          (aset y-data (+ (* i model/output-size) j) (float v)))))
    {:x x-data :y y-data :n n}))

;; AI je pomogao oko sintakse za alokaciju tenzora, funkcije transfer! i pokretanja train!, 
;; dok je logika toka treniranja, podela podataka na 80/20, računanje RMSE greške i čuvanje modela definisano samostalno.

(defn train-model! [location epochs]
  (println (str "Loading training data for " location "..."))
  (let [windows (prep/prepare-training-data location)
        n-train (int (* 0.8 (count windows)))
        train-w (vec (take n-train windows))]
    (println (str "Training samples: " n-train))
    (println "Preparing matrices...")
    (let [{:keys [x y n]} (windows->matrices train-w)
          net-bp (model/create-network)
          net    (init! (net-bp :adam))
          x-tz   (tensor [n model/input-size] :float :nc)
          y-tz   (tensor [n model/output-size] :float :nc)]
      (transfer! x x-tz)
      (transfer! y y-tz)
      (println "Starting training...")
      (doseq [round (range (quot epochs 10))]
        (let [loss (train! net x-tz y-tz :quadratic 10 [])
              std  (double (:std @prep/stats))
              rmse (* (Math/sqrt (double loss)) std)]
          (println (str "Epochs " (* round 10) "-" (* (inc round) 10)
                        " | loss: " (format "%.6f" (double loss))
                        " | RMSE: " (format "%.2f" rmse)
                        " pollen/m³"))))
      (println "Training complete!")
      (persist/save-model! net location)
      net)))

