(ns cannonical.utils
  (:require [quil.core :as q]))

(def blue [0 153 255])
(def black [0])

(def background (partial apply q/background))
(def fill (partial apply q/fill))
(def stroke (partial apply q/stroke))

(defn length
  "Calculate the length of a vector."
  [v]
  (Math/sqrt (+ (Math/pow (first v) 2) (Math/pow (second v) 2))))

(defn get-velocity-vector
  "Calculate a velocity vector from an angle in degrees and a speed
  which defaults to 1. Second element is negative because y axis is
  inverted."
  [r & {:keys [speed] :or {speed 1}}]
  (map (partial * speed)
       [(q/sin (q/radians r))
        (- (q/cos (q/radians r)))]))

(defn wrap-trans-rot
  "Perform a translation, a rotation, invoke the supplied
  function (probably drawing a sprite, then reset the transform matrix
  to the identity."
  [x y r f]
  (q/translate x y)
  (q/rotate (q/radians r))
  (f)
  (q/reset-matrix))

(defn add-held-key
  [state e]
  (update state :held-keys #(conj % (:key e))))

(defn remove-held-key
  [state e]
  (update state :held-keys #(disj % (:key e))))
