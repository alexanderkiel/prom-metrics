lint:
	clj-kondo --lint src test deps.edn

test:
	clojure -Sdeps '{:deps {org.clojure/clojure {:mvn/version "1.11.1"}}}' -M:test:kaocha --profile :ci
	clojure -Sdeps '{:deps {org.clojure/clojure {:mvn/version "1.10.3"}}}' -M:test:kaocha --profile :ci
	clojure -Sdeps '{:deps {org.clojure/clojure {:mvn/version "1.9.0"}}}' -M:test:kaocha --profile :ci

build:
	clojure -X:depstar jar

clean:
	rm -rf .clj-kondo/.cache .cpcache target

deploy:
	mvn deploy:deploy-file -Dfile=target/prom-metrics.jar -DpomFile=pom.xml -DrepositoryId=clojars -Durl=https://clojars.org/repo/

outdated:
	clojure -M:outdated

.PHONY: lint test build clean deploy outdated
