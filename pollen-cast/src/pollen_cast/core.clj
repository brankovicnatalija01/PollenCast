(ns pollen-cast.core
  (:gen-class)
  (:require [pollen-cast.cli :as cli]))

(defn -main []
  (cli/print-header)
  (let [user (cli/auth-menu)]
    (when user
      (cli/main-menu user)))
  (System/exit 0))