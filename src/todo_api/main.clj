(ns todo-api.main
  (:require [todo-api.system.core :as system]))

(defn -main [& args]
  (system/start-system))
