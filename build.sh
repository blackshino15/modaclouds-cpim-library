#!/bin/sh
mvn package -DskipTests && echo '' && cp target/cpim-library-2.0-SNAPSHOT.jar  ../kundera-test/lib/  && echo '.jar copied to lib/' && cp target/cpim-library-2.0-SNAPSHOT.jar  ../kundera-test/war/WEB-INF/lib/ && echo '.jar copied to war/WEB-INF/lib/'
