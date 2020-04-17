(ns lambdahoy.scene.end
  (:require [lambdahoy.scene :as scene]
            [lambdahoy.scene.menu :as menu]
            [lambdahoy.scene.ocean :as ocean]
            [lambdahoy.sprite :as sprite]
            [lambdahoy.sprite.button :as button]
            [lambdahoy.sprite.captain :as captain]
            [lambdahoy.sprite.text :as text]
            [lambdahoy.utils :as u]
            [quil.core :as q]))

(declare exit-game
         restart-game
         restart-game-harder)

(defn init-sprites
  []
  {:text {:victory [(text/->text "Congratulations!"
                                 [(* (q/width) 1/2)
                                  (* (q/height) 1/5)]
                                 :size u/title-text-size
                                 :color u/white)
                    (text/->text "You defeated every enemy ship, truly you are the bravest Pirate Captain."
                                 [(* (q/width) 1/2)
                                  (* (q/height) 1/4)]
                                 :style :italic
                                 :color u/white)
                    (text/->text "Level reached: 1"
                                 [(* (q/width) 1/2)
                                  (* (q/height) 6/20)]
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
                                 :color u/white)
                    (text/->text "Level reached: 1"
                                 [(* (q/width) 1/2)
                                  (* (q/height) 6/20)]
                                 :color u/white)]}
   
   :buttons {:victory [(button/->button "Harder!"
                                        [(* (q/width) 1/2) (* (q/height) 1/2)]
                                        restart-game-harder
                                        :w 300)
                       (button/->button "Quit"
                                        [(* (q/width) 1/2) (* (q/height) 2/3)]
                                        exit-game)]
             :defeat  [(button/->button "Again!"
                                        [(* (q/width) 1/2) (* (q/height) 1/2)]
                                        restart-game
                                        :w 300)
                       (button/->button "Quit"
                                        [(* (q/width) 1/2) (* (q/height) 2/3)]
                                        exit-game)]}

   :characters {:victory [(captain/->captain [(* (q/width) 1/4) (* (q/height) 1/2)]
                                             :size :big
                                             :current-animation :jump)]
                :defeat  [(captain/->captain [(* (q/width) 1/4) (* (q/height) 1/2)]
                                             :size :big)]}})

(defn exit-game
  [state]
  (q/exit))

(defn restart-game
  [{:keys [difficulty]}]
  {:current-scene (ocean/->Ocean)
   :sprites       {:menu  (menu/init-sprites)
                   :ocean (ocean/init-sprites difficulty)
                   :end   (init-sprites)}
   :held-keys     #{}
   :difficulty difficulty})

(defn restart-game-harder
  [{:keys [difficulty]}]
  {:current-scene (ocean/->Ocean)
   :sprites       {:menu  (menu/init-sprites)
                   :ocean (ocean/init-sprites (inc difficulty))
                   :end   (init-sprites)}
   :held-keys     #{}
   :difficulty (inc difficulty)})

(defn update-state
  [{:keys [outcome difficulty] :as state}]
  (-> state
      (update-in [:sprites :end :characters outcome]
                 #(map sprite/update-self %))
      (update-in [:sprites :end :text outcome 2]
                 #(assoc % :content (str "Level reached: " difficulty)))))

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
              (get-in state [:sprites :end :buttons outcome]))))

(defn mouse-pressed
  [{:keys [outcome] :as state} e]
  (update-in state [:sprites :end :buttons outcome]
             button/mouse-pressed
             e))

(defn mouse-released
  [{:keys [outcome] :as state} e]
  (as-> state state
    (reduce (fn [acc-state f]
              (f acc-state e))
            state
            [(button/mouse-released (get-in state [:sprites :end :buttons outcome]))])
    (update-in state [:sprites :end :buttons outcome]
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
