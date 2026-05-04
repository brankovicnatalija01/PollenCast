(ns pollen-cast.core
  (:gen-class)
  (:require [pollen-cast.cli :as cli]
            [pollen-cast.api :as api]
            [pollen-cast.preprocess :as prep]
            [pollen-cast.model :as model]
            [pollen-cast.train :as train]
            [uncomplicate.diamond.dnn :refer [infer!]]
            [uncomplicate.diamond.tensor :refer [tensor]]
            [uncomplicate.neanderthal.core :refer [transfer!]]))

#_ (defn -main []
  (cli/print-header)
  (let [user (cli/auth-menu)]
    (when user
      (cli/main-menu user)))
  (System/exit 0))

#_(defn -main []
  ;; Debug: check data distribution
  (let [data (prep/load-training-data "НИШ")
        all-values (mapcat :values data)
        nonzero (filter #(> % 0) all-values)
        total (count all-values)]
    (println "Total values:" total)
    (println "Non-zero values:" (count nonzero))
    (println "% non-zero:" (format "%.2f%%"
                                   (* 100.0 (/ (count nonzero) total))))
    (println "Max value:" (apply max all-values))
    (println "Mean non-zero:" (format "%.4f"
                                      (/ (reduce + nonzero)
                                         (count nonzero)))))
  (System/exit 0))

#_(defn -main []
  (let [net (train/train-model! "НИШ" 2000)]
    (println "Model trained!"))
  (System/exit 0))

(defn -main []
  (let [locations (map :name (api/get-locations))]
    (doseq [loc (take 30 locations)]
      (let [data (prep/load-training-data loc)]
        (println (str loc ": " (count data) " records")))))
  (System/exit 0))