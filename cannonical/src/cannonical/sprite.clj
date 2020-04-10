(ns cannonical.sprite
  (:require [quil.core :as q]))

(defn update-frame-delay
  [{:keys [current-animation] :as s}]
  (let [animation (current-animation (:animations s))
        frame-delay (:frame-delay animation)]
    (update s :delay-count #(mod (inc %) frame-delay))))

(defn update-animation
  [{:keys [current-animation delay-count] :as s}]
  (if (= 0 delay-count)
    (let [animation (current-animation (:animations s))
          max-frame (:frames animation)]
      (update s :animation-frame #(mod (inc %) max-frame)))
    s))

(defn update-self
  [s]
  (-> s
      update-frame-delay
      update-animation))

(defn draw-self
  [{:keys [x y w h spritesheet current-animation animation-frame] :as s}]
  (let [animation (current-animation (:animations s))
        x-offset  (* animation-frame w)
        y-offset  (* (:y-offset animation) h)
        g         (q/create-graphics w h)]
    (q/with-graphics g
      (q/image spritesheet (- x-offset) (- y-offset)))
    (q/image g x y)))
