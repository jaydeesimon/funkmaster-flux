(ns flux-master.bulbs
  (:require [clojure.java.jdbc :as jdbc]
            [flux-master.db.bulbs :as bdb]
            [clojure.set :refer [difference]]))

(defn- insert-missing-bulbs [tx cur-bulb-ids bulbs]
  (let [bulbs-to-insert (remove #(cur-bulb-ids (:id %)) bulbs)]
    (->> (map (partial bdb/insert-bulb tx) bulbs-to-insert)
         (reduce +))))

(defn- update-bulb-ips [tx bulbs]
  (->> (map (fn [{:keys [id ip]}]
              (bdb/update-bulb-ip tx {:id id :ip ip}))
            bulbs)
       (reduce +)))

(defn- update-online-offline [tx cur-bulb-ids bulbs]
  (let [online-ids (set (map :id bulbs))
        offline-ids (difference cur-bulb-ids online-ids)]
    (do (bdb/update-online tx {:ids online-ids :online true})
        (bdb/update-online tx {:ids offline-ids :online false}))))

(defn update-bulb-states! [db bulbs]
  (jdbc/with-db-transaction [tx db]
    (let [cur-bulb-ids (set (map :id (bdb/all-bulbs db)))]
      (insert-missing-bulbs tx cur-bulb-ids bulbs)
      (update-bulb-ips tx bulbs)
      (update-online-offline tx cur-bulb-ids bulbs))))
