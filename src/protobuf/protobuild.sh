#!/bin/bash

SRC_DIR="."
DST_DIR="../"


# echo protoc -I$SRC_DIR --java_out=$DST_DIR $SRC_DIR/TypeDescriptor.proto
protoc -I$SRC_DIR --java_out=$DST_DIR $SRC_DIR/TypeDescriptor.proto
protoc -I$SRC_DIR --java_out=$DST_DIR $SRC_DIR/Pin.proto
protoc -I$SRC_DIR --java_out=$DST_DIR $SRC_DIR/Port.proto
