# Pulse Width Modulation (PWM) Automation

This automation module implements [Pulse Width Modulation (PWM)](https://en.wikipedia.org/wiki/Pulse-width_modulation).

PWM can be used to control actuators continuously from 0 to 100% that only support ON/OFF commands.
E.g. valves or heating burners.
It accomplishes that by switching the actuator on and off with a fixed interval.
The higher the control percentage (duty cycle), the longer the ON phase.

Example: If you have an interval of 10 sec and the duty cycle is 30%, the output is ON for 3 sec and OFF for 7 sec.

This module is **unsuitable** for controlling LED lights as the high PWM frequency can't be met.

> Note: The module starts to work only if the duty cycle has been updated at least once.

## Modules

The PWM module can be used in openHAB's [rule engine](https://www.openhab.org/docs/configuration/rules-dsl.html).

This automation provides a trigger module ("PWM triggers") with one input Item: `dutycycleItem` (0-100%).
The module calculates the ON/OFF state and returns it.
The return value is used to feed the Action module "Item Action" aka "send a command", which controls the actuator.

To configure a rule, you need to add a Trigger ("PWM triggers") and an Action ("Item Action").
Select the Item you like to control in the "Item Action" and leave the command empty.

### Trigger

| Name                 | Type    | Description                                                                                  | Required |
|----------------------|---------|----------------------------------------------------------------------------------------------|----------|
| `dutycycleItem`      | Item    | The Item (PercentType) to read the duty cycle from                                           | Yes      |
| `interval`           | Decimal | The constant interval in which the output is switch ON and OFF again in sec.                 | Yes      |
| `minDutyCycle`       | Decimal | Any duty cycle below this value will be increased to this value                              | No       |
| `equateMinToZero`    | Boolean | True if the duty cycle below `minDutycycle` should be set to 0 (defaults to false)           | No       |
| `maxDutycycle`       | Decimal | Any duty cycle above this value will be increased to 100                                     | No       |
| `equateMaxToHundred` | Boolean | True if the duty cycle above `maxDutyCycle` should be set to 100 (defaults to true)          | No       |
| `deadManSwitch`      | Decimal | The output will be switched off, when the duty cycle is not updated within this time (in ms) | No       |

The duty cycle can be limited via the parameters `minDutycycle` and `maxDutyCycle`.
This is helpful if you need to maintain a minimum time between the switching of the output.
This is necessary for example for heating burners, which may not be switched on for very short times.
The on time is than increased to `minDutycycle`.
In this case one should also set a max duty cycle to prevent short off times.
It makes sense to apply these symmetrically e.g. 10%/90% or 20%/80%.

If the duty cycle is 0% or 100%, the min/max parameters are ignored and the output is switched ON or OFF continuously.

If the duty cycle Item is not updated within the dead-man switch timeout, the output is switched off, regardless of the current duty cycle.
The function can be used to save energy if the source of the duty cycle died for whatever reason and doesn't update the value anymore.
When the duty cycle is updated again, the module returns to normal operation.

> Note: The min/max ON/OFF times set via `minDutycycle` and `maxDutycycle` are not met if the dead-man switch triggers and recovers fast.

## Control Algorithm

This module is designed to respond fast to duty cycle changes, but at the same time maintain a constant interval and also the min/max ON/OFF parameters.
For that reason, the module might seem to act peculiarly in some cases:

- When the output is ON and the duty cycle is decreased, the output might switch off immediately, if applicable.
Example: The interval is 10 sec and the current duty cycle is 80%.
When the duty cycle is decreased to 20%, the output would switch off immediately, if it has been already ON for more than 2 sec.
- When the duty cycle is 0% for a short interval and then increased again, the output will only switch on when the new interval starts.
- When the duty cycle is 0% or 100% for more than a whole interval, a new interval will start as soon as the duty cycle is updated to a value other than 0%, respective 100%.
- The module starts to work only if the duty cycle Item has been updated at least once.
