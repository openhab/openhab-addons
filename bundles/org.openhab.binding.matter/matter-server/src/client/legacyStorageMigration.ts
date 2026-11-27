/**
 * @license
 * Copyright 2022-2026 Matter.js Authors
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copied unmodified from matter.js packages/nodejs-shell/src/util/legacyStorageMigration.ts (commit 335c3291b),
 * which is not published in a package.
 */

import {
    Environment,
    isObject,
    Logger,
    StorageManager,
    StorageService,
    SupportedStorageTypes,
    Time,
} from "@matter/general";
import { ClientNode, NetworkClient, RemoteDescriptor, ServerNode, ServerNodeStore } from "@matter/node";
import { DiscoveryData, OperationalAddress, PeerAddress } from "@matter/protocol";
import { EventNumber, FabricIndex, NodeId } from "@matter/types";

const logger = Logger.get("LegacyStorageMigration");

/** A pre-0.16 `credentials` entry (fabric config or CA key material); relocated verbatim, never decoded. */
type LegacyCredentialRecord = Record<string, SupportedStorageTypes>;

/**
 * A pre-0.16 `fabrics` entry; only `fabricIndex` is read here, the rest was relocated verbatim in step 1.
 * Intersected with `Record<string, SupportedStorageTypes>` (rather than a plain interface) so the type still
 * satisfies `StorageContext.get`'s `SupportedStorageTypes` constraint.
 */
type LegacyFabricRecord = Record<string, SupportedStorageTypes> & { fabricIndex: number };

/** Per-node metadata carried in a `commissionedNodes` list entry. `deviceData` is intentionally not migrated. */
type LegacyCommissionedEntry = Record<string, SupportedStorageTypes> & {
    operationalServerAddress?: OperationalAddress;
    discoveryData?: DiscoveryData;
};

/** One `commissionedNodes` list entry: the peer's raw stored node id paired with its metadata. */
type LegacyCommissionedNode = [bigint, LegacyCommissionedEntry];

/** True for a pre-0.16 cached attribute record (current storage keeps the bare value instead). */
function isLegacyAttributeRecord(value: SupportedStorageTypes): value is Record<string, SupportedStorageTypes> {
    return isObject(value) && "value" in value;
}

async function stepOneNeeded(mgr: StorageManager): Promise<boolean> {
    const credentials = mgr.createContext("credentials");
    if (!(await credentials.has("fabric"))) {
        return false;
    }
    const fabrics = await mgr.createContext("fabrics").get<LegacyCredentialRecord[]>("fabrics", []);
    return fabrics.length === 0;
}

async function stepTwoNeeded(mgr: StorageManager): Promise<boolean> {
    const nodes = mgr.createContext("nodes");
    const commissioned = await nodes.get<SupportedStorageTypes[]>("commissionedNodes", []);
    if (commissioned.length === 0) {
        return false;
    }
    // Current-format storage keys each peer under its own subcontext of "nodes".
    return (await nodes.contexts()).length === 0;
}

/**
 * True when a pre-0.16 controller storage still needs migrating to the current layout.
 * See docs/MIGRATION_CONTROLLER_018.md.
 */
export async function legacyMigrationNeeded(env: Environment, id: string): Promise<boolean> {
    const mgr = await env.get(StorageService).open(id);
    try {
        return (await stepOneNeeded(mgr)) || (await stepTwoNeeded(mgr));
    } finally {
        await closeStorage(mgr, id);
    }
}

/**
 * Step 1: relayout fabric + certificate authority data from the legacy `credentials` context into the
 * `fabrics`/`certificates` contexts the current controller reads. Must run before the controller ServerNode is
 * constructed. Idempotent.
 */
export async function migrateLegacyControllerCredentials(env: Environment, id: string): Promise<void> {
    const mgr = await env.get(StorageService).open(id);
    try {
        if (!(await stepOneNeeded(mgr))) {
            logger.debug(`No legacy controller credentials to migrate for store ${id}`);
            return;
        }

        const credentials = mgr.createContext("credentials");
        const fabricStore = mgr.createContext("fabrics");
        const certificates = mgr.createContext("certificates");

        let fabric: LegacyCredentialRecord | undefined;
        for (const key of await credentials.keys()) {
            if (key === "fabric") {
                fabric = await credentials.get<LegacyCredentialRecord>("fabric");
            } else if (!(await certificates.has(key))) {
                await certificates.set(key, await credentials.get<SupportedStorageTypes>(key));
            }
        }

        // Certificate authority keys must land in `certificates` before `fabrics.fabrics` is written: a crash
        // between the two would otherwise strand the CA under the old layout while the guard reports "migrated".
        if (fabric !== undefined) {
            await fabricStore.set("fabrics", [fabric]);
        }
        logger.info(`Migrated legacy controller credentials for store ${id}`);
    } finally {
        await closeStorage(mgr, id);
    }
}

/**
 * Step 2: migrate cached per-node data (`node-<id>` attribute trees + the `commissionedNodes` list) into the
 * current per-peer store layout, and register each peer. Must run after the controller ServerNode is constructed
 * (so its fabric is loaded) but before it goes online. Non-destructive — old data is left in place for
 * {@link cleanupLegacyStorage}. Idempotent (skips peers that already exist); a peer that fails to migrate is
 * skipped without aborting the rest and its partially-written store is cleared. Returns migrated counts.
 */
export async function migrateLegacyCommissionedNodes(
    node: ServerNode,
): Promise<{ nodes: number; endpoints: number; failed: number }> {
    const serverStore = node.env.get(ServerNodeStore);
    const baseStorage = serverStore.storage;
    const nodesCtx = baseStorage.createContext("nodes");

    // Per-peer idempotency is handled below; this only needs to know whether there is legacy data left to
    // look at, so a resume after a partial run isn't skipped.
    if (!(await nodesCtx.has("commissionedNodes"))) {
        logger.debug("No former commissioned nodes to migrate.");
        return { nodes: 0, endpoints: 0, failed: 0 };
    }
    const commissioned = await nodesCtx.get<LegacyCommissionedNode[]>("commissionedNodes", []);

    // A migrated Era-B store carries exactly one fabric. Anything else (no fabric yet, or an unexpected
    // multi-fabric store) cannot be addressed here; skip rather than abort the shell's startup. Report the
    // skipped peers as failed so the caller does not treat this as a clean run and proceed to delete them.
    const fabrics = await baseStorage.createContext("fabrics").get<LegacyFabricRecord[]>("fabrics", []);
    if (fabrics.length !== 1) {
        logger.warn(`Skipping legacy node migration: expected exactly one migrated fabric, found ${fabrics.length}`);
        return { nodes: 0, endpoints: 0, failed: commissioned.length };
    }
    const fabricIndex = FabricIndex(fabrics[0].fabricIndex);

    let migratedNodes = 0;
    let migratedEndpoints = 0;
    let failedNodes = 0;

    for (const [rawNodeId, { operationalServerAddress, discoveryData }] of commissioned) {
        const nodeId = NodeId(rawNodeId);
        const peerAddress = PeerAddress({ fabricIndex, nodeId });

        const existingPeer = node.peers.get(peerAddress);
        if (existingPeer !== undefined) {
            logger.debug(`Node ${nodeId} already migrated, skipping`);
            if (existingPeer.stateOf(NetworkClient).autoSubscribe) {
                await existingPeer.setStateOf(NetworkClient, { autoSubscribe: false });
            }
            continue;
        }

        const id = serverStore.clientStores.allocateId();
        let peerNode: ClientNode | undefined;
        try {
            const oldNode = baseStorage.createContext(`node-${nodeId}`);

            const maxEventNumber = await oldNode.get<EventNumber>("__maxEventNumber__", EventNumber(0));

            // Written before forAddress() below allocates the peer's store for the same id, which loads rather
            // than overwrites whatever is already on disk under that context.
            const peerStorage = nodesCtx.createContext(id).createContext("endpoints");
            let endpointsForNode = 0;
            for (const ep of await oldNode.contexts()) {
                const oldEndpoint = oldNode.createContext(ep);
                const newEndpoint = peerStorage.createContext(ep);
                for (const cluster of await oldEndpoint.contexts()) {
                    const oldCluster = oldEndpoint.createContext(cluster);
                    const newCluster = newEndpoint.createContext(cluster);
                    for (const key of await oldCluster.keys()) {
                        const value = await oldCluster.get(key);
                        if (key === "__version__") {
                            await newCluster.set(key, value);
                        } else if (isLegacyAttributeRecord(value)) {
                            await newCluster.set(key, value.value);
                        }
                    }
                }
                endpointsForNode++;
            }

            const commissioning = RemoteDescriptor.toLongForm({
                // Fallback only — a peer with its own discoveredAt keeps it.
                discoveredAt: Time.nowMs,
                ...discoveryData,
                addresses: operationalServerAddress ? [operationalServerAddress] : [],
            });

            peerNode = await node.peers.forAddress(peerAddress, { id });
            await peerNode.set({ commissioning, network: { maxEventNumber, autoSubscribe: false } });
            migratedNodes++;
            migratedEndpoints += endpointsForNode;
            logger.info(`Migrated commissioned node ${nodeId} as ${id}`);
        } catch (error) {
            failedNodes++;
            logger.error(`Failed to migrate commissioned node ${nodeId} as ${id}, skipping:`, error);
            if (peerNode !== undefined) {
                await peerNode.delete();
            }
            await nodesCtx.createContext(id).clearAll();
        }
    }

    return { nodes: migratedNodes, endpoints: migratedEndpoints, failed: failedNodes };
}

/**
 * Irreversible: once the legacy artifacts are deleted there is no way back, so this must only be run when the
 * operator is certain migration succeeded and the store will not be downgraded below 0.16.
 *
 * Deletes all legacy (pre-0.16) storage artifacts once migration is no longer needed (the same
 * `stepOneNeeded`/`stepTwoNeeded` predicate {@link legacyMigrationNeeded} uses). It does not verify that every
 * individual peer migrated successfully: this is an explicit, user-confirmed action (e.g. the shell's
 * `--cleanup-legacy-storage` flag). Running it against a store where step 1 or step 2 has not fully completed
 * loses the un-migrated data; that is accepted, and is the caller's responsibility. Idempotent.
 */
export async function cleanupLegacyStorage(env: Environment, id: string): Promise<void> {
    const mgr = await env.get(StorageService).open(id);
    try {
        if ((await stepOneNeeded(mgr)) || (await stepTwoNeeded(mgr))) {
            logger.warn(`Refusing legacy cleanup for store ${id}: migration has not completed`);
            return;
        }
        await wipeLegacyContexts(mgr);
        logger.info(`Removed legacy storage artifacts for store ${id}`);
    } finally {
        await closeStorage(mgr, id);
    }
}

/**
 * Unconditionally remove the legacy (pre-0.16) storage artifacts, no migration-state guard.
 *
 * A factory reset clears the current-format fabric but not the legacy source; leaving it would let the next boot
 * re-migrate and resurrect the identity/key material the reset destroyed. The reset path therefore wipes the
 * legacy source too. Unlike {@link cleanupLegacyStorage} this makes no completeness assumption — the reset is
 * discarding everything regardless.
 */
export async function eraseLegacyStorage(env: Environment, id: string): Promise<void> {
    const mgr = await env.get(StorageService).open(id);
    try {
        await wipeLegacyContexts(mgr);
    } finally {
        await closeStorage(mgr, id);
    }
}

async function wipeLegacyContexts(mgr: StorageManager): Promise<void> {
    for (const context of await mgr.driver.contexts([])) {
        if (context.startsWith("node-")) {
            await mgr.createContext(context).clearAll();
        }
    }
    await mgr.createContext("nodes").delete("commissionedNodes");
    await mgr.createContext("credentials").clearAll();
}

async function closeStorage(mgr: StorageManager, id: string): Promise<void> {
    try {
        await mgr.close();
    } catch (closeError) {
        logger.warn(`Error closing storage for store ${id}:`, closeError);
    }
}
