(ns vaultred.core
  (:use org.httpkit.server)
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [taoensso.timbre :as timbre :refer [info  warn  error]]
            [ring.middleware.json :refer [wrap-json-body]]
            [ring.middleware.params :refer [wrap-params]]
            [clojure.core.async :refer [go]]
            [vaultred.utils :refer [json-resp json-resp-error http-error]]
            [vaultred.auth :refer [create-token]]
            [vaultred.secrets :as secrets]
            [vaultred.users :refer [create-user]]
            [vaultred.config :refer [secret]]
            [buddy.auth.backends :as backends]
            [buddy.auth.middleware :refer [wrap-authentication wrap-authorization]]
            [environ.core :refer [env]])
  (:gen-class))



(def backend (backends/jws {:secret secret}))


(defn health [req]
  (json-resp {:health "OK"}))


(defn index [req]
  (json-resp {:name "vault.red"
              :description "vault.red - pwmngr4gks"}))

(defroutes app
  (GET "/" [req] index)
  (GET "/health" [req] health)
  (context "/api/v1" []
    (context "/auth" []
      (POST "/" [req] create-token))
    (context "/users" []
      (POST "/" [req] create-user))
    (context "/me" []
      (GET "/secrets/latest" [req] secrets/get-my-latest-secrets)
      (GET "/secrets/all-versions" [req] secrets/get-my-all-secrets)
      (GET "/secrets/:secret-directory" [req] secrets/get-my-secret)
      (PUT "/secrets/:secret-directory" [req] secrets/create-new-secret-version)
      (POST "/secrets" [req] secrets/create-new-secret)))
  (route/not-found (json-resp-error (http-error 404 "not found"))))


(def server-options
  {:port (Integer/parseInt (or (env :port) "6565"))
   :ip (or (env :host) "0.0.0.0")
   :thread 12})


(defn -main [& args]
  (info "Starting vault.red server on" (:port server-options))
  (run-server
    (-> app
      (wrap-authentication backend)
      (wrap-authorization backend)
      wrap-params
      (wrap-json-body {:keywords? true :bigdecimals? true}))
    server-options))
