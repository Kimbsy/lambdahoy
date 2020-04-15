(ns lambdahoy.sprite.bar
  (:require [lambdahoy.utils :as u]
            [quil.core :as q]))

(defn ->bar
  [pos & {:keys [w h current max-value bg-color fg-color]
          :or   {w           100
                 h           5
                 current     100
                 max-value   100
                 bg-color    u/red
                 fg-color    u/green}}]
  {:pos         pos
   :w           w
   :h           h
   :current     current
   :max-value   max-value
   :bg-color    bg-color
   :fg-color    fg-color})

(defn draw-self
  [{:keys [pos w h current max-value bg-color fg-color]}]
  (let [[x y]     pos
        x-offset  (- x (/ w 2))
        y-offset  (- y (/ h 2))
        current-w (* w (/ current max-value))]
    (u/fill bg-color)
    (q/rect x-offset y-offset w h)
    (u/fill fg-color)
    (q/rect x-offset y-offset current-w h)))
