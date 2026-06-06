// Include this first to auto-register Crypto, Network and Time Node.js implementations
import { Environment, Logger, ObserverGroup, SharedEnvironmentServices, StorageContext, StorageService } from "@matter/general";
import { NodeId } from "@matter/types";
import { CommissioningController } from "@project-chip/matter.js";
import { Endpoint, NodeStates, PairedNode } from "@project-chip/matter.js/device";
import { WebSocketSession } from "../app";
import { EventType, NodeState } from "../MessageTypes";
import { printError } from "../util/error";
import { SoftwareUpdateManager } from "@matter/node";
import { DclOtaUpdateService, PhysicalDeviceProperties } from "@matter/main/protocol";

const logger = Logger.get("ControllerNode");

function extractPhysicalProperties(node: PairedNode | undefined): PhysicalDeviceProperties | undefined {
    if (!node) return undefined;
    try {
        const deviceInformation = node.deviceInformation;
        if (!deviceInformation) return undefined;
        // these are lazy properties, so we need to access them to actually hydrate our return object
        return { ...deviceInformation };
    } catch (e) {
        logger.debug(`Could not read deviceInformation for node ${node.nodeId}: ${e}`);
        return undefined;
    }
}

/**
 * This class represents the Matter Controller / Admin client
 */
export class ControllerNode {
    private environment: Environment = Environment.default;
    private storageContext?: StorageContext;
    private nodes: Map<NodeId, PairedNode> = new Map();
    // Per-node observer groups so listeners can be removed in bulk before re-registering,
    // avoiding duplicate handlers (and duplicate WebSocket events) when the same node instance
    // is reused across reconnections.
    private nodeObservers: Map<NodeId, ObserverGroup> = new Map();
    commissioningController?: CommissioningController;
    private observers?: ObserverGroup;
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
        for (const observers of this.nodeObservers.values()) {
            observers.close();
        }
        this.nodeObservers.clear();
        this.observers?.close();
        await this.commissioningController?.close();
        this.nodes.clear();
    }

    /**
     * Disposes any existing observer group for the given node and returns a fresh one.
     * This guarantees that re-registering listeners for the same node instance (e.g. across
     * reconnections) does not accumulate duplicate handlers.
     */
    private resetNodeObservers(nodeId: NodeId): ObserverGroup {
        this.disposeNodeObservers(nodeId);
        const observers = new ObserverGroup();
        this.nodeObservers.set(nodeId, observers);
        return observers;
    }

    /**
     * Removes all listeners registered through the observer group for the given node, if any.
     */
    private disposeNodeObservers(nodeId: NodeId): void {
        const observers = this.nodeObservers.get(nodeId);
        if (observers !== undefined) {
            observers.close();
            this.nodeObservers.delete(nodeId);
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
        this.commissioningController = new CommissioningController({
            environment: {
                environment: this.environment,
                id,
            },
            autoConnect: false,
            adminFabricLabel: fabricLabel,
            enableOtaProvider: true
        });
        
        const storageService = this.commissioningController.env.get(StorageService);
        // TODO: Implement resetStorage
        // if (resetStorage) {
        //     await this.commissioningController.node.erase();
        // }
        this.storageContext = (await storageService.open(id)).createContext("Node");


        if (await this.Store.has("ControllerFabricLabel")) {
            await this.commissioningController.updateFabricLabel(
                await this.Store.get<string>("ControllerFabricLabel", fabricLabel),
            );
        }

        await this.commissioningController.start();

        //Set up observers for OTA updates, matter.js checks every 24 hours by default.
        this.observers = this.observers ?? new ObserverGroup(this.environment.runtime);
        const updateManagerEvents = this.commissioningController.otaProvider.eventsOf(SoftwareUpdateManager);
        this.observers.on(updateManagerEvents.updateAvailable, (peer, details) => {
            logger.info(`Update available for peer `, peer, `:`, details);
            const nodeId = peer?.nodeId;
            if(!nodeId) {
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
        });

        // Query for updates now once
        const updates = await this.commissioningController.otaProvider.act(agent =>
            agent.get(SoftwareUpdateManager).queryUpdates()
        );
        if (updates && updates.length > 0) {
            for (const update of updates) {
                logger.info(`Update available for peer `, update.peerAddress, `:`, update.info);
                this.ws.sendEvent(EventType.UpdateAvailable, {
                    nodeId: update.peerAddress.nodeId.valueOf(),
                    ...update.info,
                });
            }
        }
    }

    /**
     * Connects to a node, setting up event listeners. If called multiple times for the same node, it will trigger a node reconnect.
     * If a connection timeout is provided, the function will return a promise that will resolve when the node is initialized or reject if the node
     * becomes disconnected or the timeout is reached. Note that the node will continue to connect in the background and the client will be notified
     * when the node is initialized through the NodeStateInformation event. To stop the reconnection, call the disconnectNode method.
     *
     * @param nodeId  The nodeId of the node to connect to
     * @param connectionTimeout Optional timeout in milliseconds. If omitted or non-positive, no timeout will be applied
     * @returns Promise that resolves when the node is initialized
     * @throws Error if connection times out or node becomes disconnected
     */
    async initializeNode(nodeId: string | number, connectionTimeout?: number): Promise<void> {
        if (this.commissioningController === undefined) {
            throw new Error("CommissioningController not initialized");
        }

        let node = this.nodes.get(NodeId(BigInt(nodeId)));
        if (node !== undefined) {
            node.triggerReconnect();

            return new Promise((resolve, reject) => {
                let timeoutId: NodeJS.Timeout | undefined;

                if (connectionTimeout && connectionTimeout > 0) {
                    timeoutId = setTimeout(() => {
                        logger.info(`Node ${node?.nodeId} state: ${node?.state}`);
                        if (
                            node?.connectionState === NodeStates.Disconnected ||
                            node?.connectionState === NodeStates.WaitingForDeviceDiscovery ||
                            node?.connectionState === NodeStates.Reconnecting
                        ) {
                            reject(new Error(`Node ${node?.nodeId} reconnection failed: ${NodeStates[node?.connectionState]}`));
                        } else {
                            reject(new Error(`Node ${node?.nodeId} reconnection timed out`));
                        }
                    }, connectionTimeout);
                }

                // Cancel timer if node initializes
                node?.events.initializedFromRemote.once(() => {
                    logger.info(`Node ${node?.nodeId} initialized from remote`);
                    if (timeoutId) clearTimeout(timeoutId);
                    // Send a connected event so the client knows the resume completed
                    this.ws.sendEvent(EventType.NodeStateInformation, {
                        nodeId: node!.nodeId,
                        state: NodeStates[NodeStates.Connected],
                        physicalProperties: extractPhysicalProperties(node),
                    });
                    resolve();
                });
            });
        }

        node = await this.commissioningController.getNode(NodeId(BigInt(nodeId)));
        if (node === undefined) {
            throw new Error(`Node ${nodeId} not connected`);
        }
        this.nodes.set(node.nodeId, node);

        // Remove any listeners left over from a previous registration of this same node instance
        // (e.g. after a decommission/re-commission cycle) so handlers do not accumulate.
        const observers = this.resetNodeObservers(node.nodeId);

        observers.on(node.events.stateChanged, info => {
            this.ws.sendEvent(EventType.NodeStateInformation, {
                nodeId: node!.nodeId,
                state: NodeStates[info],
            });
        });

        observers.on(node.events.structureChanged, () => {
            this.ws.sendEvent(EventType.NodeStateInformation, {
                nodeId: node!.nodeId,
                state: NodeState.STRUCTURE_CHANGED,
            });
        });

        observers.on(node.events.decommissioned, () => {
            this.disposeNodeObservers(node!.nodeId);
            this.nodes.delete(node!.nodeId);
            this.ws.sendEvent(EventType.NodeStateInformation, {
                nodeId: node!.nodeId,
                state: NodeState.DECOMMISSIONED,
            });
        });

        // attributeChanged and eventTriggered only need to be wired up once initialization completes,
        // to avoid forwarding the init state updates as user visible updates. Use once() so they are
        // wired exactly once per registration; the inner handlers are tracked by the observer group
        // (reset above) so they are still removed in bulk on re-registration or decommission.
        node.events.initializedFromRemote.once(() => {
            observers.on(node!.events.attributeChanged, data => {
                data.path.nodeId = node!.nodeId;
                this.ws.sendEvent(EventType.AttributeChanged, data);
            });

            observers.on(node!.events.eventTriggered, data => {
                data.path.nodeId = node!.nodeId;
                this.ws.sendEvent(EventType.EventTriggered, data);
            });

            // send a connected event in case the stateChanged transition
            // to connected was missed despite the early listener attachment above.
            this.ws.sendEvent(EventType.NodeStateInformation, {
                nodeId: node!.nodeId,
                state: NodeStates[NodeStates.Connected],
                physicalProperties: extractPhysicalProperties(node),
            });
        });

        node.connect();

        return new Promise((resolve, reject) => {
            let timeoutId: NodeJS.Timeout | undefined;

            if (connectionTimeout && connectionTimeout > 0) {
                timeoutId = setTimeout(() => {
                    logger.info(`Node ${node?.nodeId} initialization timed out`);

                    if (
                        node?.connectionState === NodeStates.Disconnected ||
                        node?.connectionState === NodeStates.WaitingForDeviceDiscovery
                    ) {
                        reject(new Error(`Node ${node.nodeId} connection failed: ${NodeStates[node.connectionState]}`));
                    } else {
                        reject(new Error(`Node ${node!.nodeId} connection timed out`));
                    }
                }, connectionTimeout);
            }

            node!.events.initializedFromRemote.once(() => {
                if (timeoutId) clearTimeout(timeoutId);
                resolve();
            });
        });
    }

    /**
     * Returns a node by nodeId.  If the node has not been initialized, it will throw an error.
     * @param nodeId
     * @returns
     */
    getNode(nodeId: number | string | NodeId) {
        if (this.commissioningController === undefined) {
            throw new Error("CommissioningController not initialized");
        }
        const node = this.nodes.get(NodeId(BigInt(nodeId)));
        if (node === undefined) {
            throw new Error(`Node ${nodeId} not connected`);
        }
        return node;
    }

    /**
     * Removes a node from the controller
     * @param nodeId
     */
    async removeNode(nodeId: number | string | NodeId) {
        const node = this.nodes.get(NodeId(BigInt(nodeId)));
        if (node !== undefined) {
            try {
                await node.decommission();
            } catch (error) {
                logger.error(`Error decommissioning node ${nodeId}: ${error} force removing node`);
                await this.commissioningController?.removeNode(NodeId(BigInt(nodeId)), false);
                this.disposeNodeObservers(node.nodeId);
                this.nodes.delete(NodeId(BigInt(nodeId)));
            }
        } else {
            await this.commissioningController?.removeNode(NodeId(BigInt(nodeId)), false);
        }
    }

    /**
     * Returns all commissioned nodes Ids
     * @returns
     */
    async getCommissionedNodes() {
        return this.commissioningController?.getCommissionedNodes();
    }

    /**
     * Finds the given endpoint, included nested endpoints
     * @param node
     * @param endpointId
     * @returns
     */
    getEndpoint(node: PairedNode, endpointId: number) {
        const endpoints = node.getDevices();
        for (const e of endpoints) {
            const endpoint = this.findEndpoint(e, endpointId);
            if (endpoint != undefined) {
                return endpoint;
            }
        }
        return undefined;
    }

    /**
     *
     * @param root Endpoints can have child endpoints. This function recursively searches for the endpoint with the given id.
     * @param endpointId
     * @returns
     */
    private findEndpoint(root: Endpoint, endpointId: number): Endpoint | undefined {
        if (root.number === endpointId) {
            return root;
        }
        for (const endpoint of root.getChildEndpoints()) {
            const found = this.findEndpoint(endpoint, endpointId);
            if (found !== undefined) {
                return found;
            }
        }
        return undefined;
    }

    /**
     * Serializes a node and sends it to the web socket
     * @param node
     * @param endpointId Optional endpointId to serialize. If omitted, all endpoints will be serialized.
     */
    sendSerializedNode(node: PairedNode, endpointId?: number) {
        this.serializePairedNode(node, endpointId)
            .then(data => {
                this.ws.sendEvent(EventType.NodeData, data);
            })
            .catch(error => {
                logger.error(`Error serializing node: ${error}`);
                printError(logger, error, "serializePairedNode");
                node.triggerReconnect();
            });
    }

    /**
     * Serializes a node and returns the json object
     * @param node
     * @param endpointId Optional endpointId to serialize. If omitted, the root endpoint will be serialized.
     * @returns
     */
    async serializePairedNode(node: PairedNode, endpointId?: number, requestFromRemote: boolean = true) {
        if (!this.commissioningController) {
            throw new Error("CommissioningController not initialized");
        }

        // Recursive function to build the hierarchy
        async function serializeEndpoint(endpoint: Endpoint): Promise<any> {
            const endpointData: any = {
                number: endpoint.number,
                clusters: {},
                children: [],
            };

            // Serialize clusters
            for (const cluster of endpoint.getAllClusterClients()) {
                if (!cluster.id) continue;

                const clusterData: any = {
                    id: cluster.id,
                    name: cluster.name,
                };

                // Serialize attributes
                for (const attributeName in cluster.attributes) {
                    // Skip numeric referenced attributes
                    if (/^\d+$/.test(attributeName)) continue;
                    const attribute = cluster.attributes[attributeName];
                    if (!attribute) continue;
                    const attributeValue = await attribute.get(requestFromRemote);
                    logger.debug(`Attribute ${attributeName} value: ${attributeValue}`);
                    if (attributeValue !== undefined) {
                        clusterData[attributeName] = attributeValue;
                    }
                }

                endpointData.clusters[cluster.name] = clusterData;
            }

            for (const child of endpoint.getChildEndpoints()) {
                endpointData.children.push(await serializeEndpoint(child));
            }

            return endpointData;
        }

        // Start serialization from the root endpoint
        const rootEndpoint = endpointId !== undefined ? this.getEndpoint(node, endpointId) : node.getRootEndpoint();
        if (rootEndpoint === undefined) {
            throw new Error(`Endpoint not found for node ${node.nodeId} and endpointId ${endpointId}`);
        }
        const data: any = {
            id: node.nodeId,
            rootEndpoint: await serializeEndpoint(rootEndpoint),
        };

        return data;
    }
}
