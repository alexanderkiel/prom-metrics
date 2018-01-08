(ns prometheus.alpha
  (:import
    [clojure.lang Keyword PersistentVector]
    [io.prometheus.client Counter Histogram Counter$Child Histogram$Child CollectorRegistry Gauge Gauge$Child Collector SimpleCollector SimpleCollector$Builder Histogram$Timer Summary Summary$Timer Summary$Child Histogram$Child$Value]
    [io.prometheus.client.exporter.common TextFormat]
    [io.prometheus.client.hotspot DefaultExports]
    [java.io StringWriter Closeable])
  (:refer-clojure :exclude [get]))

(set! *warn-on-reflection* true)

(defn register-default-exports! []
  (DefaultExports/initialize))

(defonce ^:private registry (atom {}))

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

  Keyword
  (collector-
    ([k] (clojure.core/get @registry k))
    ([k label]
     (collector- (collector- k) label))
    ([k label-1 label-2]
     (collector- (collector- k) label-1 label-2))
    ([k label-1 label-2 label-3]
     (collector- (collector- k) label-1 label-2 label-3))
    ([k label-1 label-2 label-3 labels]
     (collector- (collector- k) label-1 label-2 label-3 labels)))

  Object
  (collector- [_]))

(defn collector
  "Coerce to collector. Throws an Exception if undefined."
  ([x]
   (if-let [collector (collector- x)]
     collector
     (throw (Exception. (str "Unknown collector " x)))))
  ([x label]
   (if-let [collector (collector- x label)]
     collector
     (throw (Exception. (str "Unknown collector " x)))))
  ([x label-1 label-2]
   (if-let [collector (collector- x label-1 label-2)]
     collector
     (throw (Exception. (str "Unknown collector " x)))))
  ([x label-1 label-2 label-3]
   (if-let [collector (collector- x label-1 label-2 label-3)]
     collector
     (throw (Exception. (str "Unknown collector " x)))))
  ([x label-1 label-2 label-3 & labels]
   (if-let [collector (collector- x label-1 label-2 label-3 labels)]
     collector
     (throw (Exception. (str "Unknown collector " x))))))

(defn- register! [c k]
  (swap! registry assoc k c)
  (.register (CollectorRegistry/defaultRegistry) c))

(defn- unregister! [c k]
  (.unregister (CollectorRegistry/defaultRegistry) c)
  (swap! registry dissoc k c))

(defn- ^SimpleCollector$Builder with-namespace
  [^SimpleCollector$Builder builder keyword]
  (if-let [namespace (namespace keyword)]
    (.namespace builder namespace)
    builder))

(defn defcounter
  "Given a namespace-qualified keyword, a help text and label-names, registers
  a counter in the default registry."
  [keyword help & label-names]
  (when-let [counter (collector- keyword)]
    (unregister! counter keyword))
  (-> (Counter/build)
      (with-namespace keyword)
      (.name (name keyword))
      (.labelNames (into-array String label-names))
      (.help help)
      (.create)
      (register! keyword)))

(defn defgauge
  "Given a namespace-qualified keyword, a help text and label-names, registers
  a gauge in the default registry."
  [keyword help & label-names]
  (when-let [gauge (collector- keyword)]
    (unregister! gauge keyword))
  (-> (Gauge/build)
      (with-namespace keyword)
      (.name (name keyword))
      (.labelNames (into-array String label-names))
      (.help help)
      (.create)
      (register! keyword)))

(defn defhistogram
  "Given a namespace-qualified keyword, a help text, label-names and buckets,
  registers a histogram in the default registry."
  [keyword help buckets & label-names]
  (when-let [histogram (collector- keyword)]
    (unregister! histogram keyword))
  (-> (Histogram/build)
      (.buckets (double-array buckets))
      (with-namespace keyword)
      (.name (name keyword))
      (.help help)
      (.labelNames (into-array String label-names))
      (.create)
      (register! keyword)))

(defprotocol Clear
  (clear- [x]))

(extend-protocol Clear
  SimpleCollector
  (clear- [c] (.clear c)))

(defn clear!
  "Clears all metrics of the collector. After calling clear! captured labeled
  instances are detached."
  [x]
  (clear- (collector x)))

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
       (~fn (collector ~'x) 1))
     ~@(for [i (range n)]
         `([~'x ~@(label-syms i) ~'label-or-amount]
            (if (string? ~'label-or-amount)
              (~fn (collector ~'x ~@(label-syms i) ~'label-or-amount) 1)
              (~fn (collector ~'x ~@(label-syms i)) ~'label-or-amount))))
     ([~'x ~@(label-syms n) & ~'labels]
       (if (string? (last ~'labels))
         (~fn (apply collector ~'x ~@(label-syms n) ~'labels) 1)
         (~fn (apply collector ~'x ~@(label-syms n) (butlast ~'labels))
           (last ~'labels))))))

(def
  ^{:doc "Increments a counter or gauge by the given amount or 1."
    :arglists '([collector & labels] [collector & labels amount])}
  inc!
  (collect-fn-opt-amount inc- 4))

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
    :arglists '([gauge & labels] [gauge & labels amount])}
  dec!
  (collect-fn-opt-amount dec- 4))

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
            (~fn (collector ~'x ~@(label-syms i)) ~'amount)))
     ([~'x ~@(label-syms n) & ~'labels-and-amount]
       (~fn (apply collector ~'x ~@(label-syms n) (butlast ~'labels-and-amount))
         (last ~'labels-and-amount)))))

(def
  ^{:doc "Sets a gauge to the given amount."
    :arglists
    '([gauge amount]
       [gauge label amount]
       [gauge & labels amount])}
  set!
  (collect-fn-req-amount set- 4))

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

(defn get
  "Gets the current value of a counter, gauge histogram or summary."
  {:arglists '([collector & labels])}
  ([x] (get- (collector x)))
  ([x & labels] (get- (apply collector x labels))))

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
    '([collector amount]
       [collector label amount]
       [collector & labels amount])}
  observe!
  (collect-fn-req-amount observe- 4))

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

(defmacro ^:private collect-fn-2 [fn n]
  `(fn
     ~@(for [i (range n)]
         `([~'x ~@(label-syms i)]
            (~fn (collector ~'x ~@(label-syms i)))))))

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
