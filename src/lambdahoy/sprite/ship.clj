(ns lambdahoy.sprite.ship
  (:require [lambdahoy.utils :as u]
            [quil.core :as q]
            [lambdahoy.sound :as sound]
            [lambdahoy.sprite :as sprite]
            [lambdahoy.sprite.projectile :as projectile]))

(defn ->ship
  [pos & {:keys [r vel rvel pc? crew]
          :or   {r    0
                 vel  [0 0]
                 rvel 0
                 pc?  false
                 crew []}}]
  {:pos   pos
   :vel   vel
   :r     r
   :rvel  rvel
   :speed (u/magnitude vel)
   :image (q/load-image "images/ship-small.png")
   :pc?   pc?
   :crew  crew

   :npc-command {:direction :nil
                 :duration  50}})

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

(defn update-if-pc-ship
  [ship held-keys]
  (if (:pc? ship)
    (-> ship
        (assoc :rvel (update-rvel ship held-keys))
        (assoc :speed (update-speed ship held-keys)))
    ship))

(defn ->npc-command
  []
  {:direction (rand-nth [:left :right nil])
   :duration (+ 10 (rand-int 50))})

(defn update-command
  [command]
  (if (= 0 (:duration command))
    (->npc-command)
    (update command :duration dec)))

(defn update-if-npc-ship
  [{:keys [npc-command] :as ship}]
  (if-not (:pc? ship)
    (-> ship
        (update :npc-command update-command)
        (assoc :rvel (update-rvel ship #{(:direction npc-command)})))
    ship))

(defn update-self
  [{:keys [held-keys]} {:keys [pos vel r speed crew] :as ship}]
  (-> ship
      (update-if-pc-ship held-keys)
      update-if-npc-ship
      (assoc :vel (u/velocity-vector ship))
      (assoc :pos (map + pos vel))
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
