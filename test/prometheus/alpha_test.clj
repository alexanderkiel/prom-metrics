(ns prometheus.alpha-test
  (:require
    [clojure.test :refer :all]
    [prometheus.alpha :as prom :refer [defcounter defgauge defhistogram]]))

(set! *warn-on-reflection* true)

(defcounter counter
  "Counter.")

(defcounter counter-namespace
  "Counter with namespace."
  {:namespace "my"})

(defcounter counter-one-label
  "Counter with one label."
  "label_name_1")

(defcounter counter-two-labels
  "Counter with two labels."
  "label_name_1" "label_name_2")

(defcounter counter-three-labels
  "Counter with three labels."
  "label_name_1" "label_name_2" "label_name_3")

(defcounter counter-four-labels
  "Counter with four labels."
  "label_name_1" "label_name_2" "label_name_3" "label_name_4")

(defgauge gauge
  "Gauge.")

(defgauge gauge-one-label
  "Gauge."
  "label")

(defhistogram histogram
  "Histogram."
  [0.0 1.0])

(defhistogram histogram-one-label
  "Histogram with one label."
  [0.0 1.0]
  "label_name_1")

(defhistogram histogram-two-labels
  "Histogram with two labels."
  [0.0 1.0]
  "label_name_1" "label_name_2")

(defhistogram histogram-four-labels
  "Histogram with two labels."
  [0.0 1.0]
  "label_name_1" "label_name_2" "label_name_3" "label_name_4")

(defhistogram histogram-five-labels
  "Histogram with two labels."
  [0.0 1.0]
  "label_name_1" "label_name_2" "label_name_3" "label_name_4" "label_name_5")

(defn- label-range [n]
  (map #(str "label-" %) (range 1 n)))

(deftest counter-test
  (testing "Increment Counter"
    (prom/clear! counter)
    (prom/inc! counter)
    (is (= 1.0 (prom/get counter))))

  (testing "Increment Counter with Namespace"
    (prom/clear! counter-namespace)
    (prom/inc! counter-namespace)
    (is (= 1.0 (prom/get counter-namespace))))

  (testing "Increment Counter by Two"
    (prom/clear! counter)
    (prom/inc! counter 2)
    (is (= 2.0 (prom/get counter))))

  (testing "Increment Counter with One Label"
    (prom/clear! counter-one-label)
    (prom/inc! counter-one-label "label-1")
    (is (= 1.0 (prom/get counter-one-label "label-1"))))

  (testing "Increment Counter with One Label by Two"
    (prom/clear! counter-one-label)
    (prom/inc! counter-one-label "label-1" 2)
    (is (= 2.0 (prom/get counter-one-label "label-1"))))

  (testing "Increment Counter with Two Labels"
    (prom/clear! counter-two-labels)
    (prom/inc! counter-two-labels "label-1" "label-2")
    (is (= 1.0 (prom/get counter-two-labels "label-1" "label-2"))))

  (testing "Increment Counter with Two Labels by Two"
    (prom/clear! counter-two-labels)
    (prom/inc! counter-two-labels "label-1" "label-2" 2)
    (is (= 2.0 (prom/get counter-two-labels "label-1" "label-2"))))

  (testing "Increment Counter with Three Labels"
    (prom/clear! counter-three-labels)
    (prom/inc! counter-three-labels "label-1" "label-2" "label-3")
    (is (= 1.0 (prom/get counter-three-labels "label-1" "label-2" "label-3"))))

  (testing "Increment Counter with Three Labels by Two"
    (prom/clear! counter-three-labels)
    (prom/inc! counter-three-labels "label-1" "label-2" "label-3" 2)
    (is (= 2.0 (prom/get counter-three-labels "label-1" "label-2" "label-3"))))

  (testing "Increment Counter with Four Labels"
    (prom/clear! counter-four-labels)
    (prom/inc! counter-four-labels "label-1" "label-2" "label-3" "label-4")
    (is (= 1.0 (prom/get counter-four-labels "label-1" "label-2" "label-3" "label-4"))))

  (testing "Increment Counter with Four Labels by Two"
    (prom/clear! counter-four-labels)
    (prom/inc! counter-four-labels "label-1" "label-2" "label-3" "label-4" 2)
    (is (= 2.0 (prom/get counter-four-labels "label-1" "label-2" "label-3" "label-4"))))

  (testing "Decrement not Possible"
    (is (thrown? Exception (prom/dec! counter)))))

(deftest gauge-test
  (testing "Increment Gauge"
    (prom/clear! gauge)
    (prom/inc! gauge)
    (is (= 1.0 (prom/get gauge))))

  (testing "Increment Gauge with One Label"
    (prom/clear! gauge-one-label)
    (prom/inc! gauge-one-label "label-1")
    (is (= 1.0 (prom/get gauge-one-label "label-1"))))

  (testing "Increment Gauge by Two"
    (prom/clear! gauge)
    (prom/inc! gauge 2)
    (is (= 2.0 (prom/get gauge))))

  (testing "Decrement Gauge"
    (prom/clear! gauge)
    (prom/dec! gauge)
    (is (= -1.0 (prom/get gauge))))

  (testing "Decrement Gauge with One Label"
    (prom/clear! gauge-one-label)
    (prom/dec! gauge-one-label "label-1")
    (is (= -1.0 (prom/get gauge-one-label "label-1"))))

  (testing "Decrement Gauge by Two"
    (prom/clear! gauge)
    (prom/dec! gauge 2)
    (is (= -2.0 (prom/get gauge))))

  (testing "Set Gauge to One"
    (prom/set! gauge 1)
    (is (= 1.0 (prom/get gauge))))

  (testing "Set Gauge with One Label to One"
    (prom/set! gauge-one-label "label-1" 1)
    (is (= 1.0 (prom/get gauge-one-label "label-1")))))

(deftest histogram-test
  (testing "Use Timer on Histogram and close it through with-open"
    (prom/clear! histogram)
    (with-open [_ (prom/timer histogram)]
      (inc 1))
    (is (pos? (prom/sum histogram))))

  (testing "Use Timer on Histogram"
    (prom/clear! histogram)
    (let [timer (prom/timer histogram)]
      (Thread/sleep 1)
      (prom/observe-duration! timer))
    (is (pos? (prom/sum histogram))))

  (testing "Use Observe on Histogram"
    (prom/clear! histogram)
    (prom/observe! histogram 2))
  (is (= 2.0 (prom/sum histogram)))

  (testing "Use Timer on Histogram with One Label"
    (prom/clear! histogram-one-label)
    (let [timer (prom/timer histogram-one-label "label-1")]
      (Thread/sleep 1)
      (prom/observe-duration! timer))
    (is (pos? (prom/sum histogram-one-label "label-1"))))

  (testing "Use Observe on Histogram with One Label"
    (prom/clear! histogram-one-label)
    (prom/observe! histogram-one-label "label-1" 2)
    (is (= 2.0 (prom/sum histogram-one-label "label-1"))))

  (testing "Use Timer on Histogram with Two Labels"
    (prom/clear! histogram-two-labels)
    (let [timer (prom/timer histogram-two-labels "label-1" "label-2")]
      (Thread/sleep 1)
      (prom/observe-duration! timer))
    (is (pos? (prom/sum histogram-two-labels "label-1" "label-2"))))

  (testing "Use Observe on Histogram with Two Labels"
    (prom/clear! histogram-two-labels)
    (prom/observe! histogram-two-labels "label-1" "label-2" 2)
    (is (= 2.0 (prom/sum histogram-two-labels "label-1" "label-2"))))

  (testing "Use Timer on Histogram with Four Labels"
    (prom/clear! histogram-four-labels)
    (let [timer (apply prom/timer histogram-four-labels (label-range 5))]
      (Thread/sleep 1)
      (prom/observe-duration! timer))
    (is (pos? (apply prom/sum histogram-four-labels (label-range 5)))))

  (testing "Use Observe on Histogram with Four Labels"
    (prom/clear! histogram-four-labels)
    (prom/observe! histogram-four-labels "label-1" "label-2" "label-3" "label-4" 2)
    (is (= 2.0 (apply prom/sum histogram-four-labels (label-range 5)))))

  (testing "Use Timer on Histogram with Five Labels"
    (prom/clear! histogram-five-labels)
    (let [timer (apply prom/timer histogram-five-labels (label-range 6))]
      (Thread/sleep 1)
      (prom/observe-duration! timer))
    (is (pos? (apply prom/sum histogram-five-labels (label-range 6)))))

  (testing "Use Observe on Histogram with Five Labels"
    (prom/clear! histogram-five-labels)
    (prom/observe! histogram-five-labels "label-1" "label-2" "label-3" "label-4"
                   "label-5" 2)
    (is (= 2.0 (apply prom/sum histogram-five-labels (label-range 6)))))

  (testing "Increment not Possible"
    (is (thrown? Exception (prom/inc! histogram)))))

(comment
  (require '[criterium.core :refer [quick-bench bench]])

  ;; JDK 1.8.0_152

  ;; 16 ns
  (quick-bench (prom/inc! counter))

  ;; 46 ns
  (quick-bench (prom/inc! counter-one-label "label-1"))

  ;; 16 ns
  (let [counter (prom/collector counter-one-label "label-1")]
    (quick-bench (prom/inc! counter)))

  ;; 50 ns
  (quick-bench (prom/inc! counter-two-labels "label-1" "label-2"))

  ;; 18 ns
  (let [counter (prom/collector counter-two-labels "label-1" "label-2")]
    (quick-bench (prom/inc! counter)))

  ;; 56 ns
  (quick-bench (prom/inc! counter-three-labels "label-1" "label-2" "label-3"))

  ;; 19 ns
  (let [counter (prom/collector counter-three-labels "label-1" "label-2" "label-3")]
    (quick-bench (prom/inc! counter)))

  ;; 800 ns
  (quick-bench (prom/inc! counter-four-labels "label-1" "label-2" "label-3" "label-4"))

  ;; 90 ns
  (quick-bench (prom/timer histogram-two-labels "label-1" "label-2"))

  ;; 995 ns
  (quick-bench (prom/timer histogram-four-labels "label-1" "label-2"
                           "label-3" "label-4"))

  ;; 1 us
  (quick-bench (prom/timer histogram-five-labels "label-1" "label-2"
                           "label-3" "label-4" "label-5"))

  ;; 56 ns
  (let [collector (prom/collector histogram-two-labels "label-1" "label-2")]
    (quick-bench (prom/timer collector)))

  (prom/dump-metrics)

  )
