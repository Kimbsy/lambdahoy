(ns lambdahoy.scene.ocean
  (:require [lambdahoy.scene :as scene]
            [lambdahoy.sprite :as sprite]
            [lambdahoy.sprite.cannon :as cannon]
            [lambdahoy.sprite.captain :as captain]
            [lambdahoy.sprite.projectile :as projectile]
            [lambdahoy.sprite.ship :as ship]
            [lambdahoy.utils :as u]
            [quil.core :as q]))

(defn random-test-projectiles
  []
  (take 40 (repeatedly #(projectile/->projectile [(rand-int (q/width)) (rand-int (q/height))] :duration 100000))))

(defn init-sprites
  []
  {:ships [ ;; player ship
           (ship/->ship [(* (q/width) 3/4) (* (q/height) 3/4)]
                        :r 0
                        :pc? true
                        :crew [(captain/->captain [0 15])]
                        :cannons [(cannon/->cannon [60 0])
                                  (cannon/->cannon [-60 0])])

           ;; npc ship
           (ship/->ship [(* (q/width) 1/2) (* (q/height) 1/2)]
                        :r 90
                        :vel [5 0]
                        :health 50
                        :cannons [(cannon/->cannon [60 0])
                                  (cannon/->cannon [-60 0])])]
   :projectiles (random-test-projectiles)
   :boundaries  []
   :islands     []
   :waves       []})

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
                  missing-projectiles (concat (groups false) (groups nil))

                  damage (->> hitting-projectiles
                              (map :damage)
                              (reduce +))]
              (-> acc
                  (update :ships (fn [ships]
                                   (conj ships (update ship :health #(- % damage)))))
                  (assoc :projectiles (concat missing-projectiles
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

(defn update-state
  [state]
  (-> state
      (update-in [:sprites :ocean :ships]
                 #(->> %
                       (map (partial ship/update-self state))
                       (remove (fn [s] (<= (:health s) 0))))) ;; @TODO: we'll want a better way of sinking ships in the future
      (update-in [:sprites :ocean :projectiles]
                 #(->> %
                      (mapv projectile/update-self)
                      (remove nil?))) ; clean up projectiles that have died
      ship-projectile-collision))

(defn draw-state
  [state]
  (u/background u/blue)
  (doall (map ship/draw-self
              (get-in state [:sprites :ocean :ships])))
  (doall (map sprite/draw-animated-sprite
              (get-in state [:sprites :ocean :projectiles]))))

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
