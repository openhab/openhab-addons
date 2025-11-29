# OneWire GPIO Binding

This binding reads temperature values from OneWire bus sensors connected to the GPIO bus on a Raspberry Pi.

## Supported Things

Temperature sensors.
Tested successfully with DS18B20 sensor on Raspberry Pi 3.

## Binding Configuration

The binding requires a OneWire sensor properly connected to the Raspberry Pi GPIO bus; the "w1_gpio" and "wire" kernel modules must be loaded.
Configuration is correct when the `/sys/bus/w1/devices` folder is present and contains the sensor's data.

## Thing Configuration

The sensors are visible in the system as folders containing files with sensor data.
By default, all OneWire GPIO devices are stored in `/sys/bus/w1/devices/DEVICE_ID_FOLDER`, and the temperature value is available in the file `w1_slave`. The Thing needs the full path to the `w1_slave` file.
Note the values in sysfs are in Celsius.

The optional `precision` parameter lets you lower the precision of the sensor value, e.g., precision `1` shows one digit after the decimal point, precision `2` shows two digits. Rounding is applied (e.g., 20.534 °C with precision 1 becomes 20.5 °C; 20.555 °C with the same precision becomes 20.6 °C). Allowed values are from 0 to 3. The default is 3 (maximum precision).

In the thing file, this looks e.g. like

```java
Thing onewiregpio:sensor:livingRoom "Living room" [gpio_bus_file="/sys/bus/w1/devices/28-0000061b587b/w1_slave",refresh_time=30,precision=1]
```

## Channels

The device currently supports one channel, "temperature", which reads the temperature from the OneWire sensor.

## Full Example

sample onewiregpio.things file content:

```java
Thing onewiregpio:sensor:livingroom "Living room" [gpio_bus_file="/sys/bus/w1/devices/28-0000061b587b/w1_slave",refresh_time=30]
```

sample onewiregpio.items file content (implements QuantityType for unit conversion):

```java
Number:Temperature LivingRoomTemperature      "Temperature: [%.2f %unit%]" <temperature>  { channel="onewiregpio:sensor:livingroom:temperature" }
```

sample demo.sitemap file content:

```perl
sitemap demo label="Main Menu"
{
    Text item=LivingRoomTemperature
}
```
