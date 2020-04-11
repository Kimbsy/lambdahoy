(ns lambdahoy.scene.transition
  (:require [lambdahoy.scene :as scene]
            [lambdahoy.scene.menu :as menu]
            [lambdahoy.scene.ocean :as ocean]
            [quil.core :as q]))

(defn update-state
  [state]
  (if (< (:transition-progress state) 25)
    (update state :transition-progress inc)
    (case (:target-scene state)
      :ocean (assoc state :current-scene (ocean/->Ocean))
      :menu (assoc state :current-scene (menu/->Menu))
      state)))

(defn draw-state
  [state]
  (let [g (q/create-graphics (q/width) (q/height))]
    (q/with-graphics g
      (q/background 0 (* 2 (:transition-progress state))))
    (q/image-mode :corner)
    (q/image g 0 0)))

(deftype Transition []
  scene/Scene
  (update-state [s state] (update-state state))
  (draw-state [s state] (draw-state state))
  (key-pressed [s state e] state)
  (key-released [s state e] state)
  (mouse-pressed [s state e] state)
  (mouse-released [s state e] state))
