import { Logger } from "@matter/main";
import { GeneralCommissioning, OperationalCredentialsCluster } from "@matter/main/clusters";
import { FabricIndex, ManualPairingCodeCodec, NodeId, QrCode, QrPairingCodeCodec } from "@matter/types";
import { NodeCommissioningOptions } from "@project-chip/matter.js";
import { ControllerNode } from "../ControllerNode";

const logger = Logger.get("matter");

/**
 * This class is used for exposing Matter nodes. This includes node lifecycle functions, node fabrics, node data, and other node related methods to websocket clients.
 * Methods not marked as private are intended to be exposed to websocket clients
 */
export class Nodes {
    constructor(private controllerNode: ControllerNode) {}

    /**
     * Returns all commissioned nodes Ids
     * @returns
     */
    async listNodes() {
        if (this.controllerNode.commissioningController === undefined) {
            throw new Error("CommissioningController not initialized");
        }
        return this.controllerNode.getCommissionedNodes();
    }

    /**
     * Initializes a node and connects to it
     * @param nodeId
     * @returns
     */
    async initializeNode(nodeId: string | number) {
        return this.controllerNode.initializeNode(nodeId);
    }

    /**
     * Requests all attributes data for a node
     * @param nodeId
     * @returns
     */
    async requestAllData(nodeId: string | number) {
        const node = this.controllerNode.getNode(nodeId);
        if (node.initialized) {
            return this.controllerNode.sendSerializedNode(node);
        } else {
            throw new Error(`Node ${nodeId} not initialized`);
        }
    }

    /**
     * Requests all attributes data for all nodes for debugging purposes
     * @returns
     */
    async getAllDataForAllNodes() {
        const nodeIds = await this.controllerNode.getCommissionedNodes();
        const data: any[] = [];
        if (nodeIds === undefined) {
            return data;
        }
        for (const nodeId of nodeIds) {
            try {
                const node = this.controllerNode.getNode(nodeId);
                if (!node.isConnected) {
                    continue;
                }
                const nodeData = await this.controllerNode.serializePairedNode(node);
                data.push(nodeData);
            } catch (error) {
                logger.error(`Error serializing node ${nodeId}: ${error}`);
            }
        }
        return data;
    }

    /**
     * Requests all attributes data for a single endpoint and its children
     * @param nodeId
     * @param endpointId
     * @returns
     */
    async requestEndpointData(nodeId: string | number, endpointId: number) {
        const node = this.controllerNode.getNode(nodeId);
        if (node.initialized) {
            return this.controllerNode.sendSerializedNode(node, endpointId);
        } else {
            throw new Error(`Node ${nodeId} not initialized`);
        }
    }

    /**
     * Pairs a node using a pairing code, supports multiple pairing code formats
     * @param pairingCode
     * @param shortDiscriminator
     * @param setupPinCode
     * @returns
     */
    async pairNode(
        pairingCode: string | undefined,
        shortDiscriminator: number | undefined,
        setupPinCode: number | undefined,
    ) {
        let discriminator: number | undefined;
        let nodeIdStr: string | undefined;
        let ipPort: number | undefined;
        let ip: string | undefined;
        let instanceId: string | undefined;
        const ble = false;

        if (typeof pairingCode === "string" && pairingCode.trim().length > 0) {
            pairingCode = pairingCode.trim();
            if (pairingCode.toUpperCase().indexOf("MT:") == 0) {
                const qrcode = QrPairingCodeCodec.decode(pairingCode.toUpperCase())[0];
                setupPinCode = qrcode.passcode;
                discriminator = qrcode.discriminator;
            } else {
                const { shortDiscriminator: pairingCodeShortDiscriminator, passcode } =
                    ManualPairingCodeCodec.decode(pairingCode);
                shortDiscriminator = pairingCodeShortDiscriminator;
                setupPinCode = passcode;
                discriminator = undefined;
            }
        } else if (discriminator === undefined && shortDiscriminator === undefined) {
            discriminator = 3840;
        }

        const nodeId = nodeIdStr !== undefined ? NodeId(BigInt(nodeIdStr)) : undefined;
        if (this.controllerNode.commissioningController === undefined) {
            throw new Error("CommissioningController not initialized");
        }

        const options = {
            discovery: {
                knownAddress: ip !== undefined && ipPort !== undefined ? { ip, port: ipPort, type: "udp" } : undefined,
                identifierData:
                    instanceId !== undefined
                        ? { instanceId }
                        : discriminator !== undefined
                          ? { longDiscriminator: discriminator }
                          : shortDiscriminator !== undefined
                            ? { shortDiscriminator }
                            : {},
                discoveryCapabilities: {
                    ble,
                    onIpNetwork: true,
                },
            },
            passcode: setupPinCode,
        } as NodeCommissioningOptions;

        options.commissioning = {
            nodeId: nodeId !== undefined ? NodeId(nodeId) : undefined,
            regulatoryLocation: GeneralCommissioning.RegulatoryLocationType.Outdoor, // Set to the most restrictive if relevant
            regulatoryCountryCode: "XX",
        };

        if (this.controllerNode.Store.has("WiFiSsid") && this.controllerNode.Store.has("WiFiPassword")) {
            options.commissioning.wifiNetwork = {
                wifiSsid: await this.controllerNode.Store.get<string>("WiFiSsid", ""),
                wifiCredentials: await this.controllerNode.Store.get<string>("WiFiPassword", ""),
            };
        }
        if (this.controllerNode.Store.has("ThreadName") && this.controllerNode.Store.has("ThreadOperationalDataset")) {
            options.commissioning.threadNetwork = {
                networkName: await this.controllerNode.Store.get<string>("ThreadName", ""),
                operationalDataset: await this.controllerNode.Store.get<string>("ThreadOperationalDataset", ""),
            };
        }

        const commissionedNodeId = await this.controllerNode.commissioningController.commissionNode(options);

        console.log(`Commissioned Node: ${commissionedNodeId}`);
        return commissionedNodeId;
    }

    /**
     * Disconnects a node
     * @param nodeId
     */
    async disconnectNode(nodeId: number | string) {
        const node = this.controllerNode.getNode(nodeId);
        await node.disconnect();
    }

    /**
     * Reconnects a node
     * @param nodeId
     */
    async reconnectNode(nodeId: number | string) {
        const node = this.controllerNode.getNode(nodeId);
        node.triggerReconnect();
    }

    /**
     * Returns the fabrics for a node. Fabrics are the set of matter networks that the node has been commissioned to (openhab, Alexa, Google, Apple, etc)
     * @param nodeId
     * @returns
     */
    async getFabrics(nodeId: number | string) {
        const node = this.controllerNode.getNode(nodeId);
        const operationalCredentialsCluster = node.getRootClusterClient(OperationalCredentialsCluster);
        if (operationalCredentialsCluster === undefined) {
            throw new Error(`OperationalCredentialsCluster for node ${nodeId} not found.`);
        }
        return await operationalCredentialsCluster.getFabricsAttribute(true, false);
    }

    /**
     * Removes a fabric from a node, effectively decommissioning the node from the specific network
     * @param nodeId
     * @param index
     * @returns
     */
    async removeFabric(nodeId: number | string, index: number) {
        if (this.controllerNode.commissioningController === undefined) {
            console.log("Controller not initialized, nothing to disconnect.");
            return;
        }

        const node = this.controllerNode.getNode(nodeId);
        if (node === undefined) {
            throw new Error(`Node ${nodeId} not found`);
        }
        const operationalCredentialsCluster = node.getRootClusterClient(OperationalCredentialsCluster);

        if (operationalCredentialsCluster === undefined) {
            throw new Error(`OperationalCredentialsCluster for node ${nodeId} not found.`);
        }

        const fabricInstance = FabricIndex(index);
        const ourFabricIndex = await operationalCredentialsCluster.getCurrentFabricIndexAttribute(true);

        if (ourFabricIndex == fabricInstance) {
            throw new Error("Will not delete our own fabric");
        }

        await operationalCredentialsCluster.commands.removeFabric({ fabricIndex: fabricInstance });
    }

    /**
     * Removes a node from the commissioning controller
     * @param nodeId
     */
    async removeNode(nodeId: number | string) {
        await this.controllerNode.removeNode(nodeId);
    }

    /**
     * Returns active session information for all connected nodes.
     * @returns
     */
    sessionInformation() {
        return this.controllerNode.commissioningController?.getActiveSessionInformation() || {};
    }

    /**
     * Opens a basic commissioning window for a node allowing for manual pairing to an additional fabric.
     * @param nodeId
     * @param timeout
     */
    async basicCommissioningWindow(nodeId: number | string, timeout = 900) {
        const node = this.controllerNode.getNode(nodeId);
        await node.openBasicCommissioningWindow(timeout);
        console.log(`Basic Commissioning Window for node ${nodeId} opened`);
    }

    /**
     * Opens an enhanced commissioning window for a node allowing for QR code pairing to an additional fabric.
     * @param nodeId
     * @param timeout
     * @returns
     */
    async enhancedCommissioningWindow(nodeId: number | string, timeout = 900) {
        const node = this.controllerNode.getNode(nodeId);
        const data = await node.openEnhancedCommissioningWindow(timeout);

        console.log(`Enhanced Commissioning Window for node ${nodeId} opened`);
        const { qrPairingCode, manualPairingCode } = data;

        console.log(QrCode.get(qrPairingCode));
        console.log(`QR Code URL: https://project-chip.github.io/connectedhomeip/qrcode.html?data=${qrPairingCode}`);
        console.log(`Manual pairing code: ${manualPairingCode}`);
        return data;
    }

    /**
     * Logs the structure of a node
     * @param nodeId
     */
    async logNode(nodeId: number | string) {
        const node = this.controllerNode.getNode(nodeId);
        console.log("Logging structure of Node ", node.nodeId.toString());
        node.logStructure();
    }
}
