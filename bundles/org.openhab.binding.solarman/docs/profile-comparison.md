# Legacy and Advanced Profile Comparison

This comparison uses the bundled definitions from official openhab-addons main revision 821bb618754b6a0973ae4a11a28ac93133be278c and the pinned Kellerza source revision f1ecbc28e3ccf2b9f87b1ccee85a41628567da43.
It covers the 925 Kellerza entries; the three binding-origin single-phase metadata additions are documented separately in the README and combined catalog.
The current binding supports 18 legacy definitions, but this register-overlap analysis compares only the explicitly listed device families.

## Compared Families

| Advanced profile  | Legacy definitions     | Legacy entries / writable | Kellerza entries / writable | Comparable family |
| ----------------- | ---------------------- | ------------------------- | --------------------------- | ----------------- |
| single_phase      | deye_hybrid            | 81 / 18                   | 147 / 73                    | True              |
| single_phase_16kw | deye_hybrid            | 81 / 18                   | 147 / 73                    | True              |
| three_phase_lv    | deye_sg04lp3           | 94 / 24                   | 251 / 121                   | True              |
| three_phase_hv    | deye_sg01hp3           | 97 / 24                   | 294 / 132                   | True              |
| micro_hybrid_ess  | deye_2mppt, deye_4mppt | 60 / 0                    | 86 / 36                     | False             |

The generic deye_hybrid definition is a comparison baseline for 16 kW; it is not a certified dedicated 16 kW mapping.
The micro hybrid ESS storage family differs from the PV microinverters represented by deye_2mppt/deye_4mppt; those address matches are context only.

## Method and Limits

The [JSON evidence](profile-comparison.json) retains input hashes, complete candidate definitions and per-profile summaries; the [CSV](profile-comparison.csv) supports row review.
Candidates require profile-qualified ordered register tuples; matching names, one shared input of a computed sensor or a partial tuple do not establish a replacement.
Numeric candidates compare signedness, absolute factor, resulting offset/intercept and units.
They do not establish identical rounding, Item type, dynamic limits, mask semantics, lookup vocabulary or physical behavior.

A negative Kellerza factor selects signed decoding and uses the absolute scale; it does not invert power direction.
Multi-register values preserve declared low-word-first tuples, including sparse pairs; Sensor16 history behavior is distinct from plain numeric decoding.
Temperature offsets apply after scaling, and shared charge/mode/sell registers require separate masks.

| Difference                    | Required review                                                                                             |
| ----------------------------- | ----------------------------------------------------------------------------------------------------------- |
| Single-phase current at 164   | Legacy signed versus advanced unsigned interpretation; positive agreement does not qualify negative values  |
| HV battery voltage at 587     | Legacy factor 0.01 V versus advanced 0.1 V; validate model/firmware rather than silently changing a profile |
| Three-phase L1 inverter power | Legacy word 633 versus Sensor16 tuple 633/691 and history-based width selection                             |
| Phase versus total power      | A total at 172/175 does not replace a phase word at 170/173 solely by similar naming                        |
| Single-phase faults           | Advanced decoded 103..106 is a partial replacement for raw legacy 101..106                                  |

## Current Architecture

One visible solarman:logger Thing selects either a legacy YAML interpreter or an explicit advanced profile.
Existing legacy selections retain their own metadata, while advanced selection deliberately changes channel IDs, types and write semantics.
The hidden earlier advanced Thing types remain compatibility entry points; they are not new creation choices.
Keep transport/framing separate from profile interpretation and preserve supported writable channels.
No automatic rewrite of existing YAML interpretations or Item links is performed.

Use the [current single-phase migration guide](examples/profile-channel-lists/README.md) for all 81 legacy entries and 150 advanced entries, with separate automatic-candidate eligibility and functional-review proposals.
Hardware limits and reproducible tooling are described in the README and [source provenance](source-provenance.md).
