(defproject mc-map "0.1.0-SNAPSHOT"
  :description "Google Maps V3 application in Om"
  :url "http://example.com/FIXME"

  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-2156"]
                 [org.clojure/core.async "0.1.267.0-0d7780-alpha"]
                 [om "0.5.0"]]

  :plugins [[lein-cljsbuild "1.0.1"]]

  :source-paths ["src"]

  :cljsbuild {
    :builds [{:id "mc-map"
              :source-paths ["src"]
              :compiler {
                :output-to "mc_map.js"
                :output-dir "out"
                :externs ["externs/google_maps_api_v3.js"]
                :optimizations :none
                :source-map true}}]})
