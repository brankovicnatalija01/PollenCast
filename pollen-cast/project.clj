(defproject pollen-cast "0.1.0"
  :description "Pollen Forecast Monitor for Serbia"
  :url "https://github.com/brankovicnatalija01/PollenCast"

 :dependencies [[org.clojure/clojure "1.11.1"]
                 [clj-http "3.12.3"]
                 [cheshire "5.12.0"]
                 [codax/codax "1.4.3"]
                 [org.uncomplicate/deep-diamond-base "0.43.0"]
                 [org.uncomplicate/deep-diamond-dnnl "0.43.0"]
                 [org.uncomplicate/neanderthal-base "0.61.0"]
                 [org.uncomplicate/neanderthal-mkl "0.61.0"]
                 [org.bytedeco/mkl "2025.3-1.5.13"
                  :classifier "linux-x86_64-redist"]]

  :main pollen-cast.core
  :target-path "target/%s"

  :jvm-opts ["-Djava.util.logging.config.file=logging.properties"]

  :profiles {:uberjar {:aot :all}})

