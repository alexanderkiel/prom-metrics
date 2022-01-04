# Prometheus Metrics

[![Build Status](https://github.com/alexanderkiel/prom-metrics/workflows/Build/badge.svg)](https://github.com/alexanderkiel/prom-metrics/actions)
[![Downloads](https://versions.deps.co/alexanderkiel/prom-metrics/downloads.svg)](https://versions.deps.co/alexanderkiel/prom-metrics)
[![cljdoc badge](https://cljdoc.xyz/badge/prom-metrics/prom-metrics)](https://cljdoc.xyz/d/prom-metrics/prom-metrics/CURRENT)

A Clojure library designed to provide wrappers for Prometheus [SimpleClient](https://github.com/prometheus/client_java) metrics.

## Installation

#### Leiningen

Prometheus Metrics is available from [Clojars](https://clojars.org/prom-metrics/prom-metrics).

[![Clojars Project](http://clojars.org/prom-metrics/prom-metrics/latest-version.svg)](https://clojars.org/prom-metrics/prom-metrics)

## Usage

Require prometheus alpha.

```clojure
(:require [prometheus.alpha :as prom])
```

Define a counter:

```clojure
(prom/defcounter counter "A counter.")
```

Increment the counter:

```clojure
(prom/inc! counter)
```

Create a compojure route so that the prometheus server can poll your application for metrics.

```clojure
(GET "/metrics" [] (prom/dump-metrics))
```

## License

Copyright © 2017-2022 Alexander Kiel

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
