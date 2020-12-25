lint:
	clj-kondo --lint src test deps.edn

test:
	clojure -Sdeps '{:deps {org.clojure/clojure {:mvn/version "1.10.1"}}}' -M:test --profile :ci
	clojure -Sdeps '{:deps {org.clojure/clojure {:mvn/version "1.9.0"}}}' -M:test --profile :ci

build:
	clojure -X:depstar jar

clean:
	rm -rf .clj-kondo/.cache .cpcache target

.PHONY: lint test build clean
