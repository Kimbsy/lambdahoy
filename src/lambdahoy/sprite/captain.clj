(ns lambdahoy.sprite.captain
  (:require [quil.core :as q]))

(defn ->captain
  [pos & {:keys [size current-animation]
          :or   {size              :default
                 current-animation :idle}}]
  {:pos pos
   :vel [0 0]
   :w   (case size
          :big 240
          16)
   :h   (case size
          :big 360
          24)

   :spritesheet       (case size
                        :big (q/load-image "images/captain-big.png")
                        (q/load-image "images/a0218040a060000.png"))
   :current-animation current-animation
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
