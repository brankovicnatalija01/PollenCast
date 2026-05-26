(ns pollen-cast.model
  (:require [uncomplicate.diamond.tensor :refer [desc]]
            [uncomplicate.diamond.dnn :refer [network init!
                                              fully-connected dropout]]
            [uncomplicate.diamond.native :refer :all]
            [clojure.set]))

;; Main model: 30 days input, 21 days output
(def input-size (* 30 32))
(def output-size (* 21 26))
(def batch-size 32)
(def window-size 30)
(def pred-size 21)

;; AI je korišćen za lakše razumevanje i pisanje koda za arhitekturu mreže, 
;; dok su dimenzije, funkcije za ravnanje vektora i izbor aktivacionih funkcija definisani samostalno.

(defn create-network
  ([] (create-network input-size output-size))
  ([in-size out-size]
   (let [net-spec [(fully-connected [512] :relu)
                   (dropout 0.3)
                   (fully-connected [256] :relu)
                   (dropout 0.3)
                   (fully-connected [128] :relu)
                   (fully-connected [out-size] :linear)]]
     (network (desc [batch-size in-size] :float :nc)
              net-spec))))

(defn flatten-input [window-input]
  (vec (mapcat identity window-input)))

(defn flatten-output [window-output]
  (vec (mapcat identity window-output)))

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

(def city-display->api
  {"Beograd - Zeleno Brdo"  "БЕОГРАД - ЗЕЛЕНО БРДО"
   "Beograd - Novi Beograd" "БЕОГРАД - НОВИ БЕОГРАД"
   "Kikinda"                "КИКИНДА"
   "Vrbas"                  "ВРБАС"
   "Vrsac"                  "ВРШАЦ"
   "Zajecar"                "ЗАЈЕЧАР"
   "Kragujevac"             "КРАГУЈЕВАЦ"
   "Krusevac"               "КРУШЕВАЦ"
   "Nis"                    "НИШ"
   "Vranje"                 "ВРАЊЕ"
   "Kraljevo"               "КРАЉЕВО"
   "Cacak"                  "ЧАЧАК"})

(def city-api->display
  (clojure.set/map-invert city-display->api))