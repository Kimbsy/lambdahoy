(ns lambdahoy.utils
  (:require [quil.core :as q]))

(def blue [0 153 255])
(def black [0])
(def white [255])
(def grey [120])
(def gray grey)

(def title-text-size 120)
(def large-text-size 50)
(def default-text-size 25)
(def small-text-size 15)

(def default-font "Ubuntu Mono")
(def bold-font "Ubuntu Mono Bold")
(def italic-font "Ubuntu Mono Italic")

(def background (partial apply q/background))
(def fill (partial apply q/fill))
(def stroke (partial apply q/stroke))

(defn zero-vector?
  "Predicate to check if a vector has length 0."
  [v]
  (every? zero? v))

(defn magnitude
  "Calculate the length of a vector."
  [v]
  (Math/sqrt (reduce + (map #(Math/pow % 2)
                            v))))

(defn unit-vector
  "Calculate the unit vector of a given 2D vector."
  [v]
  (when-not (zero-vector? v)
    (map #(/ % (magnitude v)) v)))

(defn orthogonals
  "Calculate the two orthogonal vectors to a given 2D vector.

  Returns [90-degrees-left-vector 
           90-degrees-right-vector]"
  [[x y]]
  [[(- y) x]
   [y (- x)]])

(defn direction-vector
  "Calculate the unit direction vector based on the rotation angle."
  [r]
  [(q/sin (q/radians r))
   (- (q/cos (q/radians r)))])

(defn rotation-angle
  "Calculate the rotation angle of a vector."
  [[x y]]
  (q/degrees (q/atan2 x y)))

(defn velocity-vector
  "Calculate the velocity vector based on the direction vector and
  speed."
  [{:keys [speed r] :as s}]
  (map (partial * speed)
       (direction-vector r)))

(defn scale-by
  "Generates a function which will scale a vector by the given factor."
  [factor]
  (fn [v] (map (partial * factor) v)))

(defn wrap-trans-rot
  "Perform a translation, a rotation, invoke the supplied
  function (probably drawing a sprite, then reset the transform matrix
  to the identity."
  [x y r f]
  (q/translate x y)
  (q/rotate (q/radians r))
  (f)
  (q/reset-matrix))

(defn encloses?
  "Predicate to determine if a rect contains a pos."
  [{:keys [pos w h]} {:keys [x y]}]
  (let [[xr yr] pos]
    (and (<= xr x (+ xr w))
         (<= yr y (+ yr h)))))

(defn darken
  [color]
  (map #(max 0 (- % 30)) color))

(defn lighten
  [color]
  (map #(min 255 (+ % 30)) color))

(defn change-scene
  [state target]
  (-> state
      (assoc :change-scene? true)
      (assoc :target-scene target)))

(defn add-held-key
  [state e]
  (update state :held-keys #(conj % (:key e))))

(defn remove-held-key
  [state e]
  (update state :held-keys #(disj % (:key e))))
