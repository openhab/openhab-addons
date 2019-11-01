## Temperature and humidity

The first example shows how to persist item values in a very basic way. We do group the *measurements*, but we do not add additinal *tags*. Think of two temperature sensors and two humidity sensors, each one in your living room and one in your kitchen.

Content of item file `items/temperature.items`:

    Group g_temperature
    Group g_humidity

    Number:Temperature c_livingroom_temperature "Temperature living room [%.1f °C]" (g_temperature)

    Number:Temperature c_kitchen_temperature "Temperature kitchen [%.1f °C]" (g_temperature)

    Number c_livingroom_humidity "Humidity living room [%d%%]" (g_humidity)

    Number c_kitchen_humidity "Humidity kitchen [%d%%]" (g_humidity)

The groups "g_temperature" and "g_humidity" simplify the persistence declaration as shown below. Each group will be one *measurement*.

Content of persistence file `persistence/influxdb.persist`:

    Strategies {}

    Items {
        g_temperature* -> "g_temperature" : strategy = everyUpdate
        g_humidity*    -> "g_humidity"    : strategy = everyUpdate
    }

In the end we have two *measurements*, each has two items:

    SELECT * FROM "openhab"."autogen"."g_temperature" ORDER BY time DESC LIMIT 4
    name: g_temperature
    time                item                       value
    ----                ----                       -----
    1565175206339000000 c_livingroom_temperature   21.2
    1565175203349000000 c_kitchen_temperature      20.7
    1565175199542000000 c_livingroom_temperature   21.1
    1565175194696000000 c_kitchen_temperature      20.6


    SELECT * FROM "openhab"."autogen"."g_humidity" ORDER BY time DESC LIMIT 4
    name: g_humidity
    time                item                       value
    ----                ----                       -----
    1565175206339000000 c_livingroom_humidity      55
    1565175203349000000 c_kitchen_humidity         61
    1565175199542000000 c_livingroom_humidity      55
    1565175194696000000 c_kitchen_humidity         62