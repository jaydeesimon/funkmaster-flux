(ns flux-master.endpoint.bulbs
  (:require [compojure.core :refer :all]
            [ring.util.response :refer [response not-found status]]
            [flux-master.db :as db]
            [flux-led.core :as led]
            [clojure.core.async :refer [>!! >! <! <!! go go-loop chan close! alts! timeout]]
            [taoensso.truss :refer [have have?]]
            [flux-master.util :as util]))

(defn- bulb-404 [id]
  (not-found {:message (str "Bulb " id " not found.")}))

(defn route-command [id bulb-chans f & args]
  (let [c (have some? (get @bulb-chans id))]
    (go (>! c (vec (concat [f] args))))
    {:id id}))

(defn bad-input [body]
  (-> (response body)
      (status 400)))

(defn bulb-endpoint [{{db :spec} :db
                      {bulb-chans :bulb-chans} :bulb-chans-comp}]
  (context "/api/1" []

    (GET "/bulb/:id" [id]
      (if-let [bulb (db/get-bulb-by-id-or-desc db id)]
        (response bulb)
        (bulb-404 id)))

    (GET "/bulbs" []
      (response (db/all-bulbs db)))

    (POST "/bulbs/rgb" [:as {{rgb :rgb} :body}]
      (let [bulbs (db/all-bulbs db)]
        (if-let [rgb (util/coerce-to-rgb-or-nil rgb)]
          (response (map (fn [bulb]
                           (route-command (:id bulb) bulb-chans led/rgb (:ip bulb) rgb))
                         bulbs))
          (bad-input {:error "Invalid RGB"}))))

    (POST "/bulbs/reset" []
      (let [bulbs (db/all-bulbs db)]
        (response (map (fn [bulb]
                         (route-command (:id bulb) bulb-chans led/warm-white (:ip bulb) 100))
                       bulbs))))

    (POST "/bulb/:id/rgb" [id :as {{rgb :rgb} :body}]
      (if-let [{:keys [id ip]} (db/get-bulb-by-id-or-desc db id)]
        (if-let [rgb (util/coerce-to-rgb-or-nil rgb)]
          (response (route-command id bulb-chans led/rgb ip rgb))
          (bad-input {:error "Invalid RGB"}))
        (bulb-404 id)))

    (POST "/bulb/:id/white" [id :as {{percent :percent} :body}]
      (if-let [{:keys [id ip]} (db/get-bulb-by-id-or-desc db id)]
        (if (and (integer? percent) (>= percent 0) (<= percent 100))
          (response (route-command id bulb-chans led/warm-white ip percent))
          (bad-input {:error "Invalid percent. Please specify from 1-100."}))
        (bulb-404 id)))

    (POST "/bulb/:id/on" [id]
      (if-let [{:keys [id ip]} (db/get-bulb-by-id-or-desc db id)]
        (response (route-command id bulb-chans led/turn-on ip))
        (bulb-404 id)))

    (POST "/bulb/:id/off" [id]
      (if-let [{:keys [id ip]} (db/get-bulb-by-id-or-desc db id)]
        (response (route-command id bulb-chans led/turn-off ip))
        (bulb-404 id)))

    (POST "/bulb/:id" [id :as {{:keys [description]} :body}]
      (do (db/update-bulb-description db {:id id :description description})
          (response {:id id})))))