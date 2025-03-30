# Binary To JSON Transformation Service

Transforms the input by Java Binary Block Parser syntax.

See details about syntax from [JBBP homepage](https://github.com/raydac/java-binary-block-parser)

## Example

Let's assume we have received string containing bytes in hexa string format `03FAFF` and we want to convert binary data to JSON format. Binary data contains 3 bytes and strict data format is following `byte a; byte b; ubyte c;`.

Binary to JSON converter will return following result `{"a":3,"b":-6,"c":255}`

## Usage as a Profile

Profiles are not supported by this transformation.
