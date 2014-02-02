(ns mc-map.core
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]))

(enable-console-print!)

(defn log [& more]
  (.log js/console (apply str more)))

(def app-state (atom {:mapOptions #js {:center (google.maps.LatLng. -34.397 150.644)
                                       :zoom 8}}))

(defn map-view [app owner]
  (reify
    om/IRender
    (render [this]
            (log "render" this)
            ;; render has to return a component:
            (dom/div nil))

    om/IDidMount
    (did-mount [this node]
               (log "did-mount" (js->clj (:mapOptions app)))
               ;; node refers to the component I returned in render,
               ;; but I ignore it and attach the Map to its parentNode "map-canvas":
               (google.maps.Map. (.-parentNode node) (:mapOptions app)))))

(om/root
  app-state
  map-view
  (. js/document (getElementById "map-canvas")))

