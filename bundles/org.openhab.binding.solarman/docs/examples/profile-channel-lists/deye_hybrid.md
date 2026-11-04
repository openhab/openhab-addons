# deye_hybrid Channels

81 entries: 63 read-only and 18 read/write, including aliases.

All names and the Thing UID are generic examples. See [usage instructions](README.md).

| Channel ID                                 | Item name                                   | Item type                  | Unit | Access     | Registers (decimal)            |
| ------------------------------------------ | ------------------------------------------- | -------------------------- | ---- | ---------- | ------------------------------ |
| `solar-pv1-power`                          | `LegacySolarPv1Power`                       | `Number:Power`             | W    | Read       | [186]                          |
| `solar-pv2-power`                          | `LegacySolarPv2Power`                       | `Number:Power`             | W    | Read       | [187]                          |
| `solar-pv1-voltage`                        | `LegacySolarPv1Voltage`                     | `Number:ElectricPotential` | V    | Read       | [109]                          |
| `solar-pv2-voltage`                        | `LegacySolarPv2Voltage`                     | `Number:ElectricPotential` | V    | Read       | [111]                          |
| `solar-pv1-current`                        | `LegacySolarPv1Current`                     | `Number:ElectricCurrent`   | A    | Read       | [110]                          |
| `solar-pv2-current`                        | `LegacySolarPv2Current`                     | `Number:ElectricCurrent`   | A    | Read       | [112]                          |
| `solar-daily-production`                   | `LegacySolarDailyProduction`                | `Number:Energy`            | kWh  | Read       | [108]                          |
| `solar-total-production`                   | `LegacySolarTotalProduction`                | `Number:Energy`            | kWh  | Read       | [96, 97]                       |
| `solar-micro-inverter-power`               | `LegacySolarMicroInverterPower`             | `Number:Power`             | W    | Read       | [166]                          |
| `battery-total-battery-charge`             | `LegacyBatteryTotalBatteryCharge`           | `Number:Energy`            | kWh  | Read       | [72, 73]                       |
| `battery-total-battery-discharge`          | `LegacyBatteryTotalBatteryDischarge`        | `Number:Energy`            | kWh  | Read       | [74, 75]                       |
| `battery-daily-battery-charge`             | `LegacyBatteryDailyBatteryCharge`           | `Number:Energy`            | kWh  | Read       | [70]                           |
| `battery-daily-battery-discharge`          | `LegacyBatteryDailyBatteryDischarge`        | `Number:Energy`            | kWh  | Read       | [71]                           |
| `battery-battery-status`                   | `LegacyBatteryBatteryStatus`                | `String`                   |      | Read       | [189]                          |
| `battery-battery-power`                    | `LegacyBatteryBatteryPower`                 | `Number:Power`             | W    | Read       | [190]                          |
| `battery-battery-voltage`                  | `LegacyBatteryBatteryVoltage`               | `Number:ElectricPotential` | V    | Read       | [183]                          |
| `battery-battery-soc`                      | `LegacyBatteryBatterySoc`                   | `Number:Dimensionless`     | %    | Read       | [184]                          |
| `battery-battery-current`                  | `LegacyBatteryBatteryCurrent`               | `Number:ElectricCurrent`   | A    | Read       | [191]                          |
| `battery-battery-temperature`              | `LegacyBatteryBatteryTemperature`           | `Number:Temperature`       | °C   | Read       | [182]                          |
| `grid-total-grid-power`                    | `LegacyGridTotalGridPower`                  | `Number:Power`             | W    | Read       | [169]                          |
| `grid-grid-voltage-l1`                     | `LegacyGridGridVoltageL1`                   | `Number:ElectricPotential` | V    | Read       | [150]                          |
| `grid-grid-current-l1`                     | `LegacyGridGridCurrentL1`                   | `Number:ElectricCurrent`   | A    | Read       | [160]                          |
| `grid-grid-voltage-l2`                     | `LegacyGridGridVoltageL2`                   | `Number:ElectricPotential` | V    | Read       | [151]                          |
| `grid-grid-current-l2`                     | `LegacyGridGridCurrentL2`                   | `Number:ElectricCurrent`   | A    | Read       | [161]                          |
| `grid-internal-ct-l1-power`                | `LegacyGridInternalCtL1Power`               | `Number:Power`             | W    | Read       | [167]                          |
| `grid-internal-ct-l2-power`                | `LegacyGridInternalCtL2Power`               | `Number:Power`             | W    | Read       | [168]                          |
| `grid-external-ct-l1-power`                | `LegacyGridExternalCtL1Power`               | `Number:Power`             | W    | Read       | [170]                          |
| `grid-external-ct-l2-power`                | `LegacyGridExternalCtL2Power`               | `Number:Power`             | W    | Read       | [171]                          |
| `grid-daily-energy-bought`                 | `LegacyGridDailyEnergyBought`               | `Number:Energy`            | kWh  | Read       | [76]                           |
| `grid-total-energy-bought`                 | `LegacyGridTotalEnergyBought`               | `Number:Energy`            | kWh  | Read       | [78, 80]                       |
| `grid-daily-energy-sold`                   | `LegacyGridDailyEnergySold`                 | `Number:Energy`            | kWh  | Read       | [77]                           |
| `grid-total-energy-sold`                   | `LegacyGridTotalEnergySold`                 | `Number:Energy`            | kWh  | Read       | [81, 82]                       |
| `grid-total-grid-production`               | `LegacyGridTotalGridProduction`             | `Number:Energy`            | kWh  | Read       | [63, 64]                       |
| `upload-total-load-power`                  | `LegacyUploadTotalLoadPower`                | `Number:Power`             | W    | Read       | [178]                          |
| `upload-load-l1-power`                     | `LegacyUploadLoadL1Power`                   | `Number:Power`             | W    | Read       | [176]                          |
| `upload-load-l2-power`                     | `LegacyUploadLoadL2Power`                   | `Number:Power`             | W    | Read       | [177]                          |
| `upload-load-voltage`                      | `LegacyUploadLoadVoltage`                   | `Number:ElectricPotential` | V    | Read       | [157]                          |
| `upload-daily-load-consumption`            | `LegacyUploadDailyLoadConsumption`          | `Number:Energy`            | kWh  | Read       | [84]                           |
| `upload-total-load-consumption`            | `LegacyUploadTotalLoadConsumption`          | `Number:Energy`            | kWh  | Read       | [85, 86]                       |
| `upload-smartload-enable-status`           | `LegacyUploadSmartloadEnableStatus`         | `String`                   |      | Read       | [195]                          |
| `inverter-running-status`                  | `LegacyInverterRunningStatus`               | `String`                   |      | Read       | [59]                           |
| `inverter-total-power`                     | `LegacyInverterTotalPower`                  | `Number:Power`             | W    | Read       | [175]                          |
| `inverter-grid-frequency`                  | `LegacyInverterGridFrequency`               | `Number:Frequency`         | Hz   | Read       | [79]                           |
| `inverter-current-l1`                      | `LegacyInverterCurrentL1`                   | `Number:ElectricCurrent`   | A    | Read       | [164]                          |
| `inverter-current-l2`                      | `LegacyInverterCurrentL2`                   | `Number:ElectricCurrent`   | A    | Read       | [165]                          |
| `inverter-inverter-l1-power`               | `LegacyInverterInverterL1Power`             | `Number:Power`             | W    | Read       | [173]                          |
| `inverter-inverter-l2-power`               | `LegacyInverterInverterL2Power`             | `Number:Power`             | W    | Read       | [174]                          |
| `inverter-load-frequency`                  | `LegacyInverterLoadFrequency`               | `Number:Frequency`         | Hz   | Read       | [192]                          |
| `inverter-dc-temperature`                  | `LegacyInverterDcTemperature`               | `Number:Temperature`       | °C   | Read       | [90]                           |
| `inverter-ac-temperature`                  | `LegacyInverterAcTemperature`               | `Number:Temperature`       | °C   | Read       | [91]                           |
| `inverter-inverter-id`                     | `LegacyInverterInverterId`                  | `String`                   |      | Read       | [3, 4, 5, 6, 7]                |
| `inverter-communication-board-version-no-` | `LegacyInverterCommunicationBoardVersionNo` | `String`                   |      | Read       | [14]                           |
| `inverter-control-board-version-no-`       | `LegacyInverterControlBoardVersionNo`       | `String`                   |      | Read       | [13]                           |
| `inverter-grid-connected-status`           | `LegacyInverterGridConnectedStatus`         | `String`                   |      | Read       | [194]                          |
| `inverter-work-mode`                       | `LegacyInverterWorkMode`                    | `String`                   |      | Read       | [244, 247]                     |
| `alert-alert`                              | `LegacyAlertAlert`                          | `String`                   |      | Read       | [101, 102, 103, 104, 105, 106] |
| `time-of-use-time-of-use-time-1`           | `LegacyTimeOfUseTimeOfUseTime1`             | `String`                   |      | Read/write | [250]                          |
| `time-of-use-time-of-use-time-2`           | `LegacyTimeOfUseTimeOfUseTime2`             | `String`                   |      | Read/write | [251]                          |
| `time-of-use-time-of-use-time-3`           | `LegacyTimeOfUseTimeOfUseTime3`             | `String`                   |      | Read/write | [252]                          |
| `time-of-use-time-of-use-time-4`           | `LegacyTimeOfUseTimeOfUseTime4`             | `String`                   |      | Read/write | [253]                          |
| `time-of-use-time-of-use-time-5`           | `LegacyTimeOfUseTimeOfUseTime5`             | `String`                   |      | Read/write | [254]                          |
| `time-of-use-time-of-use-time-6`           | `LegacyTimeOfUseTimeOfUseTime6`             | `String`                   |      | Read/write | [255]                          |
| `time-of-use-time-of-use-power-1`          | `LegacyTimeOfUseTimeOfUsePower1`            | `Number:Power`             | W    | Read/write | [256]                          |
| `time-of-use-time-of-use-power-2`          | `LegacyTimeOfUseTimeOfUsePower2`            | `Number:Power`             | W    | Read/write | [257]                          |
| `time-of-use-time-of-use-power-3`          | `LegacyTimeOfUseTimeOfUsePower3`            | `Number:Power`             | W    | Read/write | [258]                          |
| `time-of-use-time-of-use-power-4`          | `LegacyTimeOfUseTimeOfUsePower4`            | `Number:Power`             | W    | Read/write | [259]                          |
| `time-of-use-time-of-use-power-5`          | `LegacyTimeOfUseTimeOfUsePower5`            | `Number:Power`             | W    | Read/write | [260]                          |
| `time-of-use-time-of-use-power-6`          | `LegacyTimeOfUseTimeOfUsePower6`            | `Number:Power`             | W    | Read/write | [261]                          |
| `time-of-use-time-of-use-soc-1`            | `LegacyTimeOfUseTimeOfUseSoc1`              | `Number:Dimensionless`     | %    | Read/write | [268]                          |
| `time-of-use-time-of-use-soc-2`            | `LegacyTimeOfUseTimeOfUseSoc2`              | `Number:Dimensionless`     | %    | Read/write | [269]                          |
| `time-of-use-time-of-use-soc-3`            | `LegacyTimeOfUseTimeOfUseSoc3`              | `Number:Dimensionless`     | %    | Read/write | [270]                          |
| `time-of-use-time-of-use-soc-4`            | `LegacyTimeOfUseTimeOfUseSoc4`              | `Number:Dimensionless`     | %    | Read/write | [271]                          |
| `time-of-use-time-of-use-soc-5`            | `LegacyTimeOfUseTimeOfUseSoc5`              | `Number:Dimensionless`     | %    | Read/write | [272]                          |
| `time-of-use-time-of-use-soc-6`            | `LegacyTimeOfUseTimeOfUseSoc6`              | `Number:Dimensionless`     | %    | Read/write | [273]                          |
| `time-of-use-time-of-use-enable-1`         | `LegacyTimeOfUseTimeOfUseEnable1`           | `Number`                   |      | Read       | [274]                          |
| `time-of-use-time-of-use-enable-2`         | `LegacyTimeOfUseTimeOfUseEnable2`           | `Number`                   |      | Read       | [275]                          |
| `time-of-use-time-of-use-enable-3`         | `LegacyTimeOfUseTimeOfUseEnable3`           | `Number`                   |      | Read       | [276]                          |
| `time-of-use-time-of-use-enable-4`         | `LegacyTimeOfUseTimeOfUseEnable4`           | `Number`                   |      | Read       | [277]                          |
| `time-of-use-time-of-use-enable-5`         | `LegacyTimeOfUseTimeOfUseEnable5`           | `Number`                   |      | Read       | [278]                          |
| `time-of-use-time-of-use-enable-6`         | `LegacyTimeOfUseTimeOfUseEnable6`           | `Number`                   |      | Read       | [279]                          |
| `time-of-use-time-of-use`                  | `LegacyTimeOfUseTimeOfUse`                  | `String`                   |      | Read       | [248]                          |
