# Embedded MQTT Broker

    MQTT is a machine-to-machine (M2M)/"Internet of Things" connectivity protocol. It was designed as an extremely lightweight publish/subscribe messaging transport.

This bundle allows to configure and start an embedded MQTT broker (based on [Moquette](https://github.com/andsel/moquette)) for an easy way to have an MQTT Server up and running with a click.

## Service Configuration

All parameters are optional.

* __port__: The port, the embedded broker should run on. Defaults to not set, which means the typical ports 1883 and 8883 (SSL) are used.
* __username__: The user name that clients need to provide to connect to this broker.
* __password__: The password that clients need to provide to connect to this broker.
* __secure__: If set, hosts a secure SSL connection on port 8883 or otherwise a non secure connection on port 1883 (if not overwritten by the port parameter).
* __persistence_file__: An optional persistence file. Retained messages are stored in this file. Can be empty to not store anything. If it starts with "/" on Linux/MacOS or with a drive letter and colon (eg "c:/") it will be treated as an absolute path. Be careful to select a path that you have write access to.
