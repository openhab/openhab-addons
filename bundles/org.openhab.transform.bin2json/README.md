# Binary to JSON Transformation Service

Transforms input using the Java Binary Block Parser (JBBP) syntax.

See details about the syntax on the [JBBP homepage](https://github.com/raydac/java-binary-block-parser).

## Example

Let's assume we have received a string containing bytes in hexadecimal string format `03FAFF` and we want to convert the binary data to JSON. The binary data contains 3 bytes and the strict data format is the following: `byte a; byte b; ubyte c;`.

The Binary to JSON converter will return the following result: `{"a":3,"b":-6,"c":255}`

## Usage as a Profile

Profiles are not supported by this transformation.
