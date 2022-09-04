# PID Controller Automation

This automation implements a [PID](https://en.wikipedia.org/wiki/PID_controller)-T1 controller for openHAB.

A PID controller can be used for closed-loop controls. For example:

- Heating: A sensor measures the room temperature.
  The PID controller calculates the heater's valve opening, so that the room temperature is kept at the setpoint.
- Lighting: A light sensor measures the room's illuminance.
  The PID controller controls the dimmer of the room's lighting, so that the illuminance in the room is kept at a constant level.
- PV zero export: A meter measures the power at the grid point of the building.
  The PID controller calculates the amount of power the battery storage system needs to feed-in or charge the battery, so that the building's grid power consumption is around zero,
  i.e. PV generation, battery storage output power and the building's power consumption are at balance.

## Modules

The PID controller can be used in openHAB's [rule engine](https://www.openhab.org/docs/configuration/rules-dsl.html).
This automation provides a trigger module ("PID controller triggers").
The return value is used to feed the Action module "Item Action" aka "send a command", which controls the actuator.

To configure a rule, you need to add a Trigger ("PID controller triggers") and an Action ("Item Action").
Select the Item you like to control in the "Item Action" and leave the command empty.

### Trigger

This module triggers whenever the `input` or the `setpoint` changes or the `loopTime` expires.
Every trigger calculates the P-, the I- and the D-part and sums them up to form the `output` value.
This is then transferred to the action module.

| Name             | Type    | Description                                                                                                                                        | Required |
|------------------|---------|----------------------------------------------------------------------------------------------------------------------------------------------------|----------|
| `input`          | Item    | Name of the input [Item](https://www.openhab.org/docs/configuration/items.html) (e.g. temperature sensor value)                                    | Y        |
| `setpoint`       | Item    | Name of the setpoint Item (e.g. desired room temperature)                                                                                          | Y        |
| `kp`             | Decimal | P: [Proportional Gain](#proportional-p-gain-parameter) Parameter                                                                                   | Y        |
| `ki`             | Decimal | I: [Integral Gain](#integral-i-gain-parameter) Parameter                                                                                           | Y        |
| `kd`             | Decimal | D: [Derivative Gain](#derivative-d-gain-parameter) Parameter                                                                                       | Y        |
| `kdTimeConstant` | Decimal | D-T1: [Derivative Gain Time Constant](#derivative-time-constant-d-t1-parameter) in sec.                                                            | Y        |
| `commandItem`    | String  | Send a String "RESET" to this item to reset the I- and the D-part to 0.                                                                            | N        |
| `loopTime`       | Decimal | The interval the output value will be updated in milliseconds. Note: the output will also be updated when the input value or the setpoint changes. | Y        |
| `integralMinValue` | Decimal | The I-part will be limited (min) to this value.                                                                                                    | N        |
| `integralMaxValue` | Decimal | The I-part will be limited (max) to this value.                                                                                                    | N        |
| `pInspector`     | Item    | Name of the inspector Item for the current P-part                                                                                                  | N        |
| `iInspector`     | Item    | Name of the inspector Item for the current I-part                                                                                                  | N        |
| `dInspector`     | Item    | Name of the inspector Item for the current D-part                                                                                                  | N        |
| `eInspector`     | Item    | Name of the inspector Item for the current regulation difference (error)                                                                           | N        |

The `loopTime` should be max a tenth of the system response.
E.g. the heating needs 10 min to heat up the room, the loop time should be max 1 min.
Lower values won't harm, but need more calculation resources.

The I-part can be limited via `integralMinValue`/`integralMaxValue`.
This is useful if the regulation cannot meet its setpoint from time to time.
E.g. a heating controller in the summer, which can not cool (min limit) or when the heating valve is already at 100% and the room is only slowly heating up (max limit).
When controlling a heating valve, reasonable values are 0% (min limit) and 100% (max limit).

You can view the internal P-, I- and D-parts of the controller with the inspector Items.
These values are useful when tuning the controller.
They are updated every time the output is updated.

Inspector items are also used to recover the controller's previous state during startup. This feature allows the PID
controller parameters to be updated and openHAB to be restarted without losing the current controller state.

## Proportional (P) Gain Parameter

Parameter: `kp`

A value of 0 disables the P-part.

A value of 1 sets the output to the current setpoint deviation (error).
E.g. the setpoint is 25°C and the measured value is 20°C, the output will be set to 5.
If the output is the opening of a valve in %, you might want to set this parameter to higher values (`kp=10` would result in 50%).

## Integral (I) Gain Parameter

Parameter: `ki`

The purpose of this parameter is to let the output drift towards the setpoint.
The bigger this parameter, the faster the drifting.

A value of 0 disables the I-part.

A value of 1 adds the current setpoint deviation (error) to the output each `loopTime` (in milliseconds).
E.g. (`loopTimeMs=1000`) the setpoint is 25°C and the measured value is 20°C, the output will be set to 5 after 1 sec.
After 2 sec the output will be 10.
If the output is the opening of a valve in %, you might want to set this parameter to a lower value (`ki=0.1` would result in 30% after 60 sec: 5\*0.1\*60=30).

## Derivative (D) Gain Parameter

Parameter: `kd`

The purpose of this parameter is to react to sudden changes (e.g. an opened window) and also to damp the regulation.
This makes the regulation more resilient against oscillations, i.e. bigger `kp` and `ki` values can be set.

A value of 0 disables the D-part.

A value of 1 sets the output to the difference between the last setpoint deviation (error) and the current.
E.g. the setpoint is 25°C and the measured value is 20°C (error=5°C).
When the temperature drops to 10°C due to an opened window (error=15°C), the output is set to 15°C - 5°C = 10.

## Derivative Time Constant (D-T1) Parameter

Parameter: `kdTimeConstant`

The purpose of this parameter is to slow down the impact of the D-part.

This parameter behaves like a [low-pass](https://en.wikipedia.org/wiki/Low-pass_filter) filter.
The D-part will become 63% of its actual value after `kdTimeConstant` seconds and 99% after 5 times `kdTimeConstant`. E.g. `kdTimeConstant` is set to 10s, the D-part will become 99% after 50s.

Higher values lead to a longer lasting impact of the D-part (stretching) after a change in the setpoint deviation (error).
The "stretching" also results in a lower amplitude, i.e. if you increase this value, you might want to also increase `kd` to keep the height of the D-part at the same level.

## Tuning

Tuning the `Kp`, `Ki` and `Kd` parameters can be done by applying science.
It can also be done by heuristic methods like the [Ziegler–Nichols method](https://en.wikipedia.org/wiki/Ziegler%E2%80%93Nichols_method).
But it can also be done by trial and error.
This results in quite reasonable working systems in most cases.
So, this will be described in the following.

To be able to proceed with this method, you need to visualize the input and the output value of the PID controller over time.
It's also good to visualize the individual P-, I- and D-parts (these are forming the output value) via the inspector items.
The visualization could be done by adding a persistence and use Grafana for example.

After you added a [Rule](https://www.openhab.org/docs/configuration/rules-dsl.html) with above trigger and action module and configured those, proceed with the following steps:

> *Notice:* A good starting point for the derivative time constant `kdTimeConstant` is the response time of the control loop.
E.g. the time it takes from opening the heater valve and seeing an effect of the measured temperature.

1. Set `kp`, `ki` and `kd` to 0
2. Increase `kp` until the system starts to oscillate (continuous over- and undershoot)
3. Decrease `kp` a bit, that the system doesn't oscillate anymore
4. Repeat the two steps for the `ki` parameter (keep `kp` set)
5. Repeat the two steps for the `kd` parameter (keep `kp` and `ki` set)
6. As the D-part acts as a damper, you should now be able to increase `kp` and `ki` further without resulting in oscillations

After each modification of above parameters, test the system response by introducing a setpoint deviation (error).
This can be done either by changing the setpoint (e.g. 20°C -> 25°C) or by forcing the measured value to change (e.g. by opening a window).

This process can take some time with slow responding control loops like heating systems.
You will get faster results with constant lighting or PV zero export applications.

## Persisting controller state across restarts

Persisting controller state requires inspector items `iInspector`, `dInspector`, `eInspector` to be configured.
The PID controller uses these Items to expose internal state in order to restore it during startup or reload.

In addition, you need to have persistence set up for these items in openHAB. Please see openHAB documentation regarding
[Persistence](https://www.openhab.org/docs/configuration/persistence.html) for more details and instructions.
