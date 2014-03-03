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
    :builds [{:id "dev"
              :source-paths ["src"]
              :compiler {
                :output-to "mc_map.dev.js"
                :output-dir "out/dev"
                :externs ["externs/google_maps_api_v3.js"]
                :optimizations :none
                :source-map true}}

              {:id "prod"
               :source-paths ["src"]
               :compiler {:output-to "mc_map.prod.js"
                          :output-dir "out/prod"
                          :optimizations :advanced
                          :source-map "mc_map.prod.js.map"
                          :pretty-print false
                          :preamble ["react/react.min.js"]
                          :externs ["externs/google_maps_api_v3.js" "react/externs/react.js"]}}]})

