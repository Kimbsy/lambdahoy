(ns cannonical.sprite.captain
  (:require [quil.core :as q]))

(defn ->captain
  [x y]
  {:pos [x y]
   :vel [0 0]
   :w 16
   :h 24

   :spritesheet       (q/load-image "images/a0218040a060000.png")
   :current-animation :idle
   :delay-count       0
   :animation-frame   0
   :animations        {:none {:frames      1
                              :y-offset    0
                              :frame-delay 100}
                       :idle {:frames      4
                              :y-offset    1
                              :frame-delay 10}
                       :run  {:frames      4
                              :y-offset    2
                              :frame-delay 5}
                       :jump {:frames      7
                              :y-offset    3
                              :frame-delay 5}}})
