(ns todo-api.adapters.db.box)

(defprotocol IDBBox
  (start [_ config])
  (stop [_])
  (conn [_])
  (todos-ops [_])
  (sub-tasks-ops [_]))
