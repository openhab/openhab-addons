import {
    Diagnostic,
    Environment,
    Instant,
    Logger,
    ObserverGroup,
    SharedEnvironmentServices,
    StorageContext,
    StorageManager,
    StorageService,
} from "@matter/general";
import { DclOtaUpdateService, Fabric, FabricAuthority, Read } from "@matter/main/protocol";
import { AcceptedCommandList, AttributeList, ClusterRevision, FeatureMap, Matter } from "@matter/model";
import {
    ChangeNotificationService,
    ClientNode,
    ClientNodePhysicalProperties,
    ClusterBehavior,
    ControllerBehavior,
    Endpoint,
    EndpointLifecycle,
    NetworkClient,
    NodeConnectionState,
    ServerNode,
    SoftwareUpdateManager,
} from "@matter/node";
import { BasicInformationClient } from "@matter/node/behaviors/basic-information";
import { OtaProviderEndpoint } from "@matter/node/endpoints/ota-provider";
import { AttributeId, ClusterId, EndpointNumber, EventId, NodeId } from "@matter/types";
import { WebSocketSession } from "../app";
import { EventType, NodeState } from "../MessageTypes";
import { printError } from "../util/error";
import { migrateLegacyCommissionedNodes, migrateLegacyControllerCredentials } from "./legacyStorageMigration";

const logger = Logger.get("ControllerNode");

// Firmware version attributes, read from the device every time a node is sent to openHAB, even when the rest of the
// node comes from the local subscription cache. After a device reboots onto new firmware the cached value can be
// stale, and it is shown as the Thing's firmware-version property.
const FIRMWARE_VERSION_ATTRIBUTES: Record<string, ReadonlySet<string>> = {
    BasicInformation: new Set(["softwareVersion", "softwareVersionString"]),
    BridgedDeviceBasicInformation: new Set(["softwareVersion", "softwareVersionString"]),
};

// matter.js does not list these as cluster attributes, but openHAB needs them (featureMap in particular)
const GLOBAL_ATTRIBUTES = [
    "clusterRevision",
    "featureMap",
    "attributeList",
    "acceptedCommandList",
    "generatedCommandList",
];

// Changes of these attributes mean a cluster's capabilities changed
const STRUCTURE_ATTRIBUTES = new Set<number>([
    FeatureMap.id,
    AttributeList.id,
    AcceptedCommandList.id,
    ClusterRevision.id,
]);

// Endpoint events that mean the device added or removed endpoints or clusters. These are watched instead of the
// Descriptor attributes, because matter.js updates the node only after those attributes arrive.
const STRUCTURE_LIFECYCLE_CHANGES = new Set<EndpointLifecycle.Change>([
    EndpointLifecycle.Change.Ready,
    EndpointLifecycle.Change.Destroyed,
    EndpointLifecycle.Change.ServersChanged,
    EndpointLifecycle.Change.ClientsChanged,
]);

// A structure change arrives as several updates. Wait this long so openHAB is told once.
const STRUCTURE_CHANGE_DELAY_MS = 1000;

/**
 * This class represents the Matter Controller / Admin client
 */
export class ControllerNode {
    private environment: Environment = Environment.default;
    private storageContext?: StorageContext;
    #storageManager?: StorageManager;
    #node?: ServerNode;
    #fabric?: Fabric;
    // Nodes openHAB has initialized, keyed by matter.js peer id. Only these send changes to openHAB.
    #activePeers = new Map<string, { nodeId: NodeId; observers: ObserverGroup }>();
    #structureChangeTimers = new Map<NodeId, NodeJS.Timeout>();
    private observers = new ObserverGroup();
    #services?: SharedEnvironmentServices;

    constructor(
        private readonly storageLocation: string,
        private readonly controllerName: string,
        private readonly nodeNum: number,
        public readonly ws: WebSocketSession,
        private readonly netInterface?: string,
    ) {}

    get Store() {
        if (!this.storageContext) {
            throw new Error("Storage uninitialized");
        }
        return this.storageContext;
    }

    get node() {
        if (this.#node === undefined) {
            throw new Error("Controller not initialized");
        }
        return this.#node;
    }

    get fabric() {
        if (this.#fabric === undefined) {
            throw new Error("Controller not initialized");
        }
        return this.#fabric;
    }

    get otaProvider() {
        return this.node.endpoints.for("ota-provider") as Endpoint<OtaProviderEndpoint>;
    }

    get otaService() {
        if (!this.environment.has(DclOtaUpdateService)) {
            new DclOtaUpdateService(this.environment); // Adds itself to the environment
        }
        return this.services.get(DclOtaUpdateService);
    }

    protected get services() {
        if (!this.#services) {
            this.#services = this.environment.asDependent();
        }
        return this.#services;
    }

    /**
     * Closes the controller node
     */
    async close() {
        try {
            for (const timer of this.#structureChangeTimers.values()) {
                clearTimeout(timer);
            }
            this.#structureChangeTimers.clear();
            for (const { observers } of this.#activePeers.values()) {
                observers.close();
            }
            this.#activePeers.clear();
            this.observers.close();
            await this.#node?.close();
            await this.#storageManager?.close();
        } finally {
            // In a finally so the OTA blob storage lock is released (avoiding a leak that blocks the next
            // startup) even if the cleanup above throws.
            await this.#services?.close();
            this.#services = undefined;
            this.#node = undefined;
            this.#storageManager = undefined;
        }
    }

    /**
     * Initializes the controller node
     */
    async initialize() {
        const outputDir = this.storageLocation;
        const id = `${this.controllerName}-${this.nodeNum.toString()}`;
        const prefix = "openHAB: ";
        const fabricLabel = prefix + this.controllerName.substring(0, 31 - prefix.length);

        logger.info(`Storage location: ${outputDir} (Directory)`);
        this.environment.vars.set("storage.path", outputDir);

        // TODO we may need to choose which network interface to use
        if (this.netInterface !== undefined) {
            this.environment.vars.set("mdns.networkinterface", this.netInterface);
        }

        // openHAB 5.0 shipped matter.js 0.15, which stored controller data in an older layout that must be converted
        await migrateLegacyControllerCredentials(this.environment, id);

        // Groupcast is left out of the root so matter.js 0.17 can still load the stored controller after a downgrade
        const node = await ServerNode.create(ServerNode.RootEndpointWithoutGroupcast.with(ControllerBehavior), {
            environment: this.environment,
            id,
            network: {
                ble: false,
                // Nodes connect only once openHAB initializes their Thing
                autoStartCommissionedPeers: false,
                // Save node data right away. matter.js otherwise keeps it in memory for up to 20 minutes.
                clientCacheFlushInterval: Instant,
            },
            controller: { adminFabricLabel: fabricLabel },
            commissioning: { enabled: false },
            subscriptions: { persistenceEnabled: false },
        });
        this.#node = node;
        await node.add(new Endpoint(OtaProviderEndpoint, { id: "ota-provider" }));

        this.#storageManager = await node.env.get(StorageService).open(id);
        this.storageContext = this.#storageManager.createContext("Node");
        if (await this.Store.has("ControllerFabricLabel")) {
            const label = await this.Store.get<string>("ControllerFabricLabel", fabricLabel);
            await node.setStateOf(ControllerBehavior, { adminFabricLabel: label });
        }

        const fabricAuthority = await node.env.load(FabricAuthority);
        // ControllerBehavior initializes asynchronously, so it has to be loaded before its state can be read
        const fabricConfig = await node.act(
            async agent => (await agent.load(ControllerBehavior)).fabricAuthorityConfig,
        );
        this.#fabric = await fabricAuthority.defaultFabric(fabricConfig);

        const migrated = await migrateLegacyCommissionedNodes(node);
        if (migrated.nodes > 0 || migrated.failed > 0) {
            logger.info(
                `Converted ${migrated.nodes} nodes from the openHAB 5.0 storage layout, ${migrated.failed} failed`,
            );
        }

        // matter.js defaults to sequential operational node IDs starting at NodeId(1). The counter is not
        // restored consistently with the fabric across restarts, so it re-attempts low IDs that are already
        // commissioned ("Node ID X is already commissioned and can not be reused").
        await node.setStateOf(ControllerBehavior, { nodeIdAssignment: "random" });

        this.observers.on(node.env.get(ChangeNotificationService).change, change => this.#handleChange(change));
        this.observers.on(node.peers.deleted, peer => this.#handlePeerDeleted(peer));

        await node.start();

        //Set up observers for OTA updates, matter.js checks every 24 hours by default.
        const updateManagerEvents = this.otaProvider.eventsOf(SoftwareUpdateManager);
        this.observers.on(updateManagerEvents.updateAvailable, (peer, details) => {
            logger.info(`Update available for peer `, peer, `:`, details);
            const nodeId = peer?.nodeId;
            if (!nodeId) {
                logger.error(`Node ID not found for peer `, peer);
                return;
            }
            this.ws.sendEvent(EventType.UpdateAvailable, {
                nodeId: nodeId.valueOf(),
                ...details,
            });
        });
        this.observers.on(updateManagerEvents.updateDone, peer => {
            logger.info(`Update done for peer `, peer);
            const nodeId = peer?.nodeId;
            if (!nodeId) {
                logger.error(`Node ID not found for peer `, peer);
                return;
            }
            // matter.js only emits updateDone once the device has already rebooted onto the new firmware and
            // re-established its session. Force a fresh remote read so the reported version reflects the firmware
            // just installed, rebuild the structure/channels and bring the Thing (set OFFLINE while applying) back
            // ONLINE in one step.
            try {
                this.sendSerializedNode(this.getNode(nodeId), undefined, true);
            } catch (e) {
                logger.error(`Could not refresh node ${nodeId} after OTA update: ${e}`);
            }
        });

        // Query for updates now once
        const updates = await this.otaProvider.act(agent => agent.get(SoftwareUpdateManager).queryUpdates());
        for (const update of updates ?? []) {
            logger.info(`Update available for peer `, update.peerAddress, `:`, update.info);
            this.ws.sendEvent(EventType.UpdateAvailable, {
                nodeId: update.peerAddress.nodeId.valueOf(),
                ...update.info,
            });
        }
    }

    /**
     * Connects to a node and starts reporting its state, attribute and event changes. Resolves once the node is
     * connected and its structure is known. If called again for a connected node it only reports the connected state.
     *
     * @param nodeId  The nodeId of the node to connect to
     * @param connectionTimeout Optional timeout in milliseconds. If omitted or non-positive, no timeout will be applied
     * @throws Error if connection times out
     */
    async initializeNode(nodeId: string | number, connectionTimeout?: number): Promise<void> {
        const peer = this.getNode(nodeId);
        const id = this.#nodeIdOf(peer);
        // A node that is not ready yet sends Connected from its listeners once it is ready
        const wasReady = this.#isReady(peer);
        this.#activatePeer(peer, id);

        // Earlier binding versions disconnected a node by disabling it, which is saved and blocks any new connection
        if (peer.state.network.isDisabled) {
            await peer.enable();
        }
        if (!peer.stateOf(NetworkClient).autoSubscribe) {
            await peer.setStateOf(NetworkClient, { autoSubscribe: true });
        }
        if (!peer.lifecycle.isOnline) {
            await peer.start();
        }

        await this.#awaitReady(peer, connectionTimeout);
        if (wasReady) {
            this.#sendNodeState(peer, id, NodeConnectionState.Connected);
        }
    }

    /**
     * Whether the node's endpoints and clusters are known. matter.js has an isSeeded flag for this, but for a node
     * loaded from storage it checks before the stored data is readable, and checks again only when endpoints change.
     * So the flag stays unset, and a node that is readable and has its endpoints counts as known too.
     */
    isStructureKnown(peer: ClientNode) {
        return (
            peer.lifecycle.isSeeded ||
            (peer.lifecycle.isReadable &&
                peer.maybeStateOf(BasicInformationClient) !== undefined &&
                peer.endpoints.size > 1)
        );
    }

    #isReady(peer: ClientNode) {
        return this.isStructureKnown(peer) && peer.lifecycle.connectionState === NodeConnectionState.Connected;
    }

    /**
     * Returns a commissioned node by nodeId
     * @param nodeId
     * @returns
     */
    getNode(nodeId: number | string | NodeId) {
        const peer = this.node.peers.get(this.fabric.addressOf(NodeId(BigInt(nodeId))));
        if (peer === undefined) {
            throw new Error(`Node ${nodeId} not found`);
        }
        return peer;
    }

    /**
     * Removes a node from the controller, decommissioning it from the device first when it is reachable
     * @param nodeId
     */
    async removeNode(nodeId: number | string | NodeId) {
        const peer = this.node.peers.get(this.fabric.addressOf(NodeId(BigInt(nodeId))));
        if (peer === undefined) {
            return;
        }
        try {
            await peer.decommission();
        } catch (error) {
            logger.error(`Error decommissioning node ${nodeId}: ${error} force removing node`);
            await peer.delete();
        }
    }

    /**
     * Returns all commissioned nodes Ids
     * @returns
     */
    getCommissionedNodes() {
        return this.node.peers.commissioned.map(peer => this.#nodeIdOf(peer));
    }

    /**
     * Serializes a node and sends it to the web socket
     * @param node
     * @param endpointId Optional endpointId to serialize. If omitted, all endpoints will be serialized.
     * @param requestFromRemote When false (the default) values are taken from the local subscription cache, which
     *  the device keeps up to date. Pass true only to force a fresh remote read of every attribute.
     */
    sendSerializedNode(node: ClientNode, endpointId?: number, requestFromRemote: boolean = false) {
        this.serializeNode(node, endpointId, requestFromRemote)
            .then(data => {
                this.ws.sendEvent(EventType.NodeData, data);
            })
            .catch(error => {
                logger.error(`Error serializing node: ${error}`);
                printError(logger, error, "serializeNode");
            });
    }

    /**
     * Serializes a node and returns the json object
     * @param node
     * @param endpointId Optional endpointId to serialize. If omitted, the root endpoint will be serialized.
     * @param requestFromRemote When false (the default) attribute values are served from the local subscription
     *  cache. When true all attributes are read from the device in one interaction.
     * @returns
     */
    async serializeNode(node: ClientNode, endpointId?: number, requestFromRemote: boolean = false) {
        const root = endpointId !== undefined ? this.endpointFor(node, endpointId) : node;

        let remoteValues = new Map<string, unknown>();
        try {
            remoteValues = await this.readAttributes(node, requestFromRemote ? [{}] : this.#firmwareVersionPaths(root));
        } catch (e) {
            logger.debug(`Remote read for node ${this.#nodeIdOf(node)} failed, using cache: ${e}`);
        }

        const serializeEndpoint = (endpoint: Endpoint): any => {
            const endpointData: any = {
                number: endpoint.number,
                clusters: {},
                children: [],
            };

            for (const type of Object.values(endpoint.behaviors.supported)) {
                if (ClusterBehavior.is(type) && endpoint.behaviors.has(type)) {
                    endpointData.clusters[type.cluster.name] = this.serializeCluster(endpoint, type, remoteValues);
                }
            }

            for (const child of endpoint.parts) {
                endpointData.children.push(serializeEndpoint(child));
            }

            return endpointData;
        };

        return {
            id: this.#nodeIdOf(node),
            rootEndpoint: serializeEndpoint(root),
        };
    }

    /**
     * Serializes the attributes of a cluster the device reports
     * @param remoteValues values read from the device, keyed by "endpointId/clusterId/attributeId", that take
     *  precedence over the cached values
     */
    serializeCluster(endpoint: Endpoint, type: ClusterBehavior.Type, remoteValues = new Map<string, unknown>()) {
        const cluster = type.cluster;
        const clusterData: any = {
            id: cluster.id,
            name: cluster.name,
        };
        const state = endpoint.stateOf(type) as Record<string, unknown>;
        // Only attributes the device reports. matter.js fills in spec defaults for the others.
        const names = [...endpoint.behaviors.elementsOf(type).attributes, ...GLOBAL_ATTRIBUTES];
        for (const name of names) {
            const attributeId = cluster.attributes?.[name]?.id;
            const key = `${endpoint.number}/${cluster.id}/${attributeId}`;
            const value = remoteValues.has(key) ? remoteValues.get(key) : state[name];
            if (value !== undefined) {
                clusterData[name] = value;
            }
        }
        return clusterData;
    }

    endpointFor(node: ClientNode, endpointId: number): Endpoint {
        if (node.number === endpointId) {
            return node;
        }
        if (!node.endpoints.has(endpointId)) {
            throw new Error(`Endpoint ${endpointId} not found on node ${this.#nodeIdOf(node)}`);
        }
        return node.endpoints.for(endpointId);
    }

    /**
     * Addresses of the firmware version attributes on this endpoint and its children, for reading them from the device
     */
    #firmwareVersionPaths(root: Endpoint) {
        const paths = new Array<{ endpointId: EndpointNumber; clusterId: ClusterId; attributeId: AttributeId }>();
        const visit = (endpoint: Endpoint) => {
            for (const type of Object.values(endpoint.behaviors.supported)) {
                if (!ClusterBehavior.is(type) || !endpoint.behaviors.has(type)) {
                    continue;
                }
                const names = FIRMWARE_VERSION_ATTRIBUTES[type.cluster.name];
                if (names === undefined) {
                    continue;
                }
                const supported = endpoint.behaviors.elementsOf(type).attributes;
                for (const name of names) {
                    const attributeId = type.cluster.attributes?.[name]?.id;
                    if (attributeId !== undefined && supported.has(name)) {
                        paths.push({
                            endpointId: EndpointNumber(endpoint.number),
                            clusterId: type.cluster.id,
                            attributeId,
                        });
                    }
                }
            }
            for (const child of endpoint.parts) {
                visit(child);
            }
        };
        visit(root);
        return paths;
    }

    /**
     * Reads attributes from the device in one interaction
     *
     * @returns values keyed by "endpointId/clusterId/attributeId"
     */
    async readAttributes(
        node: ClientNode,
        attributes: { endpointId?: EndpointNumber; clusterId?: ClusterId; attributeId?: AttributeId }[],
    ) {
        const values = new Map<string, unknown>();
        if (attributes.length === 0) {
            return values;
        }
        for await (const chunk of node.interaction.read(Read({ attributes }))) {
            for await (const report of chunk) {
                if (report.kind === "attr-value") {
                    const { endpointId, clusterId, attributeId } = report.path;
                    values.set(`${endpointId}/${clusterId}/${attributeId}`, report.value);
                }
            }
        }
        return values;
    }

    #nodeIdOf(peer: ClientNode) {
        const address = peer.peerAddress;
        if (address === undefined) {
            throw new Error(`Node ${peer.id} is not commissioned`);
        }
        return address.nodeId;
    }

    /**
     * Starts sending a node's connection and structure changes to openHAB. Safe to call again for the same node.
     */
    #activatePeer(peer: ClientNode, nodeId: NodeId) {
        if (this.#activePeers.has(peer.id)) {
            return;
        }
        const observers = new ObserverGroup();
        this.#activePeers.set(peer.id, { nodeId, observers });
        observers.on(peer.lifecycle.connectionStateChanged, state => {
            // openHAB requests the node data on Connected, which needs the structure to be known
            if (state === NodeConnectionState.Connected && !this.isStructureKnown(peer)) {
                return;
            }
            this.#sendNodeState(peer, nodeId, state);
        });
        observers.on(peer.lifecycle.seeded, () => {
            if (peer.lifecycle.connectionState === NodeConnectionState.Connected) {
                this.#sendNodeState(peer, nodeId, NodeConnectionState.Connected);
            }
        });
        // Changes on any endpoint of the node are also emitted on the node itself
        observers.on(peer.lifecycle.changed, (type, endpoint) => {
            if (!STRUCTURE_LIFECYCLE_CHANGES.has(type) || !this.isStructureKnown(peer)) {
                return;
            }
            if (endpoint === peer && type !== EndpointLifecycle.Change.ServersChanged) {
                return;
            }
            this.#scheduleStructureChanged(nodeId);
        });
    }

    #handlePeerDeleted(peer: ClientNode) {
        const active = this.#activePeers.get(peer.id);
        if (active === undefined) {
            return;
        }
        const { nodeId, observers } = active;
        observers.close();
        this.#activePeers.delete(peer.id);
        clearTimeout(this.#structureChangeTimers.get(nodeId));
        this.#structureChangeTimers.delete(nodeId);
        logger.info(`Node ${nodeId} removed`);
        this.ws.sendEvent(EventType.NodeStateInformation, { nodeId, state: NodeState.DECOMMISSIONED });
    }

    #sendNodeState(peer: ClientNode, nodeId: NodeId, state: NodeConnectionState) {
        this.ws.sendEvent(EventType.NodeStateInformation, {
            nodeId,
            state: NodeConnectionState[state],
            physicalProperties: state === NodeConnectionState.Connected ? this.#physicalProperties(peer) : undefined,
        });
    }

    #physicalProperties(peer: ClientNode) {
        try {
            return ClientNodePhysicalProperties(peer);
        } catch (e) {
            logger.debug(`Could not read physical properties for node ${peer.id}: ${e}`);
            return undefined;
        }
    }

    async #awaitReady(peer: ClientNode, timeout?: number) {
        const isReady = () => this.#isReady(peer);
        if (isReady()) {
            return;
        }
        const observers = new ObserverGroup();
        let timer: NodeJS.Timeout | undefined;
        try {
            await new Promise<void>((resolve, reject) => {
                const check = () => {
                    if (isReady()) {
                        resolve();
                    }
                };
                observers.on(peer.lifecycle.seeded, check);
                observers.on(peer.lifecycle.connectionStateChanged, check);
                if (timeout !== undefined && timeout > 0) {
                    timer = setTimeout(() => {
                        const state = NodeConnectionState[peer.lifecycle.connectionState];
                        reject(new Error(`Node ${this.#nodeIdOf(peer)} connection failed: ${state}`));
                    }, timeout);
                }
                check();
            });
        } finally {
            clearTimeout(timer);
            observers.close();
        }
    }

    /**
     * Forwards attribute and event changes of initialized nodes to openHAB
     */
    #handleChange(change: ChangeNotificationService.Change) {
        if (change.kind === "readable") {
            return;
        }
        const peer = this.#owningPeer(change.endpoint);
        const active = peer === undefined ? undefined : this.#activePeers.get(peer.id);
        if (peer === undefined || active === undefined) {
            return;
        }
        const { nodeId } = active;

        switch (change.kind) {
            case "update": {
                const { behavior, endpoint, properties, version } = change;
                if (!this.isStructureKnown(peer) || !ClusterBehavior.is(behavior)) {
                    return;
                }
                if (!endpoint.behaviors.supported[behavior.id]) {
                    return;
                }
                const endpointId = endpoint.number;
                const clusterId = behavior.cluster.id;
                const state = endpoint.stateOf(behavior) as Record<string, unknown>;
                const attributes = behavior.cluster.attributes ?? {};
                for (const property of properties ?? Object.keys(attributes)) {
                    let attributeId = parseInt(property, 10);
                    if (isNaN(attributeId)) {
                        attributeId = attributes[property]?.id;
                        if (attributeId === undefined) {
                            continue;
                        }
                    }
                    const attributeName =
                        Matter.clusters(clusterId)?.attributes(attributeId)?.propertyName ??
                        `Unknown (${Diagnostic.hex(attributeId)})`;
                    this.ws.sendEvent(EventType.AttributeChanged, {
                        path: { nodeId, endpointId, clusterId, attributeId: AttributeId(attributeId), attributeName },
                        value: state[property],
                        version,
                    });
                    if (STRUCTURE_ATTRIBUTES.has(attributeId)) {
                        this.#scheduleStructureChanged(nodeId);
                    }
                }
                break;
            }

            case "event": {
                const { behavior, endpoint, event, number, timestamp, priority, payload } = change;
                if (!this.isStructureKnown(peer) || !ClusterBehavior.is(behavior)) {
                    return;
                }
                this.ws.sendEvent(EventType.EventTriggered, {
                    path: {
                        nodeId,
                        endpointId: endpoint.number,
                        clusterId: behavior.cluster.id,
                        eventId: event.id as EventId,
                        eventName: event.propertyName,
                    },
                    events: [{ eventNumber: number, epochTimestamp: timestamp, priority, data: payload }],
                });
                break;
            }
        }
    }

    #owningPeer(endpoint: Endpoint): ClientNode | undefined {
        for (let current: Endpoint | undefined = endpoint; current !== undefined; current = current.owner) {
            if (current instanceof ClientNode) {
                return current;
            }
        }
        return undefined;
    }

    #scheduleStructureChanged(nodeId: NodeId) {
        clearTimeout(this.#structureChangeTimers.get(nodeId));
        this.#structureChangeTimers.set(
            nodeId,
            setTimeout(() => {
                this.#structureChangeTimers.delete(nodeId);
                this.ws.sendEvent(EventType.NodeStateInformation, { nodeId, state: NodeState.STRUCTURE_CHANGED });
            }, STRUCTURE_CHANGE_DELAY_MS),
        );
    }
}
