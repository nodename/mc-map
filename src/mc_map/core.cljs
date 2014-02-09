(ns mc-map.core
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]))

(enable-console-print!)

(defn log [& more]
  (.log js/console (apply str more)))

(def SIXTEENTH_AND_MISSION (google.maps.LatLng. 37.764847 -122.420042))
(def ALPHABET_CITY (google.maps.LatLng. 40.724545 -73.979050))

(def app-state (atom {:mapOptions #js {:center SIXTEENTH_AND_MISSION
                                       :zoom 14}}))

(def *MAP* nil)

(defn move-to-NY [app]
  (log "move-to-NY")
  (om/update! app assoc-in [:mapOptions] #js {:center ALPHABET_CITY :zoom 14})
  ;(.panTo *MAP* ALPHABET_CITY)
  )

(defn map-view [app owner]
  (reify
    om/IRender
    (render [this]
            (log "render" this)
            ;; render has to return a component:
            (dom/div nil "DIV"))

    om/IDidMount
    (did-mount [this node]
               (log "did-mount" (js->clj (:mapOptions app)))
               ;; node refers to the component I returned in render,
               ;; but I ignore it and attach the Map to its parentNode "map-canvas":
               (let [the-map (google.maps.Map. (.-parentNode node) (:mapOptions app))]
                 (set! *MAP* the-map))
               (.addEventListener (. js/document (getElementById "map-button")) "click" (fn [e] (move-to-NY app)))
               ;(dom/button #js {:width "20%" :float "left" :onClick #(move-to-NY app)} "Move")
               )))

(om/root
  app-state
  map-view
  (. js/document (getElementById "map-canvas")))

