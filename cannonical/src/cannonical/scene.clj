(ns cannonical.scene
  (:require [cannonical.utils :as u]))

(defprotocol Scene
  (update-state [s state])
  (draw-state [s state])
  (key-pressed [s state e])
  (key-released [s state e])
  (mouse-pressed [s state e])
  (mouse-released [s state e]))

(defn update-state*
  [state]
  (update-state (:current-scene state) state))

(defn draw-state*
  [state]
  (draw-state (:current-scene state) state))

(defn key-pressed*
  [state e]
  (key-pressed (:current-scene state) (u/add-held-key state e) e))

(defn key-released*
  [state e]
  (key-released (:current-scene state) (u/remove-held-key state e) e))

(defn mouse-pressed*
  [state e]
  (mouse-pressed (:current-scene state) state e))

(defn mouse-released*
  [state e]
  (mouse-released (:current-scene state) state e))
