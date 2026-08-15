# Tasks: Remove VEPUpdate as Internal Update Carrier

## 1. Dead code removal

- [x] 1.1 Remove `RestApi.restGetVehicleAttributes()` and its `vehicleAttributesEndpoint` field
- [x] 1.2 Remove now-unused imports in `RestApi` (`VehicleEvents`, `VEPUpdate`, `InvalidProtocolBufferException`, `HttpStatus` if no longer referenced)
- [x] 1.3 Delete `TestUpdate` (manual debug utility, sole caller of 1.1) — deleted by the requesting user
  directly on the mounted project folder (sandbox delete restriction did not apply to human filesystem
  access).

## 2. Internal carrier type

- [x] 2.1 Add a new internal record carrying the full/partial flag and the attribute map, replacing `VEPUpdate`'s role between `AccountHandler` and `VehicleHandler` (`VehicleStatusAttributes`, with a defensive `Map.copyOf()` in its compact constructor)
- [x] 2.2 `AccountHandler`: build the new type from both the legacy push branch and the typed push branch (via the existing `Mapper.fromVehicleStatusUpdate()` conversion); renamed `vepUpdateMap` → `vehicleStatusMap`, `distributeVepUpdates` → `distributeVehicleUpdates`
- [x] 2.3 `VehicleHandler`: changed the event queue, `enqueueUpdate`, and `handleUpdate` to the new type
- [x] 2.4 `Utils.proto2Json`: changed signature to take the attribute map directly instead of a `VEPUpdate`

## 3. Documentation correction

- [x] 3.1 Correct the Javadoc on `Mapper.fromVehicleStatusUpdate()` — temperature points, charge programs, and auxiliary warnings are already converted, contrary to the previous comment

## 4. Test updates

- [x] 4.1 `ProtoConverter.json2Proto()`: returns the new internal type instead of `VEPUpdate`
- [x] 4.2 `VehicleHandlerTest`: all call sites updated; `testChargeProgramUpdate` now converts the raw legacy `PushMessage`/`VEPUpdate` it builds into `VehicleStatusAttributes` before calling `enqueueUpdate`
- [x] 4.3 `ProtoTest.testProtoBlob2Json`: `Utils.proto2Json` call site passes `vepUpdate.getAttributesMap()`; roundtrip assertion uses `roundTrip.attributes().size()`

## 5. Verification

- [x] 5.1 Full-text search confirms every remaining `VEPUpdate` reference is one of: a comment/Javadoc, the
  distinct `AcknowledgeVEPUpdatesByVIN`/`VEPUpdatesByVIN` protobuf types, the legacy WebSocket ingress
  branch in `AccountHandler`/`VehicleHandlerTest`, or `ProtoTest`'s wire-format-level tests
  (`vinAndPositionAnaon`, `testProtoBlob2Json`, `testEndChargeTime`), which legitimately still target the
  raw wire format — **superseded by Phase 2 below, all of these are now gone**
- [x] 5.2 `mvn clean install` (or at minimum `mvn test -pl bundles/org.openhab.binding.mercedesme`) — run
  successfully by the requesting user (this environment has no `mvn` binary and no network access to Maven
  Central, so it could not be run from here).

## 6. Phase 2 - full removal (user-requested correction)

- [x] 6.1 `AccountHandler.handleMessage()`: remove the `hasVepUpdates()` branch and the
  `AcknowledgeVEPUpdatesByVIN` import; `hasVehicleStatusUpdates()` is now the sole vehicle-update branch
- [x] 6.2 `ProtoTest`: remove `vinAndPositionAnaon`, `testProtoDecoding`, `testProtoBlob2Json`,
  `testEndChargeTime` (all decoded real `.blob` captures of the now-unsupported wire format); keep only
  `testChargeProgramValues`/`testTemperaturePointsValues`
- [x] 6.3 `VehicleHandlerTest.testChargeProgramUpdate()`: drop the `PushMessage`/`JsonFormat`/`VEPUpdate`
  round trip; convert `PartialUpdate-MaxSoc.json` to the flat internal-map format and drive it through
  `ProtoConverter.json2Proto()` directly, same as every other `proto-json/*.json` fixture
- [x] 6.4 `Mapper.java`/`MapperTest.java`: reworded comments on the `bool_value` fallback branches - kept the
  code (not `VEPUpdate`-typed, and cheap defensive robustness), but stopped implying an active legacy source
  still exists
- [x] 6.5 `VehicleStatusAttributes.java` Javadoc: no longer describes normalizing "both supported backend
  push formats"
- [x] 6.6 Orphaned resources - deleted by the requesting user directly on the mounted project folder:
  `TestUpdate.java`, `src/test/resources/proto-blob/`, and the stray `__probe2.tmp` probe file
- [x] 6.7 `mvn clean install` - run successfully by the requesting user, same environment limitation as 5.2
  (no `mvn` in this sandbox)

## 7. Phase 3 - TRACE dump anonymization (fixture-collection safety)

- [x] 7.1 `AccountHandler.anonymizeForTrace()`: builds an anonymized copy of `VehicleStatusUpdate` (fixed
  placeholder for `fin_or_vin`, dummy coordinates for `position_lat`/`position_long` if present, matching
  the values already used by `Utils.proto2Json()`) before the TRACE dump used to collect
  `proto-json/*.json` fixtures
- [x] 7.2 Wired into the existing TRACE log call in `handleMessage()`'s `hasVehicleStatusUpdates()` branch;
  all other attributes are left untouched (operational data, not personal)
- [ ] 7.3 `mvn clean install` for this addendum - not run, same environment limitation as 5.2/6.7. Verified
  by manual review against the generated protobuf API in
  `src/3rdparty/java/com/daimler/mbcarkit/proto/VehicleEvents.java` instead. A human must run this before
  merging.

---
