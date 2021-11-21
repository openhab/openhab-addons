# JavaScript Scripting

This add-on provides support for JavaScript (ECMAScript 2021+) that can be used as a scripting language within automation rules.
JavaScript scripts provide access to almost all the functionality in an openHAB runtime environment.

- [Creating JavaScript Scripts](#creating-javascript-scripts)
- [Logging](#logging)
- [Core Actions](#core-actions)
  - [itemRegistry](#itemregistry)
  - [Event Bus Actions](#event-bus-actions)
  - [Exec Actions](#exec-actions)
  - [HTTP Actions](#http-actions)
  - [Timers](#timers)
  - [Scripts Actions](#scripts-actions)
- [Cloud Notification Actions](#cloud-notification-actions)
- [Persistence Extensions](#persistence-extensions)
- [Ephemeris Actions](#ephemeris-actions)
- [Types and Units](#types-and-units)

## Creating JavaScript Scripts

When this add-on is installed, JavaScript script actions will be run by this add-on and allow ECMAScript 2021+ features.

Alternatively, you can create scripts in the `automation/jsr223` configuration directory.
If you create an empty file called `test.js`, you will see a log line with information similar to:

```text
    ... [INFO ] [.a.m.s.r.i.l.ScriptFileWatcher:150  ] - Loading script 'test.js'
```

To enable debug logging, use the [console logging]({{base}}/administration/logging.html) commands to enable debug logging for the automation functionality:

```text
log:set DEBUG org.openhab.core.automation
```

For more information on the available APIs in scripts see the [JSR223 Scripting]({{base}}/configuration/jsr223.html) documentation.

The following examples show how to access common openHAB functionalities.

## Logging

As a simple example, the following script logs "Hello, World!".
Note that `console.log` will usually not work since the output has no terminal to display the text.
The openHAB server uses the [SLF4J](https://www.slf4j.org/) library for logging.

```javascript
let logger = Java.type('org.slf4j.LoggerFactory').getLogger('org.openhab.rule.<ruleId>');
logger.info('Hello world!');
logger.warn('Successfully logged warning.');
logger.error('Successfully logged error.');
```
Replace `<ruleId>` with your rule's or script's (unique-)id.

The script uses the [LoggerFactory](https://www.slf4j.org/apidocs/org/slf4j/Logger.html) to obtain a named logger and then logs a message like:

```text
    ... [INFO ] [org.openhab.rule.<ruleId>   ] - Hello world!
    ... [WARN ] [org.openhab.rule.<ruleId>   ] - Successfully logged warning.
    ... [ERROR] [org.openhab.rule.<ruleId>   ] - Successfully logged error.
```

## Core Actions

The basic openHAB services, which are pre-included in JavaScript actions when this Add-On is __not__ installed, allow the access to items and more.

```javascript
let { itemRegistry, things, rules, events, actions } = require('@runtime');
```

### itemRegistry

```javascript
let state = itemRegistry.getItem(itemName).getState();
```

You can use `toString()` to convert an item's state to string or `toBigDecimal()` to convert to number.

### Event Bus Actions

```javascript
events.sendCommand(itemName, command);
events.postUpdate(itemName, state);
```

### Exec Actions

Execute a command line.

```javascript
let Exec = Java.type('org.openhab.core.model.script.actions.Exec');
let Duration = Java.type('java.time.Duration');

// Execute command line.
Exec.executeCommandLine('echo', 'Hello Wordl!');

// Execute command line with timeout.
Exec.executeCommandLine(Duration.ofSeconds(20), 'echo', 'Hello Wordl!');

// Get response from command line.
let response = Exec.executeCommandLine('echo', 'Hello Wordl!');

// Get response from command line with timeout.
response = Exec.executeCommandLine(Duration.ofSeconds(20), 'echo', 'Hello Wordl!');
```

### HTTP Actions

For available actions have a look at the [HTTP Actions Docs](https://www.openhab.org/docs/configuration/actions.html#http-actions).

```javascript
let HTTP = Java.type('org.openhab.core.model.script.actions.HTTP');

// Example GET Request
var response = HTTP.sendHttpGetRequest('<url>');
```

Replace `<url>` with the request url.

### Timers

```javascript
let ZonedDateTime = Java.type('java.time.ZonedDateTime');
let now = ZonedDateTime.now();
let ScriptExecution = Java.type('org.openhab.core.model.script.actions.ScriptExecution');

// Function to run when the timer goes off.
function timerOver () {
  logger.info('The timer is over.');
}

// Create the Timer.
this.myTimer = ScriptExecution.createTimer(now.plusSeconds(10), timerOver);

// Cancel the timer.
this.myTimer.cancel();

// Check whether the timer is active. Returns true if the timer is active and will be executed as scheduled.
let active = this.myTimer.isActive();

// Reschedule the timer.
this.myTimer.reschedule(now.plusSeconds(5));
```

### Scripts Actions

Call scripts created in the UI (Settings -> Scripts) with or without parameters.

```javascript
let FrameworkUtil = Java.type('org.osgi.framework.FrameworkUtil');
let scriptExtension = Java.type('org.openhab.core.automation.module.script.ScriptExtensionProvider');
let _bundle = FrameworkUtil.getBundle(scriptExtension.class);
let bundleContext = _bundle.getBundleContext();
var RuleManagerRef = bundleContext.getServiceReference('org.openhab.core.automation.RuleManager');
var RuleManager = bundleContext.getService(RuleManagerRef);

// Simple call.
RuleManager.runNow('<scriptToRun>');

// Advanced call with arguments.
var map = new java.util.HashMap();
map.put('identifier1', 'value1');
map.put('identifier2', 'value2');
// Second argument is whether to consider the conditions, third is a Map<String, Object> (a way to pass data).
RuleManager.runNow('<scriptToRun>', true, map);
```

Replace `<scriptToRun>` with your script's (unique-)id.

## Cloud Notification Actions

Notification actions may be placed in Rules to send alerts to mobile devices registered with an [openHAB Cloud instance](https://github.com/openhab/openhab-cloud) such as [myopenHAB.org](https://myopenhab.org/).

For available actions have a look at the [Cloud Notification Actions Docs](https://www.openhab.org/docs/configuration/actions.html#cloud-notification-actions).

```javascript
let NotificationAction = Java.type('org.openhab.io.openhabcloud.NotificationAction')
NotificationAction.sendNotification('<email>', '<message>'); // to a single myopenHAB user identified by e-mail
NotificationAction.sendBroadcastNotification('<message>'); // to all myopenHAB users
```

Replace `<email>` with the e-mail address of the user.
Replace `<message>` with the notification text.

## Persistence Extensions

For available commands have a look at [Persistence Extensions in Scripts ans Rules](https://www.openhab.org/docs/configuration/persistence.html#persistence-extensions-in-scripts-and-rules).

For deeper information have a look at the [Persistence Extensions JavaDoc](https://www.openhab.org/javadoc/latest/org/openhab/core/persistence/extensions/persistenceextensions).

```javascript
let PersistenceExtensions = Java.type('org.openhab.core.persistence.extensions.PersistenceExtensions');
let ZonedDateTime = Java.type('java.time.ZonedDateTime');
let now = ZonedDateTime.now();

// Example
var avg = PersistenceExtensions.averageSince(itemRegistry.getItem('<item>'), now.minusMinutes(5), "influxdb");
```

Replace `<persistence>` with the persistence service to use.
Replace `<item>` with the itemname.

## Ephemeris Actions

Ephemeris is a way to determine what type of day today or a number of days before or after today is. For example, a way to determine if today is a weekend, a bank holiday, someone’s birthday, trash day, etc.

For available actions, have a look at the [Ephemeris Actions Docs](https://www.openhab.org/docs/configuration/actions.html#ephemeris).

For deeper information have a look at the [Ephemeris JavaDoc](https://www.openhab.org/javadoc/latest/org/openhab/core/model/script/actions/ephemeris).

```javascript
let Ephemeris = Java.type('org.openhab.core.model.script.actions.Ephemeris');

// Example
let weekend = Ephemeris.isWeekend();
```

## Types and Units

Import types from openHAB Core for type conversion and more.
Import Units from openHAB Core for unit conversion and more.

```javascript
let typeOrUnit = Java.type('org.openhab.core.library.types.typeOrUnit');

// Example
let HSBType = Java.type('org.openhab.core.library.types.HSBType');
let hsb = HSBType.fromRGB(4, 6, 9);
```

Available types are:
* `QuantityType`
* `StringListType` 
* `RawType`
* `DateTimeType`
* `DecimalType`
* `HSBType`
* `PercentType`
* `PointType`
* `StringType`

Available untis are:
* `SIUnits`
* `ImperialUnits`
* `MetricPrefix`
* `Units`
* `BinaryPrefix`
