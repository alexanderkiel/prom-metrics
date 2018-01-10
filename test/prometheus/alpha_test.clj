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

(defhistogram
  :histogram/four_labels
  "Histogram with four labels."
  [0.0 1.0]
  "label_name_1" "label_name_2" "label_name_3" "label_name_4")

(defhistogram
  :histogram/five_labels
  "Histogram with five labels."
  [0.0 1.0]
  "label_name_1" "label_name_2" "label_name_3" "label_name_4" "label_name_5")

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
      (Thread/sleep 1))
    (is (pos? (prom/sum :histogram))))

  (testing "Use timer on histogram."
    (prom/clear! :histogram)
    (let [timer (prom/timer :histogram)]
      (Thread/sleep 1)
      (prom/observe-duration! timer))
    (is (pos? (prom/sum :histogram))))

  (testing "Use observe on histogram."
    (prom/clear! :histogram)
    (prom/observe! :histogram 2))
  (is (= 2.0 (prom/sum :histogram)))

  (testing "Use timer on histogram with one label."
    (prom/clear! :histogram/one_label)
    (let [timer (prom/timer :histogram/one_label "label-1")]
      (Thread/sleep 1)
      (prom/observe-duration! timer))
    (is (pos? (prom/sum :histogram/one_label "label-1"))))

  (testing "Use timer on histogram with two labels."
    (prom/clear! :histogram/two_labels)
    (let [timer (prom/timer :histogram/two_labels "label-1" "label-2")]
      (Thread/sleep 1)
      (prom/observe-duration! timer))
    (is (pos? (prom/sum :histogram/two_labels "label-1" "label-2"))))

  (testing "Use timer on histogram with four labels."
    (prom/clear! :histogram/four_labels)
    (let [timer (prom/timer :histogram/four_labels "label-1" "label-2"
                            "label-3" "label-4")]
      (Thread/sleep 1)
      (prom/observe-duration! timer))
    (is (pos? (prom/sum :histogram/four_labels "label-1" "label-2"
                        "label-3" "label-4"))))

  (testing "Use timer on histogram with five labels."
    (prom/clear! :histogram/five_labels)
    (let [timer (prom/timer :histogram/five_labels "label-1" "label-2"
                            "label-3" "label-4" "label-5")]
      (Thread/sleep 1)
      (prom/observe-duration! timer))
    (is (pos? (prom/sum :histogram/five_labels "label-1" "label-2"
                        "label-3" "label-4" "label-5"))))

  (testing "Use observe on histogram with one label."
    (prom/clear! :histogram/one_label)
    (prom/observe! :histogram/one_label "label-1" 2)
    (is (= 2.0 (prom/sum :histogram/one_label "label-1"))))

  (testing "Use timer on histogram with one label from var."
    (prom/clear! :histogram/one_label)
    (let [timer (prom/timer histogram-one-label "label-1")]
      (Thread/sleep 1)
      (prom/observe-duration! timer))
    (is (pos? (prom/sum :histogram/one_label "label-1"))))

  (testing "Use timer on histogram with one label from var 2."
    (prom/clear! :histogram/one_label)
    (let [timer (prom/timer (prom/collector histogram-one-label "label-1"))]
      (Thread/sleep 1)
      (prom/observe-duration! timer))
    (is (pos? (prom/sum :histogram/one_label "label-1"))))

  (testing "Increment not Possible"
    (is (thrown? Exception (prom/inc! :histogram)))))

(comment
  (require '[criterium.core :refer [quick-bench bench]])

  ;; JDK 1.8.0_152

  ;; 30 ns
  (quick-bench (prom/inc! :counter))

  ;; 19 ns
  (quick-bench (prom/inc! counter))

  ;; 85 ns
  (quick-bench (prom/inc! :counter/one_label "label-1"))

  ;; 46 ns
  (quick-bench (prom/inc! counter-one-label "label-1"))

  ;; 20 ns
  (let [counter (prom/collector counter-one-label "label-1")]
    (quick-bench (prom/inc! counter)))

  ;; 121 ns
  (quick-bench (prom/inc! :counter/two_labels "label-1" "label-2"))

  ;; 65 ns
  (quick-bench (prom/inc! counter-two-labels "label-1" "label-2"))

  ;; 21 ns
  (let [counter (prom/collector counter-two-labels "label-1" "label-2")]
    (quick-bench (prom/inc! counter)))

  ;; 125 ns
  (quick-bench (prom/inc! :counter/three_labels "label-1" "label-2" "label-3"))

  ;; 60 ns
  (quick-bench (prom/inc! counter-three-labels "label-1" "label-2" "label-3"))

  ;; 19 ns
  (let [counter (prom/collector counter-three-labels "label-1" "label-2" "label-3")]
    (quick-bench (prom/inc! counter)))

  ;; 773 ns
  (quick-bench (prom/inc! :counter/four_labels "label-1" "label-2" "label-3" "label-4"))

  ;; 125 ns
  (quick-bench (prom/timer :histogram/two_labels "label-1" "label-2"))

  ;; 995 ns
  (quick-bench (prom/timer :histogram/four_labels "label-1" "label-2"
                           "label-3" "label-4"))

  ;; 1 us
  (quick-bench (prom/timer :histogram/five_labels "label-1" "label-2"
                           "label-3" "label-4" "label-5"))

  ;; 56 ns
  (let [collector (prom/collector :histogram/two_labels "label-1" "label-2")]
    (quick-bench (prom/timer collector)))

  )
