(ns lambdahoy.scene.ocean
  (:require [lambdahoy.scene :as scene]
            [lambdahoy.sprite :as sprite]
            [lambdahoy.sprite.bar :as bar]
            [lambdahoy.sprite.cannon :as cannon]
            [lambdahoy.sprite.captain :as captain]
            [lambdahoy.sprite.projectile :as projectile]
            [lambdahoy.sprite.ship :as ship]
            [lambdahoy.sprite.wave :as wave]
            [lambdahoy.utils :as u]
            [quil.core :as q]))

(defn random-npc-ship
  [difficulty]
  (ship/->ship [(rand-int (* (q/width) 1.5)) (rand-int (* (q/height) 1.5))]
               :r (rand-int 360)
               :vel [0 (max 2 (min 7 (- difficulty 1)))]
               :cannons (cond
                          (< 5 difficulty)
                          [(cannon/->cannon [60 45])
                           (cannon/->cannon [60 0])
                           (cannon/->cannon [60 -45])
                           (cannon/->cannon [-60 45])
                           (cannon/->cannon [-60 0])
                           (cannon/->cannon [-60 -45])]

                          (< 3 difficulty)
                          [(cannon/->cannon [60 30])
                           (cannon/->cannon [60 -30])
                           (cannon/->cannon [-60 30])
                           (cannon/->cannon [-60 -30])]

                          :else
                          [(cannon/->cannon [60 0])
                           (cannon/->cannon [-60 0])])
               :loading (bar/->bar [0 110] 50 5
                                   :fg-color u/gold
                                   :current (rand-int 30)
                                   :max-value (+ 70 (rand-int 30)))))

(defn init-sprites
  [difficulty]
  {:ships (concat [ ;; player ship
                   (ship/->ship [(* (q/width) 1/2) (* (q/height) 1/2)]
                                :r 0
                                :pc? true
                                :crew [(captain/->captain [0 15])]
                                :cannons [(cannon/->cannon [60 45])
                                          (cannon/->cannon [60 0])
                                          (cannon/->cannon [60 -45])
                                          (cannon/->cannon [-60 45])
                                          (cannon/->cannon [-60 0])
                                          (cannon/->cannon [-60 -45])])]
                  (take (+ 2 (* 2 (min 2 (- difficulty 1)))) (repeatedly #(random-npc-ship difficulty))))

   :projectiles []
   :waves       (map wave/->wave [[900 0]
                                  [900 600]
                                  [0 1200]  [1800 1200]])})

(defn ship-hit-by-projectile?
  "Predicate to determine if a ship is colliding with a projectile. Has multiple levels of granularity.

  First we check for definite hits: equal positions, or within a small
  square wholly contained in the ship regardless of orientation.

  Then we check if the projectile is within a square the size of the
  length of the ship. Only in this case do we do the expensive
  calculations to determine the transformed bounding polygon and check
  if it encloses the position of the projectile.

  The `u/poly-encloses?` function does a further two steps of granularity."
  [ship {:keys [pos]}]
  (or (= pos (:pos ship))
      (ship/guaranteed-hit? ship pos)
      (when (ship/possibly-hit? ship pos)
        (u/poly-encloses? (ship/bounding-poly ship) pos))))

(defn collision-updates
  "Calculate updated state of ships and projectiles due to collisions.

  Ships should lose health equal to the total damage of all
  projectiles colliding with them. Colliding projectiles should have
  their duration set to zero so they can be cleaned up."
  [initial-ships initial-projectiles]
  (reduce (fn [acc ship]
            (let [groups (group-by #(ship-hit-by-projectile? ship %)
                                   (:projectiles acc))

                  hitting-projectiles (groups true)
                  missing-projectiles (into (groups false) (groups nil))

                  damage (->> hitting-projectiles
                              (map :damage)
                              (reduce +))]
              (-> acc
                  (update :ships (fn [ships]
                                   (conj ships (update-in ship [:health :current] #(max 0 (- % damage))))))
                  (assoc :projectiles (into missing-projectiles
                                            (map #(assoc % :duration 0) hitting-projectiles))))))
          {:ships       []
           :projectiles initial-projectiles}
          initial-ships))

(defn ship-projectile-collision
  "Handle updating the state of ships and projectiles based on their
  collisions."
  [state]
  (let [ships       (get-in state [:sprites :ocean :ships])
        projectiles (get-in state [:sprites :ocean :projectiles])
        updated     (collision-updates ships projectiles)]
    (-> state
        (assoc-in [:sprites :ocean :ships] (:ships updated))
        (assoc-in [:sprites :ocean :projectiles] (:projectiles updated)))))

(defn reposition-wave
  "Move waves which are offscreen to the other side of the screen so
  they tile as you move."
  [{:keys [pos] :as wave} [pc-x pc-y]]
  (let [[w-x w-y] pos
        updated-x (cond
                    (< 1000 (- w-x pc-x))
                    (update-in wave [:pos 0] #(- % 2700))

                    (< (- w-x pc-x) -1000)
                    (update-in wave [:pos 0] #(+ % 2700))

                    :else
                    wave)
        updated-xy (cond
                     (< 700 (- w-y pc-y))
                     (update-in updated-x [:pos 1] #(- % 1800))

                     (< (- w-y pc-y) -700)
                     (update-in updated-x [:pos 1] #(+ % 1800))

                     :else
                     wave)]
    updated-xy))

(defn fire-npc-ships
  "Let npc ships fire if cannons fully loaded."
  [state]
  (let [ships             (get-in state [:sprites :ocean :ships])
        pc-ships          (filter :pc? ships)
        npc-ships         (remove :pc? ships)
        firing-npc-ships  (filter ship/loaded? npc-ships)
        loading-npc-ships (remove ship/loaded? npc-ships)
        fired-projectiles (mapcat ship/fire firing-npc-ships)]
    (if (seq fired-projectiles)
      (-> state
          (update-in [:sprites :ocean :projectiles]
                     #(concat fired-projectiles %))
          (assoc-in [:sprites :ocean :ships]
                    (concat pc-ships
                            loading-npc-ships
                            (map (fn [npc-ship] (assoc-in npc-ship [:loading :current] 0))
                                 firing-npc-ships))))
      state)))

(defn game-end
  "Check victory/defeat conditions."
  [state]
  (let [ships   (get-in state [:sprites :ocean :ships])
        pc-ship (first (filter :pc? ships))]
    (cond
      (zero? (-> pc-ship :health :current))
      (-> state
          (assoc :outcome :defeat)
          (u/change-scene :end))

      (every? :pc? ships)
      (-> state
          (assoc :outcome :victory)
          (u/change-scene :end))

      :else
      state)))

(defn update-state
  [state]
  (let [ships   (get-in state [:sprites :ocean :ships])
        pc-ship (first (filter :pc? ships))]
    (-> state
        (update-in [:sprites :ocean :ships]
                   #(->> %
                         (map (partial ship/update-self state))
                         (remove (fn [s] (and (not (:pc? s))
                                              (<= (:current (:health s)) 0)))))) ;; @TODO: we'll want a better way of sinking ships in the future
        (update-in [:sprites :ocean :waves]
                   #(->> %
                         (map (fn [wave] (reposition-wave wave (:pos pc-ship))))))
        fire-npc-ships
        (update-in [:sprites :ocean :projectiles]
                   #(->> %
                         (map projectile/update-self)
                         (remove nil?))) ; clean up projectiles that have died
        ship-projectile-collision
        game-end)))

(defn draw-indicator
  "Draw a tirangle pointing at any off screen ships."
  [{npc-pos :pos} {pc-pos :pos}]
  (let [[npc-x npc-y] npc-pos
        [pc-x pc-y]   pc-pos]
    (when (or (< (* (q/width) 5/12) (q/abs (- npc-x pc-x)))
              (< (* (q/height) 5/12) (q/abs (- npc-y pc-y))))
      (let [translation [(u/bounded (- npc-x pc-x)
                                    (+ (* (q/width) 1/2) -10)
                                    (+ (- (* (q/width) 1/2)) 10))
                         (u/bounded (- npc-y pc-y)
                                    (+ (* (q/height) 1/2) -10)
                                    (+ (- (* (q/height) 1/2)) 10))]]
        (u/wrap-trans-rot
         translation
          ;; @TODO is something wrong with our rotation-angle
          ;; function? why do we need to negate the y? are our
          ;; ratational velocity calculations off?
         (u/rotation-angle [(first translation) (- (second translation))])
         #(do (u/fill u/red)
              (u/stroke u/black)
              (q/triangle 0 -10
                          -10 10
                          10 10)))))))

(defn draw-state
  [state]
  (u/background u/blue)
  (let [pc-ship (first (filter :pc? (get-in state [:sprites :ocean :ships])))]
    (u/wrap-trans-rot
     [(* (q/width) 1/2) (* (q/height) 1/2)] 0
     (fn []
       (u/wrap-trans-rot
        (map - (:pos pc-ship)) 0
        (fn []
          (doall (map sprite/draw-static-sprite
                      (get-in state [:sprites :ocean :waves])))
          (doall (map ship/draw-self
                      (remove :pc? (get-in state [:sprites :ocean :ships]))))
          (doall (map sprite/draw-animated-sprite
                      (get-in state [:sprites :ocean :projectiles])))          
          (ship/draw-self pc-ship)))
       (doall (map #(draw-indicator % pc-ship)
                   (remove :pc? (get-in state [:sprites :ocean :ships]))))))))

(defn switch-to-menu
  "If esc was pressed, exit to menu"
  [state e]
  (if (= 27 (:key-code e))
    (u/change-scene state :menu)
    state))

(defn key-pressed
  [state e]
  (reduce (fn [acc-state f]
            (f acc-state e))
          state
          [switch-to-menu
           ship/key-pressed]))

(deftype Ocean []
  scene/Scene
  (update-state [s state] (update-state state))
  (draw-state [s state] (draw-state state))
  (key-pressed [s state e] (key-pressed state e))
  (key-released [s state e] state)
  (mouse-pressed [s state e] state)
  (mouse-released [s state e] state))
