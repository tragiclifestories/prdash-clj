(ns prdash.date
  (:require [goog.date :as date])
  (:import goog.i18n.DateTimeFormat))

(defn from-now [date-string]
  (let [fmt (DateTimeFormat. "MMMM dd yyyy")]
    (->> date-string
         date/fromIsoString
         (.format fmt))))
