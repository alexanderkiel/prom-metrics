(ns prometheus.alpha-test
  (:require
    [clojure.test :refer :all]
    [prometheus.alpha :as prom :refer [defcounter defgauge defhistogram]]))

(set! *warn-on-reflection* true)

(defcounter
  :counter
  "Counter.")

(def counter
  (prom/collector :counter))

(defcounter
  :my/counter
  "Counter with namespace.")

(defcounter
  :counter/one_label
  "Counter with one label."
  "label_name_1")

(def counter-one-label
  (prom/collector :counter/one_label))

(defcounter
  :counter/two_labels
  "Counter with two labels."
  "label_name_1" "label_name_2")

(def counter-two-labels
  (prom/collector :counter/two_labels))

(defcounter
  :counter/three_labels
  "Counter with three labels."
  "label_name_1" "label_name_2" "label_name_3")

(def counter-three-labels
  (prom/collector :counter/three_labels))

(defcounter
  :counter/four_labels
  "Counter with four labels."
  "label_name_1" "label_name_2" "label_name_3" "label_name_4")

(def counter-four-labels
  (prom/collector :counter/four_labels))

(defgauge
  :gauge
  "Gauge.")

(defgauge
  :gauge/one_label
  "Gauge."
  "label")

(defhistogram
  :histogram
  "Histogram."
  [0.0 1.0])

(defhistogram
  :histogram/one_label
  "Histogram with one label."
  [0.0 1.0]
  "label_name_1")

(def histogram-one-label
  (prom/collector :histogram/one_label))

(defhistogram
  :histogram/two_labels
  "Histogram with two labels."
  [0.0 1.0]
  "label_name_1" "label_name_2")

(deftest counter-test
  (testing "Increment counter."
    (prom/clear! :counter)
    (prom/inc! :counter)
    (is (= 1.0 (prom/get :counter))))

  (testing "Increment counter from var."
    (prom/clear! counter)
    (prom/inc! counter)
    (is (= 1.0 (prom/get counter))))

  (testing "Increment counter with namespace."
    (prom/clear! :my/counter)
    (prom/inc! :my/counter)
    (is (= 1.0 (prom/get :my/counter))))

  (testing "Increment counter by two."
    (prom/clear! :counter)
    (prom/inc! :counter 2)
    (is (= 2.0 (prom/get :counter))))

  (testing "Increment counter with one label."
    (prom/clear! :counter/one_label)
    (prom/inc! :counter/one_label "label-1")
    (is (= 1.0 (prom/get :counter/one_label "label-1"))))

  (testing "Increment counter with one label from var."
    (prom/clear! :counter/one_label)
    (prom/inc! counter-one-label "label-1")
    (is (= 1.0 (prom/get :counter/one_label "label-1"))))

  (testing "Increment counter with one label from var 2."
    (prom/clear! :counter/one_label)
    (prom/inc! (prom/collector counter-one-label "label-1"))
    (is (= 1.0 (prom/get :counter/one_label "label-1"))))

  (testing "Increment counter with one label by 2."
    (prom/clear! :counter/one_label)
    (prom/inc! :counter/one_label "label-1" 2)
    (is (= 2.0 (prom/get :counter/one_label "label-1"))))

  (testing "Increment counter with two labels."
    (prom/clear! :counter/two_labels)
    (prom/inc! :counter/two_labels "label-1" "label-2")
    (is (= 1.0 (prom/get :counter/two_labels "label-1" "label-2"))))

  (testing "Increment counter with two labels from var."
    (prom/clear! :counter/two_labels)
    (prom/inc! counter-two-labels "label-1" "label-2")
    (is (= 1.0 (prom/get :counter/two_labels "label-1" "label-2"))))

  (testing "Increment counter with two labels from var 2."
    (prom/clear! :counter/two_labels)
    (prom/inc! (prom/collector counter-two-labels "label-1" "label-2"))
    (is (= 1.0 (prom/get :counter/two_labels "label-1" "label-2"))))

  (testing "Increment counter with two labels by 2."
    (prom/clear! :counter/two_labels)
    (prom/inc! :counter/two_labels "label-1" "label-2" 2)
    (is (= 2.0 (prom/get :counter/two_labels "label-1" "label-2"))))

  (testing "Increment counter with three labels."
    (prom/clear! :counter/three_labels)
    (prom/inc! :counter/three_labels "label-1" "label-2" "label-3")
    (is (= 1.0 (prom/get :counter/three_labels "label-1" "label-2" "label-3"))))

  (testing "Increment counter with three labels from var."
    (prom/clear! counter-three-labels)
    (prom/inc! counter-three-labels "label-1" "label-2" "label-3")
    (is (= 1.0 (prom/get counter-three-labels "label-1" "label-2" "label-3"))))

  (testing "Increment counter with three labels from var 2."
    (prom/clear! :counter/three_labels)
    (prom/inc! (prom/collector counter-three-labels "label-1" "label-2" "label-3"))
    (is (= 1.0 (prom/get :counter/three_labels "label-1" "label-2" "label-3"))))

  (testing "Increment counter with three labels by 2."
    (prom/clear! :counter/three_labels)
    (prom/inc! :counter/three_labels "label-1" "label-2" "label-3" 2)
    (is (= 2.0 (prom/get :counter/three_labels "label-1" "label-2" "label-3"))))

  (testing "Increment counter with four labels."
    (prom/clear! :counter/four_labels)
    (prom/inc! :counter/four_labels "label-1" "label-2" "label-3" "label-4")
    (is (= 1.0 (prom/get :counter/four_labels "label-1" "label-2" "label-3" "label-4"))))

  (testing "Increment counter with four labels from var."
    (prom/clear! :counter/four_labels)
    (prom/inc! counter-four-labels "label-1" "label-2" "label-3" "label-4")
    (is (= 1.0 (prom/get :counter/four_labels "label-1" "label-2" "label-3" "label-4"))))

  (testing "Increment counter with four labels by 2."
    (prom/clear! :counter/four_labels)
    (prom/inc! :counter/four_labels "label-1" "label-2" "label-3" "label-4" 2)
    (is (= 2.0 (prom/get :counter/four_labels "label-1" "label-2" "label-3" "label-4"))))

  (testing "Decrement not Possible"
    (is (thrown? Exception (prom/dec! :counter)))))

(deftest gauge-test
  (testing "Increment gauge."
    (prom/clear! :gauge)
    (prom/inc! :gauge)
    (is (= 1.0 (prom/get :gauge))))

  (testing "Increment gauge with label."
    (prom/clear! :gauge/one_label)
    (prom/inc! :gauge/one_label "label-1")
    (is (= 1.0 (prom/get :gauge/one_label "label-1"))))

  (testing "Increment gauge by 2."
    (prom/clear! :gauge)
    (prom/inc! :gauge 2)
    (is (= 2.0 (prom/get :gauge))))

  (testing "Decrement gauge."
    (prom/clear! :gauge)
    (prom/dec! :gauge)
    (is (= -1.0 (prom/get :gauge))))

  (testing "Decrement gauge with one label."
    (prom/clear! :gauge/one_label)
    (prom/dec! :gauge/one_label "label-1")
    (is (= -1.0 (prom/get :gauge/one_label "label-1"))))

  (testing "Decrement gauge by 2."
    (prom/clear! :gauge)
    (prom/dec! :gauge 2)
    (is (= -2.0 (prom/get :gauge))))

  (testing "Set gauge to 1."
    (prom/set! :gauge 1)
    (is (= 1.0 (prom/get :gauge))))

  (testing "Set gauge with one label to 1."
    (prom/set! :gauge/one_label "label-1" 1)
    (is (= 1.0 (prom/get :gauge/one_label "label-1")))))

(deftest histogram-test
  (testing "Use timer on histogram and close it through with-open."
    (prom/clear! :histogram)
    (with-open [_ (prom/timer :histogram)]
      (inc 1))
    (is (pos? (prom/sum :histogram))))

  (testing "Use timer on histogram."
    (prom/clear! :histogram)
    (let [timer (prom/timer :histogram)]
      (inc 1)
      (prom/observe-duration! timer))
    (is (pos? (prom/sum :histogram))))

  (testing "Use timer on histogram with one label."
    (prom/clear! :histogram/one_label)
    (let [timer (prom/timer :histogram/one_label "label-1")]
      (inc 1)
      (prom/observe-duration! timer))
    (is (pos? (prom/sum :histogram/one_label "label-1"))))

  (testing "Use timer on histogram with one label from var."
    (prom/clear! :histogram/one_label)
    (let [timer (prom/timer histogram-one-label "label-1")]
      (inc 1)
      (prom/observe-duration! timer))
    (is (pos? (prom/sum :histogram/one_label "label-1"))))

  (testing "Use timer on histogram with one label from var 2."
    (prom/clear! :histogram/one_label)
    (let [timer (prom/timer (prom/collector histogram-one-label "label-1"))]
      (inc 1)
      (prom/observe-duration! timer))
    (is (pos? (prom/sum :histogram/one_label "label-1"))))

  (testing "Increment not Possible"
    (is (thrown? Exception (prom/inc! :histogram)))))

(comment
  (require '[criterium.core :refer [quick-bench bench]])

  ;; JDK 1.8.0_152
  (quick-bench (prom/inc! :counter))                        ; 34 ns
  (quick-bench (prom/inc! counter))                         ; 23 ns
  (quick-bench (prom/inc! :counter/one_label "label-1"))    ; 122 ns
  (quick-bench (prom/inc! counter-one-label "label-1"))     ; 60 ns
  (let [counter (prom/collector counter-one-label "label-1")]
    (quick-bench (prom/inc! counter)))                      ; 20 ns
  (quick-bench (prom/inc! :counter/two_labels "label-1" "label-2")) ; 121 ns
  (quick-bench (prom/inc! counter-two-labels "label-1" "label-2")) ; 76 ns
  (let [counter (prom/collector counter-two-labels "label-1" "label-2")]
    (quick-bench (prom/inc! counter)))                      ; 20 ns
  (quick-bench (prom/inc! :counter/three_labels "label-1" "label-2" "label-3")) ; 108 ns
  (quick-bench (prom/inc! counter-three-labels "label-1" "label-2" "label-3")) ; 68 ns
  (let [counter (prom/collector counter-three-labels "label-1" "label-2" "label-3")]
    (quick-bench (prom/inc! counter)))                      ; 20 ns
  (quick-bench (prom/inc! :counter/four_labels "label-1" "label-2" "label-3" "label-4")) ; 863 ns
  (prom/get :counter)
  (prom/get :counter/one_label "label-1")
  (prom/get :counter/three_labels "label-1" "label-2" "label-3")
  (prom/get :counter/four_labels "label-1" "label-2" "label-3" "label-4")

  (quick-bench (prom/timer :histogram/two_labels "label-1" "label-2")) ; 204 ns
  (let [collector (prom/collector :histogram/two_labels "label-1" "label-2")]
    (quick-bench (prom/timer collector))) ; 57 ns

  (clojure.repl/pst)
  )
