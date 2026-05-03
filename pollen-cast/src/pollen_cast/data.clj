(ns pollen-cast.data
  (:require [codax.core :as db]))

;; Open local database
(def database (db/open-database! "data/pollencast-db"))

;; ---- USERS ----

(defn create-user! [username city allergy-profile]
  (db/assoc-at! database [:users username]
                {:username        username
                 :city            city
                 :allergy-profile allergy-profile}))

(defn get-user [username]
  (db/get-at! database [:users username]))

(defn update-user! [username updates]
  (db/merge-at! database [:users username] updates))