(defproject pollen-cast "0.1.0"
  :description "Pollen Forecast Monitor for Serbia"
  :url "https://github.com/brankovicnatalija01/PollenCast"

  :dependencies [[org.clojure/clojure "1.11.1"]
                 [clj-http "3.12.3"]
                 [cheshire "5.12.0"]
                 [codax/codax "1.4.3"]]

  :main pollen-cast.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
