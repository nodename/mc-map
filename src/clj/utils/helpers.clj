(ns utils.helpers)

(defmacro clog [& more]
  `(.log js/console (apply str (list ~@more))))