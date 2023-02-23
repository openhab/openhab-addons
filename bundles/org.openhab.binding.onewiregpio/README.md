# OneWire GPIO Binding

This binding reads temperature values from OneWire bus sensors connected to the GPIO bus on Raspberry Pi.

## Supported Things

Temperature sensors.
Tested successfully with DS18B20 sensor on Raspberry Pi 3.

## Binding Configuration

The binding requires OneWire sensor to be properly connected to Raspberry Pi GPIO bus,
"w1_gpio" and "wire" kernel modules should be loaded.
Configuration is proper when /sys/bus/w1/devices folder is present, and contains sensor's data.

## Thing Configuration

The sensors are visible in the system as folders containing files with sensor data.
By default all OneWire GPIO devices are stored in /sys/bus/w1/devices/DEVICE_ID_FOLDER,
and the temperature value is available in the file "w1_slave". The Thing needs full path to the w1_slave file.
Note the values in sysfs are in Celsius.

Optional parameter precision makes it easier to lower precision of the sensor value, i.e. precision 1 makes sensor value to show only one digit after the floating point, precision 2 - shows 2 digits. It makes precision reduction with round up, i.e. 20.534C with precision 1 will be 20.5C, 20.555 with same precision will be 20.6C. Allowed values are from 0 to 3. Default value of parameter is 3(max precision).

In the thing file, this looks e.g. like

```java
Thing onewiregpio:sensor:livingRoom "Living room" [gpio_bus_file="/sys/bus/w1/devices/28-0000061b587b/w1_slave",refresh_time=30,precision=1]
```

## Channels

The devices supports currently one channel - "temperature" which allows to read temperature from OneWire temperature sensor.

## Full Example

sample onewiregpio.things file content:

```java
Thing onewiregpio:sensor:livingroom "Living room" [gpio_bus_file="/sys/bus/w1/devices/28-0000061b587b/w1_slave",refresh_time=30]
```

sample onewiregpio.items file content (implements QuantityType for unit conversion):

``` java
Number:Temperature LivingRoomTemperature      "Temperature: [%.2f %unit%]" <temperature>  { channel="onewiregpio:sensor:livingroom:temperature" }
```

sample demo.sitemap file content:

```perl
sitemap demo label="Main Menu"
{
    Text item=LivingRoomTemperature
}
```
