(ns pollen-cast.persistence
  (:require [uncomplicate.neanderthal.core :refer [transfer!]]
            [uncomplicate.diamond.dnn :refer [init!]]
            [uncomplicate.diamond.native :refer :all]
            [pollen-cast.model :as model]
            [pollen-cast.preprocess :as prep]
            [clojure.java.io :as io])
  (:import [java.nio.channels FileChannel]
           [java.nio.file Paths StandardOpenOption]))

(def models-dir "models")

(defn model-path [city]
  (str models-dir "/"
       (clojure.string/replace city " " "_")
       ".bin"))

(defn stats-path [city]
  (str models-dir "/"
       (clojure.string/replace city " " "_")
       "_stats.edn"))

(defn model-exists? [city]
  (.exists (io/file (model-path city))))

(defn open-channel-rw [path]
  (FileChannel/open
    (Paths/get path (into-array String []))
    (into-array StandardOpenOption
                [StandardOpenOption/READ
                 StandardOpenOption/WRITE
                 StandardOpenOption/CREATE])))

(defn open-channel-new [path]
  (FileChannel/open
    (Paths/get path (into-array String []))
    (into-array StandardOpenOption
                [StandardOpenOption/READ
                 StandardOpenOption/WRITE
                 StandardOpenOption/CREATE
                 StandardOpenOption/TRUNCATE_EXISTING])))

(defn save-model! [net city]
  (io/make-parents (model-path city))
  (spit (stats-path city) (pr-str @prep/stats))
  (with-open [ch (open-channel-new (model-path city))]
    (transfer! net ch))
  (println (str "Model saved: " (model-path city))))

(defn load-model! [city]
  (when (model-exists? city)
    (println (str "Loading model for " city "..."))
    (let [stats (read-string (slurp (stats-path city)))]
      (reset! prep/stats stats))
    (let [net-bp (model/create-network)
          net    (init! (net-bp :adam))]
      (with-open [ch (open-channel-rw (model-path city))]
        (transfer! ch net))
      (println (str "Model loaded for " city))
      net)))