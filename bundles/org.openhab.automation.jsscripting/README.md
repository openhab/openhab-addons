# JavaScript Scripting

This add-on provides support for JavaScript (ECMAScript 2022+) that can be used as a scripting language within automation rules.
It is based on [GraalJS](https://www.graalvm.org/javascript/) from the [GraalVM project](https://www.graalvm.org/).

Also included is [openhab-js](https://github.com/openhab/openhab-js/), a fairly high-level ES6 library to support automation in openHAB. It provides convenient access
to common openHAB functionality within rules including items, things, actions, logging and more.

[[toc]]

## Configuration

This add-on includes by default the [openhab-js](https://github.com/openhab/openhab-js/) NPM library and exports its namespaces onto the global namespace.

This allows the use of `items`, `actions`, `cache` and other objects without the need to explicitly import them using `require()`.
This functionality can be disabled for users who prefer to manage their own imports via the add-on configuration options.

By default, the injection of the [openhab-js](https://github.com/openhab/openhab-js/) NPM library is cached (using a special mechanism instead of `require()`) to improve performance and reduce memory usage.

When configuring the add-on, you should ask yourself these questions:

1. Do I want to have the openhab-js namespaces automatically globally available (`injectionEnabled`)?
   - Yes: "Use Built-In Variables" (default)
   - No: "Do Not Use Built-In Variables", which will allow you to decide what to import and really speed up script loading, but you need to manually import the library, which actually will slow down script loading again.
2. Do I want to have a different version injected other than the included one (`injectionCachingEnabled`)?
   - Yes: "Do Not Cache Library Injection" and install your version to the `$OPENHAB_CONF/automation/js/node_modules` folder, which will slow down script loading, because the injection is not cached.
   - No: "Cache Library Injection" (default), which will speed up the initial loading of a script because the library's injection is cached.

Note that in case you disable caching or your code uses `require()` to import the library and there is no installation of the library found in the node_modules folder, the add-on will fallback to its included version.

In general, the first run of a script will take longer than the subsequent runs.
This is because on the first run both the globals (like `console`) and (if enabled) the library are injected into the script's context.

<!-- Paste the copied docs from openhab-js under this comment. Do NOT forget the table of contents. -->

### UI Based Rules

The quickest way to add rules is through the openHAB Web UI.

Advanced users, or users migrating scripts from existing systems may want to use [File Based Rules](#file-based-rules) for managing rules using files in the user configuration directory.

### Adding Triggers

Using the openHAB UI, first create a new rule and set a trigger condition.

![openHAB Rule Configuration](doc/rule-config.png)

### Adding Actions

Select "Add Action" and then select "Run Script" with "ECMAScript 262 Edition 11".
It’s important this is "Edition 11" or higher, earlier versions will not work.
This will bring up an empty script editor where you can enter your JavaScript.

![openHAB Rule Engines](doc/rule-engines.png)

You can now write rules using standard ES6 JavaScript along with the included openHAB [standard library](#standard-library).

![openHAB Rule Script](doc/rule-script.png)

For example, turning a light on:

```javascript
items.KitchenLight.sendCommand("ON");
console.log("Kitchen Light State", items.KitchenLight.state);
```

Sending a notification

```javascript
actions.NotificationAction.sendNotification("romeo@montague.org", "Balcony door is open");
```

Querying the status of a thing

```javascript
var thingStatusInfo = actions.Things.getThingStatusInfo("zwave:serial_zstick:512");
console.log("Thing status",thingStatusInfo.getStatus());
```

See [openhab-js](https://openhab.github.io/openhab-js) for a complete list of functionality.

### UI Event Object

**NOTE**: Note that `event` object is different in UI based rules and file based rules! This section is only valid for UI based rules. If you use file based rules, refer to [file based rules event object documentation](#event-object).
Note that `event` object is only available when the UI based rule was triggered by an event and is not manually run!
Trying to access `event` on manual run does not work (and will lead to an error), use `this.event` instead (will be `undefined` in case of manual run).

When you use "Item event" as trigger (i.e. "[item] received a command", "[item] was updated", "[item] changed"), there is additional context available for the action in a variable called `event`.

This table gives an overview over the `event` object for most common trigger types:

| Property Name  | Type                                                                                                                 | Trigger Types                          | Description                                                                                                   | Rules DSL Equivalent   |
|----------------|----------------------------------------------------------------------------------------------------------------------|----------------------------------------|---------------------------------------------------------------------------------------------------------------|------------------------|
| `itemState`    | sub-class of [org.openhab.core.types.State](https://www.openhab.org/javadoc/latest/org/openhab/core/types/state)     | `[item] changed`, `[item] was updated` | State that triggered event                                                                                    | `triggeringItem.state` |
| `oldItemState` | sub-class of [org.openhab.core.types.State](https://www.openhab.org/javadoc/latest/org/openhab/core/types/state)     | `[item] changed`                       | Previous state of Item or Group that triggered event                                                          | `previousState`        |
| `itemCommand`  | sub-class of [org.openhab.core.types.Command](https://www.openhab.org/javadoc/latest/org/openhab/core/types/command) | `[item] received a command`            | Command that triggered event                                                                                  | `receivedCommand`      |
| `itemName`     | string                                                                                                               | all                                    | Name of Item that triggered event                                                                             | `triggeringItem.name`  |
| `type`         | string                                                                                                               | all                                    | Type of event that triggered event (`"ItemStateEvent"`, `"ItemStateChangedEvent"`, `"ItemCommandEvent"`, ...) | N/A                    |

Note that in UI based rules `event.itemState`, `event.oldItemState`, and `event.itemCommand` are Java types (not JavaScript), and care must be taken when comparing these with JavaScript types:

```javascript
var { ON } = require("@runtime")

console.log(event.itemState == "ON")  // WRONG. Java type does not equal with string, not even with "relaxed" equals (==) comparison
console.log(event.itemState.toString() == "ON")  // OK. Comparing strings
console.log(event.itemState == ON)  // OK. Comparing Java types
```

**NOTE**: Even with `String` items, simple comparison with `==` is not working as one would expect! See below example:

```javascript
// Example assumes String item trigger
console.log(event.itemState == "test") // WRONG. Will always log "false"
console.log(event.itemState.toString() == "test") // OK
```

## Scripting Basics

The openHAB JavaScript Scripting runtime attempts to provide a familiar environment to JavaScript developers.

### `let` and `const`

Due to the way how openHAB runs UI based scripts, `let`, `const` and `class` are not supported at top-level.
Use `var` instead or wrap your script inside a self-invoking function:

```javascript
// Wrap script inside a self-invoking function:
(function (data) {
  const C = 'Hello world';
  console.log(C);
})(this.event);

// Defining a class using var:
var Tree = class {
  constructor (height) {
    this.height = height;
  }
}
```

### `require`

Scripts may include standard NPM based libraries by using CommonJS `require`.
The library search will look in the path `automation/js/node_modules` in the user configuration directory.
See [libraries](#libraries) for more information.

### `console`

The JS Scripting binding supports the standard `console` object for logging.
Script logging is enabled by default at the `INFO` level (messages from `console.debug` and `console.trace` won't be displayed), but can be configured using the [openHAB console](https://www.openhab.org/docs/administration/console.html):

```text
log:set DEBUG org.openhab.automation.script
log:set TRACE org.openhab.automation.script
log:set DEFAULT org.openhab.automation.script
```

The default logger name consists of the prefix `org.openhab.automation.script` and the script’s individual part `.file.filename` or `.ui.ruleUID`.
This logger name can be changed by assigning a new string to the `loggerName` property of the console:

```javascript
console.loggerName = 'org.openhab.custom';
```

Please be aware that messages do not appear in the logs if the logger name does not start with `org.openhab`.
This behaviour is due to [log4j2](https://logging.apache.org/log4j/2.x/) requiring a setting for each logger prefix in `$OPENHAB_USERDATA/etc/log4j2.xml` (on openHABian: `/srv/openhab-userdata/etc/log4j2.xml`).

Supported logging functions include:

- `console.log(obj1 [, obj2, ..., objN])`
- `console.info(obj1 [, obj2, ..., objN])`
- `console.warn(obj1 [, obj2, ..., objN])`
- `console.error(obj1 [, obj2, ..., objN])`
- `console.debug(obj1 [, obj2, ..., objN])`
- `console.trace(obj1 [, obj2, ..., objN])`

Where `obj1 ... objN` is a list of JavaScript objects to output.
The string representations of each of these objects are appended together in the order listed and output.

See <https://developer.mozilla.org/en-US/docs/Web/API/console> for more information about console logging.

Note: [openhab-js](https://github.com/openhab/openhab-js/) is logging to `org.openhab.automation.openhab-js`.

### Timers

JS Scripting provides access to the global `setTimeout`, `setInterval`, `clearTimeout` and `clearInterval` methods specified in the [Web APIs](https://developer.mozilla.org/en-US/docs/Web/API).

When a script is unloaded, all created timeouts and intervals are automatically cancelled.

#### `setTimeout`

The global [`setTimeout()`](https://developer.mozilla.org/en-US/docs/Web/API/setTimeout) method sets a timer which executes a function once the timer expires.
`setTimeout()` returns a `timeoutId` (a positive integer value) which identifies the timer created.

```javascript
var timeoutId = setTimeout(callbackFunction, delay, param1, /* ... */ paramN);
```

`delay` is an integer value that represents the amount of milliseconds to wait before the timer expires.
`param1` ... `paramN` are optional, additional arguments which are passed through to the `callbackFunction`.

The global [`clearTimeout(timeoutId)`](https://developer.mozilla.org/en-US/docs/Web/API/clearTimeout) method cancels a timeout previously established by calling `setTimeout()`.

If you need a more verbose way of creating timers, consider to use [`createTimer`](#createtimer) instead.

#### `setInterval`

The global [`setInterval()`](https://developer.mozilla.org/en-US/docs/Web/API/setInterval) method repeatedly calls a function, with a fixed time delay between each call.
`setInterval()` returns an `intervalId` (a positive integer value) which identifies the interval created.

```javascript
var intervalId = setInterval(callbackFunction, delay, param1, /* ... */ paramN);
```

`delay` is an integer value that represents the amount of milliseconds to wait before the timer expires.
`param1` ... `paramN` are optional, additional arguments which are passed through to the `callbackFunction`.

The global [`clearInterval(intervalId)`](https://developer.mozilla.org/en-US/docs/Web/API/clearInterval) method cancels a timed, repeating action which was previously established by a call to `setInterval()`.

#### Accessing Variables

You can access all variables of the current context in the created timers.

Note: Variables can be mutated (changed) after the timer has been created.
Be aware that this can lead to unattended side effects, e.g. when you change the variable after timer creation, which can make debugging quite difficult!

```javascript
var myVar = 'Hello world!';

// Schedule a timer that expires in ten seconds
setTimeout(() => {
  console.info(`Timer expired with variable value = "${myVar}"`);
}, 10000);

myVar = 'Hello mutation!'; // When the timer runs, it will log "Hello mutation!" instead of "Hello world!"
```

If you need to pass some variables to the timer but avoid that they can get mutated, pass those variables as parameters to `setTimeout`/`setInterval` or `createTimer`:


```javascript
var myVar = 'Hello world!';

// Schedule a timer that expires in ten seconds
setTimeout((myVariable) => {
  console.info(`Timer expired with variable value = "${myVariable}"`);
}, 10000, myVar); // Pass one or more variables as parameters here. They are passed through to the callback function.

myVar = 'Hello mutation!'; // When the timer runs, it will log "Hello world!"
```

This also works for timers created with [`actions.ScriptExecution.createTimer`](#createtimer).

### Paths

For [file based rules](#file-based-rules), scripts will be loaded from `automation/js` in the user configuration directory.

NPM libraries will be loaded from `automation/js/node_modules` in the user configuration directory.

### Deinitialization Hook

It is possible to hook into unloading of a script and register a function that is called when the script is unloaded.

```javascript
require('@runtime').lifecycleTracker.addDisposeHook(() => functionToCall());

// Example
require('@runtime').lifecycleTracker.addDisposeHook(() => {
  console.log("Deinitialization hook runs...")
});
```

## `JS` Transformation

openHAB provides several [data transformation services](https://www.openhab.org/addons/#transform) as well as the script transformations, that are available from the framework and need no additional installation.
It allows transforming values using any of the available scripting languages, which means JavaScript Scripting is supported as well.
See the [transformation docs](https://openhab.org/docs/configuration/transformations.html#script-transformation) for more general information on the usage of script transformations.

Use JavaScript Scripting as script transformation by:

1. Creating a script in the `$OPENHAB_CONF/transform` folder with the `.js` extension.
   The script should take one argument `input` and return a value that supports `toString()` or `null`:

   ```javascript
   (function(data) {
     // Do some data transformation here, e.g.
     return "String has" + data.length + "characters";
   })(input);
   ```

2. Using `JS(<scriptname>.js):%s` as Item state transformation.
3. Passing parameters is also possible by using a URL like syntax: `JS(<scriptname>.js?arg=value)`.
   Parameters are injected into the script and can be referenced like variables.

Simple transformations can aso be given as an inline script: `JS(|...)`, e.g. `JS(|"String has " + input.length + "characters")`.
It should start with the `|` character, quotes within the script may need to be escaped with a backslash `\` when used with another quoted string as in text configurations.

## Standard Library

Full documentation for the openHAB JavaScript library can be found at [openhab-js](https://openhab.github.io/openhab-js).

The openHAB JavaScript library provides type definitions for most of its APIs to enable code completion is IDEs like [VS Code](https://code.visualstudio.com).
To use the type definitions, install the [`openhab` npm package](https://npmjs.com/openhab) (read the [installation guide](https://github.com/openhab/openhab-js#custom-installation) for more information), and import the used namespaces with `const { rules, triggers, items } = require('openhab');` (adjust this to your needs).
If an API does not provide type definitions and therefore autocompletion won't work, the documentation will include a note.

### Items

The `items` namespace allows interactions with openHAB Items.
Anywhere a native openHAB `Item` is required, the runtime will automatically convert the JS-`Item` to its Java counterpart.

See [openhab-js : items](https://openhab.github.io/openhab-js/items.html) for full API documentation.

- items : `object`
  - .NAME ⇒ `Item`
  - .existsItem(name) ⇒ `boolean`
  - .getItem(name, nullIfMissing) ⇒ `Item`
  - .getItems() ⇒ `Array[Item]`
  - .getItemsByTag(...tagNames) ⇒ `Array[Item]`
  - .addItem([itemConfig](#itemconfig))
  - .removeItem(itemOrItemName) ⇒ `boolean`
  - .replaceItem([itemConfig](#itemconfig))
  - .safeItemName(s) ⇒ `string`

```javascript
var item = items.KitchenLight;
console.log("Kitchen Light State", item.state);
```

#### `getItem(name, nullIfMissing)`

Calling `getItem(...)` or `...` returns an `Item` object with the following properties:

- Item : `object`
  - .rawItem ⇒ `HostItem`
  - .persistence ⇒ [`ItemPersistence`](#itempersistence)
  - .semantics ⇒ [`ItemSemantics`](https://openhab.github.io/openhab-js/items.ItemSemantics.html)
  - .type ⇒ `string`
  - .name ⇒ `string`
  - .label ⇒ `string`
  - .state ⇒ `string`
  - .numericState ⇒ `number|null`: State as number, if state can be represented as number, or `null` if that's not the case
  - .quantityState ⇒ [`Quantity|null`](#quantity): Item state as Quantity or `null` if state is not Quantity-compatible or without unit
  - .rawState ⇒ `HostState`
  - .members ⇒ `Array[Item]`
  - .descendents ⇒ `Array[Item]`
  - .isUninitialized ⇒ `boolean`
  - .groupNames ⇒ `Array[string]`
  - .tags ⇒ `Array[string]`
  - .getMetadata(namespace) ⇒ `object|null`
  - .replaceMetadata(namespace, value, configuration) ⇒ `object`
  - .removeMetadata(namespace) ⇒ `object|null`
  - .sendCommand(value): `value` can be a string, a [`time.ZonedDateTime`](#time) or a [`Quantity`](#quantity)
  - .sendCommandIfDifferent(value) ⇒ `boolean`: `value` can be a string, a [`time.ZonedDateTime`](#time) or a [`Quantity`](#quantity)
  - .sendIncreaseCommand(value) ⇒ `boolean`: `value` can be a number, or a [`Quantity`](#quantity)
  - .sendDecreaseCommand(value) ⇒ `boolean`: `value` can be a number, or a [`Quantity`](#quantity)
  - .sendToggleCommand(): Sends a command to flip the Item's state (e.g. if it is 'ON' an 'OFF' command is sent).
  - .postUpdate(value): `value` can be a string, a [`time.ZonedDateTime`](#time) or a [`Quantity`](#quantity)
  - .addGroups(...groupNamesOrItems)
  - .removeGroups(...groupNamesOrItems)
  - .addTags(...tagNames)
  - .removeTags(...tagNames)

```javascript
// Equivalent to items.KitchenLight
var item = items.getItem("KitchenLight");
// Send an ON command
item.sendCommand("ON");
// Post an update
item.postUpdate("OFF");
// Get state
console.log("KitchenLight state", item.state);
```

See [openhab-js : Item](https://openhab.github.io/openhab-js/items.Item.html) for full API documentation.

#### `itemConfig`

Calling `addItem(itemConfig)` or `replaceItem(itemConfig)` requires the `itemConfig` object with the following properties:

- itemConfig : `object`
  - .type ⇒ `string`
  - .name ⇒ `string`
  - .label ⇒ `string`
  - .category (icon) ⇒ `string`
  - .groups ⇒ `Array[string]`
  - .tags ⇒ `Array[string]`
  - .channels ⇒ `string | Object { channeluid: { config } }`
  - .metadata ⇒ `Object { namespace: value } | Object { namespace: { value: value , config: { config } } }`
  - .giBaseType ⇒ `string`
  - .groupFunction ⇒ `string`

Note: `.type` and `.name` are required.
Basic UI and the mobile apps need `metadata.stateDescription.config.pattern` to render the state of an Item.

Example:

```javascript
// more advanced example
items.replaceItem({
  type: 'String',
  name: 'Hallway_Light',
  label: 'Hallway Light',
  category: 'light',
  groups: ['Hallway', 'Light'],
  tags: ['Lightbulb'],
  channels: {
    'binding:thing:device:hallway#light': {},
    'binding:thing:device:livingroom#light': {
      profile: 'system:follow'
    }
  },
  metadata: {
    expire: '10m,command=1',
    stateDescription: {
      config: {
        pattern: '%d%%',
        options: '1=Red, 2=Green, 3=Blue'
      }
    }
  }
});
// minimal example
items.replaceItem({
  type: 'Switch',
  name: 'MySwitch',
  metadata: {
    stateDescription: {
      config: {
        pattern: '%s'
      }
    }
  }
});
```

See [openhab-js : ItemConfig](https://openhab.github.io/openhab-js/global.html#ItemConfig) for full API documentation.

#### `ItemPersistence`

Calling `Item.persistence` returns an `ItemPersistence` object with the following functions:

- ItemPersistence :`object`
  - .averageSince(timestamp, serviceId) ⇒ `PersistedState | null`
  - .averageUntil(timestamp, serviceId) ⇒ `PersistedState | null`
  - .averageBetween(begin, end, serviceId) ⇒ `PersistedState | null`
  - .changedSince(timestamp, serviceId) ⇒ `boolean`
  - .changedUntil(timestamp, serviceId) ⇒ `boolean`
  - .changedBetween(begin, end, serviceId) ⇒ `boolean`
  - .countSince(timestamp, serviceId) ⇒ `number`
  - .countUntil(timestamp, serviceId) ⇒ `number`
  - .countBetween(begin, end, serviceId) ⇒ `number`
  - .countStateChangesSince(timestamp, serviceId) ⇒ `number`
  - .countStateChangesUntil(timestamp, serviceId) ⇒ `number`
  - .countStateChangesBetween(begin, end, serviceId) ⇒ `number`
  - .deltaSince(timestamp, serviceId) ⇒ `PersistedState | null`
  - .deltaUntil(timestamp, serviceId) ⇒ `PersistedState | null`
  - .deltaBetween(begin, end, serviceId) ⇒ `PersistedState | null`
  - .deviationSince(timestamp, serviceId) ⇒ `PersistedState | null`
  - .deviationUntil(timestamp, serviceId) ⇒ `PersistedState | null`
  - .deviationBetween(begin, end, serviceId) ⇒ `PersistedState | null`
  - .evolutionRateSince(timestamp, serviceId) ⇒ `number | null`
  - .evolutionRateUntil(timestamp, serviceId) ⇒ `number | null`
  - .evolutionRateBetween(begin, end, serviceId) ⇒ `number | null`
  - .getAllStatesSince(timestamp, serviceId)  ⇒ `Array[PersistedItem]`
  - .getAllStatesUntil(timestamp, serviceId)  ⇒ `Array[PersistedItem]`
  - .getAllStatesBetween(begin, end, serviceId)  ⇒ `Array[PersistedItem]`
  - .lastUpdate(serviceId) ⇒ `ZonedDateTime | null`
  - .nextUpdate(serviceId) ⇒ `ZonedDateTime | null`
  - .lastChange(serviceId) ⇒ `ZonedDateTime | null`
  - .nextChange(serviceId) ⇒ `ZonedDateTime | null`
  - .maximumSince(timestamp, serviceId) ⇒ `PersistedItem | null`
  - .maximumUntil(timestamp, serviceId) ⇒ `PersistedItem | null`
  - .maximumBetween(begin, end, serviceId) ⇒ `PersistedItem | null`
  - .minimumSince(timestamp, serviceId) ⇒ `PersistedItem | null`
  - .minimumUntil(timestamp, serviceId) ⇒ `PersistedItem | null`
  - .minimumBetween(begin, end, serviceId) ⇒ `PersistedItem | null`
  - .medianSince(timestamp, serviceId) ⇒ `PersistedState | null`
  - .medianUntil(timestamp, serviceId) ⇒ `PersistedState | null`
  - .medianBetween(begin, end, serviceId) ⇒ `PersistedState | null`
  - .persist(serviceId): Tells the persistence service to store the current Item state, which is then done asynchronously.
    **Warning:** This has the side effect, that if the Item state changes shortly after `.persist` has been called, the new Item state will be persisted. See [JSDoc](https://openhab.github.io/openhab-js/items.ItemPersistence.html#persist) for a possible work-around.
  - .persist(timestamp, state, serviceId): Tells the persistence service to store the given state at the given timestamp, which is then done asynchronously.
  - .persist(timeSeries, serviceId): Tells the persistence service to store the given [`TimeSeries`](#timeseries), which is then done asynchronously.
  - .persistedState(timestamp, serviceId) ⇒ `PersistedItem | null`
  - .previousState(skipEqual, serviceId) ⇒ `PersistedItem | null`
  - .nextState(skipEqual, serviceId) ⇒ `PersistedItem | null`
  - .sumSince(timestamp, serviceId) ⇒ `PersistedState | null`
  - .sumUntil(timestamp, serviceId) ⇒ `PersistedState | null`
  - .sumBetween(begin, end, serviceId) ⇒ `PersistedState | null`
  - .updatedSince(timestamp, serviceId) ⇒ `boolean`
  - .updatedUntil(timestamp, serviceId) ⇒ `boolean`
  - .updatedBetween(begin, end, serviceId) ⇒ `boolean`
  - .varianceSince(timestamp, serviceId) ⇒ `PersistedState | null`
  - .varianceUntil(timestamp, serviceId) ⇒ `PersistedState | null`
  - .varianceBetween(begin, end, serviceId) ⇒ `PersistedState | null`

Note: `serviceId` is optional, if omitted, the default persistence service will be used.

```javascript
var yesterday = new Date(new Date().getTime() - (24 * 60 * 60 * 1000));
var item = items.KitchenDimmer;
console.log('KitchenDimmer averageSince', item.persistence.averageSince(yesterday));
```

The `PersistedState` object contains the following properties, representing Item state:

- `state`: State as string
- `numericState`: State as number, if state can be represented as number, or `null` if that's not the case
- `quantityState`: Item state as [`Quantity`](#quantity) or `null` if state is not Quantity-compatible
- `rawState`: State as Java `State` object

The `PersistedItem` object extends `PersistedState` with the following properties, representing Item state and the respective timestamp:

- `timestamp`: Timestamp as [`time.ZonedDateTime`](#time)
- `instant`: Timestamp as [`time.Instant`](#time)

```javascript
var midnight = time.toZDT('00:00');
var historic = items.KitchenDimmer.persistence.maximumSince(midnight);
console.log('KitchenDimmer maximum was ', historic.state, ' at ', historic.timestamp);
```

See [openhab-js : ItemPersistence](https://openhab.github.io/openhab-js/items.ItemPersistence.html) for full API documentation.

#### `TimeSeries`

A `TimeSeries` is used to transport a set of states together with their timestamp.
It is usually used for persisting historic state or forecasts in a persistence service by using [`ItemPersistence.persist`](#itempersistence).

When creating a new `TimeSeries`, a policy must be chosen - it defines how the `TimeSeries` is persisted in a persistence service:

- `ADD` adds the content to the persistence, well suited for persisting historic data.
- `REPLACE` first removes all persisted elements in the timespan given by begin and end of the `TimeSeries`, well suited for persisting forecasts.

A `TimeSeries` object has the following properties and methods:

- `policy`: The persistence policy, either `ADD` or `REPLACE`.
- `begin`: Timestamp of the first element of the `TimeSeries`.
- `end`: Timestamp of the last element of the `TimeSeries`.
- `size`: Number of elements in the `TimeSeries`.
- `states`: States of the `TimeSeries` together with their timestamp and sorted by their timestamps.
  Be aware that this returns a reference to the internal state array, so changes to the array will affect the `TimeSeries`.
- `add(timestamp, state)`: Add a given state to the `TimeSeries` at the given timestamp.

The following example shows how to create a `TimeSeries`:

```javascript
var timeSeries = new items.TimeSeries('ADD'); // Create a new TimeSeries with policy ADD
timeSeries.add(time.toZDT('2024-01-01T14:53'), Quantity('5 m')).add(time.toZDT().minusMinutes(2), Quantity('0 m')).add(time.toZDT().plusMinutes(5), Quantity('5 m'));
console.log(ts); // Let's have a look at the TimeSeries
items.getItem('MyDistanceItem').persistence.persist(timeSeries, 'influxdb'); // Persist the TimeSeries for the Item 'MyDistanceItem' using the InfluxDB persistence service
```

### Things

The Things namespace allows to interact with openHAB Things.

See [openhab-js : things](https://openhab.github.io/openhab-js/things.html) for full API documentation.

- things : <code>object</code>
  - .getThing(uid) ⇒ <code>Thing|null</code>
  - .getThings() ⇒ <code>Array[Thing]</code>

#### `getThing(uid, nullIfMissing)`

Calling `getThing(uid)` returns a `Thing` object with the following properties:

- Thing : <code>object</code>
  - .bridgeUID ⇒ <code>String</code>
  - .label ⇒ <code>String</code>
  - .location ⇒ <code>String</code>
  - .status ⇒ <code>String</code>
  - .statusInfo ⇒ <code>String</code>
  - .thingTypeUID ⇒ <code>String</code>
  - .uid ⇒ <code>String</code>
  - .isEnabled ⇒ <code>Boolean</code>
  - .setLabel(label)
  - .setLocation(location)
  - .setProperty(name, value)
  - .setEnabled(enabled)

```javascript
var thing = things.getThing('astro:moon:home');
console.log('Thing label: ' + thing.label);
// Set Thing location
thing.setLocation('living room');
// Disable Thing
thing.setEnabled(false);
```

### Actions

The actions namespace allows interactions with openHAB actions.
The following are a list of standard actions.

**Warning:** Please be aware, that (unless not explicitly noted) there is **no** type conversion from Java to JavaScript types for the return values of actions.
Read the JavaDoc linked from the JSDoc to learn about the returned Java types.

Please note that most of the actions currently do **not** provide type definitions and therefore auto-completion does not work.

See [openhab-js : actions](https://openhab.github.io/openhab-js/actions.html) for full API documentation and additional actions.

#### Audio Actions

See [openhab-js : actions.Audio](https://openhab.github.io/openhab-js/actions.html#.Audio) for complete documentation.

#### BusEvent Actions

See [openhab-js : actions.BusEvent](https://openhab.github.io/openhab-js/actions.html#.BusEvent) for complete documentation.

#### CoreUtil Actions

See [openhab-js : actions.CoreUtil](https://openhab.github.io/openhab-js/actions.html#.CoreUtil) for complete documentation.

The `CoreUtil` actions provide access to parts of the utilities included in openHAB core, see [org.openhab.core.util](https://www.openhab.org/javadoc/latest/org/openhab/core/util/package-summary).
These include several methods to convert between color types like HSB, RGB, sRGB, RGBW and XY.

#### Ephemeris Actions

See [openhab-js : actions.Ephemeris](https://openhab.github.io/openhab-js/actions.html#.Ephemeris) for complete documentation.

Ephemeris is a way to determine what type of day today or a number of days before or after today is.
For example, a way to determine if today is a weekend, a public holiday, someone’s birthday, trash day, etc.

Additional information can be found on the  [Ephemeris Actions Docs](https://www.openhab.org/docs/configuration/actions.html#ephemeris) as well as the [Ephemeris JavaDoc](https://www.openhab.org/javadoc/latest/org/openhab/core/model/script/actions/ephemeris).

```javascript
var weekend = actions.Ephemeris.isWeekend();
```

#### Exec Actions

See [openhab-js : actions.Exec](https://openhab.github.io/openhab-js/actions.html#.Exec) for complete documentation.

Execute a command line.

```javascript
// Execute command line.
actions.Exec.executeCommandLine('echo', 'Hello World!');

// Execute command line with timeout.
actions.Exec.executeCommandLine(time.Duration.ofSeconds(20), 'echo', 'Hello World!');

// Get response from command line with timeout.
var response = actions.Exec.executeCommandLine(time.Duration.ofSeconds(20), 'echo', 'Hello World!');
```

#### HTTP Actions

See [openhab-js : actions.HTTP](https://openhab.github.io/openhab-js/actions.html#.HTTP) for complete documentation.

```javascript
// Example GET Request
var response = actions.HTTP.sendHttpGetRequest('<url>');
```

Replace `<url>` with the request url.

#### Ping Actions

See [openhab-js : actions.Ping](https://openhab.github.io/openhab-js/actions.html#.Ping) for complete documentation.

```javascript
// Check if a host is reachable
var reachable = actions.Ping.checkVitality(host, port, timeout); // host: string, port: int, timeout: int
```

#### ScriptExecution Actions

The `ScriptExecution` actions provide the `callScript(string scriptName)` method, which calls a script located at the `$OH_CONF/scripts` folder, as well as the `createTimer` method.

You can also create timers using the [native JS methods for timer creation](#timers), your choice depends on the versatility you need.
Sometimes, using `setTimeout` is much faster and easier, but other times, you need the versatility that `createTimer` provides.

Keep in mind that you should somehow manage the timers you create using `createTimer`, otherwise you could end up with unmanageable timers running until you restart openHAB.
A possible solution is to store all timers in the [private cache](#cache) and let openHAB automatically cancel them when the script is unloaded and the cache is cleared.
When using `createTimer`, please read [Accessing Variables](#accessing-variables) to avoid having unexpected results when using variables in timers.

##### `createTimer`

```javascript
actions.ScriptExecution.createTimer(time.ZonedDateTime zdt, function functionRef, any param1, /* ... */ paramN);

actions.ScriptExecution.createTimer(string identifier, time.ZonedDateTime zdt, function functionRef, any param1, /* ... */ paramN);
```

`createTimer` accepts the following arguments:

- `string` identifier (optional): Identifies the timer by a string, used e.g. for logging errors that occur during the callback execution.
- [`time.ZonedDateTime`](#timetozdt) zdt: Point in time when the callback should be executed.
- `function` functionRef: Callback function to execute when the timer expires.
- `*` param1, ..., paramN: Additional arguments which are passed through to the function specified by `functionRef`.

`createTimer` returns an openHAB Timer, that provides the following methods:

- `cancel()`: Cancels the timer. ⇒ `boolean`: true, if cancellation was successful
- `getExecutionTime()`: The scheduled execution time or null if timer was cancelled. ⇒ `time.ZonedDateTime` or `null`
- `isActive()`: Whether the scheduled execution is yet to happen. ⇒ `boolean`
- `isCancelled()`: Whether the timer has been cancelled. ⇒ `boolean`
- `hasTerminated()`: Whether the scheduled execution has already terminated. ⇒ `boolean`
- `reschedule(time.ZonedDateTime)`: Reschedules a timer to a new starting time. This can also be called after a timer has terminated, which will result in another execution of the same code. ⇒ `boolean`: true, if rescheduling was successful


```javascript
var now = time.ZonedDateTime.now();

// Function to run when the timer goes off.
function timerOver () {
  console.info('The timer expired.');
}

// Create the Timer.
var myTimer = actions.ScriptExecution.createTimer('My Timer', now.plusSeconds(10), timerOver);

// Cancel the timer.
myTimer.cancel();

// Check whether the timer is active. Returns true if the timer is active and will be executed as scheduled.
var active = myTimer.isActive();

// Reschedule the timer.
myTimer.reschedule(now.plusSeconds(5));
```

See [openhab-js : actions.ScriptExecution](https://openhab.github.io/openhab-js/actions.ScriptExecution.html) for complete documentation.

#### Transformation Actions

openHAB provides various [data transformation services](https://www.openhab.org/addons/#transform) which can translate between technical and human-readable values.
Usually, they are used directly on Items, but it is also possible to access them from scripts.

```javascript
console.log(actions.Transformation.transform('MAP', 'en.map', 'OPEN')); // open
console.log(actions.Transformation.transform('MAP', 'de.map', 'OPEN')); // offen
```

See [openhab-js : actions.Transformation](https://openhab.github.io/openhab-js/actions.Transformation.html) for complete documentation.

#### Voice Actions

See [openhab-js : actions.Voice](https://openhab.github.io/openhab-js/actions.html#.Voice) for complete documentation.

#### Cloud Notification Actions

Requires the [openHAB Cloud Connector](https://www.openhab.org/addons/integrations/openhabcloud/) to be installed.

Notification actions may be placed in rules to send alerts to mobile devices registered with an [openHAB Cloud instance](https://github.com/openhab/openhab-cloud) such as [myopenHAB.org](https://myopenhab.org/).

There are three different types of notifications:

- Broadcast Notifications: Sent to all registered devices and shown as notification on these devices.
- Standard Notifications: Sent to the registered devices of the specified user and shown as notification on his devices.
- Log Notifications: Only shown in the notification log, e.g. inside the Android and iOS Apps.

In addition to that, notifications can be updated later be re-using the same `referenceId` and hidden/removed either by `referenceId` or `tag`.

To send these three types of notifications, use the `notificationBuilder(message)` method of the `actions` namespace.
`message` is optional and may be omitted.
It returns a new `NotificationBuilder` object, which by default sends a broadcast notification and provides the following methods:

- `.logOnly()`: Send a log notification only.
- `.hide()`: Hides notification(s) with the specified `referenceId` or `tag` (`referenceId` has precedence over `tag`).
- `.addUserId(emailAddress)`: By adding the email address(es) of specific openHAB Cloud user(s), the notification is only sent to this (these) user(s).
  To add multiple users, either call `addUserId` multiple times or pass mutiple emails as multiple params, e.g. `addUserId(emailAddress1, emailAddress2)`.
- `.withIcon(icon)`: Sets the icon of the notification.
- `.withTag(tag)`: Sets the tag of the notification. Used for grouping notifications and to hide/remove groups of notifications.
- `.withTitle(title)`: Sets the title of the notification.
- `.withReferenceId(referenceId)`: Sets the reference ID of the notification. If none is set, but it might be useful, a random UUID will be generated.
  The reference ID can be used to update or hide the notification later by using the same reference ID again.
- `.withOnClickAction(action)`: Sets the action to be executed when the notification is clicked.
- `.withMediaAttachmentUrl(mediaAttachmentUrl)`: Sets the URL of a media attachment to be displayed with the notification. This URL must be reachable by the push notification client.
- `.addActionButton(label, action)`: Adds an action button to the notification. Please note that due to Android and iOS limitations, only three action buttons are supported.
- `.send()` ⇒ `string|null`: Sends the notification and returns the reference ID or `null` for log notifications and when hiding notifications.

The syntax for the `action` parameter is described in [openHAB Cloud Connector: Action Syntax](https://www.openhab.org/addons/integrations/openhabcloud/#action-syntax).

The syntax for the `mediaAttachmentUrl` parameter is described in [openHAB Cloud Connector](https://www.openhab.org/addons/integrations/openhabcloud/).

```javascript
// Send a simple broadcast notification
actions.notificationBuilder('Hello World!').send();
// Send a broadcast notification with icon, tag and title
actions.notificationBuilder('Hello World!')
  .withIcon('f7:bell_fill').withTag('important').withTitle('Important Notification').send();
// Send a broadcast notification with icon, tag, title, media attachment URL and actions
actions.notificationBuilder('Hello World!')
  .withIcon('f7:bell_fill').withTag('important').withTitle('Important Notification')
  .withOnClickAction('ui:navigate:/page/my_floorplan_page').withMediaAttachmentUrl('http://example.com/image.jpg')
  .addActionButton('Turn Kitchen Light ON', 'command:KitchenLights:ON').addActionButton('Turn Kitchen Light OFF', 'command:KitchenLights:OFF').send();

// Send a simple standard notification to two specific users
actions.notificationBuilder('Hello World!').addUserId('florian@example.com').addUserId('florian@example.org').send();
// Send a standard notification with icon, tag and title to two specific users
actions.notificationBuilder('Hello World!').addUserId('florian@example.com').addUserId('florian@example.org')
  .withIcon('f7:bell_fill').withTag('important').withTitle('Important notification').send();

// Sends a simple log notification
actions.notificationBuilder('Hello World!').logOnly().send();
// Sends a simple log notification with icon and tag
actions.notificationBuilder('Hello World!').logOnly()
  .withIcon('f7:bell_fill').withTag('important').send();
```

See [openhab-js : actions.NotificationBuilder](https://openhab.github.io/openhab-js/actions.html#.notificationBuilder) for complete documentation.

### Cache

The cache namespace provides both a private and a shared cache that can be used to set and retrieve data that will be persisted between subsequent runs of the same or between scripts.

The private cache can only be accessed by the same script and is cleared when the script is unloaded.
You can use it to store primitives and objects, e.g. store timers or counters between subsequent runs of that script.
When a script is unloaded and its cache is cleared, all timers (see [`createTimer`](#createtimer)) stored in its private cache are automatically cancelled.

The shared cache is shared across all rules and scripts, it can therefore be accessed from any automation language.
The access to every key is tracked and the key is removed when all scripts that ever accessed that key are unloaded.
If that key stored a timer, the timer will be cancelled.
You can use it to store primitives and **Java** objects, e.g. store timers or counters between multiple scripts.

Due to a multi-threading limitation in GraalJS (the JavaScript engine used by JavaScript Scripting), it is not recommended to store JavaScript objects in the shared cache.
Multi-threaded access to JavaScript objects will lead to script execution failure!
You can work-around that limitation by either serialising and deserialising JS objects or by switching to their Java counterparts.

Timers as created by [`createTimer`](#createtimer) can be stored in the shared cache.
The ids of timers and intervals as created by `setTimeout` and `setInterval` cannot be shared across scripts as these ids are local to the script where they were created.

See [openhab-js : cache](https://openhab.github.io/openhab-js/cache.html) for full API documentation.

- cache : <code>object</code>
  - .private
    - .get(key, defaultSupplier) ⇒ <code>* | null</code>
    - .put(key, value) ⇒ <code>Previous * | null</code>
    - .remove(key) ⇒ <code>Previous * | null</code>
    - .exists(key) ⇒ <code>boolean</code>
  - .shared
    - .get(key, defaultSupplier) ⇒ <code>* | null</code>
    - .put(key, value) ⇒ <code>Previous * | null</code>
    - .remove(key) ⇒ <code>Previous * | null</code>
    - .exists(key) ⇒ <code>boolean</code>

The `defaultSupplier` provided function will return a default value if a specified key is not already associated with a value.

**Example** *(Get a previously set value with a default value (times = 0))*

```js
var counter = cache.shared.get('counter', () => 0);
console.log('Counter: ' + counter);
```

**Example** *(Get a previously set value, modify and store it)*

```js
var counter = cache.private.get('counter');
counter++;
console.log('Counter: ' + counter);
cache.private.put('counter', counter);
```

### Time

openHAB internally makes extensive use of the `java.time` package.
openHAB-JS exports the excellent [JS-Joda](https://js-joda.github.io/js-joda/) library via the `time` namespace, which is a native JavaScript port of the same API standard used in Java for `java.time`.
Anywhere a native Java `ZonedDateTime` or `Duration` is required, the runtime will automatically convert a JS-Joda `ZonedDateTime` or `Duration` to its Java counterpart.

The exported JS-Joda library is also extended with convenient functions relevant to openHAB usage.

Examples:

```javascript
var now = time.ZonedDateTime.now();
var yesterday = time.ZonedDateTime.now().minusHours(24);
var item = items.Kitchen;
console.log("averageSince", item.persistence.averageSince(yesterday));
```

```javascript
actions.Exec.executeCommandLine(time.Duration.ofSeconds(20), 'echo', 'Hello World!');
```

See [JS-Joda](https://js-joda.github.io/js-joda/) for more examples and complete API usage.

#### Parsing and Formatting

Occasionally, one will need to parse a non-supported date time string or generate one from a ZonedDateTime.
To do this you will use [JS-Joda DateTimeFormatter and potentially your Locale](https://js-joda.github.io/js-joda/manual/formatting.html).
However, shipping all the locales with the openhab-js library would lead to an unacceptable large size.
Therefore, if you attempt to use the `DateTimeFormatter` and receive an error saying it cannot find your locale, you will need to manually install your locale and import it into your rule.

[JS-Joda Locales](https://github.com/js-joda/js-joda/tree/master/packages/locale#use-prebuilt-locale-packages) includes a list of all the supported locales.
Each locale consists of a two letter language indicator followed by a "-" and a two letter dialect indicator: e.g. "EN-US".
Installing a locale can be done through the command `npm install @js-joda/locale_de-de` from the *$OPENHAB_CONF/automation/js* folder.

To import and use a local into your rule you need to require it and create a `DateTimeFormatter` that uses it:

```javascript
var Locale = require('@js-joda/locale_de-de').Locale.GERMAN;
var formatter = time.DateTimeFormatter.ofPattern('dd.MM.yyyy HH:mm').withLocale(Locale);
```

#### `time.javaInstantToJsInstant()`

Converts a [`java.time.Instant`](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/time/Instant.html) to a JS-Joda [`Instant`](https://js-joda.github.io/js-joda/manual/Instant.html).

#### `time.javaZDTToJsZDT()`

Converts a [`java.time.ZonedDateTime`](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/time/ZonedDateTime.html) to a JS-Joda [`ZonedDateTime`](https://js-joda.github.io/js-joda/manual/ZonedDateTime.html).

#### `time.toZDT()`

There will be times when this automatic conversion is not available (for example when working with date times within a rule).
To ease having to deal with these cases a `time.toZDT()` function will accept almost any type that can be converted to a `time.ZonedDateTime`.
The following rules are used during the conversion:

| Argument Type                                                                | Rule                                                                                                            | Examples                                                                               |
|------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------|
| `null` or `undefined`                                                        | `time.ZonedDateTime.now()`                                                                                      | `time.toZDT();`                                                                        |
| `time.ZonedDateTime`                                                         | passed through unmodified                                                                                       |                                                                                        |
| `java.time.ZonedDateTime`                                                    | converted to the `time.ZonedDateTime` equivalent                                                                |                                                                                        |
| JavaScript native `Date`                                                     | converted to the equivalent `time.ZonedDateTime` using `SYSTEM` as the timezone                                 |                                                                                        |
| `number`, `bingint`, `java.lang.Number`, `DecimalType`                       | rounded to the nearest integer and added to `now` as milliseconds                                               | `time.toZDT(1000);`                                                                    |
| [`Quantity`](#quantity) or `QuantityType`                                    | if the unit is time-compatible, added to `now`                                                                  | `time.toZDT(item.getItem('MyTimeItem').rawState);`, `time.toZDT(Quantity('10 min'));`  |
| `items.Item` or `org.openhab.core.types.Item`                                | if the state is supported (see the `Type` rules in this table, e.g. `DecimalType`), the state is converted      | `time.toZDT(items.getItem('MyItem'));`                                                 |
| `String`, `java.lang.String`, `StringType`                                   | parsed based on the following rules; if no timezone is specified, `SYSTEM` timezone is used                     |                                                                                        |
| [ISO8601 Date/Time](https://en.wikipedia.org/wiki/ISO_8601) String           | parsed, depending on the provided data: if no date is passed, today's date; if no time is passed, midnight time | `time.toZDT('00:00');`, `time.toZDT('2022-12-24');`, `time.toZDT('2022-12-24T18:30');` |
| RFC String (output from a Java `ZonedDateTime.toString()`)                   | parsed                                                                                                          | `time.toZDT('2019-10-12T07:20:50.52Z');`                                               |
| `"kk:mm[:ss][ ]a"` (12 hour time)                                            | today's date with the time indicated, the space between the time and am/pm and seconds are optional             | `time.toZDT('1:23:45 PM');`                                                            |
| [ISO 8601 Duration](https://en.wikipedia.org/wiki/ISO_8601#Durations) String | added to `now`                                                                                                  | `time.toZDT('PT1H4M6.789S');`                                                          |

If no time zone is explicitly set, the system default time zone is used.
When a type or string that cannot be handled is encountered, an error is thrown.

#### `toToday()`

When you have a `time.ZonedDateTime`, a new `toToday()` method was added which will return a new `time.ZonedDateTime` with today's date but the original's time, accounting for DST changes.

For example, if the time was 13:45 and today was a DST changeover, the time will still be 13:45 instead of one hour off.

```javascript
var alarm = items.Alarm;
alarm.postUpdate(time.toZDT(alarm).toToday());
```

#### `isBeforeTime(timestamp)`, `isBeforeDate(timestamp)`, `isBeforeDateTime(timestamp)`

Tests whether this `time.ZonedDateTime` is before the time passed in `timestamp`, tested in various ways:

- `isBeforeTime` only compares the time portion of both, ignoring the date portion
- `isBeforeDate` only compares the date portion of both, ignoring the time portion
- `isBeforeDateTime` compares both date and time portions

`timestamp` can be anything supported by `time.toZDT()`.

Examples:

```javascript
time.toZDT('22:00').isBeforeTime('23:00')
time.toZDT('2022-12-01T12:00Z').isBeforeDateTime('2022-12-02T13:00Z')
```

#### `isAfterTime(timestamp)`, `isAfterDate(timestamp)`, `isAfterDateTime(timestamp)`

Tests whether this `time.ZonedDateTime` is after the time passed in `timestamp`, tested in various ways:

- `isAfterTime` only compares the time portion of both, ignoring the date portion
- `isAfterDate` only compares the date portion of both, ignoring the time portion
- `isAfterDateTime` compares both date and time portions

`timestamp` can be anything supported by `time.toZDT()`.

```javascript
// Equivalent to items.Sunset
time.toZDT().isAfterTime(items.getItem('Sunset')) // is now after sunset?
time.toZDT().isAfterDateTime('2022-12-01T12:00Z') // is now after 2022-12-01 noon?
```

#### `isBetweenTimes(start, end)`

Tests whether this `time.ZonedDateTime` is between the passed in `start` and `end`.
However, the function only compares the time portion of the three, ignoring the date portion.
The function takes into account times that span midnight.
`start` and `end` can be anything supported by `time.toZDT()`.

Examples:

```javascript
time.toZDT().isBetweenTimes('22:00', '05:00') // currently between 11:00 pm and 5:00 am
// Equivalent to items.Sunset
time.toZDT().isBetweenTimes(items.getItem('Sunset'), '11:30 PM') // is now between sunset and 11:30 PM?
// Equivalent to items.StartTime
time.toZDT(items.getItem('StartTime')).isBetweenTimes(time.toZDT(), 'PT1H'); // is the state of StartTime between now and one hour from now
```

#### `isBetweenDates(start, end)`

Tests whether this `time.ZonedDateTime` is between the passed in `start` and `end`.
However, the function only compares the date portion of the three, ignoring the time portion.
`start` and `end` can be anything supported by `time.toZDT()`.

Examples:

```javascript
time.toZDT().isBetweenDates('2022-06-18', '2023-12-24') // currently between 2022-06-18 and 2023-12-24
```

#### `isBetweenDateTimes(start, end)`

Tests whether this `time.ZonedDateTime` is between the passed in `start` and `end`.
`start` and `end` can be anything supported by `time.toZDT()`.

Examples:

```javascript
time.toZDT().isBetweenDateTimes('2022-06-18T22:00Z', '2023-12-24T05:00Z') // currently between 2022-06-18 22:00 and 2023-12-24 05:00
```

#### `isClose(zdt, maxDur)`

Tests to see if the delta between the `time.ZonedDateTime` and the passed in `time.ZonedDateTime` is within the passed in `time.Duration`.

```javascript
var timestamp = time.toZDT();
// do some stuff
if(timestamp.isClose(time.toZDT(), time.Duration.ofMillis(100))) {
  // did "do some stuff" take longer than 100 msecs to run?
}
```

#### `getMillisFromNow`

This method on `time.ZonedDateTime` returns the milliseconds from now to the passed in `time.ZonedDateTime`.

```javascript
var timestamp = time.ZonedDateTime.now().plusMinutes(5);
console.log(timestamp.getMillisFromNow());
```

### Quantity

The `Quantity` class greatly simplifies Quantity handling by providing unit conversion, comparisons and mathematical operations.
A Quantity consists of a measurement and its [Unit of Measurement (UoM)](https://www.openhab.org/docs/concepts/units-of-measurement.html#list-of-units), e.g. `5.7 m` (the measurement is `5.7`, the unit is `m` meters).

Internally using the openHAB `QuantityType`, which relies on [`javax.measure`](https://unitsofmeasurement.github.io/unit-api/), it supports all units and dimensions that openHAB supports.
If your unit is not listed in the UoM docs, it is very likely that it is still supported, e.g. the Angstrom Å for very small lengths (1 Å = 10 nm).
Anywhere a native openHAB `QuantityType` is required, the runtime will automatically convert the JS-`Quantity` to its Java counterpart.

#### Creation

`Quantity(value)` is used without new (it's a factory, not a constructor), pass an amount **and** a unit to it to create a new `Quantity` object:

The argument `value` can be a Quantity-compatible `Item`, a string, a `Quantity` instance or an openHAB Java [`QuantityType`](https://www.openhab.org/javadoc/latest/org/openhab/core/library/types/quantitytype).

`value` strings have the `$amount $unit` format and must follow these rules:

- `$amount` is required with a number provided as string
- `$unit` is optional (unit-less quantities are possible) and can have a prefix like `m` (milli) or `M` (mega)
- `$unit` does not allow whitespaces.
- `$unit` does allow superscript, e.g. `²` instead of `^2`.
- `$unit` requires the `*` between two units to be present, although you usually omit it (which is mathematically seen allowed, but openHAB needs the `*`).

Generally, you can expect a unit consisting of two (or more) units to need a `*`, e.g. `Nm` is `N*m`,

Nearly all [Units of Measurement (UoM)](https://www.openhab.org/docs/concepts/units-of-measurement.html#list-of-units) are expected to work with `Quantity`.
`ɡₙ` (standard gravity) is known to not work.

```javascript
// Allowed:
var qty = Quantity('5.75 m');
qty = Quantity('1 N*m');
qty = Quantity('1 m/s');
qty = Quantity('1 m^2/s^2');
qty = Quantity('1 m^2/s^-2'); // negative powers
qty = Quantity('1'); // unitless quantity
qty = Quantity(items.my_uom_item);

// Not allowed:
qty = Quantity('m');
qty = Quantity('1 Nm'); // * is required
qty = Quantity('1 m^2 / s^2'); // whitespaces are not allowed
qty = Quantity('1 m^2 s^2'); // / is required
qty = Quantity('1 m2/s2'); // ^ is required
```

Note: It is possible to create a unit-less (without unit) Quantity, however there is no advantage over using a `number` instead.

#### Conversion

It is possible to convert a `Quantity` to a new `Quantity` with a different unit or to get a `Quantity`'s amount as integer or float:

```javascript
var qty = Quantity('10.2 °C');

qty = qty.toUnit('°F');
var intValue = qty.int;
var floatValue = qty.float;
```

`toUnit` returns a new Quantity with the given unit or `null`, if conversion to that unit is not possible.

#### Comparison

`Quantity` provides the following methods for comparison:

- `equal(value)` ⇒ `boolean`: this `Quantity` equals to `value`
- `greaterThan(value)` ⇒ `boolean`: this `Quantity` is greater than `value`
- `greaterThanOrEqual(value)` ⇒ `boolean`: this `Quantity` is greater than or equal to `value`
- `lessThan(value)` ⇒ `boolean`: this `Quantity` is less than `value`
- `lessThanOrEqual(value)` ⇒ `boolean`: this `Quantity` is less than or equal to `value`

`value` can be a string or a `Quantity`, for the string the same rules apply as described above.

#### Mathematical Operators

- `add(value)` ⇒ `Quantity`: `value` can be a Quantity-compatible `Item`, a string or a `Quantity`
- `divide(value)` ⇒ `Quantity`: `value` can be a Quantity-compatible or Number `Item`, a number, a string or a `Quantity`
- `multiply(value)` ⇒ `Quantity`: `value` can be a Quantity-compatible or Number `Item`, a number, a string or a `Quantity`
- `subtract(value)` ⇒ `Quantity`: `value` can be a Quantity-compatible `Item`, a string or a `Quantity`

For the string the same rules apply as described above.

See [openhab-js : Quantity](https://openhab.github.io/openhab-js/Quantity.html) for full API documentation.

### Log

By default, the JS Scripting binding supports console logging like `console.log()` and `console.debug()` to the openHAB default log.
Additionally, scripts may create their own native openHAB logger using the log namespace.

```javascript
var logger = log('my_logger');

//prints "Hello World!"
logger.debug("Hello {}!", "world");
```

### Utils

openHAB internally is a Java program.
openHAB-JS converts between Java and JavaScript data types and reverse.

See [openhab-js : utils](https://openhab.github.io/openhab-js/utils.html) for full API documentation.

## File Based Rules

The JS Scripting binding will load scripts from `automation/js` in the user configuration directory.
The system will automatically reload scripts when changes are detected to files.
Local variable state is not persisted among reloads, see using the [cache](#cache) for a convenient way to persist objects.

File based rules can be created in 2 different ways: using [JSRule](#jsrule) or the [Rule Builder](#rule-builder).

See [openhab-js : rules](https://openhab.github.io/openhab-js/rules.html) for full API documentation.

### JSRule

`JSRule` provides a simple, declarative syntax for defining rules that will be executed based on a trigger condition:

```javascript
var email = "juliet@capulet.org"

rules.JSRule({
  name: "Balcony Lights ON at 5pm",
  description: "Light will turn on when it's 5:00pm",
  triggers: [triggers.GenericCronTrigger("0 0 17 * * ?")],
  execute: (event) => {
    // Equivalent to items.BalconyLights.sendCommand("ON")
    items.getItem("BalconyLights").sendCommand("ON");
    actions.NotificationAction.sendNotification(email, "Balcony lights are ON");
  },
  tags: ["Balcony", "Lights"],
  id: "BalconyLightsOn"
});
```

Note: `description`, `tags` and `id` are optional.

Note: You can use the passed `event` object to get information about the trigger that triggered the rule.
See [Event Object](#event-object) for documentation.

Multiple triggers can be added, some example triggers include:

```javascript
triggers.ChannelEventTrigger('astro:sun:local:rise#event', 'START');

triggers.ItemStateChangeTrigger('my_item', 'OFF', 'ON');

triggers.ItemStateUpdateTrigger('my_item', 'OFF');

triggers.ItemCommandTrigger('my_item', 'OFF');

triggers.GroupStateChangeTrigger('my_group', 'OFF', 'ON');

triggers.GroupStateUpdateTrigger('my_group', 'OFF');

triggers.GroupCommandTrigger('my_group', 'OFF');

triggers.ThingStatusUpdateTrigger('some:thing:uuid','OFFLINE');

triggers.ThingStatusChangeTrigger('some:thing:uuid','ONLINE','OFFLINE');

triggers.SystemStartlevelTrigger(40)  // Rules loaded

triggers.SystemStartlevelTrigger(50)  // Rule engine started

triggers.SystemStartlevelTrigger(70)  // User interfaces started

triggers.SystemStartlevelTrigger(80)  // Things initialized

triggers.SystemStartlevelTrigger(100) // Startup Complete

triggers.GenericCronTrigger('0 30 16 * * ? *');

triggers.TimeOfDayTrigger('19:00');

triggers.DateTimeTrigger('MyDateTimeItem');
```

You can use `null` for a trigger parameter to skip its configuration.

You may use `SwitchableJSRule` to create a rule that can be enabled and disabled with a Switch Item.
As an extension to `JSRule`, its syntax is the same, however you can specify an Item name (using the `switchItemName` rule config property) if you don't like the automatically created Item's name.

See [openhab-js : triggers](https://openhab.github.io/openhab-js/triggers.html) in the API documentation for a full list of all triggers.

### Rule Builder

The Rule Builder provides a convenient API to write rules in a high-level, readable style using a builder pattern.

Rules are started by calling `rules.when()` and can chain together [triggers](#rule-builder-triggers),
[conditions](#rule-builder-conditions) and [operations](#rule-builder-operations) in the following pattern:

```javascript
rules.when().triggerType()...if().conditionType().then().operationType()...build(name, description, tags, id);
```

Rule are completed by calling `.build(name, description, tags, id)` , all parameters are optional and reasonable defaults will be used if omitted.

- `name` String rule name - defaults generated name
- `description` String Rule description - defaults generated description
- `tags` Array of string tag names - defaults empty array
- `id` String id - defaults random UUID

A simple example of this would look like:

```javascript
rules.when().item("F1_Light").changed().then().send("changed").toItem("F2_Light").build("My Rule", "My First Rule");
```

Operations and conditions can also optionally take functions:

```javascript
rules.when().item("F1_light").changed().then(event => {
    console.log(event);
}).build("Test Rule", "My Test Rule");
```

Note that the Rule Builder currently does **not** provide type definitions and therefore auto-completion does not work.

See [Examples](#rule-builder-examples) for further patterns.

#### Rule Builder Triggers

- `when()`
- `or()`
  - `.channel(channelName)`: Specifies a channel event as a source for the rule to fire.
    - `.triggered(event)`: Trigger on a specific event name
  - `.cron(cronExpression)`: Specifies a cron schedule for the rule to fire.
  - `.timeOfDay(time)`: Specifies a time of day in `HH:mm` for the rule to fire.
  - `.item(itemName)`: Specifies an Item as the source of changes to trigger a rule.
    - `.for(duration)`
    - `.from(state)`
    - `.fromOn()`
    - `.fromOff()`
    - `.to(state)`
    - `.toOn()`
    - `.toOff()`
    - `.receivedCommand()`
    - `.receivedUpdate()`
    - `.changed()`
  - `.memberOf(groupName)`: Specifies a group Item as the source of changes to trigger the rule.
    - `.for(duration)`
    - `.from(state)`
    - `.fromOn()`
    - `.fromOff()`
    - `.to(state)`
    - `.toOn()`
    - `.toOff()`
    - `.receivedCommand()`
    - `.receivedUpdate()`
    - `.changed()`
  - `.system()`: Specifies a system event as a source for the rule to fire.
    - `.ruleEngineStarted()`
    - `.rulesLoaded()`
    - `.startupComplete()`
    - `.thingsInitialized()`
    - `.userInterfacesStarted()`
    - `.startLevel(level)`
  - `.thing(thingName)`: Specifies a Thing event as a source for the rule to fire.
    - `changed()`
    - `updated()`
    - `from(state)`
    - `to(state)`
  - `.dateTime(itemName)`: Specifies a DateTime Item whose (optional) date and time schedule the rule to fire.
    - `.timeOnly()`: Only the time of the Item should be compared, the date should be ignored.
    - `.withOffset(offset)`: The offset in seconds to add to the time of the DateTime Item.

Additionally, all the above triggers have the following functions:

- `.if()` or `.if(fn)` -> a [rule condition](#rule-builder-conditions)
- `.then()` or `.then(fn)` -> a [rule operation](#rule-builder-operations)
- `.or()` -> a [rule trigger](#rule-builder-triggers) (chain additional triggers)

#### Rule Builder Conditions

- `if(optionalFunction)`
  - `.stateOfItem(itemName)`
    - `is(state)`
    - `isOn()`
    - `isOff()`
    - `in(state...)`

#### Rule Builder Operations

- `then(optionalFunction)`
  - `.build(name, description, tags, id)`
  - `.copyAndSendState()`
  - `.copyState()`
  - `.inGroup(groupName)`
  - `.postIt()`
  - `.postUpdate(state)`
  - `.send(command)`
  - `.sendIt()`
  - `.sendOff()`
  - `.sendOn()`
  - `.sendToggle()`

#### Rule Builder Examples

```javascript
// Basic rule, when the BedroomLight1 is changed, run a custom function
rules.when().item('BedroomLight1').changed().then(e => {
  console.log("BedroomLight1 state", e.newState)
}).build();

// Turn on the kitchen light at SUNSET (using the Astro binding)
rules.when().channel('astro:sun:home:set#event').triggered('START').then().sendOn().toItem('KitchenLight').build('Sunset Rule', 'Turn on the kitchen light at SUNSET');

// Turn off the kitchen light at 9PM and tag rule
rules.when().timeOfDay('21:00').then().sendOff().toItem('KitchenLight').build('9PM Rule', 'Turn off the kitchen light at 9PM', ['Tag1', 'Tag2']);

// Set the colour of the hall light to pink at 9PM, tag rule and use a custom ID
rules.when().cron('0 0 21 * * ?').then().send('300,100,100').toItem('HallLight').build('Pink Rule', 'Set the colour of the hall light to pink at 9PM', ['Tag1', 'Tag2'], 'MyCustomID');

// When the switch S1 status changes to ON, then turn on the HallLight
rules.when().item('S1').changed().toOn().then().sendOn().toItem('HallLight').build('S1 Rule');

// When the HallLight colour changes pink, if the function fn returns true, then toggle the state of the OutsideLight
rules.when().item('HallLight').changed().to('300,100,100').if(fn).then().sendToggle().toItem('OutsideLight').build();

// Turn on the outdoor lights based on a DateTime Item's time portion
rules.when().dateTime('OutdoorLights_OffTime').timeOnly().then().sendOff().toItem('OutdoorLights').build('Outdoor Lights off');

// And some rules which can be toggled by the items created in the 'gRules' Group:

// When the HallLight receives a command, send the same command to the KitchenLight
rules.when(true).item('HallLight').receivedCommand().then().sendIt().toItem('KitchenLight').build('Hall Light to Kitchen Light');

// When the HallLight is updated to ON, make sure that BedroomLight1 is set to the same state as the BedroomLight2
rules.when(true).item('HallLight').receivedUpdate().then().copyState().fromItem('BedroomLight1').toItem('BedroomLight2').build();
```

### Event Object

**NOTE**: The `event` object is different in UI Based Rules and File Based Rules!
This section is only valid for File Based Rules.
If you use UI Based Rules, refer to [UI based rules event object documentation](#ui-event-object).

When a rule is triggered, the script is provided the event instance that triggered it.
The specific data depends on the event type.
The `event` object provides some information about that trigger.

This table gives an overview over the `event` object:

| Property Name     | Trigger Types                                       | Description                                                                   | Rules DSL Equivalent   |
|-------------------|-----------------------------------------------------|-------------------------------------------------------------------------------|------------------------|
| `oldState`        | `ItemStateChangeTrigger`, `GroupStateChangeTrigger` | Previous state of Item or Group that triggered event                          | `previousState`        |
| `newState`        | `ItemStateChangeTrigger`, `GroupStateChangeTrigger` | New state of Item or Group that triggered event                               | N/A                    |
| `receivedState`   | `ItemStateUpdateTrigger`, `GroupStateUpdateTrigger` | State of Item that triggered event                                            | `triggeringItem.state` |
| `receivedCommand` | `ItemCommandTrigger`, `GroupCommandTrigger`         | Command that triggered event                                                  | `receivedCommand`      |
| `itemName`        | `Item****Trigger`, `Group****Trigger`               | Name of Item that triggered event                                             | `triggeringItem.name`  |
| `groupName`       | `Group****Trigger`                                  | Name of the group whose member triggered event                                | N/A                    |
| `receivedEvent`   | `ChannelEventTrigger`                               | Channel event that triggered event                                            | N/A                    |
| `channelUID`      | `ChannelEventTrigger`                               | UID of channel that triggered event                                           | N/A                    |
| `oldStatus`       | `ThingStatusChangeTrigger`                          | Previous state of Thing that triggered event                                  | N/A                    |
| `newStatus`       | `ThingStatusChangeTrigger`                          | New state of Thing that triggered event                                       | N/A                    |
| `status`          | `ThingStatusUpdateTrigger`                          | State of Thing that triggered event                                           | N/A                    |
| `thingUID`        | `Thing****Trigger`                                  | UID of Thing that triggered event                                             | N/A                    |
| `cronExpression`  | `GenericCronTrigger`                                | Cron expression of the trigger                                                | N/A                    |
| `time`            | `TimeOfDayTrigger`                                  | Time of day value of the trigger                                              | N/A                    |
| `timeOnly`        | `DateTimeTrigger`                                   | Whether the trigger only considers the time part of the DateTime Item         | N/A                    |
| `offset`          | `DateTimeTrigger`                                   | Offset in seconds added to the time of the DateTime Item                      | N/A                    |
| `eventType`       | all except `PWMTrigger`, `PIDTrigger`               | Type of event that triggered event (change, command, triggered, update, time) | N/A                    |
| `triggerType`     | all except `PWMTrigger`, `PIDTrigger`               | Type of trigger that triggered event                                          | N/A                    |
| `eventClass`      | all                                                 | Java class name of the triggering event                                       | N/A                    |
| `module`          | all                                                 | (user-defined or auto-generated) name of trigger                              | N/A                    |
| `raw`             | all                                                 | Original contents of the event including data passed from a calling rule      | N/A                    |

All properties are typeof `string` except for properties contained by `raw` which are unmodified from the original types.

Please note that when using `GenericEventTrigger`, the available properties depend on the chosen event types.
It is not possible for the openhab-js library to provide type conversions for all properties of all openHAB events, as those are too many.
In case the event object does not provide type-conversed properties for your chosen event type, use the `payload` property to gain access to the event's (Java data type) payload.

**NOTE:**
`Group****Trigger`s use the equivalent `Item****Trigger` as trigger for each member.
Time triggers do not provide any event instance, therefore no property is populated.

See [openhab-js : EventObject](https://openhab.github.io/openhab-js/rules.html#.EventObject) for full API documentation.

## Advanced Scripting

### Libraries

#### Third Party Libraries

Loading of third party libraries is supported the same way as loading the openHAB JavaScript library:

```javascript
var myLibrary = require('my-library');
```

Note: Only CommonJS `require` is supported, ES module loading using `import` is not supported.

Run the `npm` command from the `automation/js` folder to install third party libraries, e.g. from [npm](https://www.npmjs.com/search?q=openhab).
This will create a `node_modules` folder (if it doesn't already exist) and install the library and it's dependencies there.

There are already some openHAB specific libraries available on [npm](https://www.npmjs.com/search?q=openhab), you may also search the forum for details.

#### Creating Your Own Library

You can also create your own personal JavaScript library for openHAB, but you can not just create a folder in `node_modules` and put your library code in it!
When it is run, `npm` will remove everything from `node_modules` that has not been properly installed.

Follow these steps to create your own library (it's called a CommonJS module):

1. Create a separate folder for your library outside of `automation/js`, you may also initialize a Git repository.
2. Run `npm init` from your newly created folder; at least provide responses for the `name`, `version` and `main` (e.g. `index.js`) fields.
3. Create the main file of your library (`index.js`) and add some exports:

   ```javascript
   var someProperty = 'Hello world!';
   function someFunction () {
     console.log('Hello from your personal library!');
   }

   module.exports = {
     someProperty,
     someFunction
   };
   ```

4. Tar it up by running `npm pack` from your library's folder.
5. Install it by running `npm install <path-to-library-folder>/<name>-<version>.tgz` from the `automation/js` folder.
6. After you've installed it with `npm`, you can continue development of the library inside `node_modules`.

It is also possible to upload your library to [npm](https://npmjs.com) to share it with other users.

If you want to get some advanced information, you can read [this blog post](https://bugfender.com/blog/how-to-create-an-npm-package/) or just google it.

### @runtime

In most cases, the [Standard Library](#standard-library) provides pure-JS APIs to interact with the openHAB runtime.
Generally speaking, you should therefore prefer to use [Standard Library](#standard-library) provided by this library instead.

However, in some cases, e.g. when needing a [`HSBType`](https://www.openhab.org/javadoc/latest/org/openhab/core/library/types/hsbtype), one needs to access raw Java utilities and types.
This can be achieved by using `require('@runtime')`, e.g.

```javascript
var { ON, OFF, QuantityType } = require('@runtime');
// Alternative, more verbose way to achieve the same:
//
// var runtime = require('@runtime');
//
// var ON = runtime.ON;
// var OFF = runtime.OFF;
// var QuantityType = runtime.QuantityType;
```

A list of available utilities and types can be found in the [JSR223 Default Preset documentation](https://www.openhab.org/docs/configuration/jsr223.html#default-preset-importpreset-not-required).

`require('@runtime')` also defines "services" such as `items`, `things`, `rules`, `events`, `actions`, `ir`, `itemRegistry`.
You can use these services for backwards compatibility purposes or ease migration from JSR223 scripts.
