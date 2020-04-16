(ns lambdahoy.sprite.wave
  (:require [lambdahoy.sprite :as sprite]
            [quil.core :as q]))

(defn ->wave
  [pos]
  {:pos   pos
   :image (q/load-image (rand-nth ["images/waves/wave-1-tiled.png"
                                   "images/waves/wave-2-tiled.png"
                                   "images/waves/wave-3-tiled.png"
                                   "images/waves/wave-4-tiled.png"
                                   "images/waves/wave-5-tiled.png"
                                   "images/waves/wave-6-tiled.png"
                                   "images/waves/wave-7-tiled.png"
                                   "images/waves/wave-8-tiled.png"]))})
