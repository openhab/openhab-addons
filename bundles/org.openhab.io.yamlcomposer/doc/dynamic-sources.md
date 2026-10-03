# Dynamic Sources

Dynamic Sources provide live, reactive access to openHAB's runtime registries directly inside your YAML Composer templates.

Instead of hardcoding device references or manually duplicating item definitions for every entity in your setup, Dynamic Sources allow you to inspect registered Things and Items, filter them dynamically (for example, by enabled status or binding type), and automatically generate items, channels, or group hierarchies.

[[toc]]

## How Dynamic Sources Work

YAML Composer exposes special top-level map variables corresponding to runtime registries, such as **`THINGS`** and **`ITEMS`**.

1. **Snapshot Consistency:** Dynamic sources evaluate against a consistent snapshot of the openHAB registry for the duration of a compilation pass, preventing mid-render state tearing if registries update mid-evaluation.
1. **Dependency Tracking**: Whenever a YAML file queries a dynamic source, YAML Composer automatically registers a dependency between that source file and the respective openHAB registry.
1. **Automatic Recompilation**: When an entity is added, removed, or updated—or when a Thing is **enabled** or **disabled**—YAML Composer automatically invalidates and recompiles all dependent YAML files in the background after a brief debounce period.

::: tip Non-Disruptive Event Filtering
YAML Composer filters out transient runtime status changes (such as a device fluctuating between `ONLINE` and `OFFLINE`).
Recompilation is only triggered when structural changes occur or when a Thing's administrative status transitions to or from `DISABLED`.
:::

---

## THINGS Source Map

The `THINGS` map is keyed by full **Thing UID** strings (for example, `mqtt:topic:mosquitto:porch_light`).

### Thing Data Schema

Each entry in `THINGS` is a structured map containing the following properties:

| Key                    | Type      | Description                                                                                           |
|:-----------------------|:----------|:------------------------------------------------------------------------------------------------------|
| `UID` / `uid`          | `String`  | Full Thing UID string (e.g., `mqtt:topic:mosquitto:porch_light`). Both `UID` and `uid` are supported. |
| `id`                   | `String`  | The short ID portion of the Thing UID (e.g., `porch_light`).                                          |
| `thingTypeUID`         | `String`  | Full UID of the Thing Type (e.g., `mqtt:topic`).                                                      |
| `bridgeUID`            | `String`  | UID of the parent bridge Thing (if applicable).                                                       |
| `enabled`              | `Boolean` | `true` if the Thing is currently enabled in openHAB; `false` if disabled.                             |
| `label`                | `String`  | Human-readable label assigned to the Thing.                                                           |
| `location`             | `String`  | Location assigned to the Thing.                                                                       |
| `semanticEquipmentTag` | `String`  | The Thing's semantic equipment tag.                                                                   |
| `properties`           | `Map`     | Map of Thing properties.                                                                              |
| `config`               | `Map`     | Map of Thing-level configuration parameters (renamed from `configuration`).                           |
| `channels`             | `Map`     | Map of channels belonging to this Thing, keyed by short **Channel ID**.                               |

### Channel Map Schema

Inside `thing.channels`, channels are keyed by their short channel ID (e.g., `power`, `temperature`) rather than full UIDs:

| Key              | Type     | Description                                                                                                  |
|:-----------------|:---------|:-------------------------------------------------------------------------------------------------------------|
| `UID` / `uid`    | `String` | Full Channel UID string (e.g., `mqtt:topic:mosquitto:porch:brightness`). Both `UID` and `uid` are supported. |
| `id`             | `String` | Short channel ID (e.g., `brightness`).                                                                       |
| `channelTypeUID` | `String` | The channel Type UID (e.g., `mqtt:number`).                                                                  |
| `itemType`       | `String` | Target openHAB item type (e.g., `Switch`, `Dimmer`, `Number`).                                               |
| `kind`           | `String` | Channel kind (`STATE` or `TRIGGER`).                                                                         |
| `label`          | `String` | Human-readable channel label.                                                                                |
| `description`    | `String` | Description of the channel.                                                                                  |
| `defaultTags`    | `List`   | The list of the channel's default tags.                                                                      |
| `properties`     | `Map`    | Map of Channel properties.                                                                                   |
| `config`         | `Map`    | Channel configuration parameters (renamed from `configuration`).                                             |

**Example:**

```yaml
astro:sun:testid:
  UID: astro:sun:testid
  uid: astro:sun:testid
  id: testid
  thingTypeUID: astro:sun
  bridgeUID: null
  enabled: true
  label: Astro Sun Data
  config:
    useMeteorologicalSeason: false
    interval: !!float '300'
    geolocation: 1,1
  properties:
    thingTypeVersion: '2'
  location: null
  semanticEquipmentTag: Application
  channels:
    rise#start:
      uid: astro:sun:testid:rise#start
      UID: astro:sun:testid:rise#start
      id: rise#start
      channelTypeUID: astro:start
      itemType: DateTime
      kind: STATE
      label: Start Time
      description: The start time of the event
      defaultTags:
        - Calculation
        - Timestamp
      properties: {
        }
      config:
        forceEvent: false
        offset: !!float '0'
    ... more channels
```

---

## ITEMS Source Map

The `ITEMS` map is keyed by openHAB **Item Name** strings (for example, `Kitchen_Light`).

### Item Data Schema

Each entry in `ITEMS` contains the item's DTO representation:

| Key          | Type     | Description                                                          |
|:-------------|:---------|:---------------------------------------------------------------------|
| `name`       | `String` | Unique item name.                                                    |
| `type`       | `String` | Item type (e.g., `Switch`, `Dimmer`, `Group`, `Number:Temperature`). |
| `label`      | `String` | Item label text.                                                     |
| `category`   | `String` | Category/icon name assigned to the item.                             |
| `tags`       | `List`   | List of semantic tags (e.g., `['Lightbulb', 'Control']`).            |
| `groupNames` | `List`   | List of parent group names this item belongs to.                     |

**Example:**

```yaml
P2S_Printer_Chamber_Light:
  type: Switch
  name: P2S_Printer_Chamber_Light
  label: Chamber Light
  category: light
  tags:
    - Point
  groupNames:
    - Printer_Lights
```

---

## Usage Patterns & Examples

### 1. Direct Entity Lookup

You can inspect specific Things or Items directly by key:

```yaml
variables:
  porch_thing: ${THINGS['mqtt:topic:mosquitto:porch']}
  porch_enabled: ${porch_thing.enabled}
```

::: tip Inspecting Data Structure
During template development, you can easily inspect the full schema and available properties of dynamic sources or specific entities by dumping them directly into a YAML key:

```yaml
# Dump dynamic sources to view all entities and fields
dump_all_things: ${THINGS}
dump_all_items: ${ITEMS}

# Dump a single entity
dump_single_item: ${ITEMS['Kitchen_Light']}
```

When compiled, YAML Composer expands the entire map into the compiled output file, making it easy to discover available properties and structure.
:::

### 2. Auto-Generating Items for Enabled Things

Use `!for` loops to iterate over `THINGS` and filter dynamically.
This example automatically creates items only for active/enabled MQTT light things:

```yaml
items:
  !for "UID, thing in THINGS if thing.enabled":
    !if UID.startsWith("mqtt:topic"):
      "Item_${thing.label | replace(' ', '_')}":
        type: Switch
        label: ${thing.label} Switch
        channel: ${thing.UID}:power
```

::: tip Formatting Long Conditional Expressions
When evaluating complex or multiline condition chains, you can format `!if` directives using standard YAML extended mapping key syntax (`? ... : ...`) paired with folded multiline strings (`>`).
This keeps complex conditional expressions clean and readable without escaping:

```yaml
items:
  !for "uid, thing in THINGS":
    ? !if >
        thing.enabled
        && thing.thingTypeUID == 'mqtt:topic'
        && thing.config.containsKey("availabilityTopic")
    :
      "Item_${thing.id}":
        type: Switch
        ...
```

:::

### 3. Iterating Over Thing Channels

Generate items dynamically from the channels defined on a Thing:

```yaml
!var target_thing: "${THINGS['mqtt:topic:garage-light']}"

test_items:
  !for "ch_id, channel in target_thing.channels":
    '${target_thing.id | replace("-", "_")}_${ch_id | label}':
      type: ${channel.itemType | default('String')}
      label: "${(target_thing.label ~ ' ' ~ channel.label | default('')) | trim}"
      channel: ${channel.uid}
```

### 4. Dynamic UI Widgets & Pages

Dynamic Sources are not limited to generating Things and Items—they can also be used to automatically assemble MainUI widgets and dashboard pages.

By querying the `ITEMS` map, you can filter items by parent groups, locations, or semantic tags (such as `Lightbulb` or `Measurement`) to dynamically build card controls without maintaining manual UI item lists.

```yaml
# Generate an openHAB MainUI list card containing controls for all items in 'gLivingRoom'
component: oh-list-card
config:
  title: Living Room Controls
slots:
  default:
    !for "name, item in ITEMS":
      ? !if >
          'gLivingRoom' in item.groupNames
          && 'Lightbulb' in item.tags
      :
        - component: oh-toggle-item
          config:
            item: ${item.name}
            title: ${item.label}
            icon: ${item.category | default('light')}
```

---

## Protection & Reserved Keywords

Dynamic source maps (`THINGS`, `ITEMS`, etc.) are system-reserved global variables managed by the YAML Composer preprocessor.

- They **cannot** be overridden using top-level `variables:` blocks or inline `!var` directives.
- Attempting to rebind dynamic source variables will log a warning and preserve the live registry binding.
