(ns lambdahoy.sprite.cannon
  (:require [quil.core :as q]
            [lambdahoy.utils :as u]))

(defn ->cannon
  [pos]
  {:pos pos})

;; temp draw-self, should make a spritesheet
(defn draw-self
  [{[x y] :pos}]
  (q/no-stroke)
  (u/fill u/black)
  (if (pos? x)
    (q/rect x y 30 10)
    (q/rect x y -30 10)))
