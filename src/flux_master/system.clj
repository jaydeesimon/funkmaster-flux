(ns flux-master.system
  (:require [com.stuartsierra.component :as component]
            [duct.component.endpoint :refer [endpoint-component]]
            [duct.component.handler :refer [handler-component]]
            [duct.component.hikaricp :refer [hikaricp]]
            [duct.middleware.not-found :refer [wrap-not-found]]
            [meta-merge.core :refer [meta-merge]]
            [ring.component.jetty :refer [jetty-server]]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [ring.middleware.json :refer [wrap-json-response wrap-json-body]]
            [flux-master.endpoint.bulbs :as be]
            [flux-master.bulbs :as b]
            [flux-master.component.bulb-scanner :as bs]
            [flux-master.component.bulb-channels :as bc]))

(def base-config
  {:app {:middleware [[wrap-not-found :not-found]
                      [wrap-json-response]
                      [wrap-json-body :json-body]
                      [wrap-defaults :defaults]]
         :not-found "Resource Not Found"
         :defaults (meta-merge api-defaults {})
         :json-body {:keywords? true}}})

(defn new-system [config]
  (let [config (meta-merge base-config config)]
    (-> (component/system-map
          :app (handler-component (:app config))
          :http (jetty-server (:http config))
          :db (hikaricp (:db config))
          :bulb-endpoint (endpoint-component be/bulb-endpoint)
          :bulb-scanner (bs/bulb-scanner b/insert-bulbs b/with-offline-bulbs b/with-all-bulbs)
          :bulb-chans (bc/bulb-chans-component))
        (component/system-using
          {:http [:app]
           :app [:bulb-endpoint]
           :bulb-endpoint [:db]
           :bulb-scanner [:db :bulb-chans]
           :bulb-chans [:db]}))))
