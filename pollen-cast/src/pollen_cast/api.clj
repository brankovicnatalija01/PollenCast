(ns pollen-cast.api
  (:require [clj-http.client :as client]))

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

;; Get last 30 days for a location (for prediction input)
(defn get-last-30-days [location-name]
  (let [current-year (.getYear (java.time.LocalDate/now))
        data (get-pollen-for-location current-year location-name)]
    (vec (take-last 30 data))))

(defn get-latest-pollen [location-name]
  (let [current-year (.getYear (java.time.LocalDate/now))]
    (last (get-pollen-for-location current-year location-name))))
