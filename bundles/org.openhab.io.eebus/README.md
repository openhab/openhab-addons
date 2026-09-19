# EEBus Add-on

> **Status**: draft PR [#21313](https://github.com/openhab/openhab-addons/pull/21313), not yet merged. Builds clean
> (checkstyle/spotbugs/spotless/i18n all pass, 18/18 unit tests pass, no compiler warnings) and has been live-tested
> against a real independent EEBus implementation
> ([meisel2000/eebus-cbsim](https://github.com/meisel2000/eebus-cbsim), built on the `enbility/eebus-go` stack) -
> SHIP pairing, mDNS discovery, and SPINE LPC use-case discovery/negotiation (including the dynamic
> `entity.addUseCase()` registration this add-on relies on instead of registering at device-build time) all
> verified working end-to-end, correctly reporting the `nominalMax` configured via item metadata. One real bug
> was found and fixed this way: `Device.build()` also adds an implicit `DEVICE_INFORMATION` entity alongside the
> requested one, so the entity to register use cases on must be selected by type, not assumed to be the first/only
> one returned. Not yet verified: a full accepted _active_ limit write from a real CEM, and the resulting command
> actually landing on the tagged item (blocked on a simulator-side heartbeat quirk in cbsim, not this add-on - see
> [openhab-addons#21211](https://github.com/openhab/openhab-addons/issues/21211) for details). No EEBus hardware
> has been used in this development; testing so far is simulator-only.
>
> Supersedes an earlier Thing-based binding prototype (mirror kept at
> [stamateviorel/openhab-eebus-binding](https://github.com/stamateviorel/openhab-eebus-binding) for history) -
> LPC/LPP are household-wide singleton limits, not per-device Things, so this is shaped as an IO add-on instead
> (service config + item metadata), per [maintainer feedback](https://github.com/openhab/openhab-addons/issues/21211#issuecomment-5152128940).

This add-on lets openHAB present itself as an [EEBus](https://www.eebus.org/) Controllable System (CS) on the local
network, backed by the [jEEBus](https://www.openmuc.org/eebus/) SHIP/SPINE implementation from Fraunhofer ISE /
OpenMUC.
A remote CEM/EMS or a smart-meter CLS gateway (§14a EnWG in Germany) can pair with it over the standard EEBus SHIP
transport and issue LPC (Limitation of Power Consumption) and LPP (Limitation of Power Production) power limits,
which are pushed onto whichever openHAB items you tag for the purpose.

Note the direction of control: this add-on implements the _device being limited_, not the _thing issuing limits_.
It does not pair with and control other EEBus devices (e.g. a real wallbox or heat pump) directly - as of this
writing, OpenMUC has not published a Java library for that (CEM/controller) side of the protocol.

This is an IO add-on, not a binding: LPC/LPP are household-wide singleton limits, not per-device data points, so
there is no Thing to add.
Configure the add-on itself under Settings, then tag the one item that should receive each limit with item
metadata - the same shape used by the HomeKit and Alexa add-ons.

## Add-on Configuration

| Parameter          | Group    | Required | Default             | Description                                                                                                                                                            |
|---------------------|----------|----------|-----------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| bindAddress         | network  | no       | `0.0.0.0`             | Local IP address the SHIP WebSocket server binds to.                                                                                                                     |
| port                | network  | no       | `4712`                | TCP port the SHIP WebSocket server listens on.                                                                                                                           |
| wssPath             | network  | no       | `/ship/`              | HTTP path the SHIP WebSocket endpoint is served under.                                                                                                                   |
| serviceDomain       | network  | no       | `local.`              | mDNS domain used for SHIP service advertisement.                                                                                                                         |
| deviceId            | identity | yes      | `d:_i:openHAB:eebus-01` | Unique SPINE device identifier. Change the default before pairing with a real partner.                                                                                |
| friendlyName        | identity | no       | `openHAB`             | Human-readable name advertised to pairing partners.                                                                                                                      |
| deviceType          | identity | no       | `GENERIC`             | SPINE device type reported to pairing partners.                                                                                                                          |
| entityType          | identity | no       | `CEM`                 | SPINE entity type hosting the LPC/LPP use cases (must be one of CEM, COMPRESSOR, EVSE, HEAT_PUMP_APPLIANCE, INVERTER, SMART_ENERGY_APPLIANCE, SUB_METER_ELECTRICITY). |
| connectPolicy       | pairing  | no       | `TRUSTED`             | `TRUSTED` (only pre-trusted SKIs), `ALL` (insecure), or `NONE`.                                                                                                          |
| trustedSkis         | pairing  | no       | -                      | Comma-separated list of remote SKIs to pre-trust. Used when `connectPolicy` is `TRUSTED`.                                                                                |
| autoAcceptPairing   | pairing  | no       | `false`               | Accept any pairing request without a pre-trusted SKI. Lab testing only, never in production.                                                                            |

Changing a `network` or `identity` parameter restarts the SHIP node.
`pairing` parameters apply live.

## Item Metadata

Tag exactly one item per direction with the `eebus` namespace to bind it to a use case:

```java
Number:Power My_Charging_Limit "EV Charging Limit" { eebus="lpc" [ nominalMax=11000, failsafeLimit=4200, failsafeDuration="PT2H" ] }
```

| Metadata value | Use case                        | Item type suggestion |
|-----------------|-----------------------------------|------------------------|
| `lpc`           | Limitation of Power Consumption   | `Number:Power`        |
| `lpp`           | Limitation of Power Production    | `Number:Power`        |

Configuration parameters (all optional, in watts / ISO 8601 duration):

| Parameter          | Default (lpc) | Default (lpp) | Description                                                                          |
|---------------------|----------------|-----------------|-----------------------------------------------------------------------------------------|
| `nominalMax`        | `4200`         | `0`             | Maximum consumption/production this installation could ever draw/feed in.               |
| `failsafeLimit`      | = `nominalMax`  | = `nominalMax`   | Limit applied if the pairing partner's heartbeat is lost. Review and set this deliberately - it is not a safe default. |
| `failsafeDuration`   | `PT2H`         | `PT2H`          | ISO 8601 duration the failsafe limit stays valid for.                                    |

LPP's `nominalMax` defaults to `0` rather than a nonzero placeholder - claiming export capacity you haven't actually configured would misreport this installation's real capability to the CEM.
Set it explicitly to whatever this installation can actually feed back.

Only one item per direction is supported at a time.
Tagging a second item for the same use case is logged as a warning and ignored.
Changing `nominalMax`/`failsafeLimit`/`failsafeDuration` on an already-bound item, or re-tagging to a different item, both require restarting the add-on, since jEEBus has no runtime API to reconfigure or detach a use case
once added.

Every active-limit update from a paired partner is posted to the tagged item as a `QuantityType<Power>` command
(`Units.WATT`), same as any other command.
Route it into your rules the same way you would a manually-entered value, e.g. clamp an EV charger's current limit
to it.

## Testing Without EEBus Hardware

Since this add-on implements the standard EEBus SHIP/SPINE Controllable System role, it can be paired against any
generic EEBus control-box simulator or the free conformance-testing lab run by the EEBus Initiative
([Living Lab Cologne](https://www.livinglabcologne.com/)) rather than only against real hardware.

## Finding Your Own SKI

The add-on's own SKI - needed by your CLS gateway/EMS installer to pre-trust this node - is logged at `INFO` level
on startup (`EEBus: SHIP node started, own SKI: ...`).
