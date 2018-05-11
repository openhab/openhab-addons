# Usefull commands to create files used by the binding from Nibe ModbusManager CSV files

Command to convert Nibe CSV file character set to UTF-8:
 ```iconv -f iso-8859-1 -t utf-8 F1X45.csv > F1X45_utf8.csv```

Command to create channel-types from CSV file (all channels are marked as advanced, so remove it manually from desired channels):
```awk -f create_channel_types.awk F1X45_utf8.csv > F1X45.xml```

Command to create all channels from CSV file:
```awk -f create_channels.awk F1X45_utf8.csv > F1X45-all-channels.xml```

Command to create dedicated channels from CSV file:
```awk -f create_sensor_channels.awk F1X45_utf8.csv > F1X45-sensor-channels.xml```
```awk -f create_settings_channels.awk F1X45_utf8.csv > F1X45-settings-channels.xml```

Command to create java variableInformation from CSV file:
```awk -f create_java_variable_information.awk F1X45_utf8.csv > F1X45-variable-information.txt```

Command to crate READ.md channel tables:
```awk -f create_readme_channel_table.awk F1X45_utf8.csv > F1X45-readme-channel-table.txt```
