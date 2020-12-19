# Pulse Width Modulation (PWM) Automation

This automation module implements [Pulse Width Modulation (PWM)](https://en.wikipedia.org/wiki/Pulse-width_modulation).

PWM can be used to control actuators continuously from 0 to 100% that only support ON/OFF commands. E.g. valves or heating burners.
It accomplishes that by switching the actuator on and off with a fixed interval.
The higher the control percentage (duty cycle), the longer the ON phase.

Example: If the fixed interval is 10 sec and the duty cycle is 30%, the output is ON for 3 sec and OFF for 7 sec.

This module is unsuitable for controlling LED lights as the high PWM frequency can't be met.

> Note: The module starts to work only if the duty cycle has been updated at least once.

## Modules

The PWM module can be used in openHAB's [rule engine](https://www.openhab.org/docs/configuration/rules-dsl.html). This automation provides a trigger and an action module.

The trigger module has an input Item `dutycycleItem` (0-100%).
It calculates the ON/OFF state and writes it to the action module, which sends the ON/OFF command the output Item `outputItem`.

### Trigger

| Name            | Type    | Description                                                                                  | Required |
|-----------------|---------|----------------------------------------------------------------------------------------------|----------|
| `dutycycleItem` | Item    | The Item (PercentType) to read the duty cycle from                                           | Yes      |
| `interval`      | Decimal | The constant interval in which the output is switch ON and OFF again in sec.                 | Yes      |
| `minDutyCycle`  | Decimal | Any duty cycle below this value will be increased to this value                              | No       |
| `maxDutycycle`  | Decimal | Any duty cycle above this value will be decreased to this value                              | No       |
| `command`       | Item    | An Item (String) to send commands to, to control the module during runtime                   | No       |
| `deadManSwitch` | Decimal | The output will be switched off, when the duty cycle is not updated within this time (in ms) | No       |

The duty cycle can be limited via the parameters `minDutycycle` and `maxDutyCycle`. This is helpful if you need to maintain a minimum time between the switching of the output.
This is necessary for example for heating burners, which may not be switched on for very short times. The ON time is than increased to `minDutycycle`.
In this case one should also set a max duty cycle to prevent short OFF times.
It makes sense to apply these symmetrically e.g. 10%/90% or 20%/80%.

If the duty cycle is 0% or 100%, the min/max parameters are ignored and the output is switched ON or OFF continuously.

If the duty cycle Item is not updated within the dead-man switch timeout, the output is switched OFF, regardless of the current duty cycle.
The function can be used to save energy if the source of the duty cycle died for whatever reason and doesn't update the value anymore.
When the duty cycle is updated again, the module returns to normal operation.

> Note: The min/max ON/OFF times set via `minDutycycle` and `maxDutycycle` are not met if the dead-man switch triggers and recovers fast.

### Action

| Name         | Type | Description                                      | Required |
|--------------|------|--------------------------------------------------|----------|
| `outputItem` | Item | The Item (Switch) to send the ON/OFF commands to | Yes      |

## Control Algorithm

This module is designed to act fast on duty cycle changes, but at the same time maintains a constant interval and the min/max ON/OFF parameters.
For that reason, the module might seem to act peculiarly in rare cases:

- When the output is ON and the duty cycle is decreased, the output might switch off immediately, if applicable.
Example: The interval is 10 sec and the current duty cycle is 80%.
When the duty cycle is decreased to 20%, the output would switch off immediately, if it has been already ON for more than 2 sec.
- When the duty cycle is 0% for a short interval and then increased again, the output will only switch on when the new interval starts.
- When the duty cycle is 0% or 100% for more than a whole interval, a new interval will start as soon as the duty cycle is updated to a value other than 0%, respective 100%.
- The module starts to work only if the duty cycle Item has been updated at least once.
