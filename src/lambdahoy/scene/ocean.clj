(ns lambdahoy.scene.ocean
  (:require [lambdahoy.scene :as scene]
            [lambdahoy.sprite :as sprite]
            [lambdahoy.sprite.cannon :as cannon]
            [lambdahoy.sprite.captain :as captain]
            [lambdahoy.sprite.ship :as ship]
            [lambdahoy.utils :as u]
            [quil.core :as q]))

(defn init-sprites
  []
  {:ships       [(ship/->ship [(* (q/width) 1/2) (* (q/height) 1/2)]
                              :r 0
                              :pc? true
                              :crew [(captain/->captain [0 15])]
                              :cannons [(cannon/->cannon [60 0])
                                        (cannon/->cannon [-60 0])])
                 (ship/->ship [(* (q/width) 1/4) (* (q/height) 1/4)]
                              :r 90
                              :vel [5 0])]
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
  (if (= 27 (:key-code e))
    (u/change-scene state :menu)
    state))

(defn key-pressed
  [state e]
  (reduce (fn [acc-state f]
            (f acc-state e))
          state
          [switch-to-menu
           ship/key-pressed]))

(deftype Ocean []
  scene/Scene
  (update-state [s state] (update-state state))
  (draw-state [s state] (draw-state state))
  (key-pressed [s state e] (key-pressed state e))
  (key-released [s state e] state)
  (mouse-pressed [s state e] state)
  (mouse-released [s state e] state))
