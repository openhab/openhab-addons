# Existing Things and new channels

Research and test scope recorded on 2026-11-03. This developer note does not prescribe deleting Things or change the installation instructions.

## What the openHAB sources establish

The official [Thing description documentation](https://www.openhab.org/docs/developer/bindings/thing-xml#updating-thing-types) explains that managed Things retain their structure in the database. Updating a binding's XML description alone does not necessarily migrate existing Things. The framework supports explicit channel/group migration instructions in `OH-INF/update`.

The official [binding development guide](https://www.openhab.org/docs/developer/bindings/#updating-the-thing-structure) also supports adding/removing channels programmatically through `editThing()` and `updateThing()`. These are distinct update paths. XML migration instructions should not be added automatically to a dynamically assembled profile without assessing which Thing types need them.

Two community discussions illustrate the distinction:

- [Avoid update/xxx.xml while developping an addon](https://community.openhab.org/t/avoid-update-xxx-xml-while-developping-an-addon/169267): a developer reports channels defined in XML appearing after Thing recreation or explicit update instructions, while a different binding using programmatic channels updated immediately.
- [Bulk recreate Things after binding updates?](https://community.openhab.org/t/bulk-recreate-things-after-binding-updates/168536): participants distinguish restarting/replacing a handler from deleting and recreating the persisted Thing. Disabling/enabling may reinitialize a handler without replacing the saved structure. The thread includes tentative and differing lifecycle explanations; it is not an API guarantee for every runtime/provider.

Recreation after structural changes is therefore a known openHAB scenario, not proof of a defect in this binding. Conversely, the general XML explanation does not prove the cause of a missing dynamically generated channel.

## Solarman-specific path

For unified `solarman:logger` with `inverterType="sunsynk_single_phase"`, initialization loads the profile, reconstructs its marked dynamic channels with `buildAdvancedChannels`, and calls `updateThing`. Existing Thing identity/configuration is copied through `editThing`. The three metadata channels are runtime additions from the separate single-phase metadata resource, with types declared in the generated XML.

Hidden compatibility Thing types follow a different path: `dynamicProfile` is false, so their structure is not rebuilt by this method. Tests of the unified logger do not establish migration behavior for these historical types. No `OH-INF/update` instructions are introduced by this work.

## Directed regression test

`SolarmanExistingThingTest` starts with a unified logger Thing containing the 147 channel declarations captured from the archived 0.5.6 single-phase descriptor. It does not construct the old channel list by removing metadata from the current generated list. Fixture provenance is retained in [the historical channel fixture](../src/test/resources/sunsynk/single-phase-0.5.6-channels.json), including artifact and resource hashes. The archived descriptor supplies channel identity/type/labels; test construction supplies the known dynamic marker of the unified logger. It is a historical structure fixture, not an exported installation database.

Three lifecycle cases cover:

1. Initializing the current handler on an existing 147-channel Thing.
1. Publishing the historical profile, then using the real `BaseThingHandler.thingUpdated` entry point on the same handler with the current profile available.
1. Disposing the historical-profile handler and constructing a replacement handler around the existing Thing, without deleting that Thing.

The previous profile is represented by a controlled sensor subset/read plan to exercise lifecycle transitions; the test does not execute the old JAR. Each current initialization must add exactly the three expected String channels, retain all 147 previous channel UIDs/types/labels/configurations/properties, and preserve Thing UID/type, label, location, properties and configuration. A further initialization checks idempotence. Old scheduled workers must be cancelled without shutting down the shared scheduler. Network polling is not executed.

The fixture uses default historical channel configuration. This is not evidence that arbitrary user edits to dynamic channel labels/configuration survive reconstruction. It also does not test custom channels, which advanced profiles reject.

## Integration boundary and next check

Validation on 2026-11-03: all three directed cases passed. The complete `mvn -o spotless:apply clean install` run passed 1820 tests with zero failures/errors/skips; inspected Checkstyle/PMD/SpotBugs reports contain 0/0/0 findings, with zero Maven WARNING/ERROR entries and successful XML/BND/Karaf checks. No runtime source was changed. The verification-only build retains delivery 0.5.7, bundle `5.2.4.202611031729`, SHA-256 `e0bae0bbea5d871a464be56c6fa874d168ad0c8cd3f3e7a17a2aea74b0b5547b`. Runtime classes/resources match the previous archived build byte for byte; only the manifest changed. No new installation is required for these test additions.

The test callback synchronously accepts the updated Thing, and its scheduler is controlled. Passing it establishes the handler's intended structural update and stable link target UIDs. It does not execute JSONDB persistence, asynchronous core callbacks, UI caching, OSGi service replacement or the Item-link registry. Do not label it a complete framework upgrade test or hardware qualification.

For a framework-level check, use an isolated openHAB instance with a backed-up Thing and link configuration. Record the runtime version, Thing type/UID, installed bundle version and channel list before the upgrade. Install the intended bundle and capture initialization logs and the REST channel list after initialization/restart and after a handler disable/enable cycle, while retaining the same Thing. Compare REST versus UI to distinguish model updates from display caching. Confirm persistence and link targets after an instance restart. No inverter writes are required for this check.

Only choose a runtime change, XML update instructions or documented recreation procedure after identifying which path failed. User observation so far confirms that recreating a Thing exposed the new String channels; it does not isolate the cause or confirm their physical values.
