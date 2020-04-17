(ns lambdahoy.core
  (:gen-class)
  (:require [lambdahoy.scene :as scene]
            [lambdahoy.scene.end :as end]
            [lambdahoy.scene.menu :as menu]
            [lambdahoy.scene.ocean :as ocean]
            [lambdahoy.scene.transition :as transition]
            [lambdahoy.sound :as sound]
            [quil.core :as q]
            [quil.middleware :as m]))

(def starting-difficulty 1)

(defn setup
  []
  (q/frame-rate 60)
  (sound/init)
  {:current-scene (menu/->Menu)
   :sprites       {:menu  (menu/init-sprites)
                   :ocean (ocean/init-sprites starting-difficulty)
                   :end   (end/init-sprites)}
   :held-keys     #{}
   :difficulty    starting-difficulty})

(defn update-state
  "The current scene is responsible for updating the state, but we need
  to be able to handle when we are switching scene."
  [state]
  (if (:change-scene? state)
    (-> state
        (assoc :current-scene (transition/->Transition))
        (assoc :transition-progress 0)
        (assoc :change-scene? false)
        scene/update-state*)
    (scene/update-state* state)))

(defn sanitized-key-pressed
  "Preventing esc from closing the sketch by setting current key to 0."
  [state e]
  (if (= 27 (q/key-code)) ;; escape
    (set! (.key (quil.applet/current-applet)) (char 0)))
  (scene/key-pressed* state e))

(defn full-exit
  [state]
  (sound/stop-music))

(defn -main
  [& args]
  (q/sketch
   :title "Lambdahoy: functional adventures on the high seas"
   :size [1800 1200] #_ :fullscreen
   :setup setup
   :update update-state
   :draw scene/draw-state*
   :key-pressed sanitized-key-pressed
   :key-released scene/key-released*
   :mouse-pressed scene/mouse-pressed*
   :mouse-released scene/mouse-released*
   :on-close full-exit
   :middleware [m/fun-mode]))
