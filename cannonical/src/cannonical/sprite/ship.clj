(ns cannonical.sprite.ship
  (:require [cannonical.utils :as u]
            [quil.core :as q]
            [cannonical.sprite :as sprite]))

(defn ->ship
  [x y r & {:keys [pc? crew vx vy] :or {pc? false crew [] vx 0 vy 0}}]
  (let [pos [x y]
        vel [vx vy]]
    {:pos   pos
     :vel   vel
     :r     r
     :rvel  0
     :speed (u/length vel)
     :image (q/load-image "images/ship.png")
     :pc?   pc?
     :crew  crew}))

(defn rvel-drift
  [rvel]
  (cond (< rvel 0) 1
        (> rvel 0) -1
        :else      0))

(defn update-rvel
  [{:keys [rvel]} held-keys]
  (let [drift      0.025
        back-drift (* drift (rvel-drift rvel))]
    (cond (held-keys :right)
          (min (+ rvel 0.1) 2)

          (held-keys :left)
          (max (- rvel 0.1) -2)

          (> drift (Math/abs (+ rvel back-drift)))
          0

          :else
          (+ rvel back-drift))))

(defn update-speed
  [{:keys [speed]} held-keys]
  (cond (held-keys :up)
        (min (+ speed 0.1) 10)

        (held-keys :down)
        (max (- speed 0.1) 0)

        :else
        (max (- speed 0.05) 0)))

(defn update-self
  [{:keys [held-keys]} {:keys [pos vel r speed crew] :as ship}]
  (-> ship
      (assoc :vel (u/get-velocity-vector r :speed speed))
      (assoc :pos (map + pos vel))
      (assoc :rvel (update-rvel ship held-keys))
      (assoc :speed (update-speed ship held-keys))
      (update :r #(mod (+ % (:rvel ship)) 360))
      (assoc :crew (map #(sprite/update-self %) crew))))

(defn draw-self
  [{:keys [pos r image crew] :as ship}]
  (let [[x y] pos]
    (u/wrap-trans-rot
     x y r
     #(do
        (q/image-mode :center)
        (q/image image 0 0)
        (doall (map sprite/draw-self crew))))))
