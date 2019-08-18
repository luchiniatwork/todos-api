(ns todo-api.system.web-server
  (:require [immutant.web :as web]
            [integrant.core :as ig]))

(defmethod ig/init-key :web/server [_ {:keys [port handler] :as opts}]
  (web/run handler {:host "localhost" :port (or port 8080) :path "/"}))

(defmethod ig/halt-key! :web/server [_ ^java.io.Closeable closeable]
  (web/stop))
