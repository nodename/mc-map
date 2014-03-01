(ns mc-map.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :refer [put! chan <! alts!]]))

(enable-console-print!)

(defn log [& more]
  (.log js/console (apply str more)))

(def SIXTEENTH_AND_MISSION (google.maps.LatLng. 37.764847 -122.420042))
(def ALPHABET_CITY (google.maps.LatLng. 40.724545 -73.979050))

;; state that is expected to have some significance outside of the Google Maps component:
(def app-state (atom {:directions nil
                      :points []}))

(defn add-mc-layer
  "Initialize and return the mc-layer"
  [the-map owner]
  (let [directions-service (google.maps.DirectionsService.)
        directions-handler (fn [response status]
                             (when (= status google.maps.DirectionsStatus.OK)
                               (put! (om/get-state owner :route) response)))
        calc-route (fn [start end]
                     (let [request #js {:origin start
                                        :destination end
                                        :travelMode google.maps.TravelMode.DRIVING}]
                       (.route directions-service request directions-handler)))
        mc-click-handler (fn [e]
                           ; (log (js->clj (.. e -latLng)))
                           (calc-route SIXTEENTH_AND_MISSION (.. e -latLng)))
        mc-layer (google.maps.KmlLayer. #js {:url "http://nodename.com/MotorcycleMeters_20130412.kml"
                                             :preserveViewport true ; don't zoom or position the map to the
                                                                    ; KmlLayer's bounds when showing the layer
                                             :suppressInfoWindows true ; don't let clickable features in the layer
                                                                       ; trigger display of InfoWindow objects
                                             })]
    (.setMap mc-layer the-map)
    (google.maps.event/addListener mc-layer "click" mc-click-handler)
    mc-layer))

(defn toggle-mc-layer
  [owner]
  (let [mc-layer (om/get-state owner :mc-layer)
        google-map (om/get-state owner :google-map)
        current-map (.-map mc-layer)]
    (.setMap mc-layer (if current-map nil google-map))))

(defn marker-from-point
  [point google-map]
  (google.Maps.Marker. #js {:position (google.Maps.LatLng (.latitude point) (.longitude point))
                            :map google-map
                            :title (.label point)}))

(defn update-markers
  [points {:keys [google-map markers] :as owner}]
  (map markers #(.setMap % nil))
  (let [new-markers (map #(marker-from-point % google-map) points)]
    (om/set-state! owner :markers new-markers)))

(defn map-view [app owner]
  "The Google Maps (plus a couple of buttons) component"
  (reify
    om/IInitState
    (init-state [_]
                {; channels for update requests:
                 :move (chan)  ; move map center
                 :toggle (chan) ; toggle display of mc-layer
                 :route (chan) ; update directions

                 ; view elements:
                 :google-map nil ; the Google Maps object
                 :mc-layer nil ; the motorcycle meters map layer
                 :directions-renderer nil
                 :center SIXTEENTH_AND_MISSION
                 :markers []})

    om/IWillMount
    (will-mount
     ;; establish a go-loop that will listen for requests and act on them:
     [_]
     (let [toggle (om/get-state owner :toggle)
           route (om/get-state owner :route)
           move (om/get-state owner :move)]
       (go (loop []
             (let [[value source] (alts! [toggle route move])]
               (condp = source
                 toggle (toggle-mc-layer owner)
                 route (om/update! app assoc-in [:directions] value)
                 move (do
                        ;; note: panTo has no effect when there are directions
                        (om/update! app assoc-in [:directions] nil)
                        (om/set-state! owner :center value))))
             (recur)))))

    om/IRenderState
    (render-state [this {:keys [move toggle google-map directions-renderer center]}]
                  (when-let [directions (:directions app)]
                    (.setDirections directions-renderer directions))
                  (when google-map
                    ;; note: panTo has no effect when there are directions
                    (.panTo google-map center))
                  (update-markers (:points app) owner)

                  (dom/div #js {:style #js {:width "100%" :height "100%"}}
                           (dom/div #js {:id "map-holder" :style #js {:width "80%" :height "100%" :float "left"}})
                           (dom/button #js {:width "20%" :float "left"
                                            :onClick (fn [e]
                                                       (put! toggle :request))}
                                       "Toggle MC Meters")
                           (dom/button #js {:width "20%" :float "left"
                                            :onClick (fn [e] (put! move ALPHABET_CITY))}
                                       "Move to New York")))

    om/IDidMount
    (did-mount [this node]
               (let [the-map (google.maps.Map. (. js/document (getElementById "map-holder"))
                                               #js {:center (om/get-state owner :center) :zoom 14})]
                 (om/set-state! owner :directions-renderer (google.maps.DirectionsRenderer. #js {:map the-map}))
                 (om/set-state! owner :mc-layer (add-mc-layer the-map owner))
                 (om/set-state! owner :google-map the-map)))))

(om/root
  app-state
  map-view
  (. js/document (getElementById "content")))

