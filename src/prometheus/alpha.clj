(ns prometheus.alpha
  (:require
    [clojure.string :as str])
  (:import
    [io.prometheus.client Counter Histogram Counter$Child Histogram$Child
                          CollectorRegistry Gauge Gauge$Child Collector
                          SimpleCollector Histogram$Timer Summary Summary$Timer
                          Summary$Child Histogram$Child$Value]
    [io.prometheus.client.exporter.common TextFormat]
    [io.prometheus.client.hotspot DefaultExports]
    [java.io StringWriter Closeable])
  (:refer-clojure :exclude [get]))

(set! *warn-on-reflection* true)

(defn register-default-exports! []
  (DefaultExports/initialize))

(defn collector? [x]
  (instance? Collector x))

(defn counter? [x]
  (instance? Counter x))

(defn gauge? [x]
  (instance? Gauge x))

(defn histogram? [x]
  (instance? Histogram x))

(defn summary? [x]
  (instance? Summary x))

(defprotocol ToCollector
  (collector-
    [x]
    [x label]
    [x label-1 label-2]
    [x label-1 label-2 label-3]
    [x label-1 label-2 label-3 labels]))

(defn- label-syms [n]
  (map #(symbol (str "label-" %)) (range n)))

(defn- collector-fn-part
  "Returns the collector- function part with arity n for SimpleCollector."
  [n]
  `([~(with-meta 'c {:tag `SimpleCollector}) ~@(label-syms n)]
     (let [~(with-meta 'a {:tag "[Ljava.lang.String;"}) (make-array String ~n)]
       ~@(for [i (range n)]
           `(aset ~'a ~i ~(symbol (str "label-" i))))
       (.labels ~'c ~'a))))

(defmacro collector-fn
  "Generates the collector- function for SimpleCollector with a max arity of n."
  [n]
  `(fn
     ([~'c] ~'c)
     ~@(for [i (range 1 (inc n))]
         (collector-fn-part i))
     ([~(with-meta 'c {:tag `SimpleCollector}) ~@(label-syms n) ~'labels]
       (.labels ~'c (into-array String (into [~@(label-syms n)] ~'labels))))))

(extend SimpleCollector
  ToCollector
  {:collector- (collector-fn 3)})

(extend-protocol ToCollector
  Counter$Child
  (collector-
    ([c] c))

  Gauge$Child
  (collector-
    ([c] c))

  Histogram$Child
  (collector-
    ([c] c))

  Summary$Child
  (collector-
    ([c] c))

  Collector
  (collector-
    ([c] c))

  Object
  (collector- [_]))

(defn collector
  "Coerce to collector."
  ([x]
   (collector- x))
  ([x label]
   (collector- x label))
  ([x label-1 label-2]
   (collector- x label-1 label-2))
  ([x label-1 label-2 label-3]
   (collector- x label-1 label-2 label-3))
  ([x label-1 label-2 label-3 & labels]
   (collector- x label-1 label-2 label-3 labels)))

(defn swap-collector [old new]
  (when (instance? Collector old)
    (.unregister (CollectorRegistry/defaultRegistry) old))
  (.register (CollectorRegistry/defaultRegistry) new)
  new)

(defn- collector-name [name]
  (str/replace name \- \_))

(defn create-counter [name help & more]
  (let [{:keys [namespace subsystem] :as attr-map} (when (map? (first more)) (first more))
        label-names (if attr-map (next more) more)]
    (-> (-> (Counter/build)
            (.name (or (:name attr-map) (collector-name name)))
            (.help help))
        (cond->
          namespace (.namespace namespace)
          subsystem (.subsystem subsystem)
          label-names (.labelNames (into-array String label-names)))
        (.create))))

(defmacro defcounter
  "Defines a counter as var with name.

  Metrics are required to have a name and a help text. An optional namespace and
  subsystem can be defined via attr-map. The metrics name can be overridden by
  name in attr-map. Per default the var name is used with dashes replaced by
  underscores. The full name of the metric is `namespace_subsystem_name` and has
  to conform to `/[a-zA-Z_:][a-zA-Z0-9_:]*/`.

  Labels are used to provide dimensions to metrics. For example: a counter
  defined with two labels has two dimensions for which you have to supply values
  when incrementing.

  Replaces already defined collectors with the same name."
  {:arglists '([name help attr-map? & label-names])}
  [name help & more]
  `(let [c# (create-counter ~(clojure.core/name name) ~help ~@more)
         v# (def ~name)]
     (alter-var-root v# swap-collector c#)
     v#))

(defn create-gauge [name help & more]
  (let [{:keys [namespace subsystem] :as attr-map} (when (map? (first more)) (first more))
        label-names (if attr-map (next more) more)]
    (-> (-> (Gauge/build)
            (.name (or (:name attr-map) (collector-name name)))
            (.help help))
        (cond->
          namespace (.namespace namespace)
          subsystem (.subsystem subsystem)
          label-names (.labelNames (into-array String label-names)))
        (.create))))

(defmacro defgauge
  "Defines a gauge as var with name.

  Metrics are required to have a name and a help text. An optional namespace and
  subsystem can be defined via attr-map. The metrics name can be overridden by
  name in attr-map. Per default the var name is used with dashes replaced by
  underscores. The full name of the metric is `namespace_subsystem_name` and has
  to conform to `/[a-zA-Z_:][a-zA-Z0-9_:]*/`.

  Labels are used to provide dimensions to metrics. For example: a gauge
  defined with two labels has two dimensions for which you have to supply values
  when incrementing.

  Replaces already defined collectors with the same name."
  {:arglists '([name help attr-map? & label-names])}
  [name help & more]
  `(let [c# (create-gauge ~(clojure.core/name name) ~help ~@more)
         v# (def ~name)]
     (alter-var-root v# swap-collector c#)
     v#))

(defn create-histogram [name help & more]
  (let [{:keys [namespace subsystem] :as attr-map} (when (map? (first more)) (first more))
        buckets (if attr-map (second more) (first more))
        label-names (if attr-map (nnext more) (next more))]
    (-> (-> (Histogram/build)
            (.buckets (double-array buckets))
            (.name (or (:name attr-map) (collector-name name)))
            (.help help))
        (cond->
          namespace (.namespace namespace)
          subsystem (.subsystem subsystem)
          label-names (.labelNames (into-array String label-names)))
        (.create))))

(defmacro defhistogram
  "Defines a histogram as var with name.

  Metrics are required to have a name and a help text. An optional namespace and
  subsystem can be defined via attr-map. The metrics name can be overridden by
  name in attr-map. Per default the var name is used with dashes replaced by
  underscores. The full name of the metric is `namespace_subsystem_name` and has
  to conform to `/[a-zA-Z_:][a-zA-Z0-9_:]*/`.

  Buckets is a collection of upper bounds of buckets for the histogram.

  Labels are used to provide dimensions to metrics. For example: a histogram
  defined with two labels has two dimensions for which you have to supply values
  when incrementing.

  Replaces already defined collectors with the same name."
  {:arglists '([name help attr-map? buckets & label-names])}
  [name help & more]
  `(let [c# (create-histogram ~(clojure.core/name name) ~help ~@more)
         v# (def ~name)]
     (alter-var-root v# swap-collector c#)
     v#))

(defprotocol Clear
  (clear- [x]))

(extend-protocol Clear
  SimpleCollector
  (clear- [c] (.clear c)))

(defn clear!
  "Clears all metrics of the collector. After calling clear! captured labeled
  instances are detached."
  [x]
  (clear- (collector- x)))

(defprotocol Inc
  (inc- [x amount]))

(defmacro extend-protocol-inc [& classes]
  `(extend-protocol Inc
     ~@(mapcat
         (fn [class]
           [class `(inc- [~'x ~'amount] (.inc ~'x ~'amount))])
         classes)))

(extend-protocol-inc Counter Counter$Child Gauge Gauge$Child)

(defmacro ^:private collect-fn-opt-amount [fn n]
  `(fn
     ([~'x]
       (~fn (collector- ~'x) 1))
     ~@(for [i (range n)]
         `([~'x ~@(label-syms i) ~'label-or-amount]
            (if (string? ~'label-or-amount)
              (~fn (collector- ~'x ~@(label-syms i) ~'label-or-amount) 1)
              (~fn (collector- ~'x ~@(label-syms i)) ~'label-or-amount))))
     ([~'x ~@(label-syms n) & ~'labels]
       (if (string? (last ~'labels))
         (~fn (collector- ~'x ~@(label-syms n) ~'labels) 1)
         (~fn (collector- ~'x ~@(label-syms n) (butlast ~'labels))
           (last ~'labels))))))

(def
  ^{:doc "Increments a counter or gauge by the given amount or 1."
    :arglists '([collector & labels-and-amount])}
  inc!
  (collect-fn-opt-amount inc- 3))

(defprotocol Dec
  (dec- [x amount]))

(defmacro extend-protocol-dec [& classes]
  `(extend-protocol Dec
     ~@(mapcat
         (fn [class]
           [class `(dec- [~'x ~'amount] (.dec ~'x ~'amount))])
         classes)))

(extend-protocol-dec Gauge Gauge$Child)

(def
  ^{:doc "Decrements a gauge by the given amount or 1."
    :arglists '([gauge & labels-and-amount])}
  dec!
  (collect-fn-opt-amount dec- 3))

(defprotocol Set
  (set- [x amount]))

(defmacro extend-protocol-set [& classes]
  `(extend-protocol Set
     ~@(mapcat
         (fn [class]
           [class `(set- [~'x ~'amount] (.set ~'x ~'amount))])
         classes)))

(extend-protocol-set Gauge Gauge$Child)

(extend-protocol Set
  Histogram$Child
  (set- [_ _]
    (throw (Exception. "It's not possible to set the value of a histogram."))))

(defmacro ^:private collect-fn-req-amount [fn n]
  `(fn
     ~@(for [i (range n)]
         `([~'x ~@(label-syms i) ~'amount]
            (~fn (collector- ~'x ~@(label-syms i)) ~'amount)))
     ([~'x ~@(label-syms n) & ~'labels-and-amount]
       (~fn (collector- ~'x ~@(label-syms n) (butlast ~'labels-and-amount))
         (last ~'labels-and-amount)))))

(def
  ^{:doc "Sets a gauge to the given amount."
    :arglists
    '([gauge amount]
       [gauge label amount]
       [gauge & labels amount])}
  set!
  (collect-fn-req-amount set- 3))

(defprotocol Get
  (get- [x]))

(defmacro extend-protocol-get [& classes]
  `(extend-protocol Get
     ~@(mapcat
         (fn [class]
           [class `(get- [~'x] (.get ~'x))])
         classes)))

(def ^:private empty-string-array (make-array String 0))

(extend-protocol-get
  Counter Counter$Child
  Gauge Gauge$Child
  Histogram$Child
  Summary Summary$Child)

(extend-protocol Get
  Histogram
  (get- [histogram] (get- (.labels histogram empty-string-array))))

(defmacro ^:private collect-fn-2 [fn n]
  `(fn
     ~@(for [i (range n)]
         `([~'x ~@(label-syms i)]
            (~fn (collector- ~'x ~@(label-syms i)))))
     ([~'x ~@(label-syms (dec n)) & ~'labels]
       (~fn (collector- ~'x ~@(label-syms (dec n)) ~'labels)))))

(def
  ^{:doc "Gets the current value of a counter, gauge histogram or summary."
    :arglists '([collector & labels])}
  get
  (collect-fn-2 get- 4))

(defprotocol Observe
  (observe- [x amount]))

(defmacro extend-protocol-observe [& classes]
  `(extend-protocol Observe
     ~@(mapcat
         (fn [class]
           [class `(observe- [~'x ~'amount] (.observe ~'x ~'amount))])
         classes)))

(extend-protocol-observe Histogram Histogram$Child Summary Summary$Child)

(def
  ^{:doc "Observes a given amount to a histogram or summary."
    :arglists
    '([collector & labels-and-amount])}
  observe!
  (collect-fn-req-amount observe- 3))

(defprotocol StartTimer
  (start-timer- [x]))

(defmacro extend-protocol-start-timer [& classes]
  `(extend-protocol StartTimer
     ~@(mapcat
         (fn [class]
           [class `(start-timer- [~'x] (.startTimer ~'x))])
         classes)))

(extend-protocol-start-timer
  Gauge Gauge$Child
  Histogram Histogram$Child
  Summary Summary$Child)

(def
  ^{:doc "Returns a timer of a gauge, histogram or summary.

  The timer is closeable and observes the time at close. It can be used with
  with-open."
    :arglists '([collector & labels])
    :tag `Closeable}
  timer
  (collect-fn-2 start-timer- 4))

(defprotocol Timer
  (observe-duration- [x]))

(defmacro extend-protocol-timer [& classes]
  `(extend-protocol Timer
     ~@(mapcat
         (fn [class]
           [class `(observe-duration- [~'x] (.observeDuration ~'x))])
         classes)))

(extend-protocol-timer
  Histogram$Timer
  Summary$Timer)

(defn observe-duration!
  "Observes the duration of a histogram or summary timer."
  [timer]
  (observe-duration- timer))

(defprotocol Sum
  (sum- [x]))

(extend-protocol Sum
  Histogram$Child$Value
  (sum- [val] (.-sum val)))

(defn sum
  "Gets the current sum of a histogram."
  [histogram & labels]
  (sum- (apply get histogram labels)))

(defprotocol Buckets
  (buckets- [x]))

(extend-protocol Buckets
  Histogram$Child$Value
  (buckets- [val] (vec (.-buckets val))))

(defn buckets
  "Gets the current buckets of a histogram."
  [histogram & labels]
  (buckets- (apply get histogram labels)))

(defn dump-metrics
  "Dumps metrics of the default registry using simple client's text format."
  []
  (let [registry (CollectorRegistry/defaultRegistry)
        writer (StringWriter.)]
    (TextFormat/write004 writer (.metricFamilySamples registry))
    {:status 200
     :headers {"Content-Type" TextFormat/CONTENT_TYPE_004}
     :body (.toString writer)}))
