(ns lambdahoy.scene.menu
  (:require [lambdahoy.scene :as scene]
            [lambdahoy.sprite :as sprite]
            [lambdahoy.sprite.button :as button]
            [lambdahoy.sprite.text :as text]
            [lambdahoy.utils :as u]
            [quil.core :as q]))

(defn start-game
  [state]
  (u/change-scene state :ocean))

(defn exit-game
  [state]
  (prn "exit")
  state)

(defn init-sprites
  []
  {:text    [(text/->text "Lambda-hoy!"
                          [(* (q/width) 1/2)
                           (* (q/height) 1/5)]
                          :size u/title-text-size
                          :color u/white)
             (text/->text "functional adventures on the high seas"
                          [(* (q/width) 1/2)
                           (* (q/height) 5/20)]
                          :style :italic
                          :color u/white)]
   :waves   []
   :buttons [(button/->button "PLAY" [(* (q/width) 1/2) (* (q/height) 1/2)] start-game)
             (button/->button "QUIT" [(* (q/width) 1/2) (* (q/height) 2/3)] exit-game)]})

(defn update-state
  [state]
  state)

(defn draw-state
  [state]
  (u/background u/blue)
  (doall (map sprite/draw-animated-sprite
              (get-in state [:sprites :menu :waves])))
  (doall (map text/draw-self
              (get-in state [:sprites :menu :text])))
  (doall (map button/draw-self
              (get-in state [:sprites :menu :buttons]))))

(defn key-pressed
  [state e]
  state)

(defn key-released
  [state e]
  state)

(defn mouse-pressed
  [state e]
  (-> state
      (update-in [:sprites :menu :buttons]
                 button/mouse-pressed
                 e)))

(defn mouse-released
  [state e]
  (as-> state state
    (reduce (fn [acc-state f]
              (f acc-state e))
            state
            [(button/mouse-released (get-in state [:sprites :menu :buttons]))])
    (update-in state [:sprites :menu :buttons]
               (fn [buttons]
                 (map #(assoc % :held? false)
                      buttons)))))

(deftype Menu []
  scene/Scene
  (update-state [s state] (update-state state))
  (draw-state [s state] (draw-state state))
  (key-pressed [s state e] (key-pressed state e))
  (key-released [s state e] (key-released state e))
  (mouse-pressed [s state e] (mouse-pressed state e))
  (mouse-released [s state e] (mouse-released state e)))
