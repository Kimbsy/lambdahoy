(ns cannonical.scene.ocean
  (:require [cannonical.scene :as scene]
            [cannonical.utils :as u]
            [cannonical.sprite.ship :as ship]
            [cannonical.sprite.captain :as captain]
            [quil.core :as q]))

(defn init-sprites
  []
  {:ships [(ship/->ship (/ (q/width) 2)
                        (/ (q/height) 2)
                        0
                        :pc? true
                        :crew [(captain/->captain 0 110)])]})

(defn update-state
  [state]
  (-> state
      (update-in [:sprites :ocean :ships]
                 #(map (partial ship/update-self state) %))))

(defn draw-state
  [state]
  (u/background u/blue)
  (doall (map ship/draw-self
              (get-in state [:sprites :ocean :ships]))))

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

(deftype Ocean []
  scene/Scene
  (update-state [s state] (update-state state))
  (draw-state [s state] (draw-state state))
  (key-pressed [s state e] (key-pressed state e))
  (key-released [s state e] (key-released state e))
  (mouse-pressed [s state e] (mouse-pressed state e))
  (mouse-released [s state e] (mouse-released state e)))
