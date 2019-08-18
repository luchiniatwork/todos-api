(ns user
  (:require[todo-api.system.core :as system]
           [integrant.repl :refer [clear go halt prep init reset reset-all]]))

(integrant.repl/set-prep! (constantly system/config))
