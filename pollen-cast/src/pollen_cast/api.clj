(ns pollen-cast.api
  (:require [clj-http.client :as client]
            [cheshire.core :as json]))

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