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

(defn model-path
  ([city] (model-path city ""))
  ([city suffix]
   (str models-dir "/"
        (clojure.string/replace city " " "_")
        suffix
        ".bin")))

(defn stats-path
  ([city] (stats-path city ""))
  ([city suffix]
   (str models-dir "/"
        (clojure.string/replace city " " "_")
        suffix
        "_stats.edn")))

(defn model-exists?
  ([city] (model-exists? city ""))
  ([city suffix]
   (.exists (io/file (model-path city suffix)))))

;; AI je pomogao oko sintakse za Java FileChannel i StandardOpenOption parametara za rad sa binarnim datotekama

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

(defn save-model!
  ([net city] (save-model! net city ""))
  ([net city suffix]
   (io/make-parents (model-path city suffix))
   (spit (stats-path city suffix) (pr-str @prep/stats))
   (with-open [ch (open-channel-new (model-path city suffix))]
     (transfer! net ch))
   (println (str "Model saved: " (model-path city suffix)))))

(defn load-model!
  ([city] (load-model! city ""))
  ([city suffix]
   (when (model-exists? city suffix)
     (println (str "Loading model for " city "..."))
     (let [stats (read-string (slurp (stats-path city suffix)))]
       (reset! prep/stats stats))
     (let [net-bp (model/create-network)
           net    (init! (net-bp :adam))]
       (with-open [ch (open-channel-rw (model-path city suffix))]
         (transfer! ch net))
       (println (str "Model loaded for " city))
       net))))