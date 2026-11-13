# sunsynk_single_phase Channels

150 entries: 77 read-only and 73 read/write, including aliases.

All names and the Thing UID are generic examples. See [usage instructions](README.md).

| Channel ID                        | Item name                              | Item type                  | Unit  | Access     | Registers (decimal)  |
| --------------------------------- | -------------------------------------- | -------------------------- | ----- | ---------- | -------------------- |
| `pv-power`                        | `AdvancedPvPower`                      | `Number:Power`             | W     | Read       | [0]                  |
| `battery-temperature`             | `AdvancedBatteryTemperature`           | `Number:Temperature`       | °C    | Read       | [182]                |
| `battery-voltage`                 | `AdvancedBatteryVoltage`               | `Number:ElectricPotential` | V     | Read       | [183]                |
| `battery-soc`                     | `AdvancedBatterySoc`                   | `Number:Dimensionless`     | %     | Read       | [184]                |
| `battery-power`                   | `AdvancedBatteryPower`                 | `Number:Power`             | W     | Read       | [190]                |
| `battery-current`                 | `AdvancedBatteryCurrent`               | `Number:ElectricCurrent`   | A     | Read       | [191]                |
| `battery-charge-limit-current`    | `AdvancedBatteryChargeLimitCurrent`    | `Number:ElectricCurrent`   | A     | Read       | [314]                |
| `battery-discharge-limit-current` | `AdvancedBatteryDischargeLimitCurrent` | `Number:ElectricCurrent`   | A     | Read       | [315]                |
| `inverter-power`                  | `AdvancedInverterPower`                | `Number:Power`             | W     | Read       | [175]                |
| `inverter-voltage`                | `AdvancedInverterVoltage`              | `Number:ElectricPotential` | V     | Read       | [154]                |
| `inverter-current`                | `AdvancedInverterCurrent`              | `Number:ElectricCurrent`   | A     | Read       | [164]                |
| `inverter-frequency`              | `AdvancedInverterFrequency`            | `Number:Frequency`         | Hz    | Read       | [193]                |
| `grid-frequency`                  | `AdvancedGridFrequency`                | `Number:Frequency`         | Hz    | Read       | [79]                 |
| `grid-power`                      | `AdvancedGridPower`                    | `Number:Power`             | W     | Read       | [169]                |
| `grid-ld-power`                   | `AdvancedGridLdPower`                  | `Number:Power`             | W     | Read       | [167]                |
| `grid-l2-power`                   | `AdvancedGridL2Power`                  | `Number:Power`             | W     | Read       | [168]                |
| `grid-voltage`                    | `AdvancedGridVoltage`                  | `Number:ElectricPotential` | V     | Read       | [150]                |
| `grid-current`                    | `AdvancedGridCurrent`                  | `Number:ElectricCurrent`   | A     | Read       | [160, 161]           |
| `grid-ct-power`                   | `AdvancedGridCtPower`                  | `Number:Power`             | W     | Read       | [172]                |
| `grid-trickle-feed-power`         | `AdvancedGridTrickleFeedPower`         | `Number:Power`             | W     | Read/write | [206]                |
| `load-power`                      | `AdvancedLoadPower`                    | `Number:Power`             | W     | Read       | [178]                |
| `load-l1-power`                   | `AdvancedLoadL1Power`                  | `Number:Power`             | W     | Read       | [176]                |
| `load-l2-power`                   | `AdvancedLoadL2Power`                  | `Number:Power`             | W     | Read       | [177]                |
| `load-frequency`                  | `AdvancedLoadFrequency`                | `Number:Frequency`         | Hz    | Read       | [192]                |
| `aux-port-usage`                  | `AdvancedAuxPortUsage`                 | `String`                   |       | Read/write | [235]                |
| `generator-input`                 | `AdvancedGeneratorInput`               | `String`                   |       | Read/write | [235]                |
| `gen-power`                       | `AdvancedGenPower`                     | `Number:Power`             | W     | Read       | [166]                |
| `aux-power`                       | `AdvancedAuxPower`                     | `Number:Power`             | W     | Read       | [166]                |
| `pv1-power`                       | `AdvancedPv1Power`                     | `Number:Power`             | W     | Read       | [186]                |
| `pv1-voltage`                     | `AdvancedPv1Voltage`                   | `Number:ElectricPotential` | V     | Read       | [109]                |
| `pv1-current`                     | `AdvancedPv1Current`                   | `Number:ElectricCurrent`   | A     | Read       | [110]                |
| `pv2-power`                       | `AdvancedPv2Power`                     | `Number:Power`             | W     | Read       | [187]                |
| `pv2-voltage`                     | `AdvancedPv2Voltage`                   | `Number:ElectricPotential` | V     | Read       | [111]                |
| `pv2-current`                     | `AdvancedPv2Current`                   | `Number:ElectricCurrent`   | A     | Read       | [112]                |
| `pv3-power`                       | `AdvancedPv3Power`                     | `Number:Power`             | W     | Read       | [188]                |
| `pv3-voltage`                     | `AdvancedPv3Voltage`                   | `Number:ElectricPotential` | V     | Read       | [113]                |
| `pv3-current`                     | `AdvancedPv3Current`                   | `Number:ElectricCurrent`   | A     | Read       | [114]                |
| `pv4-power`                       | `AdvancedPv4Power`                     | `Number:Power`             | W     | Read       | [189]                |
| `pv4-voltage`                     | `AdvancedPv4Voltage`                   | `Number:ElectricPotential` | V     | Read       | [115]                |
| `pv4-current`                     | `AdvancedPv4Current`                   | `Number:ElectricCurrent`   | A     | Read       | [116]                |
| `essential-power`                 | `AdvancedEssentialPower`               | `Number:Power`             | W     | Read       | [175, 169, 166]      |
| `essential-1-power`               | `AdvancedEssential1Power`              | `Number:Power`             | W     | Read       | [175, 167, 166]      |
| `essential-2-power`               | `AdvancedEssential2Power`              | `Number:Power`             | W     | Read       | [175, 169, 166]      |
| `non-essential-power`             | `AdvancedNonEssentialPower`            | `Number:Power`             | W     | Read       | [172, 167]           |
| `day-active-energy`               | `AdvancedDayActiveEnergy`              | `Number:Energy`            | kWh   | Read       | [60]                 |
| `day-battery-charge`              | `AdvancedDayBatteryCharge`             | `Number:Energy`            | kWh   | Read       | [70]                 |
| `day-battery-discharge`           | `AdvancedDayBatteryDischarge`          | `Number:Energy`            | kWh   | Read       | [71]                 |
| `day-grid-import`                 | `AdvancedDayGridImport`                | `Number:Energy`            | kWh   | Read       | [76]                 |
| `day-grid-export`                 | `AdvancedDayGridExport`                | `Number:Energy`            | kWh   | Read       | [77]                 |
| `day-load-energy`                 | `AdvancedDayLoadEnergy`                | `Number:Energy`            | kWh   | Read       | [84]                 |
| `day-pv-energy`                   | `AdvancedDayPvEnergy`                  | `Number:Energy`            | kWh   | Read       | [108]                |
| `day-reactive-energy`             | `AdvancedDayReactiveEnergy`            | `Number:Energy`            | kVarh | Read       | [61]                 |
| `month-grid-energy`               | `AdvancedMonthGridEnergy`              | `Number:Energy`            | kWh   | Read       | [67]                 |
| `month-load-energy`               | `AdvancedMonthLoadEnergy`              | `Number:Energy`            | kWh   | Read       | [66]                 |
| `month-pv-energy`                 | `AdvancedMonthPvEnergy`                | `Number:Energy`            | kWh   | Read       | [65]                 |
| `total-active-energy`             | `AdvancedTotalActiveEnergy`            | `Number:Energy`            | kWh   | Read       | [63, 64]             |
| `total-battery-charge`            | `AdvancedTotalBatteryCharge`           | `Number:Energy`            | kWh   | Read       | [72, 73]             |
| `total-battery-discharge`         | `AdvancedTotalBatteryDischarge`        | `Number:Energy`            | kWh   | Read       | [74, 75]             |
| `total-grid-export`               | `AdvancedTotalGridExport`              | `Number:Energy`            | kWh   | Read       | [81, 82]             |
| `total-grid-import`               | `AdvancedTotalGridImport`              | `Number:Energy`            | kWh   | Read       | [78, 80]             |
| `total-load-energy`               | `AdvancedTotalLoadEnergy`              | `Number:Energy`            | kWh   | Read       | [85, 86]             |
| `total-pv-energy`                 | `AdvancedTotalPvEnergy`                | `Number:Energy`            | kWh   | Read       | [96, 97]             |
| `year-grid-export`                | `AdvancedYearGridExport`               | `Number:Energy`            | kWh   | Read       | [98, 99]             |
| `year-load-energy`                | `AdvancedYearLoadEnergy`               | `Number:Energy`            | kWh   | Read       | [87, 88]             |
| `year-pv-energy`                  | `AdvancedYearPvEnergy`                 | `Number:Energy`            | kWh   | Read       | [68, 69]             |
| `rated-power`                     | `AdvancedRatedPower`                   | `Number:Power`             | W     | Read       | [16, 17]             |
| `fault`                           | `AdvancedFault`                        | `String`                   |       | Read       | [103, 104, 105, 106] |
| `overall-state`                   | `AdvancedOverallState`                 | `String`                   |       | Read       | [59]                 |
| `sd-status`                       | `AdvancedSdStatus`                     | `String`                   |       | Read       | [92]                 |
| `dc-transformer-temperature`      | `AdvancedDcTransformerTemperature`     | `Number:Temperature`       | °C    | Read       | [90]                 |
| `environment-temperature`         | `AdvancedEnvironmentTemperature`       | `Number:Temperature`       | °C    | Read       | [95]                 |
| `radiator-temperature`            | `AdvancedRadiatorTemperature`          | `Number:Temperature`       | °C    | Read       | [91]                 |
| `grid-connected`                  | `AdvancedGridConnected`                | `Switch`                   |       | Read       | [194]                |
| `date-time`                       | `AdvancedDateTime`                     | `DateTime`                 |       | Read/write | [22, 23, 24]         |
| `inverter-enabled`                | `AdvancedInverterEnabled`              | `Switch`                   |       | Read/write | [43]                 |
| `control-mode`                    | `AdvancedControlMode`                  | `Number`                   |       | Read       | [200]                |
| `grid-charge-enabled`             | `AdvancedGridChargeEnabled`            | `Switch`                   |       | Read/write | [232]                |
| `battery-charging-voltage`        | `AdvancedBatteryChargingVoltage`       | `Number:ElectricPotential` | V     | Read       | [312]                |
| `bat1-soc`                        | `AdvancedBat1Soc`                      | `Number:Dimensionless`     | %     | Read       | [603]                |
| `bat1-cycle`                      | `AdvancedBat1Cycle`                    | `Number`                   |       | Read       | [611]                |
| `battery-equalization-voltage`    | `AdvancedBatteryEqualizationVoltage`   | `Number:ElectricPotential` | V     | Read/write | [201]                |
| `battery-absorption-voltage`      | `AdvancedBatteryAbsorptionVoltage`     | `Number:ElectricPotential` | V     | Read/write | [202]                |
| `battery-float-voltage`           | `AdvancedBatteryFloatVoltage`          | `Number:ElectricPotential` | V     | Read/write | [203]                |
| `battery-shutdown-capacity`       | `AdvancedBatteryShutdownCapacity`      | `Number:Dimensionless`     | %     | Read/write | [217]                |
| `battery-restart-capacity`        | `AdvancedBatteryRestartCapacity`       | `Number:Dimensionless`     | %     | Read/write | [218]                |
| `battery-low-capacity`            | `AdvancedBatteryLowCapacity`           | `Number:Dimensionless`     | %     | Read/write | [219]                |
| `battery-shutdown-voltage`        | `AdvancedBatteryShutdownVoltage`       | `Number:ElectricPotential` | V     | Read/write | [220]                |
| `battery-restart-voltage`         | `AdvancedBatteryRestartVoltage`        | `Number:ElectricPotential` | V     | Read/write | [221]                |
| `battery-low-voltage`             | `AdvancedBatteryLowVoltage`            | `Number:ElectricPotential` | V     | Read/write | [222]                |
| `priority-load`                   | `AdvancedPriorityLoad`                 | `Switch`                   |       | Read/write | [243]                |
| `load-limit`                      | `AdvancedLoadLimit`                    | `String`                   |       | Read/write | [244]                |
| `max-solar-power`                 | `AdvancedMaxSolarPower`                | `Number:Power`             | W     | Read/write | [53]                 |
| `max-sell-power`                  | `AdvancedMaxSellPower`                 | `Number:Power`             | W     | Read/write | [245]                |
| `solar-export`                    | `AdvancedSolarExport`                  | `Switch`                   |       | Read/write | [247]                |
| `use-timer`                       | `AdvancedUseTimer`                     | `Switch`                   |       | Read/write | [248]                |
| `peak-shaving`                    | `AdvancedPeakShaving`                  | `Switch`                   |       | Read/write | [280]                |
| `gen-peak-shaving`                | `AdvancedGenPeakShaving`               | `Switch`                   |       | Read/write | [280]                |
| `grid-peak-shaving`               | `AdvancedGridPeakShaving`              | `Switch`                   |       | Read/write | [280]                |
| `grid-always-on`                  | `AdvancedGridAlwaysOn`                 | `Switch`                   |       | Read/write | [280]                |
| `gen-peak-shaving-power`          | `AdvancedGenPeakShavingPower`          | `Number:Power`             | W     | Read/write | [292]                |
| `grid-peak-shaving-power`         | `AdvancedGridPeakShavingPower`         | `Number:Power`             | W     | Read/write | [293]                |
| `prog1-time`                      | `AdvancedProg1Time`                    | `String`                   |       | Read/write | [250]                |
| `prog2-time`                      | `AdvancedProg2Time`                    | `String`                   |       | Read/write | [251]                |
| `prog3-time`                      | `AdvancedProg3Time`                    | `String`                   |       | Read/write | [252]                |
| `prog4-time`                      | `AdvancedProg4Time`                    | `String`                   |       | Read/write | [253]                |
| `prog5-time`                      | `AdvancedProg5Time`                    | `String`                   |       | Read/write | [254]                |
| `prog6-time`                      | `AdvancedProg6Time`                    | `String`                   |       | Read/write | [255]                |
| `prog1-power`                     | `AdvancedProg1Power`                   | `Number:Power`             | W     | Read/write | [256]                |
| `prog2-power`                     | `AdvancedProg2Power`                   | `Number:Power`             | W     | Read/write | [257]                |
| `prog3-power`                     | `AdvancedProg3Power`                   | `Number:Power`             | W     | Read/write | [258]                |
| `prog4-power`                     | `AdvancedProg4Power`                   | `Number:Power`             | W     | Read/write | [259]                |
| `prog5-power`                     | `AdvancedProg5Power`                   | `Number:Power`             | W     | Read/write | [260]                |
| `prog6-power`                     | `AdvancedProg6Power`                   | `Number:Power`             | W     | Read/write | [261]                |
| `prog1-capacity`                  | `AdvancedProg1Capacity`                | `Number:Dimensionless`     | %     | Read/write | [268]                |
| `prog2-capacity`                  | `AdvancedProg2Capacity`                | `Number:Dimensionless`     | %     | Read/write | [269]                |
| `prog3-capacity`                  | `AdvancedProg3Capacity`                | `Number:Dimensionless`     | %     | Read/write | [270]                |
| `prog4-capacity`                  | `AdvancedProg4Capacity`                | `Number:Dimensionless`     | %     | Read/write | [271]                |
| `prog5-capacity`                  | `AdvancedProg5Capacity`                | `Number:Dimensionless`     | %     | Read/write | [272]                |
| `prog6-capacity`                  | `AdvancedProg6Capacity`                | `Number:Dimensionless`     | %     | Read/write | [273]                |
| `prog1-charge`                    | `AdvancedProg1Charge`                  | `String`                   |       | Read/write | [274]                |
| `prog2-charge`                    | `AdvancedProg2Charge`                  | `String`                   |       | Read/write | [275]                |
| `prog3-charge`                    | `AdvancedProg3Charge`                  | `String`                   |       | Read/write | [276]                |
| `prog4-charge`                    | `AdvancedProg4Charge`                  | `String`                   |       | Read/write | [277]                |
| `prog5-charge`                    | `AdvancedProg5Charge`                  | `String`                   |       | Read/write | [278]                |
| `prog6-charge`                    | `AdvancedProg6Charge`                  | `String`                   |       | Read/write | [279]                |
| `prog1-mode`                      | `AdvancedProg1Mode`                    | `String`                   |       | Read/write | [274]                |
| `prog2-mode`                      | `AdvancedProg2Mode`                    | `String`                   |       | Read/write | [275]                |
| `prog3-mode`                      | `AdvancedProg3Mode`                    | `String`                   |       | Read/write | [276]                |
| `prog4-mode`                      | `AdvancedProg4Mode`                    | `String`                   |       | Read/write | [277]                |
| `prog5-mode`                      | `AdvancedProg5Mode`                    | `String`                   |       | Read/write | [278]                |
| `prog6-mode`                      | `AdvancedProg6Mode`                    | `String`                   |       | Read/write | [279]                |
| `prog1-sell`                      | `AdvancedProg1Sell`                    | `Switch`                   |       | Read/write | [274]                |
| `prog2-sell`                      | `AdvancedProg2Sell`                    | `Switch`                   |       | Read/write | [275]                |
| `prog3-sell`                      | `AdvancedProg3Sell`                    | `Switch`                   |       | Read/write | [276]                |
| `prog4-sell`                      | `AdvancedProg4Sell`                    | `Switch`                   |       | Read/write | [277]                |
| `prog5-sell`                      | `AdvancedProg5Sell`                    | `Switch`                   |       | Read/write | [278]                |
| `prog6-sell`                      | `AdvancedProg6Sell`                    | `Switch`                   |       | Read/write | [279]                |
| `prog1-voltage`                   | `AdvancedProg1Voltage`                 | `Number:ElectricPotential` | V     | Read/write | [262]                |
| `prog2-voltage`                   | `AdvancedProg2Voltage`                 | `Number:ElectricPotential` | V     | Read/write | [263]                |
| `prog3-voltage`                   | `AdvancedProg3Voltage`                 | `Number:ElectricPotential` | V     | Read/write | [264]                |
| `prog4-voltage`                   | `AdvancedProg4Voltage`                 | `Number:ElectricPotential` | V     | Read/write | [265]                |
| `prog5-voltage`                   | `AdvancedProg5Voltage`                 | `Number:ElectricPotential` | V     | Read/write | [266]                |
| `prog6-voltage`                   | `AdvancedProg6Voltage`                 | `Number:ElectricPotential` | V     | Read/write | [267]                |
| `grid-charge-battery-current`     | `AdvancedGridChargeBatteryCurrent`     | `Number:ElectricCurrent`   | A     | Read/write | [230]                |
| `battery-max-charge-current`      | `AdvancedBatteryMaxChargeCurrent`      | `Number:ElectricCurrent`   | A     | Read/write | [210]                |
| `battery-max-discharge-current`   | `AdvancedBatteryMaxDischargeCurrent`   | `Number:ElectricCurrent`   | A     | Read/write | [211]                |
| `bms-protocol`                    | `AdvancedBmsProtocol`                  | `String`                   |       | Read/write | [325]                |
| `communication-board-version`     | `AdvancedCommunicationBoardVersion`    | `String`                   |       | Read       | [14]                 |
| `control-board-version`           | `AdvancedControlBoardVersion`          | `String`                   |       | Read       | [13]                 |
| `inverter-id`                     | `AdvancedInverterId`                   | `String`                   |       | Read       | [3, 4, 5, 6, 7]      |
