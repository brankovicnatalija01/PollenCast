(ns pollen-cast.core
  (:gen-class)
  (:require [pollen-cast.cli :as cli]
            [pollen-cast.preprocess :as prep]))

#_ (defn -main []
  (cli/print-header)
  (let [user (cli/auth-menu)]
    (when user
      (cli/main-menu user)))
  (System/exit 0))

(defn -main []
  (let [windows (prep/prepare-training-data "НИШ")]
  (println "First window input (day 1):"
           (take 5 (first (:input (first windows)))))
  (println "First window output (day 1):"
           (take 5 (first (:output (first windows))))))
  (System/exit 0))