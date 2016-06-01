(ns flux-master.db.bulbs
  (:require [hugsql.core :as hugsql]
            [clojure.java.io :as io]))

(hugsql/def-db-fns (io/resource "bulbs.sql"))

(defn create-tables [db]
  (create-bulb-table db))

(defn drop-tables [db]
  (drop-bulb-table db))


