(ns pollen-cast.core
  (:gen-class)
  (:require [pollen-cast.cli :as cli]
            [pollen-cast.preprocess :as prep]
            [pollen-cast.model :as model]))

#_ (defn -main []
  (cli/print-header)
  (let [user (cli/auth-menu)]
    (when user
      (cli/main-menu user)))
  (System/exit 0))

(defn -main []
(println "Input size:" model/input-size)
(println "Output size:" model/output-size)
  (System/exit 0))