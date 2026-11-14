# Bridge Lifecycle Specification

## Purpose

This domain covers how the E3DC bridge connects to the device, how it reports its status and how the child things (power plant, battery, battery module, power meter, wallbox) follow that status.
It also covers how commands are handed over without blocking the openHAB framework.

## Requirements

### Requirement: Configuration is validated before connecting

The binding MUST NOT open a connection while a mandatory bridge setting is missing.

#### Scenario: Blank host

- GIVEN a bridge whose host is blank
- WHEN the bridge is initialized
- THEN the bridge goes `OFFLINE` with the detail `CONFIGURATION_ERROR`
- AND no connection to the device is attempted

#### Scenario: Blank RSCP password

- GIVEN a bridge whose RSCP password is blank
- WHEN the bridge is initialized
- THEN the bridge goes `OFFLINE` with the detail `CONFIGURATION_ERROR`

### Requirement: Bridge status reflects the device connection

The bridge SHALL be `ONLINE` only while the device answers, and it MUST NOT block the framework thread while connecting.

#### Scenario: Device answers

- GIVEN a reachable device that accepts the credentials
- WHEN the bridge is initialized
- THEN the bridge goes `ONLINE` after the first successful poll

#### Scenario: Device is unreachable

- GIVEN a host that does not answer
- WHEN the bridge is initialized
- THEN the bridge goes `OFFLINE` with the detail `COMMUNICATION_ERROR`
- AND a reconnect is scheduled

#### Scenario: Bridge dispose

- GIVEN an online bridge
- WHEN the bridge is disposed
- THEN the connection is closed
- AND the bridge does not go online again

### Requirement: Refresh requests are ignored while offline

The bridge MUST ignore a refresh request while it is not connected, because there is no data to deliver.

#### Scenario: Refresh while offline

- GIVEN an offline bridge
- WHEN a channel is refreshed
- THEN no request is sent to the device

### Requirement: Only errors about a data point stop its requests

The bridge SHALL stop requesting a data point only when the device reports an error about that data point itself.

#### Scenario: Error about the data point

- GIVEN a device that answers a tag with an error about that tag
- WHEN the tag is polled
- THEN the tag is dropped from the following requests

#### Scenario: Other error

- GIVEN a device that answers with an error that is not about the tag itself
- WHEN the tag is polled
- THEN the tag stays in the following requests

### Requirement: Child things follow the bridge

A child thing SHALL report the state of its bridge and MUST go `ONLINE` only after it received data.

#### Scenario: Bridge has no handler

- GIVEN a child thing whose bridge has no handler
- WHEN the child thing is initialized
- THEN it goes `OFFLINE` with the detail `BRIDGE_UNINITIALIZED`

#### Scenario: Bridge is not connected

- GIVEN a child thing whose bridge is not `ONLINE`
- WHEN the child thing is initialized
- THEN it goes `OFFLINE` with the detail `BRIDGE_OFFLINE`

#### Scenario: Bridge is connected

- GIVEN a child thing whose bridge is `ONLINE`
- WHEN the child thing is initialized
- THEN it is `UNKNOWN`
- AND it asks the bridge for a refresh

#### Scenario: Data arrives

- GIVEN a child thing that is `UNKNOWN`
- WHEN the first data is delivered
- THEN it goes `ONLINE`

#### Scenario: Child dispose

- GIVEN an initialized child thing
- WHEN it is disposed
- THEN it is removed from the bridge and receives no more data

### Requirement: Commands never block the framework thread

The binding MUST execute commands in the order they were received, off the framework thread.

#### Scenario: Slow device

- GIVEN a device that answers slowly
- WHEN a command is sent to a channel
- THEN the call returns immediately
- AND the command is executed afterwards

#### Scenario: Several commands

- GIVEN commands that are waiting
- WHEN they are executed
- THEN they run in the order of submission

#### Scenario: A command fails

- GIVEN a command that throws an unexpected exception
- WHEN the following command is waiting
- THEN the following command still runs

#### Scenario: Dispose with waiting commands

- GIVEN commands that are still waiting
- WHEN the thing is disposed
- THEN the waiting commands are dropped

---

_Tests: `E3DCBridgeHandlerTest`, `E3DCChildHandlerTest`, `E3DCCommandQueueTest`._
