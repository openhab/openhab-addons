# Salus Binding

The Salus Binding facilitates seamless integration between openHAB and [Salus Cloud](https://eu.salusconnect.io/).

For years, SALUS Controls has been at the forefront of designing building automation solutions for the heating industry.
Our commitment to innovation has resulted in modern, efficient solutions to control various heating systems. With
extensive experience, we accurately identify user needs and introduce products that precisely meet those needs.

## Supported Things

- **`salus-cloud-bridge`**: This bridge connects to Salus Cloud. Multiple bridges are supported for those with multiple
  accounts.
- **`salus-aws-bridge`**: This bridge connects to AWS Salus Cloud. Multiple bridges are supported for those with multiple accounts.
- **`salus-device`**: A generic Salus device that exposes all properties (as channels) from the Cloud without any
  modifications.
- **`salus-it600-device`**:  A temperature controller with extended capabilities.

## Discovery

After adding a bridge, all connected devices can be automatically discovered from Salus Cloud. The type of device is
assumed automatically based on the `oem_model`.

## Thing Configuration

### `salus-cloud-bridge` Thing Configuration

| Name                      | Type              | Description                                 | Default                        | Required | Advanced |
|---------------------------|-------------------|---------------------------------------------|--------------------------------|----------|----------|
| username                  | text              | Username/email to log in to Salus Cloud     | N/A                            | yes      | no       |
| password                  | text              | Password to log in to Salus Cloud           | N/A                            | yes      | no       |
| url                       | text              | URL to Salus Cloud                          | <https://eu.salusconnect.io>   | no       | yes      |
| refreshInterval           | integer (seconds) | Refresh time in seconds                     | 30                             | no       | yes      |
| propertiesRefreshInterval | integer (seconds) | How long device properties should be cached | 5                              | no       | yes      |

### `salus-aws-bridge` Thing Configuration

| Name                      | Type              | Description                                  | Default                        | Required | Advanced |
|---------------------------|-------------------|----------------------------------------------|--------------------------------|----------|----------|
| username                  | text              | Username/email to log in to Salus Cloud      | N/A                            | yes      | no       |
| password                  | text              | Password to log in to Salus Cloud            | N/A                            | yes      | no       |
| url                       | text              | URL to Salus Cloud                           | `https://eu.salusconnect.io`   | no       | yes      |
| refreshInterval           | integer (seconds) | Refresh time in seconds                      | 30                             | no       | yes      |
| propertiesRefreshInterval | integer (seconds) | How long device properties should be cached  | 5                              | no       | yes      |
| userPoolId                | text              |                                              | XGRz3CgoY                  | no       | yes      |
| clientId                  | text              | The app client ID                            | 4pk5efh3v84g5dav43imsv4fbj | no       | yes      |
| region                    | text              | Region with which the SDK should communicate | eu-central-1               | no       | yes      |
| companyCode               | text              |                                              | salus-eu                   | no       | yes      |
| awsService                | text              |                                              | a24u3z7zzwrtdl-ats         | no       | yes      |

### `salus-device` and `salus-it600-device` Thing Configuration

| Name | Type | Description              | Default | Required | Advanced |
|------|------|--------------------------|---------|----------|----------|
| dsn  | text | ID in Salus cloud system | N/A     | yes      | no       |

## Channels

### `salus-device` Channels

| Channel                       | Type   | Read/Write | Description            |
|-------------------------------|--------|------------|------------------------|
| generic-output-channel        | String | RO         | Generic channel        |
| generic-input-channel         | String | RW         | Generic channel        |
| generic-output-bool-channel   | Switch | RO         | Generic bool channel   |
| generic-input-bool-channel    | Switch | RW         | Generic bool channel   |
| generic-output-number-channel | Number | RO         | Generic number channel |
| generic-input-number-channel  | Number | RW         | Generic number channel |
| temperature-output-channel    | Number | RO         | Temperature channel    |
| temperature-input-channel     | Number | RW         | Temperature channel    |

#### `x100` Channels

If a property from Salus Cloud ends with `x100`, in the binding, the value is divided by `100`, and the `x100` suffix is
removed.

### `salus-it600-device` Channels

| Channel                    | Type               | Read/Write | Description                                                                                                                                                                                                                                                                                     |
|----------------------------|--------------------|------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| temperature         | Number:Temperature | RO         | Current temperature in the room                                                                                                                                                                                                                                                                 |
| expected-temperature | Number:Temperature | RW         | Sets the desired temperature in the room                                                                                                                                                                                                                                                        |
| work-type | String             | RW         | Sets the work type for the device. OFF - device is turned off MANUAL - schedules are turned off, following a manual temperature set, AUTOMATIC - schedules are turned on, following schedule, TEMPORARY_MANUAL - schedules are turned on, following manual temperature until the next schedule. |
| running-state     | Switch             | RO         | Is the device running |

## Full Example

### salus-cloud-bridge

```yaml
UID: salus:salus-cloud-bridge:01f3a5bff0
label: Salus Cloud
thingTypeUID: salus:salus-cloud-bridge
configuration:
  password: qwerty123
  propertiesRefreshInterval: 5
  refreshInterval: 30
  url: https://eu.salusconnect.io
  username: joe.doe@abc.xyz
```

### salus-device

```yaml
UID: salus:salus-device:01f3a5bff0:1619a6f927
label: Salus Binding Thing
thingTypeUID: salus:salus-device
configuration:
  dsn: VR00ZN00000000
bridgeUID: salus:salus-cloud-bridge:01f3a5bff0
channels:
  - id: ep_9_sAWSReg_Registration
    channelTypeUID: salus:generic-output-number-channel
    label: Registration
    description: null
    configuration: { }
  - id: ep_9_sBasicS_ApplicationVersion_d
    channelTypeUID: salus:generic-output-number-channel
    label: ApplicationVersion_d
    description: null
    configuration: { }
  - id: ep_9_sBasicS_HardwareVersion
    channelTypeUID: salus:generic-output-channel
    label: HardwareVersion
    description: null
    configuration: { }
  - id: ep_9_sBasicS_ManufactureName
    channelTypeUID: salus:generic-output-channel
    label: ManufactureName
    description: null
    configuration: { }
  - id: ep_9_sBasicS_ModelIdentifier
    channelTypeUID: salus:generic-output-channel
    label: ModelIdentifier
    description: null
    configuration: { }
  - id: ep_9_sBasicS_PowerSource
    channelTypeUID: salus:generic-output-number-channel
    label: PowerSource
    description: null
    configuration: { }
  - id: ep_9_sBasicS_SetFactoryDefaultReset
    channelTypeUID: salus:generic-input-bool-channel
    label: SetFactoryDefaultReset
    description: null
    configuration: { }
  - id: ep_9_sBasicS_StackVersion_d
    channelTypeUID: salus:generic-output-number-channel
    label: StackVersion_d
    description: null
    configuration: { }
  - id: ep_9_sGenSche_GenScheTimeStamp
    channelTypeUID: salus:generic-output-channel
    label: GenScheTimeStamp
    description: null
    configuration: { }
  - id: ep_9_sGenSche_GenScheURL
    channelTypeUID: salus:generic-output-channel
    label: GenScheURL
    description: null
    configuration: { }
  - id: ep_9_sGenSche_SetGenScheURL
    channelTypeUID: salus:generic-input-channel
    label: SetGenScheURL
    description: null
    configuration: { }
  - id: ep_9_sGenSche_SetUpdateGenScheURL
    channelTypeUID: salus:generic-input-channel
    label: SetUpdateGenScheURL
    description: null
    configuration: { }
  - id: ep_9_sGenSche_UpdateGenScheStatus
    channelTypeUID: salus:generic-output-number-channel
    label: UpdateGenScheStatus
    description: null
    configuration: { }
  - id: ep_9_sIT600D_DeviceIndex
    channelTypeUID: salus:generic-output-number-channel
    label: DeviceIndex
    description: null
    configuration: { }
  - id: ep_9_sIT600D_SetReboot_d
    channelTypeUID: salus:generic-input-bool-channel
    label: SetReboot_d
    description: null
    configuration: { }
  - id: ep_9_sIT600D_SetUpload_d
    channelTypeUID: salus:generic-input-bool-channel
    label: SetUpload_d
    description: null
    configuration: { }
  - id: ep_9_sIT600D_SyncResponseVersion_d
    channelTypeUID: salus:generic-output-channel
    label: SyncResponseVersion_d
    description: null
    configuration: { }
  - id: ep_9_sIT600D_UploadData_d
    channelTypeUID: salus:generic-output-channel
    label: UploadData_d
    description: null
    configuration: { }
  - id: ep_9_sIT600I_CommandResponse_d
    channelTypeUID: salus:generic-output-channel
    label: CommandResponse_d
    description: null
    configuration: { }
  - id: ep_9_sIT600I_LastMessageLQI_d
    channelTypeUID: salus:generic-output-number-channel
    label: LastMessageLQI_d
    description: null
    configuration: { }
  - id: ep_9_sIT600I_LastMessageRSSI_d
    channelTypeUID: salus:generic-output-number-channel
    label: LastMessageRSSI_d
    description: null
    configuration: { }
  - id: ep_9_sIT600I_Mode
    channelTypeUID: salus:generic-output-number-channel
    label: Mode
    description: null
    configuration: { }
  - id: ep_9_sIT600I_PairedThermostatShortID
    channelTypeUID: salus:generic-output-number-channel
    label: PairedThermostatShortID
    description: null
    configuration: { }
  - id: ep_9_sIT600I_RXError33
    channelTypeUID: salus:generic-output-number-channel
    label: RXError33
    description: null
    configuration: { }
  - id: ep_9_sIT600I_RelayStatus
    channelTypeUID: salus:generic-output-bool-channel
    label: RelayStatus
    description: null
    configuration: { }
  - id: ep_9_sIT600I_SetCommand_d
    channelTypeUID: salus:generic-input-channel
    label: SetCommand_d
    description: null
    configuration: { }
  - id: ep_9_sIT600I_SetReadLastMessageRSSI_d
    channelTypeUID: salus:generic-input-number-channel
    label: SetReadLastMessageRSSI_d
    description: null
    configuration: { }
  - id: ep_9_sIT600I_TRVError01
    channelTypeUID: salus:generic-output-bool-channel
    label: TRVError01
    description: null
    configuration: { }
  - id: ep_9_sIT600I_TRVError22
    channelTypeUID: salus:generic-output-bool-channel
    label: TRVError22
    description: null
    configuration: { }
  - id: ep_9_sIT600I_TRVError23
    channelTypeUID: salus:generic-output-bool-channel
    label: TRVError23
    description: null
    configuration: { }
  - id: ep_9_sIT600I_TRVError30
    channelTypeUID: salus:generic-output-bool-channel
    label: TRVError30
    description: null
    configuration: { }
  - id: ep_9_sIT600I_TRVError31
    channelTypeUID: salus:generic-output-bool-channel
    label: TRVError31
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_AllowAdjustSetpoint
    channelTypeUID: salus:generic-output-number-channel
    label: AllowAdjustSetpoint
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_AllowUnlockFromDevice
    channelTypeUID: salus:generic-output-number-channel
    label: AllowUnlockFromDevice
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_AutoCoolingSetpoint
    channelTypeUID: salus:temperature-output-channel
    label: AutoCoolingSetpoint
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_AutoCoolingSetpoint_a
    channelTypeUID: salus:temperature-output-channel
    label: AutoCoolingSetpoint_a
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_AutoHeatingSetpoint
    channelTypeUID: salus:temperature-output-channel
    label: AutoHeatingSetpoint
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_AutoHeatingSetpoint_a
    channelTypeUID: salus:temperature-output-channel
    label: AutoHeatingSetpoint_a
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_BatteryLevel
    channelTypeUID: salus:generic-output-number-channel
    label: BatteryLevel
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_CloudOverride
    channelTypeUID: salus:generic-output-number-channel
    label: CloudOverride
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_CloudySetpoint
    channelTypeUID: salus:generic-output-number-channel
    label: CloudySetpoint
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_CoolingControl
    channelTypeUID: salus:generic-output-number-channel
    label: CoolingControl
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_CoolingSetpoint
    channelTypeUID: salus:temperature-output-channel
    label: CoolingSetpoint
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_CoolingSetpoint_a
    channelTypeUID: salus:temperature-output-channel
    label: CoolingSetpoint_a
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_DaylightSaving_d
    channelTypeUID: salus:generic-output-number-channel
    label: DaylightSaving_d
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_DelayStart
    channelTypeUID: salus:generic-output-number-channel
    label: DelayStart
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_Error01
    channelTypeUID: salus:generic-output-bool-channel
    label: Error01
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_Error02
    channelTypeUID: salus:generic-output-bool-channel
    label: Error02
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_Error03
    channelTypeUID: salus:generic-output-bool-channel
    label: Error03
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_Error04
    channelTypeUID: salus:generic-output-bool-channel
    label: Error04
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_Error05
    channelTypeUID: salus:generic-output-bool-channel
    label: Error05
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_Error06
    channelTypeUID: salus:generic-output-bool-channel
    label: Error06
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_Error07
    channelTypeUID: salus:generic-output-bool-channel
    label: Error07
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_Error07TRVIndex
    channelTypeUID: salus:generic-output-number-channel
    label: Error07TRVIndex
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_Error08
    channelTypeUID: salus:generic-output-bool-channel
    label: Error08
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_Error09
    channelTypeUID: salus:generic-output-bool-channel
    label: Error09
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_Error21
    channelTypeUID: salus:generic-output-bool-channel
    label: Error21
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_Error22
    channelTypeUID: salus:generic-output-bool-channel
    label: Error22
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_Error23
    channelTypeUID: salus:generic-output-bool-channel
    label: Error23
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_Error24
    channelTypeUID: salus:generic-output-bool-channel
    label: Error24
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_Error25
    channelTypeUID: salus:generic-output-bool-channel
    label: Error25
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_Error30
    channelTypeUID: salus:generic-output-bool-channel
    label: Error30
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_Error31
    channelTypeUID: salus:generic-output-bool-channel
    label: Error31
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_Error32
    channelTypeUID: salus:generic-output-bool-channel
    label: Error32
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_FloorCoolingMax
    channelTypeUID: salus:temperature-output-channel
    label: FloorCoolingMax
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_FloorCoolingMin
    channelTypeUID: salus:temperature-output-channel
    label: FloorCoolingMin
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_FloorHeatingMax
    channelTypeUID: salus:temperature-output-channel
    label: FloorHeatingMax
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_FloorHeatingMin
    channelTypeUID: salus:temperature-output-channel
    label: FloorHeatingMin
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_FrostSetpoint
    channelTypeUID: salus:temperature-output-channel
    label: FrostSetpoint
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_GroupNumber
    channelTypeUID: salus:generic-output-number-channel
    label: GroupNumber
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_HeatingControl
    channelTypeUID: salus:generic-output-number-channel
    label: HeatingControl
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_HeatingSetpoint
    channelTypeUID: salus:temperature-output-channel
    label: HeatingSetpoint
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_HeatingSetpoint_a
    channelTypeUID: salus:temperature-output-channel
    label: HeatingSetpoint_a
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_HoldType
    channelTypeUID: salus:generic-output-number-channel
    label: HoldType
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_HoldType_a
    channelTypeUID: salus:generic-output-number-channel
    label: HoldType_a
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_LocalTemperature
    channelTypeUID: salus:temperature-output-channel
    label: LocalTemperature
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_LockKey
    channelTypeUID: salus:generic-output-number-channel
    label: LockKey
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_LockKey_a
    channelTypeUID: salus:generic-output-number-channel
    label: LockKey_a
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_MaxCoolSetpoint
    channelTypeUID: salus:temperature-output-channel
    label: MaxCoolSetpoint
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_MaxHeatSetpoint
    channelTypeUID: salus:temperature-output-channel
    label: MaxHeatSetpoint
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_MaxHeatSetpoint_a
    channelTypeUID: salus:temperature-output-channel
    label: MaxHeatSetpoint_a
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_MinCoolSetpoint
    channelTypeUID: salus:temperature-output-channel
    label: MinCoolSetpoint
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_MinCoolSetpoint_a
    channelTypeUID: salus:temperature-output-channel
    label: MinCoolSetpoint_a
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_MinHeatSetpoint
    channelTypeUID: salus:temperature-output-channel
    label: MinHeatSetpoint
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_MinTurnOffTime
    channelTypeUID: salus:generic-output-number-channel
    label: MinTurnOffTime
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_MoonSetpoint
    channelTypeUID: salus:generic-output-number-channel
    label: MoonSetpoint
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_OUTSensorProbe
    channelTypeUID: salus:generic-output-number-channel
    label: OUTSensorProbe
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_OUTSensorType
    channelTypeUID: salus:generic-output-number-channel
    label: OUTSensorType
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_PairedTRVShortID
    channelTypeUID: salus:generic-output-channel
    label: PairedTRVShortID
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_PairedWCNumber
    channelTypeUID: salus:generic-output-number-channel
    label: PairedWCNumber
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_PipeTemperature
    channelTypeUID: salus:temperature-output-channel
    label: PipeTemperature
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_ProgramOperationMode
    channelTypeUID: salus:generic-output-number-channel
    label: ProgramOperationMode
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_RunningMode
    channelTypeUID: salus:generic-output-number-channel
    label: RunningMode
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_RunningState
    channelTypeUID: salus:generic-output-number-channel
    label: RunningState
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_Schedule
    channelTypeUID: salus:generic-output-channel
    label: Schedule
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_ScheduleOffset_x10
    channelTypeUID: salus:generic-output-number-channel
    label: ScheduleOffset_x10
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_ScheduleType
    channelTypeUID: salus:generic-output-number-channel
    label: ScheduleType
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_SetAllowAdjustSetpoint
    channelTypeUID: salus:generic-input-number-channel
    label: SetAllowAdjustSetpoint
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_SetAllowUnlockFromDevice
    channelTypeUID: salus:generic-input-number-channel
    label: SetAllowUnlockFromDevice
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_SetAutoCoolingSetpoint
    channelTypeUID: salus:temperature-input-channel
    label: SetAutoCoolingSetpoint
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_SetAutoHeatingSetpoint
    channelTypeUID: salus:temperature-input-channel
    label: SetAutoHeatingSetpoint
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_SetCloudOverride
    channelTypeUID: salus:generic-input-number-channel
    label: SetCloudOverride
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_SetCoolingControl
    channelTypeUID: salus:generic-input-number-channel
    label: SetCoolingControl
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_SetCoolingSetpoint
    channelTypeUID: salus:temperature-input-channel
    label: SetCoolingSetpoint
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_SetDelayStart
    channelTypeUID: salus:generic-input-number-channel
    label: SetDelayStart
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_SetFloorCoolingMin
    channelTypeUID: salus:temperature-input-channel
    label: SetFloorCoolingMin
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_SetFloorHeatingMax
    channelTypeUID: salus:temperature-input-channel
    label: SetFloorHeatingMax
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_SetFloorHeatingMin
    channelTypeUID: salus:temperature-input-channel
    label: SetFloorHeatingMin
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_SetFrostSetpoint
    channelTypeUID: salus:temperature-input-channel
    label: SetFrostSetpoint
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_SetHeatingControl
    channelTypeUID: salus:generic-input-number-channel
    label: SetHeatingControl
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_SetHeatingSetpoint
    channelTypeUID: salus:temperature-input-channel
    label: SetHeatingSetpoint
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_SetHoldType
    channelTypeUID: salus:generic-input-number-channel
    label: SetHoldType
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_SetLockKey
    channelTypeUID: salus:generic-input-number-channel
    label: SetLockKey
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_SetMaxHeatSetpoint
    channelTypeUID: salus:temperature-input-channel
    label: SetMaxHeatSetpoint
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_SetMinCoolSetpoint
    channelTypeUID: salus:temperature-input-channel
    label: SetMinCoolSetpoint
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_SetMinTurnOffTime
    channelTypeUID: salus:generic-input-number-channel
    label: SetMinTurnOffTime
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_SetOUTSensorProbe
    channelTypeUID: salus:generic-input-number-channel
    label: SetOUTSensorProbe
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_SetOUTSensorType
    channelTypeUID: salus:generic-input-number-channel
    label: SetOUTSensorType
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_SetPairedTRVShortID
    channelTypeUID: salus:generic-input-channel
    label: SetPairedTRVShortID
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_SetScheduleOffset_x10
    channelTypeUID: salus:generic-input-number-channel
    label: SetScheduleOffset_x10
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_SetShutOffDisplay
    channelTypeUID: salus:generic-input-number-channel
    label: SetShutOffDisplay
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_SetSystemMode
    channelTypeUID: salus:generic-input-number-channel
    label: SetSystemMode
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_SetTemperatureDisplayMode
    channelTypeUID: salus:generic-input-number-channel
    label: SetTemperatureDisplayMode
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_SetTemperatureOffset
    channelTypeUID: salus:generic-input-number-channel
    label: SetTemperatureOffset
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_SetTimeFormat24Hour
    channelTypeUID: salus:generic-input-number-channel
    label: SetTimeFormat24Hour
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_SetValveProtection
    channelTypeUID: salus:generic-input-number-channel
    label: SetValveProtection
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_ShutOffDisplay
    channelTypeUID: salus:generic-output-number-channel
    label: ShutOffDisplay
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_Status_d
    channelTypeUID: salus:generic-output-channel
    label: Status_d
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_SunnySetpoint
    channelTypeUID: salus:generic-output-number-channel
    label: SunnySetpoint
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_SyncResponseDST_d
    channelTypeUID: salus:generic-output-number-channel
    label: SyncResponseDST_d
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_SyncResponseTimeOffset_d
    channelTypeUID: salus:generic-output-number-channel
    label: SyncResponseTimeOffset_d
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_SyncResponseTimeZone_d
    channelTypeUID: salus:generic-output-number-channel
    label: SyncResponseTimeZone_d
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_SystemMode
    channelTypeUID: salus:generic-output-number-channel
    label: SystemMode
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_SystemMode_a
    channelTypeUID: salus:generic-output-number-channel
    label: SystemMode_a
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_TemperatureDisplayMode
    channelTypeUID: salus:generic-output-number-channel
    label: TemperatureDisplayMode
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_TemperatureOffset
    channelTypeUID: salus:generic-output-number-channel
    label: TemperatureOffset
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_TimeFormat24Hour
    channelTypeUID: salus:generic-output-number-channel
    label: TimeFormat24Hour
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_TimeZone_d
    channelTypeUID: salus:generic-output-number-channel
    label: TimeZone_d
    description: null
    configuration: { }
  - id: ep_9_sIT600TH_ValveProtection
    channelTypeUID: salus:generic-output-number-channel
    label: ValveProtection
    description: null
    configuration: { }
  - id: ep_9_sIdentiS_IdentifyTime_d
    channelTypeUID: salus:generic-output-number-channel
    label: IdentifyTime_d
    description: null
    configuration: { }
  - id: ep_9_sIdentiS_SetIndicator
    channelTypeUID: salus:generic-input-number-channel
    label: SetIndicator
    description: null
    configuration: { }
  - id: ep_9_sIdentiS_SetReadIdentifyTime_d
    channelTypeUID: salus:generic-input-bool-channel
    label: SetReadIdentifyTime_d
    description: null
    configuration: { }
  - id: ep_9_sOTA_OTADisableTime
    channelTypeUID: salus:generic-output-channel
    label: OTADisableTime
    description: null
    configuration: { }
  - id: ep_9_sOTA_OTAFirmwareURL_d
    channelTypeUID: salus:generic-output-channel
    label: OTAFirmwareURL_d
    description: null
    configuration: { }
  - id: ep_9_sOTA_OTAStatus_d
    channelTypeUID: salus:generic-output-number-channel
    label: OTAStatus_d
    description: null
    configuration: { }
  - id: ep_9_sOTA_SetOTADisableTime
    channelTypeUID: salus:generic-input-channel
    label: SetOTADisableTime
    description: null
    configuration: { }
  - id: ep_9_sOTA_SetOTAFirmwareURL_d
    channelTypeUID: salus:generic-input-channel
    label: SetOTAFirmwareURL_d
    description: null
    configuration: { }
  - id: ep_9_sZDO_DeviceName
    channelTypeUID: salus:generic-output-channel
    label: DeviceName
    description: null
    configuration: { }
  - id: ep_9_sZDO_EUID
    channelTypeUID: salus:generic-output-channel
    label: EUID
    description: null
    configuration: { }
  - id: ep_9_sZDO_FirmwareVersion
    channelTypeUID: salus:generic-output-channel
    label: FirmwareVersion
    description: null
    configuration: { }
  - id: ep_9_sZDO_GatewayNodeDSN
    channelTypeUID: salus:generic-output-channel
    label: GatewayNodeDSN
    description: null
    configuration: { }
  - id: ep_9_sZDO_LeaveNetwork
    channelTypeUID: salus:generic-output-bool-channel
    label: LeaveNetwork
    description: null
    configuration: { }
  - id: ep_9_sZDO_LeaveRequest_d
    channelTypeUID: salus:generic-output-bool-channel
    label: LeaveRequest_d
    description: null
    configuration: { }
  - id: ep_9_sZDO_SetDeviceName
    channelTypeUID: salus:generic-input-channel
    label: SetDeviceName
    description: null
    configuration: { }
  - id: ep_9_sZDO_SetLeaveNetwork
    channelTypeUID: salus:generic-input-bool-channel
    label: SetLeaveNetwork
    description: null
    configuration: { }
  - id: ep_9_sZDO_SetOnlineRefresh
    channelTypeUID: salus:generic-input-bool-channel
    label: SetOnlineRefresh
    description: null
    configuration: { }
  - id: ep_9_sZDO_SetRefresh_d
    channelTypeUID: salus:generic-input-bool-channel
    label: SetRefresh_d
    description: null
    configuration: { }
  - id: ep_9_sZDO_SetTriggerJoin
    channelTypeUID: salus:generic-input-bool-channel
    label: SetTriggerJoin
    description: null
    configuration: { }
  - id: ep_9_sZDO_ShortID_d
    channelTypeUID: salus:generic-output-number-channel
    label: ShortID_d
    description: null
    configuration: { }
  - id: ep_9_sZDOInfo_AppData_c
    channelTypeUID: salus:generic-output-channel
    label: AppData_c
    description: null
    configuration: { }
  - id: ep_9_sZDOInfo_ConfigureReportResponse
    channelTypeUID: salus:generic-output-channel
    label: ConfigureReportResponse
    description: null
    configuration: { }
  - id: ep_9_sZDOInfo_JoinConfigEnd
    channelTypeUID: salus:generic-output-number-channel
    label: JoinConfigEnd
    description: null
    configuration: { }
  - id: ep_9_sZDOInfo_OnlineStatus_i
    channelTypeUID: salus:generic-output-bool-channel
    label: OnlineStatus_i
    description: null
    configuration: { }
  - id: ep_9_sZDOInfo_ServerData_c
    channelTypeUID: salus:generic-output-channel
    label: ServerData_c
    description: null
    configuration: { }
  - id: ep_9_sZDOInfo_SetAppData_c
    channelTypeUID: salus:generic-input-channel
    label: SetAppData_c
    description: null
    configuration: { }
  - id: ep_9_sZDOInfo_SetConfigureReport
    channelTypeUID: salus:generic-input-channel
    label: SetConfigureReport
    description: null
    configuration: { }
  - id: ep_9_sZDOInfo_zigbeeOTAcontrol_i
    channelTypeUID: salus:generic-input-number-channel
    label: zigbeeOTAcontrol_i
    description: null
    configuration: { }
  - id: ep_9_sZDOInfo_zigbeeOTAfile_i
    channelTypeUID: salus:generic-input-channel
    label: zigbeeOTAfile_i
    description: null
    configuration: { }
  - id: ep_9_sZDOInfo_zigbeeOTArespond_i
    channelTypeUID: salus:generic-input-number-channel
    label: zigbeeOTArespond_i
    description: null
    configuration: { }
```

### salus-it600-device

```yaml
UID: salus:salus-it600-device:01f3a5bff0:VR00ZN000247491
label: Office
thingTypeUID: salus:salus-it600-device
configuration:
  dsn: VR00ZN00000000
  propertyCache: 5
bridgeUID: salus:salus-cloud-bridge:01f3a5bff0
channels:
  - id: temperature
    channelTypeUID: salus:it600-temp-channel
    label: Temperature
    description: Current temperature in room
    configuration: { }
  - id: expected-temperature
    channelTypeUID: salus:it600-expected-temp-channel
    label: Expected Temperature
    description: Sets the desired temperature in room
    configuration: { }
  - id: work-type
    channelTypeUID: salus:it600-work-type-channel
    label: Work Type
    description: Sets the work type for the device. OFF - device is turned off
      MANUAL - schedules are turned off, following a manual temperature set,
      AUTOMATIC - schedules are turned on, following schedule, TEMPORARY_MANUAL
      - schedules are turned on, following manual temperature until next
      schedule.
    configuration: { }
```

## Developer's Note

The Salus API poses challenges, and all coding efforts are a result of reverse engineering. Attempts were made to
contact the Salus Team, but the closed-source nature of the API limited assistance. Consequently, there may be errors in
implementation or channel visibility issues. If you encounter any issues, please report them, and efforts will be made
to address and resolve them.
