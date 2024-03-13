(ns lib.debug)

(defn wd
  "Watch - disabled"
  ([in] in)
  ([id in] in)
  ([id f in] in))

(defn we
  "Watch - enabled"
  ([in] (prn " #>" (pr-str in) "<# ") in)
  ([id in] (we id nil in))
  ([id f in] (prn "\n" id ">" ((or f pr-str) in) "<" id "\n") in))