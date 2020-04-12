(ns lambdahoy.sprite.ship
  (:require [lambdahoy.utils :as u]
            [quil.core :as q]
            [lambdahoy.sound :as sound]
            [lambdahoy.sprite :as sprite]
            [lambdahoy.sprite.projectile :as projectile]))

(defn ->ship
  [pos r & {:keys [pc? crew vx vy]
            :or   {pc? false crew [] vx 0 vy 0}}]
  {:pos   pos
   :vel   [vx vy]
   :r     r
   :rvel  0
   :speed (u/magnitude [vx vy])
   :image (q/load-image "images/ship-small.png")
   :pc?   pc?
   :crew  crew})

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
      (assoc :vel (u/velocity-vector ship))
      (assoc :pos (map + pos vel))
      (assoc :rvel (update-rvel ship held-keys))
      (assoc :speed (update-speed ship held-keys))
      (update :r #(mod (+ % (:rvel ship)) 360))
      (update :crew #(map sprite/update-self %))))

(defn draw-self
  [{:keys [pos r image crew] :as ship}]
  (let [[x y] pos]
    (u/wrap-trans-rot
     x y r
     #(do
        (q/image-mode :center)
        (q/image image 0 0)
        (doall (map sprite/draw-animated-sprite crew))))))

;; @TODO: maybe fire should be based on held keys? once we implement cannon firing delays?
(defn fire
  [{:keys [pos vel] :as s}]
  (sound/play-sound-effect :cannon)
  (->> s
       u/direction-vector
       u/orthogonals
       (map (u/scale-by 10))
       (map (partial map + vel))
       (map #(projectile/->projectile pos :vel %))))

(defn key-pressed
  [state e]
  (if (= :space (:key e))
    (let [pc-ships (filter :pc? (get-in state [:sprites :ocean :ships]))]
      (update-in state
                 [:sprites :ocean :projectiles]
                 #(take 100 (concat (apply concat (map fire pc-ships)) %))))
    state))
