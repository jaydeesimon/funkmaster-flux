(ns dev
  (:refer-clojure :exclude [test])
  (:require [clojure.repl :refer :all]
            [clojure.pprint :refer [pprint]]
            [clojure.tools.namespace.repl :refer [refresh]]
            [clojure.java.io :as io]
            [com.stuartsierra.component :as component]
            [duct.generate :as gen]
            [meta-merge.core :refer [meta-merge]]
            [reloaded.repl :refer [system init start stop go reset]]
            [ring.middleware.stacktrace :refer [wrap-stacktrace]]
            [dev.tasks :refer :all]
            [flux-master.config :as config]
            [flux-master.system :as system]
            [flux-master.db :as db]))

(def dev-config
  {:app {:middleware [wrap-stacktrace]}})

(def config
  (meta-merge config/defaults
              config/environ
              dev-config))

(defn new-system []
  (into (system/new-system config)
        {}))

(when (io/resource "local.clj")
  (load "local"))

(gen/set-ns-prefix 'flux-master)

(reloaded.repl/set-init! new-system)

(def db (-> system :db :spec))

(defn reset-tables []
  (let [db (-> system :db :spec)]
    (db/drop-tables db)
    (db/create-tables db)))
