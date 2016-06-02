(ns flux-master.endpoint.bulbs
  (:require [compojure.core :refer :all]
            [ring.util.response :refer [response not-found]]
            [flux-master.db.bulbs :as db]
            [flux-master.db.bulbs :refer [convert-bools]]
            [flux-led.core :as led]))

(defn- bulb-404 [id]
  (not-found {:message (str "Bulb " id " not found.")}))

(defn bulb-request [id f & args]
  )

(defn bulb-endpoint [{{db :spec } :db}]
  (context "/api/1" []
    (GET "/bulb/:id" [id]
      (if-let [bulb (db/get-bulb db {:id id} {} {:row-fn convert-bools})]
        (response bulb)
        (bulb-404 id)))

    (GET "/bulbs" []
      (response (db/all-bulbs db {} {} {:row-fn convert-bools})))

    (POST "/bulb/:id/rgb" [id :as {{rgb :rgb} :body}]
      (bulb-request id led/rgb rgb)
      (if-let [{ip :ip} (db/get-bulb db {:id id})]
        (do (led/rgb ip rgb)
            (response {:id id}))
        (bulb-404 id)))

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