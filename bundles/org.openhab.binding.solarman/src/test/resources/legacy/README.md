# Pinned upstream legacy compatibility fixtures

`upstream-baseline.json` is frozen output from openhab/openhab-addons revision `908b3ddfa388885edb9f72ca37b73802de121aab`, containing 905 generated channels and the original read requests for all 17 bundled legacy definitions. Source hashes identify the exact upstream Java/YAML inputs. Derived upstream material retains the project's EPL-2.0 licensing.

The exporter compiles the pinned upstream parser, channel manager, channel utilities, configuration/model classes and constants in an isolated temporary directory and executes them with the pinned YAML resources. Neither working production classes nor working YAML resources supply expected channels. Existing build dependencies are resolved from a Surefire report, excluding working class directories. Generation fixes the upstream locale to English/US because that revision used the JVM default locale. The checked-in fixture is consumed directly by tests; normal tests do not execute Git, a compiler, Python or network requests to regenerate it.

After an initial module test build, regenerate explicitly from the module directory:

```shell
python tools/export_legacy_baseline.py --java-home /path/to/jdk-21
```

The pinned commit must be present in the local Git object database. The script executes its Java sources, so review the pin before changing it. Regeneration is not an automatic response to a failing compatibility test.

`reviewed-differences.json` is a separately reviewed list of existing intentional changes; it is not generated from production code:

- Eight channel Item types now use `Number:Time` for hour units instead of the original generic `Number`: four generation/discharge duration readings across three Sofar definitions and four timed-charge/discharge hour settings in `solis_hybrid`. Channel IDs, type IDs, labels and register/conversion configurations remain the same. This is a dimension change to document for migration; it is not bit-for-bit unchanged metadata.
- Five profiles have different read-request lists due to the existing normalizer. `deye_hybrid` merges adjacent windows and splits at 125 words; `deye_sg01hp3` and `deye_sg04lp3` reorder windows, remove overlapping reads and merge adjacent reads; `kstar_hybrid` groups requests by function and merges adjacent windows; `solis_hybrid` sorts by function and splits its original 151-word FC03 request. Every function/address pair remains covered, no new register is read, and each request has at most 125 registers. The other twelve profiles retain the exact original request list.

`SolarmanLegacyBaselineTest` initializes the real handler with a controlled scheduler and intercepts the updater boundary to inspect the polling plan without opening a connection. It compares all channels, accepted Item types (with only the eight declared exceptions), channel type IDs, labels, kinds, dynamic markers, register order, scale, offset, rule, units and configuration access flags. It checks exact reviewed request sequences plus independent function/address coverage, absence of duplicate reads and the protocol request-size limit.

A second 17-profile test exercises an added static channel, an overlapping additional request and new FC03/FC04 windows at the same addresses. This guards against dropped custom configuration, duplicate overlap reads and cross-function merging. No physical communications occur. State-description access/options corrections and write behavior remain covered separately by provider/updater tests; this fixture does not claim that all old numeric conversions or writes are unchanged.

These fixtures close the independent comparison gap against the pinned 5.2 baseline. They do not establish compatibility against a newer upstream main commit, framework persistence of existing Things, runtime 5.3 compatibility or physical inverter qualification. Reassess the baseline when preparing the main-based contribution.

## Current main baseline

main-baseline.json is independently exported from official main commit 821bb618754b6a0973ae4a11a28ac93133be278c: 18 profiles and 966 channels. The original 5.2 upstream-baseline.json and reviewed-differences.json remain unchanged as historical independent evidence. main-reviewed-differences.json retains the same eight hour-dimension and five read-plan exceptions against the new baseline.

The independent export uses upstream parser/model/channel Java and YAML blobs compiled in isolation, never working production classes. Two pre-port exports were byte-identical. Use tools/export_main_legacy_baseline.py with Java 21 after a main-based test build; --classpath-report optionally selects a Surefire XML containing dependency JAR paths. Initial pre-port capture used the already resolved 5.2 dependency classpath. After the successful main build, repeating the export with its actual 5.3 dependency classpath produced byte-identical output. The exporter reads Git object IDs, avoiding Windows long revision:path arguments. Normal tests use checked-in fixtures and require no Git, Python, compilation or network access.

The main baseline adds the 63-channel Sunwoda FC04 profile and removes inverter-gen-connected-status and inverter-gen-power from deye_hybrid. The tests explicitly compare historical profiles with current main, allowing only those two upstream removals and the new Sunwoda profile. Runtime publication/polling tests compare all 18 profiles with main, including custom FC03 and FC04 windows at the same addresses, bounded requests and exact reviewed batching sequences.
