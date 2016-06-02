(ns flux-master.util
  (:import (clojure.lang MapEntry)))

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

