# For Developers

SunSpec is a big specification with many different type of devices.
If you own or have access to an appliance that is not supported at the moment then your help is welcome.

If you want to extend the bundle yourself, you have to do the followings:

- Define your thing type, channel types and channel groups according to openHAB development practices.
 You can look at the meter and inverter types to get ideas how you can avoid repeating the same configuration over and over.

- Extend the `AbstractSunSpecHandler` and implement the handlePolledData method.
 This method will be regularly called with the register data read from the appliance.
 The method should parse the data and update the channels with them.

- The preferred way to parse the raw data is to write a parser for you model block type.
 Your class should implement the `SunspecParser` class and it is preferred to extend the `AbstractBaseParser` class.
 This base class has methods to accurately extract fields from the register array.

- The parser should only retrieve the data from the register array and return them in a block descriptor class.
 Scaling and other higher level transformation should be done by the handler itself.

- To include your block type in auto discovery you have to add its id to the `SUPPORTED_THING_TYPES_UIDS` map in `SunSpecConstants`. This is enough for our discovery process to include your thing type in the results.
