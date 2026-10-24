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
| `integralDecayTime` | Decimal | Time constant in seconds for fading out the I-part while the deviation is not growing. 0 (default) disables the fade-out.                          | N        |
| `integralHoldItem` | Item    | Switch or Contact Item that suspends the I-part while the actuator cannot act on the process. Empty (default) always integrates.                     | N        |
| `integralHoldDirectional` | Boolean | While the hold is active, suspend only the accumulation that takes the I-part further from zero and let a step that brings it back through. `false` (default) suspends both. | N        |
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

### Integral Decay

A plain integrator only unwinds on an error of the opposite sign.
A process that settles slightly off its setpoint, because the actuator is discrete or because the load cannot be fully served, therefore holds a saturated I-part indefinitely: the error never changes sign, so nothing ever reduces it.
Limiting the I-part does not help here, because it bounds the value but not how long it stays there.

`integralDecayTime` fades the I-part towards zero while the deviation is no longer growing.
After one decay time the I-part has fallen to about 37% of its value, after three decay times to about 5%.
While the deviation is still increasing in the direction the I-part is already pushing, which is when integral action is actually needed, the I-part accumulates normally and is not faded out.

Choose the decay time from how long the I-part should be allowed to stay saturated once the demand has gone, and keep it well above the `loopTime`.
A decay that is too fast caps the I-part the controller can build up at all: for a constant error the I-part settles near `ki * error * integralDecayTime / loopTime`, with `loopTime` in seconds, so a control loop that needs a large steady-state I-part to hold its actuator open needs a correspondingly long decay time.
The loop period matters because the controller integrates once per invocation: at a 60 s `loopTime` the same gains and decay time settle sixty times lower than at 1 s.
0 (the default) disables the fade-out and keeps the classic behaviour.

### Integral Hold

Some processes have periods where the actuator physically cannot influence the process variable, without the controller output being saturated.
A mixing damper is the usual example: while the supply air is colder than the room, opening the damper cannot heat that room, so the error persists no matter what the controller does.
Integrating through such a period winds the I-part up to its limit, and the loop is then muted for a long time once the plant recovers.

The controller cannot detect this itself, because from its point of view the error is real and the output is free to move.
`integralHoldItem` lets the rule that knows about the plant say so: while that Item is `ON` (or `CLOSED` for a Contact), the I-part keeps its current value but stops growing.
What has already been accumulated is deliberately retained, because it still describes the steady-state action the process needs; only the growth is suspended.

Anything that is not a definite `ON` leaves the controller integrating, so a missing or uninitialised Item cannot silently freeze the loop.
A configured `integralDecayTime` still applies while the hold is active.

The hold is symmetric by default: it suspends the accumulation in both directions.
That suits a condition the rule reports only briefly, but it also blocks the step that would bring the I-part back, so a loop held through a long period stays at the value it reached even once the process starts moving the right way again.

Set `integralHoldDirectional` to suspend only the accumulation that takes the I-part further from zero, and let a step that brings it back through.
This is the conditional-integration form of anti-windup: the I-part cannot wind further into a condition it cannot act on, but it recovers as soon as the deviation reverses, without the rule having to decide when to stop reporting the condition.
It also makes the hold tolerant of a noisy plant signal, because the decision is taken per step from the sign of the deviation rather than from how long the condition has been reported.

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

> **Notice:** A good starting point for the derivative time constant `kdTimeConstant` is the response time of the control loop.
E.g. the time it takes from opening the heater valve and seeing an effect of the measured temperature.

1. Set `kp`, `ki` and `kd` to 0
1. Increase `kp` until the system starts to oscillate (continuous over- and undershoot)
1. Decrease `kp` a bit, that the system doesn't oscillate anymore
1. Repeat the two steps for the `ki` parameter (keep `kp` set)
1. Repeat the two steps for the `kd` parameter (keep `kp` and `ki` set)
1. As the D-part acts as a damper, you should now be able to increase `kp` and `ki` further without resulting in oscillations

After each modification of above parameters, test the system response by introducing a setpoint deviation (error).
This can be done either by changing the setpoint (e.g. 20°C -> 25°C) or by forcing the measured value to change (e.g. by opening a window).

This process can take some time with slow responding control loops like heating systems.
You will get faster results with constant lighting or PV zero export applications.

## Persisting controller state across restarts

Persisting controller state requires inspector items `iInspector`, `dInspector`, `eInspector` to be configured.
The PID controller uses these Items to expose internal state in order to restore it during startup or reload.

In addition, you need to have persistence set up for these items in openHAB. Please see openHAB documentation regarding
[Persistence](https://www.openhab.org/docs/configuration/persistence.html) for more details and instructions.
