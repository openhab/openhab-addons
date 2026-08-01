# Attribute Mapping: VEPUpdate (alt) vs. VehicleStatusUpdate (neu)

Diese Tabelle vergleicht die alten `VehicleAttributeStatus`-Schlüssel (`Constants.MB_KEY_*`, geliefert
über `PushMessage.vepUpdates`, Feld 2) mit den neuen typisierten Feldern aus `VehicleStatusUpdate`
(geliefert über `PushMessage.vehicle_status_updates`, Feld 24, seit App-Version 165-1). Quelle:
`proto/vehicle-events.proto`.

Stand: 2026-08-01.

## 1. Alte Attribute mit neuem Mapping

| Alt (`Constants.MB_KEY_*`) | Alter Key-String | Neues Feld (`VehicleStatusUpdate`) | Neuer Proto-Typ | Status |
|---|---|---|---|---|
| MB_KEY_TIRE_SENSOR_AVAILABLE | tireSensorAvailable | tire_sensor_available | TireSensorAvailableEnumAttribute | gemappt |
| MB_KEY_CHARGE_COUPLER_DC_LOCK_STATUS | chargeCouplerDCLockStatus | charge_coupler_d_c_lock_status | ChargeCouplerLockStatusEnumAttribute | gemappt |
| MB_KEY_CHARGE_COUPLER_DC_STATUS | chargeCouplerDCStatus | charge_coupler_d_c_status | ChargeCouplerStatusEnumAttribute | gemappt |
| MB_KEY_CHARGE_COUPLER_AC_STATUS | chargeCouplerACStatus | charge_coupler_a_c_status | ChargeCouplerStatusEnumAttribute | gemappt |
| MB_KEY_CHARGE_FLAP_DC_STATUS | chargeFlapDCStatus | charge_flap_d_c_status | ChargeFlapStatusEnumAttribute | gemappt |
| MB_KEY_CHARGE_STATUS | chargingstatus | chargingstatus | ChargingstatusEnumAttribute | gemappt |
| MB_KEY_CHARGE_ERROR | chargingErrorDetails | charging_error_details | ChargingErrorDetailsEnumAttribute | gemappt |
| MB_KEY_SERVICEINTERVALDAYS | serviceintervaldays | serviceintervaldays | Int64Attribute | gemappt |
| MB_KEY_TIREWARNINGSRDK | tirewarningsrdk | tirewarningsrdk | TirewarningsrdkEnumAttribute | gemappt |
| MB_KEY_STARTER_BATTERY_STATE | starterBatteryState | starter_battery_state | StarterBatteryStateEnumAttribute | gemappt |
| MB_KEY_FLIP_WINDOW_STATUS | flipWindowStatus | flip_window_status | FlipWindowStatusEnumAttribute | gemappt |
| MB_KEY_WINDOW_STATUS_REAR_BLIND | windowStatusRearBlind | window_status_rear_blind | WindowStatusBlindEnumAttribute | gemappt |
| MB_KEY_WINDOW_STATUS_REAR_LEFT_BLIND | windowStatusRearLeftBlind | window_status_rear_left_blind | WindowStatusBlindEnumAttribute | gemappt |
| MB_KEY_WINDOW_STATUS_REAR_RIGHT_BLIND | windowStatusRearRightBlind | window_status_rear_right_blind | WindowStatusBlindEnumAttribute | gemappt |
| MB_KEY_WINDOWSTATUSREARRIGHT | windowstatusrearright | windowstatusrearright | WindowstatusEnumAttribute | gemappt |
| MB_KEY_WINDOWSTATUSREARLEFT | windowstatusrearleft | windowstatusrearleft | WindowstatusEnumAttribute | gemappt |
| MB_KEY_WINDOWSTATUSFRONTRIGHT | windowstatusfrontright | windowstatusfrontright | WindowstatusEnumAttribute | gemappt |
| MB_KEY_WINDOWSTATUSFRONTLEFT | windowstatusfrontleft | windowstatusfrontleft | WindowstatusEnumAttribute | gemappt |
| MB_KEY_ROOFTOPSTATUS | rooftopstatus | rooftopstatus | RooftopstatusEnumAttribute | gemappt |
| MB_KEY_SUNROOF_STATUS_REAR_BLIND | sunroofStatusRearBlind | sunroof_status_rear_blind | SunroofStatusBlindEnumAttribute | gemappt |
| MB_KEY_SUNROOF_STATUS_FRONT_BLIND | sunroofStatusFrontBlind | sunroof_status_front_blind | SunroofStatusBlindEnumAttribute | gemappt |
| MB_KEY_SUNROOFSTATUS | sunroofstatus | sunroofstatus | SunroofstatusEnumAttribute | gemappt |
| MB_KEY_IGNITIONSTATE | ignitionstate | ignitionstate | IgnitionstateEnumAttribute | gemappt |
| MB_KEY_DOOR_STATUS_OVERALL | doorStatusOverall | door_status_overall | DoorStatusOverallEnumAttribute | gemappt |
| MB_KEY_WINDOW_STATUS_OVERALL | windowStatusOverall | window_status_overall | WindowStatusOverallEnumAttribute | gemappt |
| MB_KEY_DOOR_LOCK_STATUS_OVERALL | doorlockstatusvehicle | doorlockstatusvehicle | DoorlockstatusvehicleEnumAttribute | gemappt |
| MB_KEY_TIRE_MARKER_FRONT_RIGHT | tireMarkerFrontRight | tire_marker_front_right | TireMarkerEnumAttribute | gemappt |
| MB_KEY_TIRE_MARKER_FRONT_LEFT | tireMarkerFrontLeft | tire_marker_front_left | TireMarkerEnumAttribute | gemappt |
| MB_KEY_TIRE_MARKER_REAR_RIGHT | tireMarkerRearRight | tire_marker_rear_right | TireMarkerEnumAttribute | gemappt |
| MB_KEY_TIRE_MARKER_REAR_LEFT | tireMarkerRearLeft | tire_marker_rear_left | TireMarkerEnumAttribute | gemappt |
| MB_KEY_PARKBRAKESTATUS | parkbrakestatus | parkbrakestatus | ParkbrakestatusEnumAttribute | gemappt |
| MB_KEY_PRECOND_NOW | precondNow | precond_now | PrecondNowEnumAttribute | gemappt |
| MB_KEY_PRECOND_SEAT_FRONT_RIGHT | precondSeatFrontRight | precond_seat_front_right | PrecondSeatEnumAttribute | gemappt |
| MB_KEY_PRECOND_SEAT_FRONT_LEFT | precondSeatFrontLeft | precond_seat_front_left | PrecondSeatEnumAttribute | gemappt |
| MB_KEY_PRECOND_SEAT_REAR_RIGHT | precondSeatRearRight | precond_seat_rear_right | PrecondSeatEnumAttribute | gemappt |
| MB_KEY_PRECOND_SEAT_REAR_LEFT | precondSeatRearLeft | precond_seat_rear_left | PrecondSeatEnumAttribute | gemappt |
| MB_KEY_WARNINGBRAKEFLUID | warningbrakefluid | warningbrakefluid | BoolAttribute | gemappt |
| MB_KEY_WARNINGBRAKELININGWEAR | warningbrakeliningwear | warningbrakeliningwear | BoolAttribute | gemappt |
| MB_KEY_WARNINGWASHWATER | warningwashwater | warningwashwater | WarningwashwaterEnumAttribute | gemappt |
| MB_KEY_WARNINGCOOLANTLEVELLOW | warningcoolantlevellow | warningcoolantlevellow | BoolAttribute | gemappt |
| MB_KEY_WARNINGENGINELIGHT | warningenginelight | warningenginelight | BoolAttribute | gemappt |
| MB_KEY_CHARGINGACTIVE | chargingactive | chargingactive | BoolAttribute | gemappt |
| MB_KEY_DOORLOCKSTATUSFRONTRIGHT | doorlockstatusfrontright | doorlockstatusfrontright | DoorlockstatusEnumAttribute | gemappt |
| MB_KEY_DOORLOCKSTATUSFRONTLEFT | doorlockstatusfrontleft | doorlockstatusfrontleft | DoorlockstatusEnumAttribute | gemappt |
| MB_KEY_DOORLOCKSTATUSREARRIGHT | doorlockstatusrearright | doorlockstatusrearright | DoorlockstatusEnumAttribute | gemappt |
| MB_KEY_DOORLOCKSTATUSREARLEFT | doorlockstatusrearleft | doorlockstatusrearleft | DoorlockstatusEnumAttribute | gemappt |
| MB_KEY_DOORLOCKSTATUSDECKLID | doorlockstatusdecklid | doorlockstatusdecklid | DoorlockstatusEnumAttribute | gemappt |
| MB_KEY_DOORLOCKSTATUSGAS | doorlockstatusgas | doorlockstatusgas | DoorlockstatusEnumAttribute | gemappt |
| MB_KEY_TIREPRESSURE_FRONT_LEFT | tirepressureFrontLeft | tirepressure_front_left | DoublePressureAttribute | gemappt |
| MB_KEY_TIREPRESSURE_FRONT_RIGHT | tirepressureFrontRight | tirepressure_front_right | DoublePressureAttribute | gemappt |
| MB_KEY_TIREPRESSURE_REAR_LEFT | tirepressureRearLeft | tirepressure_rear_left | DoublePressureAttribute | gemappt |
| MB_KEY_POSITION_HEADING | positionHeading | position_heading | DoubleAttribute | gemappt |
| MB_KEY_TIREPRESSURE_REAR_RIGHT | tirepressureRearRight | tirepressure_rear_right | DoublePressureAttribute | gemappt |
| MB_KEY_ENGINE_HOOD_STATUS | engineHoodStatus | engine_hood_status | EngineHoodStatusEnumAttribute | gemappt |
| MB_KEY_DECKLIDSTATUS | decklidstatus | decklidstatus | DoorstatusEnumAttribute | gemappt |
| MB_KEY_DOORSTATUSREARLEFT | doorstatusrearleft | doorstatusrearleft | DoorstatusEnumAttribute | gemappt |
| MB_KEY_DOORSTATUSREARRIGHT | doorstatusrearright | doorstatusrearright | DoorstatusEnumAttribute | gemappt |
| MB_KEY_DOORSTATUSFRONTLEFT | doorstatusfrontleft | doorstatusfrontleft | DoorstatusEnumAttribute | gemappt |
| MB_KEY_DOORSTATUSFRONTRIGHT | doorstatusfrontright | doorstatusfrontright | DoorstatusEnumAttribute | gemappt |
| MB_KEY_TANKLEVELPERCENT | tanklevelpercent | tanklevelpercent | Int64RatioAttribute | gemappt |
| MB_KEY_ADBLUELEVELPERCENT | tankLevelAdBlue | tank_level_ad_blue | Int64RatioAttribute | gemappt |
| MB_KEY_SOC | soc | soc | Int64RatioAttribute | gemappt |
| MB_KEY_TIRE_PRESS_MEAS_TIMESTAMP | tirePressMeasTimestamp | tire_press_meas_timestamp | Int64Attribute | gemappt |
| MB_KEY_ENDOFCHARGETIME | endofchargetime | endofchargetime | Int64ClockHourAttribute | gemappt |
| MB_KEY_ENDOFCHARGEDAY | endofChargeTimeWeekday | endof_charge_time_weekday | WeekdayEnumAttribute | gemappt |
| MB_KEY_LIQUIDCONSUMPTIONRESET | liquidconsumptionreset | liquidconsumptionreset | DoubleCombustionConsumptionAttribute | gemappt |
| MB_KEY_LIQUIDCONSUMPTIONSTART | liquidconsumptionstart | liquidconsumptionstart | DoubleCombustionConsumptionAttribute | gemappt |
| MB_KEY_ELECTRICCONSUMPTIONRESET | electricconsumptionreset | electricconsumptionreset | DoubleElectricityConsumptionAttribute | gemappt |
| MB_KEY_ELECTRICCONSUMPTIONSTART | electricconsumptionstart | electricconsumptionstart | DoubleElectricityConsumptionAttribute | gemappt |
| MB_KEY_AVERAGE_SPEED_RESET | averageSpeedReset | average_speed_reset | DoubleSpeedAttribute | gemappt |
| MB_KEY_AVERAGE_SPEED_START | averageSpeedStart | average_speed_start | DoubleSpeedAttribute | gemappt |
| MB_KEY_CHARGING_POWER | chargingPower | charging_power | DoubleAttribute | gemappt |
| MB_KEY_DRIVEN_TIME_RESET | drivenTimeReset | driven_time_reset | Int64Attribute | gemappt |
| MB_KEY_DRIVEN_TIME_START | drivenTimeStart | driven_time_start | Int64Attribute | gemappt |
| MB_KEY_DISTANCE_RESET | distanceReset | distance_reset | DoubleDistanceAttribute | gemappt |
| MB_KEY_DISTANCE_START | distanceStart | distance_start | DoubleDistanceAttribute | gemappt |
| MB_KEY_RANGELIQUID | rangeliquid | rangeliquid | Int64DistanceAttribute | gemappt |
| MB_KEY_OVERALL_RANGE | overallRange | overall_range | DoubleDistanceAttribute | gemappt |
| MB_KEY_RANGEELECTRIC | rangeelectric | rangeelectric | Int64DistanceAttribute | gemappt |
| MB_KEY_ODO | odo | odo | Int64DistanceAttribute | gemappt |
| MB_KEY_POSITION_LONG | positionLong | position_long | DoubleAttribute | gemappt |
| MB_KEY_POSITION_LAT | positionLat | position_lat | DoubleAttribute | gemappt |
| MB_KEY_TEMPERATURE_POINTS | temperaturePoints | temperature_points | TemperaturePointsArrayAttribute | gemappt (komplex) |
| MB_KEY_SELECTED_CHARGE_PROGRAM | selectedChargeProgram | selected_charge_program | SelectedChargeProgramEnumAttribute | gemappt |
| MB_KEY_CHARGE_PROGRAMS | chargePrograms | charge_programs | ChargeProgramsArrayAttribute | gemappt (komplex) |
| MB_KEY_MAX_SOC | maxSoc | max_soc | Int64RatioAttribute | gemappt |
| MB_KEY_MAX_SOC_LOWER_LIMIT | maxSocLowerLimit | max_soc_lower_limit | Int64RatioAttribute | gemappt |
| MB_KEY_MAX_SOC_UPPER_LIMIT | maxSocUpperLimit | max_soc_upper_limit | Int64RatioAttribute | gemappt |
| MB_KEY_POSITION_ERROR | vehiclePositionErrorCode | vehicle_position_error_code | VehiclePositionErrorCodeEnumAttribute | gemappt |
| MB_KEY_AUXILIARY_WARNINGS | auxheatwarnings | auxheatwarnings | AuxheatwarningsArrayAttribute | gemappt (komplex) |
| MB_KEY_PRECOND_NOW_ERROR | precondNowError | precond_now_error | PrecondErrorEnumAttribute | gemappt |
| MB_KEY_ECOSCORE_ACCEL | ecoscoreaccel | ecoscoreaccel | Int64RatioAttribute | gemappt |
| MB_KEY_ECOSCORE_CONSTANT | ecoscoreconst | ecoscoreconst | Int64RatioAttribute | gemappt |
| MB_KEY_ECOSCORE_COASTING | ecoscorefreewhl | ecoscorefreewhl | Int64RatioAttribute | gemappt |
| MB_KEY_ECOSCORE_BONUS | ecoscorebonusrange | ecoscorebonusrange | DoubleDistanceAttribute | gemappt |

## 2. Alte Attribute OHNE neues Mapping

Diese Keys stammen nicht aus dem Websocket-Statusstrom, sondern aus dem REST-`/capabilities`-Endpunkt
(`RestApi.getCapabilities`). Sie haben deshalb bewusst **kein** Gegenstück in `VehicleStatusUpdate` -
das ist kein Datenverlust, sondern ein anderer Kanal, der unverändert weiterläuft.

| Alt (`Constants.MB_KEY_*`) | Alter Key-String | Quelle |
|---|---|---|
| MB_KEY_COMMAND_CAPABILITIES | command-capabilities | REST /capabilities |
| MB_KEY_FEATURE_CAPABILITIES | feature-capabilities | REST /capabilities |
| MB_KEY_COMMAND_ZEV_PRECONDITION_CONFIGURE_SEATS | commandZevPreconditionConfigureSeats | REST /capabilities |
| MB_KEY_COMMAND_SUNROOF_OPEN | commandSunroofOpen | REST /capabilities |
| MB_KEY_COMMAND_CHARGE_PROGRAM_CONFIGURE | commandChargeProgramConfigure | REST /capabilities |
| MB_KEY_COMMAND_SIGPOS_START | commandSigposStart | REST /capabilities |
| MB_KEY_FEATURE_AUX_HEAT | featureAuxHeat | REST /capabilities |
| MB_KEY_COMMAND_ZEV_PRECONDITIONING_START | commandZevPreconditioningStart | REST /capabilities |
| MB_KEY_COMMAND_ZEV_PRECONDITION_CONFIGURE | commandZevPreconditionConfigure | REST /capabilities |
| MB_KEY_COMMAND_DOORS_LOCK | commandDoorsLock | REST /capabilities |
| MB_KEY_COMMAND_WINDOWS_OPEN | commandWindowsOpen | REST /capabilities |
| MB_KEY_COMMAND_ENGINE_START | commandEngineStart | REST /capabilities |

## 3. Neue Attribute ohne altes Pendant

`VehicleStatusUpdate` hat 275 Felder, die alte Seite kennt nur ~65 vehicle-status-relevante Keys - der
Rest (~200 Felder) ist entweder komplett neu oder war vorher nur intern/verdeckt in komplexeren
Objekt-Typen enthalten. Gruppiert nach Themenbereich:

**AMG / Fahrmodus:** amg_stage_mode_error, amg_stage_mode_state, teenage_driving_mode,
valet_driving_mode, performance_limitation_mode_status

**Auxheat:** auxheat_active, auxheatruntime, auxheattime1/2/3

**Batterie / SoH:** battery_health, hv_battery_state_of_health,
hv_battery_state_of_health_distance_update, hv_battery_state_of_health_reserve_capacity,
hv_battery_thermal_propagation_event, soh_calibration_notifications, soh_calibration_planned,
soh_calibration_request, soh_calibration_required, soh_calibration_state, soh_charge_time_extension,
soh_favorable_conditions, soc_calibration_request

**Bidirektionales Laden / Ladeplanung:** bidirectional_charging_active, charging_break_clock_timer,
charging_power_eco_limit, charging_power_restriction, charging_prediction_departure_time,
charging_prediction_full_soc, charging_prediction_max_soc, charging_prediction_min_soc,
charging_prediction_target_soc, charging_schedule_active, charging_schedule_requested,
charging_timer, dc_charging_profile, gained_range_since_start_of_charging, smart_charging,
smart_charging_at_departure, smart_charging_at_departure2, target_soc, departuretimesoc,
departure_time_mode, departure_time_weekday, departuretime, next_departure_time,
next_departure_time_weekday

**Ladefehler / Ladestecker:** charging_compatibility_error, charging_coupler_error_details,
charging_error_impossible_change_to400_v, charging_error_impossible_change_to800_v,
charging_error_vehicle_no_support400_v, charging_flap_error_details, charging_stop_error_details,
charge_flaps, charge_inlets, evse_pairing_state

**Kindersitz-Erkennung:** child_presence_detection_warning_counter,
child_presence_detection_warning_last_event, child_presence_detection_warning_level,
child_presence_detection_occupancy_status (neu, kein altes Pendant)

**Reifen (erweitert):** tire_marker_inner_rear_left, tire_marker_inner_rear_right,
tire_pressure_inner_rear_left, tire_pressure_inner_rear_right, tire_warning_level_prw

**Reichweite / Verbrauch (erweitert):** distance_electrical_reset/start, distance_gas_reset/start,
distance_z_e_reset/start, driven_time_z_e_reset/start, electric_ratio_reset/start,
electrical_range_skip_indication, liquid_range_skip_indication, range_ad_blue,
range_electric_wltp, maxrange, gasconsumptionreset/start, gas_tank_level, gas_tank_level_percent,
gas_tank_range, ev_range_assist_drive_on_s_o_c, ev_range_assist_drive_on_time

**Diebstahlschutz / Überwachung:** exterior_monitoring_last_event, exterior_monitoring_status,
interior_monitoring_last_event, interior_monitoring_status, interior_protection_activation_status,
interior_protection_selection_status, interior_protection_sensor_status, tow_protection_activation_status,
tow_protection_selection_status, tow_protection_sensor_status, panic_alarm_active,
last_theft_warning, last_theft_warning_reason, theft_alarm_active, theft_system_armed,
vehicle_theft_alarm_inactive_reason, emergency_power_supply

**Parkassistent / Kollision:** park_collision_activation_status, park_collision_inactive_reason,
park_collision_picture_transfer_status, park_collision_selection_status, park_event_level,
park_event_picture_selection_status, park_event_picture_transmission_status,
park_event_sensor_status, park_event_type, last_park_event, last_park_event_id,
last_park_event_not_confirmed, picture_recording_status, picture_transfer_selection_status

**Vorklimatisierung (erweitert):** precond_active, precond_at_departure_disable, precond_duration,
precond_error, precond_now_error (bereits gemappt), precond_operability_state,
precond_operating_mode, precond_state, precondatdeparture, hv_battery_precond_availability,
hv_battery_precond_duration, hv_battery_precond_request_state, hv_battery_precond_state

**Fernstart:** remote_start_active, remote_start_endtime, remote_start_temperature,
remote_update_start_status

**Reifendruck-Sonstiges:** filter_particle_loading, oil_level, oil_warning_level

**Sonstige Fahrzeuggesundheit / Verbindung:** vehicle_data_connection_state, vehicle_health_status,
tcu_connection_state_low_channel, tcu_thermo_shut_down, key_activation_state,
keyline_activation_state

**Sitze / Komfort:** seatballet_position_state (neu), pet_mode (neu)

**Klima/Sonstiges HU:** language_h_u, speed_unit_from_i_c, temperature_unit_h_u, time_format_h_u,
tracking_state_h_u, weekly_profile, weekly_set_h_u

**Sonnendach:** sunroof_event, sunroof_event_active

**Fahrzeugidentifikation / Meta:** fin_or_vin, full_update

Diese Felder haben aktuell **keine Channels** im Binding. Ob/welche davon sich lohnen, ist eine
separate Priorisierungsfrage (Folge-Arbeit).

## 4. Umsetzungsstatus im Code

- Kein separater Konverter mehr: `Mapper.fromVehicleStatusUpdate(VehicleStatusUpdate)` in
  `internal/utils/Mapper.java` baut die `Map<String, VehicleAttributeStatus>` direkt, mit denselben
  `MB_KEY_*`-Konstanten, die `Mapper.getChannelStateMap()` (ebenfalls in dieser Klasse) ohnehin schon
  kennt. `AccountHandler.handleMessage()` ruft diese Methode bei `pm.hasVehicleStatusUpdates()`
  direkt auf, baut daraus ein `VEPUpdate` und übergibt es an die **bestehende**
  `distributeVepUpdates()`-Pipeline - keine neuen Channels, keine Änderung an `VehicleHandler`,
  keine separate Klasse.
- Es gibt bewusst **keine neuen `MB_KEY_*`-Konstanten** für die VSU-Attribute: `Mapper`s
  `getChannelStateMap()`-Switch ist bereits nach den bestehenden Konstanten aufgebaut (Channel-Zuordnung,
  Einheiten-Handling, Spezialfälle wie invertierte Lock-Booleans). Neue, parallele Konstanten hätten
  bedeutet, denselben Switch mit ~90 weiteren `case`-Zweigen zu duplizieren, ohne fachlichen Nutzen -
  die alten Schlüssel sind reine interne Dictionary-Keys, kein extern sichtbares Protokoll, und
  funktionieren für Daten aus `VEPUpdate` und `VehicleStatusUpdate` gleichermaßen.
- Enum-Felder werden über `getValueValue()` gelesen - das ist die im `.proto` **explizit deklarierte**
  Zahl des jeweiligen Enum-Werts (z. B. `IGNITIONSTATE_ON = 4;` mit Kommentar `// value 3 not defined`
  in `vehicle-events.proto`), keine Listenposition. Gegengecheckt gegen mbapi2020s generierte
  `vsu_enums.py`-Tabelle (u. a. `IGNITIONSTATE_ON: 4`, `IGNITIONSTATE_ACCESSORY: 2`,
  `DOORLOCKSTATUSVEHICLE_EXTERNAL_LOCKED: 2`, `TIRE_SENSOR_AVAILABLE_ALL_MISSING: 2`) - die Werte
  stimmen exakt überein. mbapi2020 braucht dort trotzdem eine Name-zu-Int-Tabelle, weil sein
  Python-Pfad die Protobuf-Nachricht vorher in ein JSON/Dict umwandelt und dabei Enums zu ihrem
  String-Namen werden; das Binding arbeitet direkt mit den generierten Java-Klassen und hat dieses
  Problem nicht. Damit ist die Umrechnung fest, keine offene Verifikation mehr nötig.
- Abschnitt 3 (neue Felder) ist **nicht** implementiert - dafür existieren noch keine Channels.
- **Aufräumen:** `internal/api/VehicleStatusUpdateConverter.java` ist ein Rest aus einem früheren
  Zwischenstand (separate Konverterklasse) und wird von nirgends mehr referenziert - bitte die Datei
  von Hand löschen, das Sandbox-Terminal in dieser Session konnte das nicht zuverlässig selbst tun.

### Komplexe Array-Typen (temperature_points, charge_programs, auxheatwarnings)

Analysiert anhand eines Live-Debug-Dumps (2026-08-01, `Mapper.fromVehicleStatusUpdate()` mit
`LOGGER.debug`), dann implementiert in `Mapper.putTemperaturePoints/putChargePrograms/putAuxheatwarnings()`.
`VehicleHandler` liest diese drei Keys nicht über `getChannelStateMap()`, sondern direkt per
`value.getTemperaturePointsValue()` / `value.getChargeProgramsValue()` (Spezialbehandlung, siehe
Kommentar bei `MB_KEY_TEMPERATURE_POINTS` in `Mapper.init()`), deshalb bauen diese drei Methoden die
passenden alten oneof-Varianten von `VehicleAttributeStatus` statt eines einfachen `int_value`/`double_value`.

| Feld | Live-Beispiel | Altes Ziel | Erkenntnis |
|---|---|---|---|
| `temperature_points` | `value { zone: FRONT_CENTER temperature { value: 21.0 unit: CELSIUS display_value: "21.0" } }` | `TemperaturePointsValue` (oneof case 20) | Neues `Zone`-Enum ist 0-basiert (`FRONT_LEFT=0` ... `REAR_2_RIGHT=8`), altes Format erwartet einen String (`"frontLeft"` etc., laut Kommentar in `TemperaturePointsValue`). Mapping-Tabelle in `Mapper.zoneToLegacyString()`. Temperatur kommt neu als `DoubleTemperatureAttribute` (value/unit/display_value) statt rohem double - `getValue()`/`getDisplayValue()` werden direkt übernommen. `active` (bool) wird in die alte `google.protobuf.BoolValue`-Wrapperform gepackt. |
| `charge_programs` | `value { max_soc: 80 } value { charge_program: INSTANT_CHARGE_PROGRAM max_soc: 100 } ...` | `ChargeProgramsValue` (oneof case 31) | `ChargeProgramsArrayAttribute` nutzt exakt dieselbe `ChargeProgramParameters`-Message wie das alte `ChargeProgramsValue` (`repeated ChargeProgramParameters` in beiden) - reines Passthrough, keine Feldkonvertierung nötig. |
| `auxheatwarnings` | `metadata { status: VALUE_NOT_AVAILABLE }` (Liste leer, kein Beispielwert mit echter Warnung) | `int_value` ("Number Status"-Fall in `getChannelStateMap()`) | Neues Format liefert eine **Liste** von `Auxheatwarning`-Enum-Werten (`NONE=0`, `CONFIRMATION=1`, `CONFIRMATION_2=2`) statt eines einzelnen Codes. Umgesetzt als "schwerwiegendster Wert der Liste, 0 falls leer" (`Mapper.putAuxheatwarnings()`), um den bestehenden Number-Channel sinnvoll zu befüllen. **Nicht gegen echte Warnung verifiziert** - im Log war die Liste leer/`status: VALUE_NOT_AVAILABLE`. Bei Gelegenheit mit einer aktiven Standheizungs-Warnung gegenchecken. |
