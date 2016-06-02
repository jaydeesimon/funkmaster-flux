(ns flux-master.endpoint.bulbs
  (:require [compojure.core :refer :all]
            [ring.util.response :refer [response not-found]]
            [flux-master.db :as db]
            [flux-master.db :refer [convert-bools]]
            [flux-led.core :as led]
            [clojure.core.async :refer [>!! >! <! <!! go go-loop chan close! alts! timeout]]))

(defn- bulb-404 [id]
  (not-found {:message (str "Bulb " id " not found.")}))

(defn bulb-endpoint [{{db :spec} :db
                      {bulb-chans :bulb-chans} :bulb-chans-comp}]
  (context "/api/1" []
    (GET "/bulb/:id" [id]
      (if-let [bulb (db/get-bulb db id)]
        (response bulb)
        (bulb-404 id)))

    (GET "/bulbs" []
      (response (db/all-bulbs db)))

    (POST "/bulb/:id/rgb" [id :as {{rgb :rgb} :body}]
      (let [{ip :ip} (db/get-bulb db {:id id})
            c (get @bulb-chans id)]
        (if ip
          (do (go (>! c [led/rgb ip rgb]))
              (go (>! c :pause))
              (response {:id id}))
          (bulb-404 id))))

    (POST "/bulb/:id/white" [id :as {{percent :percent} :body}]
      (response [id percent]))

    (POST "/bulb/:id/on" [id]
      (if-let [{ip :ip} (db/get-bulb db {:id id})]
        (do (led/turn-on ip)
            (response {:id id}))
        (bulb-404 id)))

    (POST "/bulb/:id/off" [id]
      (if-let [{ip :ip} (db/get-bulb db {:id id})]
        (do (led/turn-off ip)
            (response {:id id}))
        (bulb-404 id)))

    (POST "/bulb/:id" [id :as {{:keys [description]} :body}]
      (do (db/update-bulb-description db {:id id :description description})
          (response {:id id})))))