(ns todo-api.system.core
  (:require ^:keep [todo-api.system.api]
            ^:keep [todo-api.system.db]
            ^:keep [todo-api.system.router]
            ^:keep [todo-api.system.web-server]
            [clojure.java.io :as io]
            [integrant.core :as ig]))

(def env "dev")

(def config
  (->> (str env ".edn")
       (io/file "config")
       .getPath
       io/resource
       slurp
       ig/read-string))

(def system nil)

(defn start-system []
  (alter-var-root #'system (fn [_] (ig/init config)))
  :started)

(defn stop-system []
  (when (and (bound? #'system)
             (not (nil? system)))
    (alter-var-root #'system (fn [system] (ig/halt! system)))
    :stopped))
