---
layout: documentation
title: Home Builder
source: https://github.com/openhab/openhab-core/blob/master/bundles/org.openhab.ui.homebuilder/USAGE.md
---

{% include base.html %}

<!-- Attention authors: Do not edit directly. Please add your changes to the appropriate source repository -->

# Home Builder

Boilerplate for the [Items](https://www.openhab.org/docs/configuration/items.html), [sitemap](https://www.openhab.org/docs/configuration/sitemaps.html) files and [HABPanel](https://www.openhab.org/docs/configuration/habpanel.html) dashboard.

## Features

- Classifies the objects within each room and creates groups for them
- Optionally adds icons from [Classic Icon Set](https://www.openhab.org/docs/configuration/iconsets/classic/) to the items
- Optionally adds Tags to the items - convenient for [HomeKit](https://www.openhab.org/addons/integrations/homekit/)/[Hue Emulation](https://www.openhab.org/addons/integrations/hueemulation/#device-tagging) add-ons users
- Automatically aligns the items vertically
- Generates a [Sitemap](https://www.openhab.org/docs/configuration/sitemaps.html) file
- Generates a set of HABPanel Dashboards corresponding with the Items

## Usage

### Localization

Home Builder recognizes the locale by itself by simply checking your existing language configuration through openHAB's REST API.
All the Item's labels generated with Home Builder will be translated to the language of your choice.

### Home Name

Provide your Home Name. It'll be a label for the `Home` Item, as well as the name of your Sitemap.
The `Home` item is the root item of your entire home structure - it contains all the floors as well as groups of Objects.

### Floors

After that you can select the number of stories (floors) comprising the building.
Each floor will have its own `Group` Item - Ground Floor will be called `GF`, First Floor `FF` and so on.
If there's only one floor, no additional `Group` Items shall be created.

### Rooms

Choose the rooms by simply selecting them on the list.
They contain the icons that you can adjust later.
If there's a custom room you'd like to add (e.g. a Piano room), provide its **label** to the field and hit Enter.
It will be added to the list.
The custom Item's **name** will be generated - e.g. for the "Piano Room" label the name would be `PianoRoom`

Note that the room doesn't appear on the Objects list right away - it's a known defect.
In order to add Objects to your custom room, simply remove the room from the list and re-add it again.

### Objects

Objects are the devices or sensors that physically exist in the room.
They are represented in Home Builder as singular Items (e.g. Light, Window, Motion Sensor, Temperature etc.)
Each Object is added to the Items list with its corresponding type, label, icon and list of Groups.
E.g. `Light` Object in the Bedroom will appear as:

```java
Switch   Bedroom_Light    "Light"    <light>    (Bedroom, gLight)    {channel=""}
```

Note that the Objects have the `{channel=""}` prefilled for convenience.
You can turn it off with "Append channel to the non-Group items" option.

#### Custom Objects

You can use the existing Objects from the list, or create custom ones.
Simply typing the custom Object's label in the field will create it.
E.g. typing `Lamp` will result in:

```java
Switch   Bedroom_Lamp    "Lamp"    <none>    (Bedroom, gLamp)    {channel=""}
```

Note that the default type for your custom Item is `Switch`.
You can, however, change it by typing `{type}:{label}`, e.g. `Number:Pressure` so it appears as:

```java
Number   Bedroom_Pressure   "Pressure"    <none>    (Bedroom, gPressure)    {channel=""}
```

#### Grouping Objects

All Objects of the same kind are being grouped within Home Builder.
If you add a `Light` Object in bedroom, you'll see that there's an additional Item on the bottom:

```java
Group:Switch:OR(ON, OFF)   gLight    "Light"    <light>    (Home)
```

It doesn't matter if there's just one Object or dozens - they will be grouped within this `Group` Item.

## Items

You can choose to generate textual `*.items` file content or construct a request directly to the REST API that'll create the items for you.

## Sitemap

You can generate a simple sitemap with Home Builder too.
Sitemap name will be generated from "Home Setup Name" parameter that you've provided before.
Don't forget to save your sitemap with the correct file name.

Sitemaps generated with Home Builder contain a `Frame` for each floor.
Each one of those Frames contain corresponding rooms.

Last Frame inside the Sitemap is a list of Object's groups.

## HABPanel Dashboard

Home Builder will help you with creating your set of dashboards for [HABPanel](https://www.openhab.org/docs/configuration/habpanel.html) too.
It creates a separate dashboard for each group of Objects.
All you need to do is to copy the generated JSON structure and paste it in HABPanel settings page.
The page is located in the following URL: `http://{youropenHAB:8080}/habpanel/index.html#/settings/localconfig`
Note that it'll override the existing Panel configuration!
