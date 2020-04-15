(ns lambdahoy.sprite.projectile
  (:require [lambdahoy.sprite :as sprite]
            [quil.core :as q]))

(defn ->projectile
  [pos & {:keys [vel damage duration]
          :or   {vel      [0 0]
                 damage   10
                 duration 50}}]
  {:pos      pos
   :vel      vel
   :w        14
   :h        14
   :damage   damage
   :duration duration

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

(defn update-self
  [{:keys [duration] :as p}]
  (when (pos? duration)
    (-> p
        (update :duration dec)
        sprite/update-self)))
