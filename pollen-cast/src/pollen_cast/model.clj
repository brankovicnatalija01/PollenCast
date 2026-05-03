(ns pollen-cast.model
  (:require [uncomplicate.diamond.tensor :refer [tensor desc]]
            [uncomplicate.diamond.dnn :refer [network init! train! cost
                                              fully-connected dropout]]
            [uncomplicate.diamond.native :refer :all]))

;; Network dimensions
(def input-size (* 14 26))   ;; 14 days x 26 species
(def output-size (* 7 26))   ;; 7 days x 26 species
(def batch-size 32)

;; Define network architecture
(defn create-network []
  (let [net-spec [(fully-connected [512] :relu)
                  (dropout 0.3)
                  (fully-connected [256] :relu)
                  (dropout 0.3)
                  (fully-connected [128] :relu)
                  (fully-connected [output-size] :sigmoid)]]
    (network (desc [batch-size input-size] :float :nc)
             net-spec)))

(defn flatten-input [window-input]
  (vec (mapcat identity window-input)))

(defn flatten-output [window-output]
  (vec (mapcat identity window-output)))