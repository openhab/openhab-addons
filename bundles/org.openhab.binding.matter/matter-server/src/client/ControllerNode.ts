// Include this first to auto-register Crypto, Network and Time Node.js implementations
import { Environment, Logger, StorageContext } from "@matter/general";
import { NodeId } from "@matter/types";
import { CommissioningController, ControllerStore } from "@project-chip/matter.js";
import { Endpoint, NodeStates, PairedNode } from "@project-chip/matter.js/device";
import { WebSocketSession } from "../app";
import { EventType, NodeState } from "../MessageTypes";
import { printError } from "../util/error";
const logger = Logger.get("ControllerNode");

/**
 * This class represents the Matter Controller / Admin client
 */
export class ControllerNode {
    private environment: Environment = Environment.default;
    private storageContext?: StorageContext;
    private nodes: Map<NodeId, PairedNode> = new Map();
    commissioningController?: CommissioningController;

    constructor(
        private readonly storageLocation: string,
        private readonly controllerName: string,
        private readonly nodeNum: number,
        private readonly ws: WebSocketSession,
        private readonly netInterface?: string,
    ) {}

    get Store() {
        if (!this.storageContext) {
            throw new Error("Storage uninitialized");
        }
        return this.storageContext;
    }

    /**
     * Closes the controller node
     */
    async close() {
        await this.commissioningController?.close();
        this.nodes.clear();
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
        });
        await this.commissioningController.initializeControllerStore();

        const controllerStore = this.environment.get(ControllerStore);
        // TODO: Implement resetStorage
        // if (resetStorage) {
        //     await controllerStore.erase();
        // }
        this.storageContext = controllerStore.storage.createContext("Node");

        if (await this.Store.has("ControllerFabricLabel")) {
            await this.commissioningController.updateFabricLabel(
                await this.Store.get<string>("ControllerFabricLabel", fabricLabel),
            );
        }

        await this.commissioningController.start();
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
                            node?.state === NodeStates.Disconnected ||
                            node?.state === NodeStates.WaitingForDeviceDiscovery ||
                            node?.state === NodeStates.Reconnecting
                        ) {
                            reject(new Error(`Node ${node?.nodeId} reconnection failed: ${NodeStates[node?.state]}`));
                        } else {
                            reject(new Error(`Node ${node?.nodeId} reconnection timed out`));
                        }
                    }, connectionTimeout);
                }

                // Cancel timer if node initializes
                node?.events.initializedFromRemote.once(() => {
                    logger.info(`Node ${node?.nodeId} initialized from remote`);
                    if (timeoutId) clearTimeout(timeoutId);
                    resolve();
                });
            });
        }

        node = await this.commissioningController.getNode(NodeId(BigInt(nodeId)));
        if (node === undefined) {
            throw new Error(`Node ${nodeId} not connected`);
        }
        node.connect();
        this.nodes.set(node.nodeId, node);

        // register event listeners once the node is fully connected
        node.events.initializedFromRemote.once(() => {
            node.events.attributeChanged.on(data => {
                data.path.nodeId = node.nodeId;
                this.ws.sendEvent(EventType.AttributeChanged, data);
            });

            node.events.eventTriggered.on(data => {
                data.path.nodeId = node.nodeId;
                this.ws.sendEvent(EventType.EventTriggered, data);
            });

            node.events.stateChanged.on(info => {
                const data: any = {
                    nodeId: node.nodeId,
                    state: NodeStates[info],
                };
                this.ws.sendEvent(EventType.NodeStateInformation, data);
            });

            node.events.structureChanged.on(() => {
                const data: any = {
                    nodeId: node.nodeId,
                    state: NodeState.STRUCTURE_CHANGED,
                };
                this.ws.sendEvent(EventType.NodeStateInformation, data);
            });

            node.events.decommissioned.on(() => {
                this.nodes.delete(node.nodeId);
                const data: any = {
                    nodeId: node.nodeId,
                    state: NodeState.DECOMMISSIONED,
                };
                this.ws.sendEvent(EventType.NodeStateInformation, data);
            });
        });

        return new Promise((resolve, reject) => {
            let timeoutId: NodeJS.Timeout | undefined;

            if (connectionTimeout && connectionTimeout > 0) {
                timeoutId = setTimeout(() => {
                    logger.info(`Node ${node?.nodeId} initialization timed out`);

                    // register a listener to send the node state information once the node is connected at some future time
                    node.events.initializedFromRemote.once(() => {
                        const data: any = {
                            nodeId: node.nodeId,
                            state: NodeStates.Connected,
                        };
                        this.ws.sendEvent(EventType.NodeStateInformation, data);
                    });

                    if (
                        node?.state === NodeStates.Disconnected ||
                        node?.state === NodeStates.WaitingForDeviceDiscovery
                    ) {
                        reject(new Error(`Node ${node.nodeId} connection failed: ${NodeStates[node.state]}`));
                    } else {
                        reject(new Error(`Node ${node.nodeId} connection timed out`));
                    }
                }, connectionTimeout);
            }

            node.events.initializedFromRemote.once(() => {
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
        //const node = await this.commissioningController.connectNode(NodeId(BigInt(nodeId)))
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
    async serializePairedNode(node: PairedNode, endpointId?: number) {
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
                    const attributeValue = await attribute.get();
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
