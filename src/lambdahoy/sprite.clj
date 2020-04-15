(ns lambdahoy.sprite
  (:require [quil.core :as q]))

(defn update-frame-delay
  [{:keys [current-animation] :as s}]
  (let [animation (current-animation (:animations s))
        frame-delay (:frame-delay animation)]
    (update s :delay-count #(mod (inc %) frame-delay))))

(defn update-animation
  [{:keys [current-animation delay-count] :as s}]
  (if (zero? delay-count)
    (let [animation (current-animation (:animations s))
          max-frame (:frames animation)]
      (update s :animation-frame #(mod (inc %) max-frame)))
    s))

(defn update-pos
  [{:keys [pos vel] :as s}]
  (assoc s :pos (map + pos vel)))

(defn update-self
  [s]
  (-> s
      update-frame-delay
      update-animation
      update-pos))

(defn draw-static-sprite
  [{:keys [pos image]}]
  (let [[x y] pos]
    (q/image image x y)))

(defn draw-animated-sprite
  [{:keys [pos w h spritesheet current-animation animation-frame] :as s}]
  (let [[x y]     pos
        animation (current-animation (:animations s))
        x-offset  (* animation-frame w)
        y-offset  (* (:y-offset animation) h)
        g         (q/create-graphics w h)]
    (q/with-graphics g
      (q/image spritesheet (- x-offset) (- y-offset)))
    (q/image g x y)))
