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

(def mgdb-name "databaseName")
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
      (device-info device))
    (catch Exception e nil)))

;;; Mongo Functions
;;; ===============

(defn store-in-mongo
  "Stores the given information in mongo."
  [db-name collection data]
  (try
    (let [conn (mg/connect)
          db (mg/get-db conn db-name)]
      (mc/insert db collection data)
      (mg/disconnect conn))
    (catch Exception e (println :connection-failed))))

(defn create-db-map
  "Creates a map that is a row in the mongo database"
  [uri]
  {:_id (digest/md5 uri)
   :ip (uri->ip uri)
   :uri uri})

(defn collection
  "Retrieve information from mongo"
  [db-name coll-name]
  :temp-collection)

(defn -main
  "Returns a list of URIs of devices found on the network."
  [& args]
  (let [uris (discover-camera-uris)
        ; Create a list of data to be inserted.
        data (map create-db-map uris)]
    (println :data)
    (println data)))
    ; Query mongo to display what is there already.
    ; Insert data into mongo.
