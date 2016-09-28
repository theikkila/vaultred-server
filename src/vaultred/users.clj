(ns vaultred.users
  (:require [vaultred.redis :refer [R]]
            [taoensso.carmine :as car]
            [vaultred.requirements :as re]
            [vaultred.utils :refer [json-resp json-resp-error http-error]]
            [clojure.spec :as s]
            [buddy.hashers :as hashers]))




(defn create-user! [user]
  (let [key (str "USER:" (:username user))]
    (R (car/setnx key
        {:password (hashers/derive (:password user))
         :token (hashers/derive (:token user))}))))

(defn create-user [req]
  (let [body (:body req)
        valid? (s/valid? ::re/new-user-request body)]
    (if (not valid?)
      (json-resp-error (http-error 400 (s/explain-str ::re/new-user-request body)))
      (json-resp {:status (create-user! body)} 201))))
