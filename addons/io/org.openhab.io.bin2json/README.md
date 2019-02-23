# Binary data to JSON format converter

This bundle can be used to convert binary data to JSON format.

This bundle utilize awesome Java Binary Block Parser. See more details about the library and parse rule syntax from page [Java Binary Block Parser](https://github.com/raydac/java-binary-block-parser">https://github.com/raydac/java-binary-block-parser).


Example usage:

```java

// @formatter:off
/*
 * Frame format:
 * +----+----+-----+-----+-----+-------+-----+----+-------+
 * | CC | 64 |  F  | <D> | <S> | <LEN> | <DATA>   | <CRC> |
 * +----+----+-----+-----+-----+-------+-----+----+-------+
 * |<------------------ HDR ---------->|
 *                                     |<----- LEN ------>|
 *      |<-------------------- CRC -------------->|
 *
 */

String frameParserRule =
          "ubyte          cc;"                   // 0xCC, 204
        + "ubyte          start;"                // 0x64, 100
        + "ubyte          flag;"                 // 0x85, 133
        + "ubyte          destinationAddress;"   // 0xFD, 253
        + "ubyte          sourceAddress;"        // 0x0A, 10
        + "ubyte          dataLen;"              // 0x0B, 11
        + "ubyte[dataLen] data;"                 // 0x2101A0010000030A040000, 33 1 160 1 0 0 3 10 4 0 0
        + "ushort         crc;";                 // 0x8C17, 35863


final byte[] testdata = new byte[] {
        (byte) 0xCC,
        (byte) 0x64,
        (byte) 0x85,
        (byte) 0xFD,
        (byte) 0x0A,
        (byte) 0x0B,
        (byte) 0x21, (byte) 0x01, (byte) 0xA0, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x03, (byte) 0x0A, (byte) 0x04, (byte) 0x00, (byte) 0x00,
        (byte) 0x8C, (byte) 0x17 };
// @formatter:on

JsonObject json = new Bin2Json(frameParserRule).convert(testdata);
logger.debug(json.toString());
```

Outputs:
```javascript
{
  "cc": 204,
  "start": 100,
  "flag": 133,
  "destinationaddress": 253,
  "sourceaddress": 10,
  "datalen": 11,
  "data": [
    33,
    1,
    160,
    1,
    0,
    0,
    3,
    10,
    4,
    0,
    0
  ],
  "crc": 35863
}
```

