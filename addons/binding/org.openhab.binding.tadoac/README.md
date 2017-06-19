# TadoAC Binding

This is the TadoAC binding which uses the unofficial v2 REST-API.  

## Supported Things

Currently the binding supports the Tado AC Control (IR). 

## Discovery

Tado uses a cloud service for their devices. So discovery is not available


## Thing Configuration

We need to find out two parameters: Your home ID and your zone ID.

First we need to login into the webinterface (https://my.tado.com). Then navigate to your zone that you want to control and look at your URL-bar. In our example chrome shows us this URL:

`https://my.tado.com/webapp/#/home/zone/1`

The last number is your zone id. Finding out the home id is a little bit tricky. We need to sniff the API calls. We recommend the Google Chrome developer tools. Look for a call looking like this:

`https://my.tado.com/api/v2/homes/1234/zones/1/state`

In this case 1234 is your home ID

## Channels

 channel  | item-type  | description |
|---|---|---|
| power      | Switch |AC Power switch|
| mode      | Number | 1=Cool, 2=Dry, 3=Fan |
| fanspeed       | Number | 1=Low, 2=Medium, 3=High|
|temperature| Number| The interval and the steps are given by your AC|

## Full Example

.things file

```
tadoac:airconditioner:myAircon [username="foo@bar.de", password="foobar", homeid=1234, zoneid=1]
```

You can add the optional parameter `interval=<refresh interval in seconds>` to your thing to edit the refresh interval. The default value is 60 seconds.

.items file

```
Switch ac_power "Power" {channel="tadoac:airconditioner:myAircon:power"}
Number ac_mode "Mode "{channel="tadoac:airconditioner:myAircon:mode"}
Number ac_temperature "Temperature [%d°C]" {channel="tadoac:airconditioner:myAircon:temperature"}
Number ac_fanspeed "Fanspeed" {channel="tadoac:airconditioner:myAircon:fanspeed"}
```

.sitemap file

```
Switch item=ac_power label="Power"
Switch item=ac_mode mappings=[1="Cool", 2="Dry", 3="Fan"]
Switch item=ac_fanspeed mappings=[1="Low", 2="Medium", 3="High"]
Setpoint item=ac_temperature minValue=16 maxValue=25 step=1 //Depends on your AC
```
