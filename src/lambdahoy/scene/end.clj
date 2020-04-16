(ns lambdahoy.scene.end
  (:require [lambdahoy.scene.menu :as menu]
            [lambdahoy.scene.ocean :as ocean]
            [lambdahoy.sprite.captain :as captain]
            [quil.core :as q]
            [lambdahoy.sprite :as sprite]
            [lambdahoy.sprite.text :as text]
            [lambdahoy.sprite.button :as button]
            [lambdahoy.scene :as scene]))

(defn init-sprites
  []
  {:text {:victory [(text/->text "Congratulations!"
                                 [(* (q/width) 1/2)
                                  (* (q/height) 1/5)]
                                 :size u/title-text-size
                                 :color u/white)
                    (text/->text "You defeated every enemy ship, truly you are the bravest Priate Captain."
                                 [(* (q/width) 1/2)
                                  (* (q/height) 5/20)]
                                 :style :italic
                                 :color u/white)]
          :defeat  [(text/->text "Defeat."
                                 [(* (q/width) 1/2)
                                  (* (q/height) 1/5)]
                                 :size u/title-text-size
                                 :color u/white)
                    (text/->text "You were destroyed by an enemy ship, better luck next time :("
                                 [(* (q/width) 1/2)
                                  (* (q/height) 5/20)]
                                 :style :italic
                                 :color u/white)]}
   
   :buttons    [(button/->button "RESTART" [(* (q/width) 1/2) (* (q/height) 1/2)] restart-game)
                (button/->button "QUIT" [(* (q/width) 1/2) (* (q/height) 2/3)] exit-game)]
   :characters {:victory [(captain/->captain [(* (q/width) 1/4) (* (q/height) 1/2)]
                                             :size :big
                                             :current-animation :jump)]
                :defeat  [(captain/->captain [(* (q/width) 1/4) (* (q/height) 1/2)]
                                             :size :big)]}})

(defn restart-game
  [state]
  {:current-scene (ocean/->Ocean)
   :sprites       {:menu  (menu/init-sprites)
                   :ocean (ocean/init-sprites)
                   :end   (init-sprites)}
   :held-keys     #{}})

(defn exit-game
  [state]
  (q/exit))

(defn update-state
  [{:keys [outcome] :as state}]
  (update-in state [:sprites :end :characters outcome]
             #(map sprite/update-self %)))

(defn draw-state
  [{:keys [outcome] :as state}]
  (u/background u/blue)
  (q/image-mode :center)
  (doall (map sprite/draw-animated-sprite
              (get-in state [:sprites :end :waves])))
  (doall (map text/draw-self
              (get-in state [:sprites :end :text outcome])))
  (doall (map sprite/draw-animated-sprite
              (get-in state [:sprites :end :characters outcome])))
  (doall (map button/draw-self
              (get-in state [:sprites :end :buttons]))))

(defn mouse-pressed
  [state e]
  (update-in state [:sprites :end :buttons]
             button/mouse-pressed
             e))

(defn mouse-released
  [state e]
  (as-> state state
    (reduce (fn [acc-state f]
              (f acc-state e))
            state
            [(button/mouse-released (get-in state [:sprites :end :buttons]))])
    (update-in state [:sprites :end :buttons]
               (fn [buttons]
                 (map #(assoc % :held? false)
                      buttons)))))

(deftype End []
  scene/Scene
  (update-state [s state] (update-state state))
  (draw-state [s state] (draw-state state))
  (key-pressed [s state e] state)
  (key-released [s state e] state)
  (mouse-pressed [s state e] (mouse-pressed state e))
  (mouse-released [s state e] (mouse-released state e)))
