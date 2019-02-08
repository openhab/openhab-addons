# Facade Light Calculator Binding

The Facade Light Calculator binding is used for calculating lightning status of facades depending upon sun position.
Sun position is can be provided by Astro Binding.

## Supported Things

This binding supports one Thing: Facade

## Discovery

This binding does not offer auto-discovery.

## Binding Configuration

No binding configuration required.

## Thing Configuration

Facade requires a bridge reference to the Sun and the orientation of the facade toward geographic north. By default, the facade is considered enlighted when the sun azimuth reaches orientation - 90° (negative offset) until orientation + 90° (positive offset). The two parameters (negative and positive offset) are set by default to 90. These can be modified to take in account natural obstacles (wall, trees) that reduces the lighting angle.

## Channels

* **thing** `facade`
        * **channel**: 
            * `facingsun` (Switch) : ON if orientation - negative offset <= sun azimut <= orientation + negative offset
            * `bearing` (Number:Dimensionless) : percentage of direct lighting. 100% when the sun is in front, 0% when not facing sun 
            * `side` (String) : indicates if the sun is in the left, in front or on the right of the facade
            
### Trigger Channels

* **thing** `face`
        * **facadeEvent**:: `SUN_ENTER, SUN_LEAVE

## Full Example

_Provide a full usage example based on textual configuration files (*.things, *.items, *.sitemap)._

## Any custom content here!

_Feel free to add additional sections for whatever you think should also be mentioned about your binding!_
