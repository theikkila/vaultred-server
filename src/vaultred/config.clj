(ns vaultred.config
  (:require [environ.core :refer [env]]))

(def secret (or (env :jwtsecret) "97114da5e694b7f77d16a7cd5677d44643537c6bd948fffa94a9cdab54500ffa6581064ce3d10ac5eac472ad95b6d1e90065cc273fc2db834fbd8b087c697049"))

(def redis-url (or (env :redis) "redis://localhost:32768"))
