(ns prdash.date)

(defn- moment [date-string]
  (.moment js/window date-string))

(defn from-now [date-string]
  (->> date-string
       (moment)
       (.fromNow)))
