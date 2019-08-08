#!/usr/bin/env bash

CURR_DIR=$(cd "$(dirname $0)" && pwd)

export HADOOP_CLASSPATH=${CURR_DIR}/build/libs/ch2-0.0.1.jar
hadoop MaxTemperature ../input/ncdc/sample.txt output
