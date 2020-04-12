(ns lambdahoy.sound
  (:require [clojure.java.io :as io]))

(defonce ^:dynamic *main-music-thread* (atom nil))

(def tracks {:default "music/Captain Scurvy.mp3"})

(def sound-effects {:cannon ["sound-effects/cannon-fire-1.mp3"
                             "sound-effects/cannon-fire-2.mp3"
                             "sound-effects/cannon-fire-3.mp3"
                             "sound-effects/cannon-fire-4.mp3"]})

(defn ->player
  [resource-name]
  (-> resource-name
      io/resource
      io/input-stream
      java.io.BufferedInputStream.
      javazoom.jl.player.Player.))

(defn loop-track
  [track-key]
  (reset! *main-music-thread* (Thread. #(while true (doto (->player (track-key tracks))
                                                      (.play)
                                                      (.close)))))
  (.start @*main-music-thread*))

(defn stop-music
  []
  (.stop @*main-music-thread*))

(defn play-sound-effect
  [sound-effect-key]
  (.start (Thread. #(doto (->player (rand-nth (sound-effect-key sound-effects)))
                      (.play)
                      (.close)))))

(defn init
  []
  (loop-track :default))