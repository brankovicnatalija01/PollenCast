(ns pollen-cast.model
  (:require [uncomplicate.diamond.tensor :refer [desc]]
          [uncomplicate.diamond.dnn :refer [network init!
                                            fully-connected dropout]]
          [uncomplicate.diamond.native :refer :all]
          [clojure.set]))

;; 14 days x 32 features (26 pollen + 6 cyclic date features)
(def input-size (* 14 32))

;; 7 days x 26 pollen species (output has no date features)
(def output-size (* 7 26))

(def batch-size 32)

;; Define network architecture
(defn create-network []
  (let [net-spec [(fully-connected [512] :relu)
                  (dropout 0.3)
                  (fully-connected [256] :relu)
                  (dropout 0.3)
                  (fully-connected [128] :relu)
                  (fully-connected [output-size] :linear)]]
    (network (desc [batch-size input-size] :float :nc)
             net-spec)))

;; Flatten 14 days of input into one vector of 448 floats
(defn flatten-input [window-input]
  (vec (mapcat identity window-input)))

;; Flatten 7 days of output into one vector of 182 floats
(defn flatten-output [window-output]
  (vec (mapcat identity window-output)))

(def city-display->api
  {"Beograd - Zeleno Brdo"  "БЕОГРАД - ЗЕЛЕНО БРДО"
   "Beograd - Novi Beograd" "БЕОГРАД - НОВИ БЕОГРАД"
   "Kikinda"                "КИКИНДА"
   "Vrbas"                  "ВРБАС"
   "Vršac"                  "ВРШАЦ"
   "Zaječar"                "ЗАЈЕЧАР"
   "Kragujevac"             "КРАГУЈЕВАЦ"
   "Kruševac"               "КРУШЕВАЦ"
   "Niš"                    "НИШ"
   "Vranje"                 "ВРАЊЕ"
   "Kraljevo"               "КРАЉЕВО"
   "Čačak"                  "ЧАЧАК"})

(def city-api->display
  (clojure.set/map-invert city-display->api))

(def supported-cities
  ["БЕОГРАД - ЗЕЛЕНО БРДО"
   "БЕОГРАД - НОВИ БЕОГРАД"
   "КИКИНДА"
   "ВРБАС"
   "ВРШАЦ"
   "ЗАЈЕЧАР"
   "КРАГУЈЕВАЦ"
   "КРУШЕВАЦ"
   "НИШ"
   "ВРАЊЕ"
   "КРАЉЕВО"
   "ЧАЧАК"])