# Prometheus Metrics

[![Build Status](https://travis-ci.org/alexanderkiel/prom-metrics.svg?branch=master)](https://travis-ci.org/alexanderkiel/prom-metrics)
[![Dependencies Status](https://versions.deps.co/alexanderkiel/prom-metrics/status.svg)](https://versions.deps.co/alexanderkiel/prom-metrics)
[![Downloads](https://versions.deps.co/alexanderkiel/prom-metrics/downloads.svg)](https://versions.deps.co/alexanderkiel/prom-metrics)

A Clojure library designed to provide wrappers for Prometheus [SimpleClient](https://github.com/prometheus/client_java) metrics.

## Installation

#### Leiningen

Prometheus Metrics is available from [Clojars](https://clojars.org/prom-metrics/prom-metrics).

[![Clojars Project](http://clojars.org/prom-metrics/prom-metrics/latest-version.svg)](https://clojars.org/prom-metrics/prom-metrics)

## Concepts

This library embraces the usage of one central registry where all collectors are registered. It provides `def` functions life `defcounter` for each collector of the Java library. Defined collectors are named by keywords which can have namespaces. All functions mutating collectors accept the keyword of a collector. In each JVM there is a global space of named collectors.

## Usage

Require prometheus alpha.

```clojure
(:require [prometheus.alpha :as prom])
```

Define a counter:

```clojure
(prom/defcounter :counter "A counter.")
```

Increment the counter:

```clojure
(prom/inc! :counter)
```

Create a compojure route so that the prometheus server can poll your application for metrics.

```clojure
(GET "/metrics" [] (prom/dump-metrics))
```

## License

Copyright © 2017 Alexander Kiel

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
