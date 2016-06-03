(ns flux-master.db
  (:require [hugsql.core :as hugsql]
            [clojure.java.io :as io]
            [flux-master.util :refer [map-vals-with-keys map-keys]]
            [clojure.string :as str]))

(hugsql/def-db-fns (io/resource "bulbs.sql"))

(defn create-tables [db]
  (create-bulb-table db))

(defn drop-tables [db]
  (drop-bulb-table db))

(defn all-bulbs [db]
  (all-bulbs* db))

(defn get-bulb [db id]
  (get-bulb* db {:id id}))

(defn get-bulb-desc [db desc]
  (first (filter #(= (str/lower-case desc) (str/lower-case (:description %)))
                 (all-bulbs db))))

(defn get-bulb-id-or-desc [db id-or-desc]
  (or (get-bulb db id-or-desc) (get-bulb-desc db id-or-desc)))


