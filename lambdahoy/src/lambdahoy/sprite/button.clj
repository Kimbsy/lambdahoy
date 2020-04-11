(ns lambdahoy.sprite.button
  (:require [lambdahoy.utils :as u]
            [quil.core :as q]))

(defn ->button
  [text [x y] on-click & {:keys [w h color text-color held?]
                          :or   {w 200 h 100 color u/grey text-color u/white held? false}}]
  {:text       text
   :pos        [(- x (/ w 2))
                (- y (/ h 2))]
   :text-pos   [x y]
   :w          w
   :h          h
   :color      color
   :text-color text-color
   :on-click   on-click
   :held?      held?})

(defn draw-self
  [{:keys [text pos text-pos w h color text-color held?]}]
  (q/no-stroke)
  (q/text-align :center :center)
  (q/text-font (q/create-font u/bold-font u/large-text-size))
  (let [[x y]   pos
        [tx ty] text-pos]
    (if held?
      (do (u/fill color)
          (q/rect (+ 2 x) (+ 2 y) w h)
          (u/fill text-color)
          (q/text text (+ 2 tx) (+ 2 ty)))
      (do (u/fill (u/darken color))
          (q/rect (+ 2 x) (+ 2 y) w h)
          (u/fill color)
          (q/rect x y w h)
          (u/fill text-color)
          (q/text text tx ty)))))

(defn mouse-pressed
  "Map across a seq of buttons and set `:held?` to `true` when the
  button bounds enclose the event pos."
  [buttons e]
  (map (fn [b]
         (if (u/encloses? b e)
           (assoc b :held? true)
           b))
       buttons))

(defn mouse-released
  "Returns a function which takes a state and a mouse-event and will
  reduce across a seq of buttons applying their `:on-click` functions
  to the accumulating state if `:held?` is `true`."
  [buttons]
  (fn [state e]
    (reduce (fn [acc-state b]
              ((:on-click b) state))
            state
            (filter :held? buttons))))
