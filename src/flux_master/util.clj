(ns flux-master.util
  (:require [clojure.string :as str])
  (:import (clojure.lang MapEntry)))


;; These map-related functions were lifted from:
;; https://github.com/amalloy/useful/blob/develop/src/flatland/useful/map.clj
(defmacro map-entry
  "Create a clojure.lang.MapEntry from a and b. Equivalent to a cons cell.
  flatland.useful.experimental.unicode contains a shortcut to this, named ·."
  [a b]
  `(MapEntry. ~a ~b))

(defn map-keys
  "Create a new map from m by calling function f on each key to get a new key."
  [m f & args]
  (when m
    (into {}
          (for [[k v] m]
            (map-entry (apply f k args) v)))))

(defn map-vals-with-keys
  "Create a new map from m by calling function f, with two arguments (the key and value)
  to get a new value."
  [m f & args]
  (when m
    (into {}
          (for [[k v] m]
            (map-entry k (apply f k v args))))))

(def colors {:black [0 0 0]
             :blue [0 0 255]
             :brown [165 42 42]
             :cyan [0 255 255]
             :gold [255 215 0]
             :green [0 128 0]
             :gray [128 128 128]
             :maroon [128 0 0]
             :orange [255 165 0]
             :pink [255 192 203]
             :purple [128 0 128]
             :red [255 0 0]
             :white [255 255 255]
             :yellow [255 255 0]})

(defn- valid? [rgb]
  (let [size-three? #(= (count %) 3)
        in-range? (fn [n] (and (>= n 0) (<= n 255)))
        all-vals-in-range? #(every? in-range? %)
        valid-rgb? (every-pred size-three? all-vals-in-range?)]
    (when (valid-rgb? rgb)
      rgb)))

(defn coerce-to-rgb-or-nil [rgb]
  (cond (string? rgb) (get colors (keyword (str/lower-case rgb)))
        (vector? rgb) (valid? rgb)
        :else nil))

