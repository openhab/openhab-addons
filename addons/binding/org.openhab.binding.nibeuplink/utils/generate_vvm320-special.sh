#!/bin/bash

echo "<!-- General -->" > vvm320_special_channels.xml
echo "// General" > vvm320_java-special.txt
for i in 40004 40067 43005 43009 40033 43161 40008 40012 40071 40072 44270 43081 43084 47212 48914 40121 44308 44304 44302 44300; do
    grep "${i}" vvm320_java.txt | sed -e "s/ChannelGroup.Sensor/ChannelGroup.General/g; s/ChannelGroup.Setting/ChannelGroup.General/g" >> vvm320_java-special.txt
    grep "${i}" vvm320_sensor_channels.xml >> vvm320_special_channels.xml
    grep "${i}" vvm320_settings_channels.xml >> vvm320_special_channels.xml
    grep "${i}" vvm320_readme.txt | sed -e "s/sensor#/general#/g; s/setting#/general#/g" >> vvm320_readme-special.txt
done

echo "<!-- Hotwater -->" >> vvm320_special_channels.xml
echo "// Hotwater" >> vvm320_java-special.txt
for i in 40013 40014 44306 44298 48132 47041; do
    grep "${i}" vvm320_java.txt | sed -e "s/ChannelGroup.Sensor/ChannelGroup.Hotwater/g; s/ChannelGroup.Setting/ChannelGroup.Hotwater/g" >> vvm320_java-special.txt
    grep "${i}" vvm320_sensor_channels.xml >> vvm320_special_channels.xml
    grep "${i}" vvm320_settings_channels.xml >> vvm320_special_channels.xml
    grep "${i}" vvm320_readme.txt | sed -e "s/sensor#/hotwater#/g; s/setting#/hotwater#/g" >> vvm320_readme-special.txt
done

echo "<!-- Compressor -->" >> vvm320_special_channels.xml
echo "// Compressor" >> vvm320_java-special.txt
for i in 44362 44396 44703 44073 40737 44071 44069 44061 44060 44059 44058 44055 44363 44699 40781 44701 44702 44700; do
    grep "${i}" vvm320_java.txt | sed -e "s/ChannelGroup.Sensor/ChannelGroup.Compressor/g; s/ChannelGroup.Setting/ChannelGroup.Compressor/g" >> vvm320_java-special.txt
    grep "${i}" vvm320_sensor_channels.xml >> vvm320_special_channels.xml
    grep "${i}" vvm320_settings_channels.xml >> vvm320_special_channels.xml
    grep "${i}" vvm320_readme.txt | sed -e "s/sensor#/compressor#/g; s/setting#/compressor#/g" >> vvm320_readme-special.txt
done

echo "<!-- Airsupply -->" >> vvm320_special_channels.xml
echo "// Airsupply" >> vvm320_java-special.txt
for i in 40025 40026 40075 40183 40311 40312 40942; do
    grep "${i}" vvm320_java.txt | sed -e "s/ChannelGroup.Sensor/ChannelGroup.Airsupply/g; s/ChannelGroup.Setting/ChannelGroup.Airsupply/g" >> vvm320_java-special.txt
    grep "${i}" vvm320_sensor_channels.xml >> vvm320_special_channels.xml
    grep "${i}" vvm320_settings_channels.xml >> vvm320_special_channels.xml
    grep "${i}" vvm320_readme.txt | sed -e "s/sensor#/airsupply#/g; s/setting#/airsupply#/g" >> vvm320_readme-special.txt
done