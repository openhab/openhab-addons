#!/bin/bash

fname=${1%%.*}

iconv -f iso-8859-1 -t utf-8 ${fname}.csv > ${fname}-utf8.csv

awk -v model="${fname}" -f create_channel_types.awk ${fname}-utf8.csv > ${fname}_types.xml
awk -v model="${fname}" -f create_sensor_channels.awk ${fname}-utf8.csv > ${fname}_sensor_channels.xml
awk -v model="${fname}" -f create_settings_channels.awk ${fname}-utf8.csv > ${fname}_settings_channels.xml

awk -f create_java_variable_information.awk ${fname}-utf8.csv > ${fname}_java.txt

awk -f create_readme_channel_table.awk ${fname}-utf8.csv > ${fname}_readme.txt
