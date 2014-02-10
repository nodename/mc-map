(ns mc-map.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :refer [put! chan <!]]))

(enable-console-print!)

(defn log [& more]
  (.log js/console (apply str more)))

(def SIXTEENTH_AND_MISSION (google.maps.LatLng. 37.764847 -122.420042))
(def ALPHABET_CITY (google.maps.LatLng. 40.724545 -73.979050))

(def app-state (atom {:mapOptions #js {:center SIXTEENTH_AND_MISSION
                                       :zoom 14}
                      :points []}))

(defn marker-from-point [point gmap]
  (google.Maps.Marker. #js {:position (google.Maps.LatLng (.latitude point) (.longitude point))
                            :map gmap
                            :title (.label point)}))

(defn update-markers [points {:keys [gmap markers] :as owner}]
  (map markers #(.setMap % nil))
  (let [new-markers (map #(marker-from-point % gmap) points)]
    (om/set-state! owner :markers new-markers)))

(defn map-view [app owner]
  (reify
    om/IInitState
    (init-state [_]
                {:move (chan)
                 :gmap nil
                 :markers []})

    om/IWillMount
    (will-mount [_]
      ;; establish a go loop that will listen for move events and
      ;; change the location in the app state using om/update! :
      (let [move (om/get-state owner :move)]
        (go (loop []
          (let [loc (<! move)]
            (om/update! app assoc-in [:mapOptions] {:center loc :zoom 14}))
            (recur)))))

    om/IRenderState
    (render-state [this {:keys [move gmap]}]
                  (when (and gmap (:center (:mapOptions app)))
                    (.panTo gmap (:center (:mapOptions app))))

                  (dom/div #js {:style #js {:width "100%" :height "100%"}}
                           (dom/div #js {:id "map-holder" :style #js {:width "80%" :height "100%" :float "left"}})
                           (dom/button #js {:width "20%" :float "left"
                                            :onClick (fn [e] (put! move ALPHABET_CITY))}
                                       "Move to New York")))

    om/IDidMount
    (did-mount [this node]
               (let [the-map (google.maps.Map. (. js/document (getElementById "map-holder"))
                                               (:mapOptions app))]
                 (om/set-state! owner :gmap the-map))
               (update-markers (:points app) owner))))

(om/root
  app-state
  map-view
  (. js/document (getElementById "content")))

