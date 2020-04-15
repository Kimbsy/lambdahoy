(ns lambdahoy.utils
  (:require [quil.core :as q]))

(def blue [0 153 255])
(def black [0])
(def white [255])
(def grey [120])
(def gray grey)
(def red [255 0 0])
(def green [0 255 0])

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

(defn bounded
  "Bounds a number to a maximum and minimum value."
  [n maximum minimum]
  (max minimum (min maximum n)))

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

(defn rotate-vector
  "Rotate a vector about the origin by `r` degreees."
  [[x y] r]
  (let [radians (q/radians r)]
    [(- (* x (q/cos radians))
        (* y (q/sin radians)))
     (+ (* x (q/sin radians))
        (* y (q/cos radians)))]))

(defn orthogonals
  "Calculate the two orthogonal vectors to a given 2D vector.

  Y axis is inverted so this returns [90-degrees-right-vector
                                      90-degrees-left-vector]"
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
  [[x y] r f]
  (q/push-matrix)
  (q/translate x y)
  (q/rotate (q/radians r))
  (f)
  (q/pop-matrix))

(defn encloses?
  "Predicate to determine if a rect contains a pos."
  [{:keys [pos w h]} {:keys [x y]}]
  (let [[xr yr] pos]
    (and (<= xr x (+ xr w))
         (<= yr y (+ yr h)))))

(defn poly-lines
  "Construct the lines that make up a polygon from its points."
  [{:keys [points]}]
  (partition 2 1 (conj points
                       (first points))))

(defn coarse-poly-encloses?
  "Predicate to filter points that are clearly not inside a polygon.

  Checks if the point is outside the minimum rectangle containing the
  polygon. If the point is inside this rectangle we should use
  `fine-poly-encloses` to check properly."
  [{:keys [points]} [x y]]
  (let [xs (map first points)
        ys (map second points)]
    (and (<= (apply min xs) x (apply max xs))
         (<= (apply min ys) y (apply max ys)))))

(defn lines-intersect?
  "Predicate to determine if two lines intersect.

  line a: (x1, y1) -> (x2, y2)
  line b: (x3, y3) -> (x4, y4)"
  [[[x1 y1] [x2 y2]] [[x3 y3] [x4 y4]]]

  ;; Division by zero protection, if this should have been an
  ;; intersection we'll likely get it on the next frame.
  (let [denom-a (- (* (- y4 y3) (- x2 x1))
                   (* (- x4 x3) (- y2 y1)))

        denom-b (- (* (- y4 y3) (- x2 x1))
                   (* (- x4 x3) (- y2 y1)))]
    (when-not (or (zero? denom-a)
                  (zero? denom-b))
      (let [intersection-a (/ (- (* (- x4 x3) (- y1 y3))
                                 (* (- y4 y3) (- x1 x3)))
                              denom-a)
            intersection-b (/ (- (* (- x2 x1) (- y1 y3))
                                 (* (- y2 y1) (- x1 x3)))
                              denom-b)]
        (and (<= 0 intersection-a)
             (< intersection-a 1)
             (<= 0 intersection-b)
             (< intersection-b 1))))))

(defn pos->ray
  "Creates an arbitrarily long line starting at the specified pos."
  [[x y]]
  [[x y] [(+ x 10000) y]])

(defn fine-poly-encloses?
  "Uses ray casting to check if a polygon encloses a pos.

  We construct a line starting at our point and count how many of the
  polygon lines it intersects, an odd number of intersections means
  the point is inside the polygon.

  Our line should be infinite, but in practice any large number will
  suffice."
  [poly pos]
  (let [ray (pos->ray pos)]
    (->> (poly-lines poly)
         (filter #(lines-intersect? % ray))
         count
         odd?)))

(defn poly-encloses?
  "Predicate to determine if a polygon contains a pos"
  [poly pos]
  (when (coarse-poly-encloses? poly pos)
    (fine-poly-encloses? poly pos)))

(defn darken
  [color]
  (map #(max 0 (- % 30)) color))

(defn lighten
  [color]
  (map #(min 255 (+ % 30)) color))

(defn draw-bounding-poly
  [poly]
  (q/no-fill)
  (stroke black)
  (q/stroke-weight 4)
  (doseq [[[x1 y1] [x2 y2]] (poly-lines poly)]
    (q/line x1 y1 x2 y2))
  (stroke red)
  (q/stroke-weight 3)
  (doseq [[[x1 y1] [x2 y2]] (poly-lines poly)]
    (q/line x1 y1 x2 y2))
  (q/stroke-weight 1))

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
