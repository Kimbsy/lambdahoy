(ns lambdahoy.scene.transition
  (:require [lambdahoy.scene :as scene]))

(defn update-state
  [state]
  state)

(defn draw-state
  [state]
  nil)

(defn key-pressed
  [state e]
  state)

(defn key-released
  [state e]
  state)

(defn mouse-pressed
  [state e]
  state)

(defn mouse-released
  [state e]
  state)

(deftype Transition []
  scene/Scene
  (update-state [s state] (update-state state))
  (draw-state [s state] (draw-state state))
  (key-pressed [s state e] (key-pressed state e))
  (key-released [s state e] (key-released state e))
  (mouse-pressed [s state e] (mouse-pressed state e))
  (mouse-released [s state e] (mouse-released state e)))
