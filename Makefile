# Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
# SPDX-License-Identifier: Apache-2.0

VERSION=1.0
SRC_HOME=src/main/java/com/digitalasset/examples/repoTrading
JAVA_SRC=$(SRC_HOME)/*.java $(SRC_HOME)/model/*.java $(SRC_HOME)/util/*.java
APP_JAR_NAME=ex-repo-trading-$(VERSION).jar
APP_JAR=lib/$(APP_JAR_NAME)

build: $(APP_JAR)

clean:
	rm -rf target lib

$(APP_JAR): $(JAVA_SRC) pom.xml
	mvn package ; \
	rm -r dependency-reduced-pom.xml
	mkdir -p lib ; cp target/$(APP_JAR_NAME) lib
