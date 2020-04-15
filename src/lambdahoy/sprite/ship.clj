(ns lambdahoy.sprite.ship
  (:require [lambdahoy.sound :as sound]
            [lambdahoy.sprite :as sprite]
            [lambdahoy.sprite.bar :as bar]
            [lambdahoy.sprite.cannon :as cannon]
            [lambdahoy.sprite.projectile :as projectile]
            [lambdahoy.utils :as u]
            [quil.core :as q]))

(def ^:dynamic *debug-mode* (atom false))

(def ship-height 318)
(def ship-width 159)

(def bounding-offsets
  [[0 -160]
   [79 -67]
   [79 90]
   [52 160]
   [-55 160]
   [-80 90]
   [-80 -67]])

(defn ->ship
  [pos & {:keys [r vel rvel health pc? crew cannons]
          :or   {r       0
                 vel     [0 0]
                 rvel    0
                 health  (bar/->bar [0 100] 50 5)
                 pc?     false
                 crew    []
                 cannons []}}]
  {:pos     pos
   :vel     vel
   :r       r
   :rvel    rvel
   :speed   (u/magnitude vel)
   :health  health
   :image   (q/load-image "images/ship-small.png")
   :pc?     pc?
   :crew    crew
   :cannons cannons

   :npc-command {:direction :nil
                 :duration  25}})

(defn bounding-poly
  "Calculate the collision boundary polygon for a ship."
  [{:keys [pos r]}]
  {:points (->> bounding-offsets
                (map #(u/rotate-vector % r))
                (mapv (partial mapv + pos)))})

(defn possibly-hit?
  "Predicate to determine if a pos is within a square the size of the
  ship's length centered on the ship's pos."
  [{[sx sy] :pos} [x y]]
  (let [half-height (* ship-height 1/2)]
    (and (<= (- sx half-height) x (+ sx half-height))
         (<= (- sy half-height) y (+ sy half-height)))))

(defn guaranteed-hit?
  "Predicate to check it a pos is within a 65x65 square centered on the
  ship's pos."
  [{[sx sy] :pos} [x y]]
  (and (<= (- sx 32.5) x (+ sx 32.5))
       (<= (- sy 32.5) y (+ sy 32.5))))

(defn rvel-drift
  [rvel]
  (cond (neg? rvel) 1
        (pos? rvel) -1
        :else       0))

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
  (if (zero? (:duration command))
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
  [{:keys [pos r image health pc? crew cannons indicator] :as ship}]
  (u/wrap-trans-rot
   pos r
   #(do
      (q/image-mode :center)
      (q/image image 0 0)
      (doall (map sprite/draw-animated-sprite crew))
      (doall (map cannon/draw-self cannons))))
  (u/wrap-trans-rot
   pos 0
   #(bar/draw-self health)))

(defn cannon-rotational-velocity
  "Determine the current velocity vector of a cannon based on the
  rotational velocity of the ship and the position of the cannon in
  relation to the center of rotation."
  [cannon rvel ship-rotation]
  (let [r                (:pos cannon)          ; radius vector
        r-length         (u/magnitude r)        ; length of radius vector
        c                (* 2 Math/PI r-length) ; circumference
        w                (/ rvel 360)           ; angular velocity (rotations per frame)
        speed            (* w c)                ; linear speed of cannon
        cannon-direction (u/direction-vector (+ ship-rotation (u/rotation-angle (:pos cannon))))
        orthogonals      (map u/unit-vector [cannon-direction (map (partial * -1) cannon-direction)])
        direction        (cond
                           (pos? rvel)  (last orthogonals)  ; rotating clockwise
                           (neg? rvel)  (first orthogonals) ; rotating anticlockwise
                           (zero? rvel) [0 0])              ; not rotating
        velocity         (map (partial * speed) direction)]
    velocity))

(defn firing-velocity
  "Determine the starting velocity of a projectile."
  [cannon {:keys [vel r rvel]}]
  (let [direction   (u/direction-vector r)
        orthogonals (u/orthogonals direction)
        rot-vel     (cannon-rotational-velocity cannon rvel r)]
    (->> cannon
         ;; firing direction
         ((fn [{[x y] :pos}] (if (pos? x)
                               (first orthogonals)
                               (last orthogonals))))

         ;; default projectile speed
         ((u/scale-by 20))

         ;; add ship velocity
         (map + vel)

         ;; account for cannon rotational velocity
         (map + rot-vel))))

(defn firing-position
  "Determine the starting position of a projectile."
  [cannon ship]
  (let [scaled-offset  (map * [1.5 1] (:pos cannon))
        rotated-offset (u/rotate-vector scaled-offset (:r ship))]
    (map + (:pos ship) rotated-offset)))

;; @TODO: maybe firing should be based on held keys? once we implement cannon firing delays?
(defn fire
  "Create a vector of projectiles as a result of firing all cannons on a
  ship."
  [{:keys [cannons] :as ship}]
  (sound/play-sound-effect :cannon)
  (map (fn [cannon]
         (projectile/->projectile (firing-position cannon ship)
                                  :vel (firing-velocity cannon ship)))
       cannons))

(defn key-pressed
  [state e]
  (if (= :space (:key e))
    (let [pc-ships (filter :pc? (get-in state [:sprites :ocean :ships]))]
      (update-in state
                 [:sprites :ocean :projectiles]
                 #(concat (mapcat fire pc-ships)
                          %)))
    state))
