#!/bin/sh

serialPort=$1

echo "172;4;1;1;15;1" > $serialPort
