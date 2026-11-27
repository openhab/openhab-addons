import { CertSeedEntry, SeedSource } from "@matter/main/protocol";
import cdSignersData from "dcl-data/data/cd-signers.jsonl";
import paaRootsData from "dcl-data/data/paa-roots.jsonl";
import manifest from "dcl-data/manifest.json";

/**
 * Snapshot of the Matter certificate list (DCL) from the @matter/dcl-data package. It is built into matter.js so
 * pairing can check device certificates without internet access. The certificate service still updates it from the
 * DCL when it can.
 */
export function dclSeed() {
    return {
        paaRoots: seedSource(paaRootsData),
        cdSigners: seedSource(cdSignersData),
    };
}

function seedSource(jsonl: string): SeedSource<CertSeedEntry> {
    const entries = jsonl
        .split("\n")
        .filter(line => line.trim().length > 0)
        .map(line => JSON.parse(line) as CertSeedEntry);
    return {
        builtAt: manifest.builtAt,
        expectedCount: entries.length,
        entries: (async function* () {
            yield* entries;
        })(),
    };
}
