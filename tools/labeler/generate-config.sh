#!/bin/bash

config="tools/labeler/config.yml"
echo -n > $config

for dir in $(find bundles/ -mindepth 1 -maxdepth 1 -type d)
do
    label=$(echo $dir | sed -r -e 's|bundles/org.openhab.(.*)|\1|g' -e 's|\.|-|g')
    echo -e "${label}:\n\t- ${dir}/*\n" >> $config
done
