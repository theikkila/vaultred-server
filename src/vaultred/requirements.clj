(ns vaultred.requirements
  (:require [clojure.spec :as s]))


(def numbers (set "1234567890"))
(defn min-x? [x] (comp (partial <= x) count))

(defn same-set? [a b] (and (clojure.set/subset? a b)
                           (clojure.set/superset? a b)))

(defn has-only-keys? [k]
  (fn [m]
    (same-set? (set k) (set (keys m)))))

(def count-pos? (comp pos? count))


(defn min-one-number? [d]
  (count-pos? (filter #(contains? numbers %) d)))

(defn min-one-uppercase? [s]
  (count-pos? (filter #(not= (str %) (clojure.string/lower-case %)) s)))


(s/def ::username (s/and string?
                         (min-x? 6)))

(s/def ::password (s/and string?
                      (min-x? 8)
                      min-one-number?
                      min-one-uppercase?))

(s/def ::title string?)
(s/def ::ctag string?)
(s/def ::tags (s/coll-of string?))
(s/def ::iv string?)
(s/def ::salt string?)
(s/def ::public-description string?)
(s/def ::highsec boolean?)
(s/def ::secret string?)


(s/def ::token (s/and string?
                      (min-x? 64)))

(s/def ::username-and-token (s/and
                              (has-only-keys? [:username :token])
                              (s/keys :req-un [::username ::token])))

(s/def ::username-and-password (s/and
                                 (has-only-keys? [:username :password])
                                 (s/keys :req-un [::username ::password])))


(s/def ::login-request (s/or :token ::username-and-token
                             :password ::username-and-password))


(s/def ::new-user-request (s/and
                            (has-only-keys? [:username :token :password])
                            (s/keys :req-un [::username ::token ::password])))

(s/def ::save-secret-request (s/keys :req-un [::title
                                              ::ctag
                                              ::iv
                                              ::salt
                                              ::highsec
                                              ::tags
                                              ::public-description
                                              ::secret]))

(s/conform ::save-secret-request {:title "moi" :version 2 :public-description "moi" :secret "123"})
