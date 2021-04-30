# UpdateOpenHab Binding

This binding supports automated updating of OpenHAB.

## Supported Things

This binding supports one type of Thing - namely `updater`.

## Thing Configuration for `updater`

The `updater` thing requires one configuration parameter - namely `targetVersion`.
This defines which version/type of OpenHAB it should install during updates.
There are three possible values..

-  `STABLE` the updater installs the latest stable release of OpenHAB. This is the default.
-  `MILESTONE` the updater installs the latest Milestone release of OpenHAB.
-  `SNAPSHOT` the updater installs the latest daily SNAPSHOT release of OpenHAB. This is NOT recommended.

## Channels for `updater`

| Channel         | Type   | Description                                                 |
|-----------------|--------|-------------------------------------------------------------|
| actualVersion   | String | The version of the running OpenHab instance.                |
| latestVersion   | String | The latest released version of OpenHab.                     |
| updateAvailable | Switch | State is ON if an OpenHab update is available.              |
| updateCommand   | Switch | Switch ON to start updating OpenHab to the 'latestVersion'. |

## Full Example

```
TBD
```
