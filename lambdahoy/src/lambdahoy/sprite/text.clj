(ns lambdahoy.sprite.text
  (:require [lambdahoy.utils :as u]
            [quil.core :as q]))

(defn ->text
  [content pos & {:keys [size style color]
                  :or   {size u/default-text-size style :normal color u/black}}]
  {:content content
   :pos     pos
   :size    size
   :style   style
   :color   color})

(defn draw-self
  [{:keys [content pos size style color]}]
  (let [[x y] pos]
    (q/text-align :center)
    (q/text-font (q/create-font (case style                              
                                  :bold "Ubuntu Mono Bold"
                                  :italic "Ubuntu Mono Italic"
                                  "Ubuntu Mono")
                                size))
    (u/fill color)
    (q/text content x y)))
