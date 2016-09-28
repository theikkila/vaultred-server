(ns vaultred.redis
  (:require [taoensso.carmine :as car :refer (wcar)]
            [vaultred.config :refer [redis-url]]))


(def redis-conn {:pool {} :spec {:uri redis-url}}) ; See `wcar` docstring for opts

(defmacro R [& body] `(car/wcar redis-conn ~@body))
