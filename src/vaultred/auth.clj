(ns vaultred.auth
  (:require [buddy.sign.jwt :as jwt]
            [vaultred.config :refer [secret]]
            [vaultred.redis :refer [R]]
            [taoensso.carmine :as car]
            [vaultred.requirements :as re]
            [vaultred.utils :refer [bytes->hex]]
            [buddy.core.nonce :as nonce]
            [vaultred.config :refer [secret]]
            [vaultred.utils :refer [json-resp json-resp-error http-error]]
            [clojure.spec :as s]
            [clojure.core.match :refer [match]]
            [buddy.hashers :as hashers]))


(defn now []
  (System/currentTimeMillis))

(defn new-nonce []
  (bytes->hex (nonce/random-bytes 16)))

(defn get-user [username]
  (R
    (car/get (str "USER:" username))))


(defn ok-with-method [method username]
  (let [n (new-nonce)]
    (json-resp {:method method
                :jwt (jwt/sign {:user username
                                :nonce n
                                :ts (now)} secret)})))


(def ok-with-password (partial ok-with-method "password"))
(def ok-with-token (partial ok-with-method "token"))

(defn- fail []
  (json-resp-error
              (http-error 403 "access denied - no can do!")))


(defn create-token [req]
  (let [body (:body req)
        valid? (s/valid? ::re/login-request body)
        username (:username body)
        user (get-user username)]
    (match [valid? (s/conform ::re/login-request body) (hashers/check (:password body) (:password user)) (hashers/check (:token body) (:token user))]
      [false _ _ _] (json-resp-error
                      (http-error 400 (s/explain-str ::re/login-request body)))
      [true [:password _] true _] (ok-with-password username)
      [true [:token _]    _ true] (ok-with-token username)
      [true [_ _] _ _] (fail))))
