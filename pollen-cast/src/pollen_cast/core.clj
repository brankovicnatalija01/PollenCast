(ns pollen-cast.core
  (:gen-class)
  (:require [pollen-cast.api :as api]))

(defn -main []
  (println "======================================")
  (println "         POLLENCAST v1.0")
  (println "   Pollen Forecast Monitor for Serbia")
  (println "======================================")
  (println "\nFetching locations...")
  (println "\nRecent pollen data for Nis:")
(let [recent (api/get-recent-pollen "НИШ")]
  (doseq [entry recent]
    (println (:date entry) 
             "POACEAE:" (:POACEAE entry)
             "AMBROSIA:" (:AMBROSIA entry))))
  (System/exit 0))