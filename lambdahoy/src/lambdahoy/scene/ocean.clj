(ns lambdahoy.scene.ocean
  (:require [lambdahoy.scene :as scene]
            [lambdahoy.utils :as u]
            [lambdahoy.sprite.ship :as ship]
            [lambdahoy.sprite.captain :as captain]
            [lambdahoy.sprite.projectile :as projectile]
            [quil.core :as q]
            [lambdahoy.sprite :as sprite]))

(defn init-sprites
  []
  {:ships       [(ship/->ship (/ (q/width) 2)
                              (/ (q/height) 2)
                              0
                              :pc? true
                              :crew [(captain/->captain 0 110)])]
   :projectiles []
   :boundaries  []
   :islands     []
   :waves       []})

(defn update-state
  [state]
  (-> state
      (update-in [:sprites :ocean :ships]
                 #(map (partial ship/update-self state) %))
      (update-in [:sprites :ocean :projectiles]
                 #(map sprite/update-self %))))

(defn draw-state
  [state]
  (u/background u/blue)
  (doall (map ship/draw-self
              (get-in state [:sprites :ocean :ships])))
  (doall (map sprite/draw-animated-sprite
              (get-in state [:sprites :ocean :projectiles]))))

(defn switch-to-menu
  "If esc was pressed, exit to menu"
  [state e]
  ;; @TODO: this
  state)

(defn key-pressed
  [state e]
  (reduce (fn [acc-state f]
            (f acc-state e))
          state
          [switch-to-menu
           ship/key-pressed]))

(defn key-released
  [state e]
  state)

(defn mouse-pressed
  [state e]
  state)

(defn mouse-released
  [state e]
  state)

(deftype Ocean []
  scene/Scene
  (update-state [s state] (update-state state))
  (draw-state [s state] (draw-state state))
  (key-pressed [s state e] (key-pressed state e))
  (key-released [s state e] (key-released state e))
  (mouse-pressed [s state e] (mouse-pressed state e))
  (mouse-released [s state e] (mouse-released state e)))
