(ns camera-detection.core
  (:import [de.onvif.discovery OnfivDiscovery OnvifPointer]
           [de.onvif.soap OnvifDevice]
           [de.onvif.soap.devices InitialDevices MediaDevices]
           [org.me.javawsdiscovery DeviceDiscovery]
           [java.util List])
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [digest])
  (:gen-class))

(def mgdb-name "camera-detection")
(def coll-name "discovered-cameras")

;;; Onvif Functions
;;; ===============
(defn device-info
  "Creates a datastructure that contains device information"
  [onvif-device]
  (if (not (nil? onvif-device))
    {:device onvif-device
     :username (.getUsername onvif-device)
     :time (.getUTCTime onvif-device)
     :date (.getDate onvif-device)
     :uri (.getDeviceUri onvif-device)}
    nil))

(defn uri->ip
  "Get the ip of a uri"
  [uri]
  (re-find #"\d+\.\d+\.\d+.\d+" uri))

(defn create-ip-list
  "Converts a list of strings to a list of ip addresses"
  [strings]
  (into [] (map uri->ip strings)))

(defn discover-camera-uris
  "Retrieves IP addresses of discovered cameras."
  []
  (println "Searching for onvif devices...")
  (let [found-cameras (into [] (DeviceDiscovery/discoverWsDevices))]
    (if (> (count found-cameras) 0)
      (do
        (println (count found-cameras) "devices found.\n")
        found-cameras)
      (do
        (println "No onvif devices found.")
        []))))

(defn discover-camera-ips
  "Retrieves URI's of discovered cameras."
  []
  (create-ip-list (discover-camera-uris)))

(defn camera-info
  "Retrieves information from a camera with a given ip."
  [ip]
  (try
    (let [device (OnvifDevice. ip)]
      (device-info device)) (catch Exception e nil)))

;;; Mongo Functions
;;; ===============

(defn store
  "Stores the given data in mongo."
  [mgdb coll data]
  (try
    (do (mc/insert mgdb coll data)
        (println "STORED!"))
    (catch Exception e (println :connection-failed-store-in-mongo e))))

(defn bulk-store
  "Stores a vector of information in mongo."
  [mgdb coll data-vector]
  (try
    (do (mc/insert-batch mgdb coll data-vector)
        (println "Stored!"))
    (catch Exception e (println "Store Failed:" e))))

(defn create-db-map
  "Creates a map that is a row in the mongo database"
  [uri]
  {:_id (digest/md5 uri)
   :ip (uri->ip uri)
   :uri uri})

(defn collection
  "Retrieve information from mongo"
  [mgdb coll]
  (try
    (mc/find-maps mgdb coll)
    (catch Exception e (println :connection-failed-collection))))

(defn new-cameras
  "Compares found cameras with db cameras. Returns found that arent stored."
  [found database]
  (loop [new-list []
         found-list found
         db-list database]
    (if (empty? found-list)
      new-list
      (if (some #(= (:ip (first found-list)) (:ip %)) db-list)
        (do (println (:ip (first found-list)) "has already been entered.")
            (recur new-list (rest found-list) db-list))
        (do (println (:ip (first found-list)) "IS NEW!")
            (recur (into new-list [(first found-list)]) (rest found-list) db-list))))))

(defn -main
  "Returns a list of URIs of devices found on the network."
  [& args]
        ; Discover cameras on the network.
  (let [uris (discover-camera-uris)
        ; Create a vector of maps that contain :_id :ip :uri.
        data (future (into [] (map create-db-map uris)))
        ; Connect to mongo
        conn (mg/connect)
        ; Select the database we want to use.
        db   (mg/get-db conn mgdb-name)
        ; Retrieve all devices already stored in mongo.
        curr (future (into [] (collection db coll-name)))
        ; Find what devices are newly found.
        diff (future (new-cameras @data @curr))]
    (when (> (count @diff) 0)
      (bulk-store db coll-name @diff))
    (mg/disconnect conn)
    (shutdown-agents)))
