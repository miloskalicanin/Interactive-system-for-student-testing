#!/bin/bash

mvn clean install
mvn dependency:copy-dependencies
cd target
java -cp InitDatabase-1.0-SNAPSHOT.jar:dependency/* rs.etf.initdatabase.Main ../../users.txt 
