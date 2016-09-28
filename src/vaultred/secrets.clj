(ns vaultred.secrets
  (:require [buddy.sign.jwt :as jwt]
            [vaultred.config :refer [secret]]
            [vaultred.redis :refer [R]]
            [taoensso.carmine :as car]
            [vaultred.requirements :as re]
            [vaultred.utils :refer [bytes->hex uuid]]
            [buddy.core.nonce :as nonce]
            [buddy.auth :refer [authenticated? throw-unauthorized]]
            [vaultred.config :refer [secret]]
            [vaultred.utils :refer [json-resp json-resp-error http-error]]
            [clojure.spec :as s]
            [clojure.core.match :refer [match]]))


(defn get-secret-directory [username]
  (R
    (car/smembers (str "DIRECTORY:" username))))

(defn add-secret-to-directory [username directory-uuid]
  (R
    (car/sadd (str "DIRECTORY:" username) directory-uuid)))

(defn remove-secret-from-directory [username directory-uuid]
  (R
    (car/srem (str "DIRECTORY:" username) directory-uuid)))

;(remove-secret-from-directory "theikkila" nil)

(defn remove-directory [username]
  (R
    (car/del (str "DIRECTORY:" username))))



(defn get-all-secret-versions [username secret-uuid]
  (R
    (car/lrange (str "SECRET:" username ":" secret-uuid) 0 -1)))

(defn get-latest-secret-version [username secret-uuid]
  (first
    (R
      (car/lrange (str "SECRET:" username ":" secret-uuid) 0 1))))


(defn add-secret-version [username secret-uuid secret-content]
  (R
    (car/lpush (str "SECRET:" username ":" secret-uuid) secret-content)))




(defn get-my-latest-secrets [req]
  (if-not (authenticated? req)
    (throw-unauthorized)
    (let [identity (:identity req)
          username (:user identity)
          secret-directory (get-secret-directory username)
          secret-archives (map (partial get-latest-secret-version username) secret-directory)]
      (json-resp (zipmap secret-directory secret-archives)))))


(defn get-my-all-secrets [req]
  (if-not (authenticated? req)
    (throw-unauthorized)
    (let [identity (:identity req)
          username (:user identity)
          secret-directory (get-secret-directory username)
          secret-archives (map (partial get-latest-secret-version username) secret-directory)]
      (json-resp (zipmap secret-directory secret-archives)))))


(defn get-my-secret [req]
  (if-not (authenticated? req)
    (throw-unauthorized)
    (let [identity (:identity req)
          secret-directory (-> req :params :secret-directory)
          username (:user identity)]
      (json-resp (get-all-secret-versions username secret-directory)))))

(defn create-new-secret-version [req]
  (if-not (authenticated? req)
    (throw-unauthorized)
    (let [identity (:identity req)
          username (:user identity)
          secret-content (:body req)
          secret-directory (-> req :params :secret-directory)]
      (do
        (add-secret-to-directory username secret-directory)
        (add-secret-version username secret-directory secret-content)
        (json-resp (get-latest-secret-version username secret-directory) 201)))))

(defn create-new-secret [req]
  (if-not (authenticated? req)
    (throw-unauthorized)
    (let [identity (:identity req)
          username (:user identity)
          secret-content (:body req)
          secret-directory (uuid)]
      (do
        (add-secret-to-directory username secret-directory)
        (add-secret-version username secret-directory secret-content)
        (json-resp {:identifier secret-directory
                    :secret (get-latest-secret-version username secret-directory)} 201)))))
