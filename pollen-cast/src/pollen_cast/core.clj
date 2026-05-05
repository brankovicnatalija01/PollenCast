(ns pollen-cast.core
  (:gen-class)
  (:require [pollen-cast.cli :as cli]))


(defn -main []
  (cli/print-header)
  (loop []
    (let [user (cli/auth-menu)]
      (when user
        (cli/main-menu user)
        (recur)))))

