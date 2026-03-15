# Scale Transformation Service

The Scale Transformation Service is an easy-to-use tool that helps with the discretization of numeric inputs.
It transforms a given input by matching it to specified ranges.
The input must represent a numeric value.

The file must exist in the `transform` configuration directory and have the `.scale` extension.
It should follow the format shown in the table below.

Range expressions always contain two parts:

- the range to scale on (left of the equals sign), and
- the corresponding output string (right of it).

A range consists of two bounds. Each bound is optional (making the range open-ended when omitted). Each bound can be inclusive or exclusive.

| Scale Expression | Returns XYZ when the given value is               |
|------------------|---------------------------------------------------|
| `[12..23]=XYZ`   | between (or equal to) 12 and 23                   |
| `]12..23[=XYZ`   | between 12 and 23 (12 and 23 are excluded)        |
| `[..23]=XYZ`     | less than or equal to 23                          |
| `]12..]=XYZ`     | greater than 12                                   |

These expressions are evaluated from top to bottom.
The first range that includes the value is selected.

## Special entries

Some special entries can be used in the scale file.

### Catch-all Entry

`[..]=Catchall`

This entry matches all numeric values not matched by a previous range. It should be placed at the end of the scale file.

### Not a Number

The Scale transform is designed to work with numeric or quantity states.
If the value presented to the Scale transform is not numeric (often NULL or UNDEF), it will not be handled and a warning is written to openhab.log.
You can handle this case with:

`NaN=Non-numeric state presented`

### Formatting output

The Scale transform can also format the output using this entry:

`format=%label% (%value%) !`

Where:

- `%label%` will be replaced by the transformed value, and
- `%value%` is the original numeric value presented

## Example

The following example shows how to break down numeric UV values into fixed UV Index categories.
We have an example UV sensor that sends numeric values from `0` to `100`, which we then want to scale into the [UV Index](https://en.wikipedia.org/wiki/Ultraviolet_index) range.

Example Item:

```java
Number Uv_Sensor_Level "UV Level [SCALE(uvindex.scale):%s]"
```

Referenced scale file `uvindex.scale` in the `transform` folder:

```text
[..3]=1
]3..6]=2
]6..8]=3
]8..10]=4
]10..100]=5
```

Each value the Item receives will be categorized into one of the five given ranges.
Values less than or equal to 3 are matched by `[..3]=1`.
Greater values are matched by the subsequent ranges.
The only condition here is that the received value has to be less than or equal to `100` in our example, since we haven't defined other cases.
If none of the configured conditions match the given value, the response will be empty.

Please note that all ranges for values above 3 start with a `]`.
So the boundary values (3, 6, 8, and 10) are always transformed to the lower range, since `]` excludes the lower bound.

## Usage as a Profile

The functionality of this `TransformationService` can be used in a `Profile` on an `ItemChannelLink` too.
To do so, it can be configured in the `.items` file as follows:

```java
String <itemName> { channel="<channelUID>"[profile="transform:SCALE", function="<filename>", sourceFormat="<valueFormat>"]}
```

The filename (within the `transform` folder) of the scale file has to be set in the `function` parameter.
The parameter `sourceFormat` is optional and can be used to format the input value **before** the transformation, e.g., `%.3f`.
If omitted, the default is `%s`, so the input value will be put into the transformation without any format changes.

Please note: This profile is a one-way transformation, i.e., only values from a device towards the Item are changed; the other direction is left untouched.
