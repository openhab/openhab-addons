# RSCP Tooling

Helper scripts and the capability probe used while building this binding. They are not part of the
add-on and are not referenced by the build.

## Purpose

The RSCP interface is not published as a complete specification, so the set of data points a concrete
device implements has to be determined empirically. The probe connects to a device, authenticates and
walks a list of request tags, reporting for each one whether the device answered with a value or with
an error code. This is how device and model specific capabilities are established instead of assumed.

Because the probe uses the binding's own wire implementation, a successful run also confirms the
encryption against real hardware: a wrong cipher fails on the very first response.

## Contents

| File                            | Purpose                                                                                     |
|---------------------------------|---------------------------------------------------------------------------------------------|
| `Probe.java`                    | Connects to a device and sweeps request tags, printing decoded responses and error codes.    |
| `RijndaelReference.java`        | Standalone Rijndael-256 with selectable row shifts and key derivation, used for diagnosis.   |
| `ReferenceCheck.java`           | Checks `RijndaelReference` against a recorded answer vector.                                |
| `CipherMatrix.java`             | Tries the realistic cipher configurations against a device and reports which one it answers. |
| `BlockCheck.java`               | Compares a candidate cipher's first block against a captured first block.                    |
| `AuthFrames.java`               | Builds RSCP frames by hand so diagnostics do not depend on the binding's own encoder.        |
| `extract_tags.py`               | Extracts tag names and ids from an RSCP tag header into `rscp_tags.tsv`.                     |
| `extract_mapping.py`            | Extracts the default data point mapping into `rscp_mapping.tsv`.                             |
| `check_probe_tags.py`           | Verifies every tag the probe uses resolves against the catalogue.                            |
| `check_thing_types.py`          | Verifies channel type definitions and references in the generated metadata.                  |
| `gen_rscp_tag.py`               | Generates the binding's `RscpTag` catalogue from the extracted data and the binding sources.  |
| `gen_thing_types.py`            | Generates the channel type and channel group XML from `E3DCChannelSpec.java`.                |

## Diagnosing the Handshake

A device whose encryption does not match closes the connection without an answer, which is
indistinguishable from rejected credentials by the error alone. These tools separate the two cases:

```bash
# verify the cipher implementation itself, no device needed
java -cp target/probe-classes ReferenceCheck

# try each plausible configuration against a device and report which one answers
java -cp target/probe-classes CipherMatrix <host> <rscpPassword> <user> <password>

# compare a candidate cipher against a real captured first block
java -cp target/probe-classes BlockCheck <rscpPassword> <capturedFirstBlockHex> [user] [password]
```

`CipherMatrix` sends one frame per configuration. A configuration that is correct makes the device
answer with an RSCP frame, which the tool confirms through the magic in the decrypted reply.

## Data Files

`rscp_tags.tsv` and `rscp_mapping.tsv` are extracted reference data. They are kept so the generated
sources can be reproduced without access to the original headers. To regenerate them, point the
extractors at a local copy of the RSCP tag definitions:

```bash
python tools/rscp/extract_tags.py <path>/RscpTags.h tools/rscp/rscp_tags.tsv
python tools/rscp/extract_mapping.py <path>/RscpMqttMapping.h <path>/RscpTags.h tools/rscp/rscp_mapping.tsv
```

## Regenerating Generated Sources

Both generators read the extracted data and write into `src`. Run them after changing the catalogue
needs or the channel model:

```bash
python tools/rscp/gen_rscp_tag.py tools/rscp/rscp_tags.tsv tools/rscp/rscp_mapping.tsv \
    src/main/java/org/openhab/binding/e3dc/internal/rscp/RscpTag.java \
    src/main/java/org/openhab/binding/e3dc/internal/E3DCChannelSpec.java \
    src/main/java/org/openhab/binding/e3dc/internal/E3DCThingHandler.java

python tools/rscp/gen_thing_types.py \
    src/main/java/org/openhab/binding/e3dc/internal/E3DCChannelSpec.java \
    src/main/resources/OH-INF/thing
```

Afterwards run `mvn spotless:apply`, because the generators emit plain text with Unix line endings
while this repository uses its own formatting.

## Running the Probe

Besides the compiled binding classes the probe needs only two of their runtime dependencies, the
BouncyCastle provider and the SLF4J API. The complete dependency classpath is not needed, and using it
would exceed the command line limit on Windows. In the commands below the separator is the semicolon of
Windows; `target/cp.txt` uses `:` on Linux and macOS, so adjust the separator and the `tr` call there.

```bash
# Git Bash or MSYS2
mvn -q compile dependency:build-classpath -Dmdep.outputFile=target/cp.txt
jars="$(tr ';' '\n' < target/cp.txt | grep -E '(bcprov|slf4j-api).*\.jar$' | tr '\n' ';')"
javac -d target/probe-classes -cp target/classes tools/rscp/Probe.java
java -cp "target/probe-classes;target/classes;${jars%;}" Probe \
    <host> <rscpPassword> <user> <password> tools/rscp/rscp_tags.tsv
```

```powershell
# PowerShell
mvn -q compile dependency:build-classpath "-Dmdep.outputFile=target/cp.txt"
$jars = ((Get-Content target/cp.txt -Raw).Trim().Split(';') | Where-Object { $_ -match '(bcprov|slf4j-api).*\.jar$' }) -join ';'
javac -d target/probe-classes -cp target/classes tools/rscp/Probe.java
java -cp "target/probe-classes;target/classes;$jars" Probe `
    <host> <rscpPassword> <user> <password> tools/rscp/rscp_tags.tsv
```

A missing class at that point means the binding gained a dependency since; then run the probe with the
complete classpath from `target/cp.txt` instead.

By default the probe requests the data points the binding uses, which is quick and answers both of the
questions that matter: whether the encryption matches the device, and which of the binding's channels
that device actually supports.

Appending `--full` sweeps every request tag of the EMS, PVI, BAT, PM, WB, DB, HA, DCDC, SGR, SE, INFO
and RSCP namespaces, which is thorough but takes a long time because each tag is a separate round trip.
The probe prints one block per tag and writes a summary to `probe_summary.tsv`.

### Request Shapes of the Addressed Containers

Some containers address one of several devices, so the device has to be told which one: `WB_REQ_DATA`
takes a `WB_INDEX`, `PM_REQ_DATA` a `PM_INDEX` and `BAT_REQ_DATA` a `BAT_INDEX`. These containers are
therefore not part of the plain tag lists. The probe asks each of them in every shape that could be
meant, that is with the index as its first child, with no parameter at all, and with the container's own
tag as a child. It prints the answer and reports which shape returned the expected response container.
For a device where only the indexed shape works, the summary looks like this:

```text
shape wb bare                          EMPTY(0 values) without WB_DATA, answered with WB_DATA
shape wb requesting its own tag        EMPTY(0 values) without WB_DATA, answered with WB_DATA
shape wb with WB_INDEX                 OK(9 values) answered with WB_DATA
shape bat with BAT_INDEX and DCB info  OK(24 values) answered with BAT_DATA
```

The index these shapes use is `PROBE_INDEX` in `Probe.java`; change it if the device is not addressed as
index 0. The shape that answers with the expected container is the one the binding has to send.

The request tags are derived from the extracted catalogue rather than being listed by hand, so the
probe can only ask for tags that exist. `check_probe_tags.py` verifies the same thing offline:

```bash
python tools/rscp/check_probe_tags.py tools/rscp/rscp_tags.tsv tools/rscp/Probe.java
```
