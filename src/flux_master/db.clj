(ns flux-master.db
  (:require [hugsql.core :as hugsql]
            [clojure.java.io :as io]
            [flux-master.util :refer [map-vals-with-keys map-keys]]
            [clojure.string :as str]))

(hugsql/def-db-fns (io/resource "bulbs.sql"))

(defn- bool-field? [k]
  (str/starts-with? (name k) "is_"))

(defn- convert-bool-val [v]
  (let [true-vals ["true" 1]
        false-vals ["false" 0]]
    (cond (some #(= % v) true-vals) true
          (some #(= % v) false-vals) false
          :else (throw (ex-info (format "Do not recognize boolean val: %s" v) {})))))

(defn- convert-bool-keys [row]
  (map-keys row (fn [k]
                  (if (bool-field? k)
                    (keyword (apply str (drop 3 (name k))))
                    k))))

(defn- convert-bool-vals [row]
  (map-vals-with-keys row (fn [k v]
                            (if (bool-field? k)
                              (convert-bool-val v)
                              v))))

(def convert-bools (comp convert-bool-keys convert-bool-vals))

(defn create-tables [db]
  (create-bulb-table db))

(defn drop-tables [db]
  (drop-bulb-table db))


