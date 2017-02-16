# PID Controller Binding

The PID Controller binding provides PID controllers with the purpose of fine tuning systems using input/setpoint variables (such as temperature control).
For a glimpse of how a PID Controller works, please check: https://en.wikipedia.org/wiki/PID_controller 
The binding provides 9 channels,  3 (input, setpoint, output) of them being the required channels for the binding to cause result for the items linked to the channels. The other 6 (LoopTime, kpadjuster, kiadjuster, kdadjuster, pidlowerlimit, pidupperlimit) channels (configuration channels for the controller) only provide tweaking  of the controllers initialized, if not set the controller defaults to the following:
LoopTime=1000 (milliseconds)
kpadjuster=1 (multiplier for the Kp - proportional constant in the PID controller)
kiadjuster=1 (multiplier for the Ki - integral constant in the PID controller)
kdadjuster=1 (multiplier for the Kd - derivative constant in the PID controller)
pidlowerlimit=-255 (the lower limit of the output of controller)
pidupperlimit=255 (the upper limit of the output of controller)

In order for the binding to produce result, the input, setpoint and output channels have to be linked to items. 

## Binding configuration

The binding does not need any configuration, all the configuration options of the controllers are available through channels.

##Thing configuration

In the things folder, create a file called pidcontroller.things (or any other name) and configure your controllers inside.

Example:

```
pidcontroller.Controller.Your_Controller_Name

```


## Channels
All channels are provided as Number type.
*input: the input of the controller (from a sensor)
*setpoint: the setpoint of the controller (a virtual item or an item receiving setpoint status from the outside world)
*output: the output of the controller
*LoopTime: sets the duration of the PID loop (has to be provided in positive values milliseconds)
*kpadjuster: multiplier for the Kp - proportional constant in the PID controller
*kiadjuster: multiplier for the Ki - integral constant in the PID controller
*kdadjuster=1 (multiplier for the Kd - derivative constant in the PID controller)
*pidlowerlimit=-255 (the lower limit of the output of controller)
*pidupperlimit=255 (the upper limit of the output of controller)

## Full example

*demo.Things:

```
pidcontroller:Controller:Your_Controller_Name
```

*demo.items:

```
/* PID Controller Items */
Number Your_Controller_Name_input         "Your_Controller_Name input"         { channel="pidcontroller:Controller:Your_Controller_Name:input", someotherbinding=""}
Number Your_Controller_Name_setpoint      "Your_Controller_Name setpoint"      { channel="pidcontroller:Controller:Your_Controller_Name:setpoint" , someotherbinding=""}
Number Your_Controller_Name_output        "Your_Controller_Name output"        { channel="pidcontroller:Controller:Your_Controller_Name:output", someotherbinding="" }
Number Your_Controller_Name_LoopTime      "Your_Controller_Name LoopTime"      { channel="pidcontroller:Controller:Your_Controller_Name:LoopTime" }
Number Your_Controller_Name_kpadjuster    "Your_Controller_Name kpadjuster"    { channel="pidcontroller:Controller:Your_Controller_Name:kpadjuster" }
Number Your_Controller_Name_kiadjuster    "Your_Controller_Name kiadjuster"    { channel="pidcontroller:Controller:Your_Controller_Name:kiadjuster" }
Number Your_Controller_Name_kdadjuster    "Your_Controller_Name kdadjuster"    { channel="pidcontroller:Controller:Your_Controller_Name:kdadjuster" }
Number Your_Controller_Name_pidlowerlimit "Your_Controller_Name pidlowerlimit" { channel="pidcontroller:Controller:Your_Controller_Name:pidlowerlimit" }
Number Your_Controller_Name_pidupperlimit "Your_Controller_Name pidupperlimit" { channel="pidcontroller:Controller:Your_Controller_Name:pidupperlimit" }


```

*demo.sitemap:

```
sitemap demo label="Main Menu"
{
    Frame label="PID Controller inputs" {
        Text item=Your_Controller_Name_input label="Input value [%s]"
        Setpoint item=Your_Controller_Name_input label="Setpoint value [%s]" minValue=X, maxValue=X, step=X 
        }
    Frame label="PID Controller output" {
        Text item=Your_Controller_Name_output label="COntroller output value [%s]"
        }
    Frame label="PID Controller parameters" {
        Setpoint item=Your_Controller_Name_LoopTime label="Duration of loop value [%s]" minValue=100, maxValue=X, step=X
        Setpoint item=Your_Controller_Name_kpadjuster label="Adjust Kp value [%s]" minValue=0, maxValue=X, step=0.1
        Setpoint item=Your_Controller_Name_kiadjuster label="Adjust Ki value [%s]" minValue=0, maxValue=X, step=0.1
        Setpoint item=Your_Controller_Name_kdadjuster label="Adjust Kp value [%s]" minValue=0, maxValue=X, step=0.1
        Setpoint item=Your_Controller_Name_pidlowerlimit label="Controller output minimum value [%s]" minValue=X, maxValue=X, step=X
        Setpoint item=Your_Controller_Name_pidupperlimit label="Controller output maximum value [%s]" minValue=X, maxValue=X, step=X
    }
}
```
