(ns cannonical.sprite.projectile
  (:require [quil.core :as q]))

(defn ->projectile
  [pos & {:keys [vel] :or {vel [0 0]}}]
  {:pos pos
   :vel vel
   :w   14
   :h   14

   :spritesheet       (q/load-image "images/cannonball.png")
   :current-animation :spin
   :delay-count       0
   :animation-frame   0
   :animations        {:none {:frames      1
                              :y-offset    0
                              :frame-delay 100}
                       :spin {:frames      4
                              :y-offset    0
                              :frame-delay 5}}})
