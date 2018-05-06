# OneWire GPIO Binding

This binding reads temperature values from OneWire bus sensors connected to the GPIO bus on Raspberry PI. 


## Supported Things

Temperature sensors. 
Tested successfully with DS18B20 sensor on Raspberry PI 3.


## Binding Configuration

The binding requires OneWire sensor to be properly connected to Raspberry PI GPIO bus,
"w1_gpio" and "wire" kernel modules should be loaded.
Configuration is proper when /sys/bus/w1/devices folder is present, and contains sensor's data. 

## Thing Configuration

The sensors are visible in the system as folders containing files with sensor data.
By default all OneWire GPIO devices are stored in /sys/bus/w1/devices/DEVICE_ID_FOLDER, 
and the temperature value is available in the file "w1_slave". The Thing needs full path to the w1_slave file.

In the thing file, this looks e.g. like

```
Thing onewiregpio:sensor:livingRoom "Living room" [gpio_bus_file="/sys/bus/w1/devices/28-0000061b587b/w1_slave",refresh_time=30]
```

## Channels

The devices supports currently one channel - "temperature" which allows to read temperature from OneWire temperature sensor.


## Full Example

sample onewiregpio.things file content:

```
Thing onewiregpio:sensor:livingroom "Living room" [gpio_bus_file="/sys/bus/w1/devices/28-0000061b587b/w1_slave",refresh_time=30]
```

sample onewiregpio.items file content:

``` 
Number LivingRoomTemperature      "Temperature: [%.2f °C]" <temperature>  { channel="onewiregpio:sensor:livingroom:temperature" }
```

sample demo.sitemap file content:

```
sitemap demo label="Main Menu"
{
    Text item=LivingRoomTemperature
}
```
