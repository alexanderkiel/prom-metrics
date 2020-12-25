lint:
	clj-kondo --lint src test deps.edn

test:
	clojure -M:test --profile :ci

clean:
	rm -rf .clj-kondo/.cache .cpcache target

.PHONY: lint test clean
