(ns mc-map.core
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]))

(enable-console-print!)

(def app-state (atom {:text "Hello world!"}))

(defn map-view [app owner]
  (reify
    om/IInitState
    (init-state [_]
        {:text "Hello world!"
          :mapOptions #js {:center (google.maps.LatLng. -34.397 150.644)
                           :zoom 8}})

    om/IRenderState
    (render-state [this state]
       (dom/div nil
          (dom/h1 nil (:text state))
          (google.maps.Map. (. js/document (getElementById "map-canvas")) (:mapOptions state))))))

(om/root
  app-state
  map-view
  (. js/document (getElementById "map-canvas")))

