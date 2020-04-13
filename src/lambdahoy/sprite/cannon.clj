(ns lambdahoy.sprite.cannon
  (:require [quil.core :as q]
            [lambdahoy.utils :as u]))

(defn ->cannon
  [pos]
  {:pos pos})

;; temp draw-self, should make a spritesheet
(defn draw-self
  [{[x y] :pos}]
  (u/fill u/black)
  (q/rect x y 30 10))
