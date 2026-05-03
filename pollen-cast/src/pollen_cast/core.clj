(ns pollen-cast.core
  (:gen-class)
  (:require [pollen-cast.api :as api]))

(defn -main []
  (println "======================================")
  (println "         POLLENCAST v1.0")
  (println "   Pollen Forecast Monitor for Serbia")
  (println "======================================")
  (println "\nFetching locations...")
  (let [locations (api/get-locations)]
    (doseq [loc locations]
      (println (:name loc))))
  (System/exit 0))