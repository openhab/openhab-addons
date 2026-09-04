# Open PRs Overview

[All open PRs](https://github.com/openhab/openhab-addons/pulls?q=is%3Apr+is%3Aopen+author%3Amarkus7017) · [All open issues](https://github.com/openhab/openhab-addons/issues?q=is%3Aissue+is%3Aopen+author%3Amarkus7017)

| Topic | PR # | Description | Participants |
|---|---|---|---|
| Gen1 Discovery | [#21027](https://github.com/openhab/openhab-addons/pull/21027) | Fix Gen1 device discovery<br>[#20478](https://github.com/openhab/openhab-addons/issues/20478): Gen1 devices added as unknown | lsiepel, matmai |
| BLU Gateway | [#21287](https://github.com/openhab/openhab-addons/pull/21287) | Fix BLU Gateway memory crash<br>[#21276](https://github.com/openhab/openhab-addons/issues/21276): Fix oh-blu-scanner.js OOM crash<br>[#19777](https://github.com/openhab/openhab-addons/issues/19777): BLE scanner runs out of memory | ErikDB87, alaub81, lsiepel, scheuerer |
| Dimmer Models | [#19226](https://github.com/openhab/openhab-addons/pull/19226) | Add Plus/Pro Dimmer models<br>[#15993](https://github.com/openhab/openhab-addons/issues/15993): Support Shelly Pro Dimmer 2<br>[#16409](https://github.com/openhab/openhab-addons/issues/16409): Support Shelly Pro Dimmer 2PM<br>[#16408](https://github.com/openhab/openhab-addons/issues/16408): Support Shelly Pro Dimmer 1PM<br>Conflicts with [#20909](https://github.com/openhab/openhab-addons/pull/20909) — shared dispatch method | lsiepel |
| RGBW PM | [#19227](https://github.com/openhab/openhab-addons/pull/19227) | Add Shelly Pro RGBWW PM support<br>[#18001](https://github.com/openhab/openhab-addons/issues/18001): Plus RGBW PM 'Lights x4' mode<br>[#18215](https://github.com/openhab/openhab-addons/issues/18215): Brightness cannot be set to zero<br>[#19216](https://github.com/openhab/openhab-addons/issues/19216): Plus RGBW PM issues in RGB Mode<br>[#20627](https://github.com/openhab/openhab-addons/issues/20627): Add Pro RGBW mode 'rgbcct' | JacekKac, lsiepel |
| Bulb G3 | [#20909](https://github.com/openhab/openhab-addons/pull/20909) | Add Duo/Multicolor Bulb G3<br>[#20851](https://github.com/openhab/openhab-addons/issues/20851): Support for Duo bulb Gen3<br>Conflicts with [#19226](https://github.com/openhab/openhab-addons/pull/19226) — shared dispatch method | andrewfg, lsiepel, matmai |
| LoRa Add-On | [#19006](https://github.com/openhab/openhab-addons/pull/19006) | Add Shelly LoRa Add-On support<br>[#20952](https://github.com/openhab/openhab-addons/issues/20952): Add support Plus/Pro LoRa Addon | lolodomo, lsiepel, jlaur |
| Presence Gen4 | [#20992](https://github.com/openhab/openhab-addons/pull/20992) | Add Shelly Presence Gen4 support<br>[#20978](https://github.com/openhab/openhab-addons/issues/20978): Support Shelly Presence | lsiepel, matmai |
| Thread Safety | [#20386](https://github.com/openhab/openhab-addons/pull/20386) | Add Gen2 API thread-safety | Nadahar, lsiepel |

<!--
Changes since last posted version:
- Removed: Roller Flicker #21316 (merged), RGBW PM #21256 (merged)
- BLU Gateway #21287: dropped #16344 (closed)
- RGBW PM #19227: dropped "Depends on #21256" note (merged, no longer blocking)
- Bulb G3 #20909: added andrewfg, matmai to participants
-->
