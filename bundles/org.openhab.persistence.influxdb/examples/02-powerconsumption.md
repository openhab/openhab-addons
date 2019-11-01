## Power meter

The second example shows how to add additional *tags*. Imagine you have a two power meters for the fridge and the dishwasher in your kitchen, and one for the TV on you living room. They all measure the same thing ("instant power"), so you can combine them into one *measurement*.

Content of item file `items/powermeter.items`:

    Group g_instantpower

    Number:Power c_fridge_instantpower "Power fridge [%.1f W]" (g_instantpower) {influxdb="" [room="Kitchen", target="Fridge"]}

    Number:Power c_dishwasher_instantpower "Power dishwasher [%.1f W]" (g_instantpower) {influxdb="" [room="Kitchen", target="Dishwasher"]}

    Number:Power c_tv_instantpower "Power TV [%.1f W]" (g_instantpower) {influxdb="" [room="Living room", target="TV"]}

We are adding two *tags*: "room" and "target". Another *tag* "item" with the item name gets added automatically.

The group "g_instantpower" simplifies the persistence declaration as shown below.

Content of persistence file `persistence/influxdb.persist`:

    Strategies {}

    Items {
        g_instantpower* -> "g_instantpower" : strategy = everyUpdate
    }

This will create a single *measurement* "g_instantpower":

    SELECT * FROM "openhab"."autogen"."g_instantpower" ORDER BY time DESC LIMIT 6
    name: g_instantpower
    time                target     item                       room         value
    ----                ------     ----                       ----         -----
    1565175206339000000 Fridge     c_fridge_instantpower      Kitchen      80
    1565175203349000000 Dishwasher c_dishwasher_instantpower  Kitchen      348
    1565175199542000000 TV         c_tv_instantpower          Living room  63
    1565175194696000000 Fridge     c_fridge_instantpower      Kitchen      81
    1565175194185000000 Dishwasher c_dishwasher_instantpower  Kitchen      390
    1565175193689000000 TV         c_tv_instantpower          Living room  70

You are now able to query the database for the specific item, the target and the room. When adding more power meters, you do not have to modify your Grafana dashboards or alert rules, the only thing you have to do is add them to the group "g_instantpower".