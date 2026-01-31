# Math transformation

Transforms the input by applying simple math on it.

The available transformations are 

- `MULTIPLY`: multiply the state by a value
- `ADD`: add a value to the state
- `DIVIDE`: divide the state by a value
- `BITAND`: perform a bit-wise AND of the state and a mask
- `BITOR`: perform a bit-wise OR of the state and a mask
- `BITXOR`: perform a bit-wise exclusive-OR of the state and a mask

The values need to be either decimals or strings.
For bit-wise operations hexadecimal notation (e.g. `0x67`) or binary notation (e.g. `0b00010000`) are supported for both, value and state.

Arithmetic math Profiles have an optional configuration parameter `itemName` which allows to use an alternative Item State as value for the transformation.
The Item State will be prioritized if given and different from `UnDefType` and interpretable as a decimal number.

## Full Example

Example Items:

```
Number multiply "Value multiplied by [MULTIPLY(1000):%s]" { channel="<channelUID>" }
Number add "Value added [ADD(5.1):%s]" { channel="<channelUID>" }
Number secondsToMinutes "Time [DIVIDE(60):%s]" { channel="<channelUID>" }
Number subtracted "Value subtracted [ADD(-1):%s]" { channel="<channelUID>" }
Number bitor "Value bitor [BITOR(0x27):%s]" { channel="<channelUID>" }

// Usage as a Profile
Number multiply "Value multiplied by [%.1f]" { channel="<channelUID>" [profile="transform:MULTIPLY", multiplicand=1000] }
Number add "Value added [%.1f]" { channel="<channelUID>" [profile="transform:ADD", addend=5.1, itemName="multiply"] }
Number secondsToMinutes "Time [%d]" { channel="<channelUID>" [profile="transform:DIVIDE", divisor=60] }
Number subtracted "Value subtracted [%.1f]" { channel="<channelUID>" [profile="transform:ADD", addend=-1] }
Number bitand "Value bitand [%s]" { channel="<channelUID>" [profile="transform:BITAND", mask="0b00010000"] }
```

Example in Rules:

```
transform("MULTIPLY", "1000")
transform("ADD", "5.1")
transform("DIVIDE", "60")
transform("ADD", "-1")
transform("BITXOR", "127")
```
