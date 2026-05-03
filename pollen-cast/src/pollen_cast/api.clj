(ns pollen-cast.api
  (:require [clj-http.client :as client ]))

(def base-url "http://77.46.150.200")

(defn get-locations []
  (let [response (client/get (str base-url "/api/opendata/locations/")
                             {:as :json})]
    (:body response)))

(defn get-allergens []
  (let [response (client/get (str base-url "/api/opendata/allergens/")
                             {:as :json})]
    (:body response)))

(defn get-pollen-by-year [year]
  (let [response (client/get (str base-url "/api/opendata/pollens/" year "/")
                             {:as :json})]
    (:body response)))

(defn get-pollen-for-location [year location-name]
  (let [all-data (get-pollen-by-year year)]
    (filter #(= (:location %) location-name) all-data)))

(defn get-recent-pollen [location-name]
  (let [current-year 2026
data (get-pollen-for-location current-year location-name)]
    (take-last 7 data)))

(defn get-latest-pollen [location-name]
  (last (get-recent-pollen location-name)))

