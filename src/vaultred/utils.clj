(ns vaultred.utils
  (:require [clojure.data.json :as json]
            [ring.util.response :refer [response redirect content-type status]]))


(defn http-error [code message]
  {:code code :message message})


(defn to-json [x]
  (json/write-str x))

(defn from-json [text]
  (when text
    (json/read-str text
                   :key-fn keyword)))

(defn json-resp
  ([x] (json-resp x 200))
  ([x http-status]
   (-> (response (to-json x))
     (status http-status)
     (content-type "application/json"))))


(defn json-resp-error [error]
  (json-resp error (:code error)))


(defn mapmap-kv [f m]
  "maps hashmaps with pairs"
  (reduce-kv (fn [prev k v]
                (let [[n-k n-v] (f k v)]
                  (assoc prev n-k n-v))) {} m))

(defn mapmap [f m]
  "maps hashmaps"
  (mapmap-kv (fn [k v] (list k (f v))) m))


(defn uuid [] (str (java.util.UUID/randomUUID)))



(def ^:private ^"[B" hex-chars
  (byte-array (.getBytes "0123456789abcdef" "UTF-8")))


(defn bytes->hex
  "Convert Byte Array to Hex String"
  ^String
  [^"[B" data]
  (let [len (alength data)
        ^"[B" buffer (byte-array (* 2 len))]
    (loop [i 0]
      (when (< i len)
        (let [b (aget data i)]
          (aset buffer (* 2 i) (aget hex-chars (bit-shift-right (bit-and b 0xF0) 4)))
          (aset buffer (inc (* 2 i)) (aget hex-chars (bit-and b 0x0F))))
        (recur (inc i))))
    (String. buffer "UTF-8")))
