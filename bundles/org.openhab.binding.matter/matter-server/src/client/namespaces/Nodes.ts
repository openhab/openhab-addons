import { Logger, Seconds } from "@matter/general";
import { GeneralCommissioning, OperationalCredentials } from "@matter/main/clusters";
import { AttestationFinding, DeviceAttestationCheck, Read, SessionManager } from "@matter/main/protocol";
import { SoftwareUpdateManager } from "@matter/node";
import { OperationalCredentialsClient } from "@matter/node/behaviors/operational-credentials";
import {
    AttributeId,
    ClusterId,
    EndpointNumber,
    FabricIndex,
    ManualPairingCodeCodec,
    NodeId,
    QrCode,
    QrPairingCodeCodec,
} from "@matter/types";
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
        if (!this.controllerNode.isStructureKnown(node)) {
            throw new Error(`Node ${nodeId} not initialized`);
        }
        return this.controllerNode.sendSerializedNode(node);
    }

    /**
     * Requests all attributes data for all nodes for debugging purposes
     * @returns
     */
    async getAllDataForAllNodes() {
        const data: any[] = [];
        for (const nodeId of this.controllerNode.getCommissionedNodes()) {
            try {
                const node = this.controllerNode.getNode(nodeId);
                if (!node.lifecycle.isConnected) {
                    continue;
                }
                data.push(await this.controllerNode.serializeNode(node));
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
        if (!this.controllerNode.isStructureKnown(node)) {
            throw new Error(`Node ${nodeId} not initialized`);
        }
        return this.controllerNode.sendSerializedNode(node, endpointId);
    }

    /**
     * Pairs a node using a pairing code, supports multiple pairing code formats
     * @param pairingCode
     * @param shortDiscriminator
     * @param setupPinCode
     * @returns the node id and the device certificate warnings that did not stop pairing
     */
    async pairNode(
        pairingCode: string | undefined,
        shortDiscriminator: number | undefined,
        setupPinCode: number | undefined,
    ) {
        let discriminator: number | undefined;

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
            }
        } else if (shortDiscriminator === undefined) {
            discriminator = 3840;
        }
        if (setupPinCode === undefined) {
            throw new Error("No pairing code or setup PIN provided");
        }

        // Only pass network credentials when they are actually present and non-empty. matter.js rejects an
        // empty wifiNetwork/threadNetwork with "must not be empty" before commissioning even starts. Devices that
        // are already on their operational network (e.g. a Thread device joined externally beforehand) are
        // commissioned over IP without re-provisioning, so omitting the credentials here is the correct behaviour.
        const store = this.controllerNode.Store;
        const wifiSsid = await store.get<string>("WiFiSsid", "");
        const wifiCredentials = await store.get<string>("WiFiPassword", "");
        const threadName = await store.get<string>("ThreadName", "");
        const threadOperationalDataset = await store.get<string>("ThreadOperationalDataset", "");

        // Wait for the certificate list, as pairing checks the device's certificates against it
        await this.controllerNode.certificateService.construction;
        // A set, as matter.js checks again when it retries commissioning
        const warnings = new Set<string>();
        let rejection: string | undefined;
        const checkAttestation = (findings: AttestationFinding[]) => {
            // A device with a test certificate is a development or uncertified device. It may pair, with a warning.
            const errors = findings.filter(
                ({ level, type }) => level === "error" && type !== DeviceAttestationCheck.TrustedAsTestCertificate,
            );
            if (errors.length > 0) {
                rejection = `Device certificate check failed: ${errors.map(({ message }) => message).join(", ")}`;
                return false;
            }
            for (const { level, type, message } of findings) {
                if (type === DeviceAttestationCheck.TrustedAsTestCertificate) {
                    warnings.add("The device uses a test certificate, so it is a development or uncertified device");
                } else if (level !== "info") {
                    warnings.add(message);
                }
            }
            return true;
        };

        let node;
        try {
            node = await this.controllerNode.node.peers.commission({
                ...(discriminator !== undefined ? { longDiscriminator: discriminator } : { shortDiscriminator }),
                discoveryCapabilities: { onIpNetwork: true },
                passcode: setupPinCode,
                fabric: this.controllerNode.fabric,
                regulatoryLocation: GeneralCommissioning.RegulatoryLocationType.Outdoor, // Set to the most restrictive if relevant
                regulatoryCountryCode: "XX",
                onAttestationFailure: checkAttestation,
                // openHAB subscribes once the Thing is initialized
                autoSubscribe: false,
                ...(wifiSsid.length > 0 && wifiCredentials.length > 0
                    ? { wifiNetwork: { wifiSsid, wifiCredentials } }
                    : undefined),
                ...(threadName.length > 0 && threadOperationalDataset.length > 0
                    ? { threadNetwork: { networkName: threadName, operationalDataset: threadOperationalDataset } }
                    : undefined),
            });
        } catch (error) {
            // matter.js reports a rejected attestation as a general commissioning error, so give the actual reason
            if (rejection !== undefined) {
                throw new Error(rejection, { cause: error });
            }
            throw error;
        }

        const commissionedNodeId = node.peerAddress?.nodeId;
        logger.info(`Commissioned Node: ${commissionedNodeId}`);
        if (warnings.size > 0) {
            logger.info(`Device certificate warnings for node ${commissionedNodeId}: ${[...warnings].join(", ")}`);
        }
        return { nodeId: commissionedNodeId, warnings: [...warnings] };
    }

    /**
     * Disconnects a node
     * @param nodeId
     */
    async disconnectNode(nodeId: number | string) {
        const node = this.controllerNode.getNode(nodeId);
        await node.stop();
    }

    /**
     * Reconnects a node
     * @param nodeId
     */
    async reconnectNode(nodeId: number | string) {
        const node = this.controllerNode.getNode(nodeId);
        if (node.lifecycle.isConnected) {
            return;
        }
        await node.stop();
        await node.start();
    }

    /**
     * Returns the fabrics for a node. Fabrics are the set of matter networks that the node has been commissioned to (openhab, Alexa, Google, Apple, etc)
     * @param nodeId
     * @returns
     */
    async getFabrics(nodeId: number | string) {
        const node = this.controllerNode.getNode(nodeId);
        const read = Read({
            attributes: [
                {
                    endpointId: EndpointNumber(0),
                    clusterId: ClusterId(OperationalCredentials.Cluster.id),
                    attributeId: AttributeId(OperationalCredentials.Cluster.attributes.fabrics.id),
                },
            ],
            // Include the fabrics of other controllers
            fabricFilter: false,
        });
        for await (const chunk of node.interaction.read(read)) {
            for await (const report of chunk) {
                if (report.kind === "attr-value") {
                    return report.value;
                }
            }
        }
        throw new Error(`Fabrics for node ${nodeId} not reported`);
    }

    /**
     * Removes a fabric from a node, effectively decommissioning the node from the specific network
     * @param nodeId
     * @param index
     * @returns
     */
    async removeFabric(nodeId: number | string, index: number) {
        const node = this.controllerNode.getNode(nodeId);
        const fabricIndex = FabricIndex(index);
        if ((await this.getOurFabricIndex(nodeId)) === fabricIndex) {
            throw new Error("Will not delete our own fabric");
        }
        await node.commandsOf(OperationalCredentialsClient).removeFabric({ fabricIndex });
    }

    async getOurFabricIndex(nodeId: number | string) {
        const node = this.controllerNode.getNode(nodeId);
        return node.stateOf(OperationalCredentialsClient).currentFabricIndex;
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
        return this.controllerNode.node.env.get(SessionManager).getActiveSessionInformation();
    }

    /**
     * Opens a basic commissioning window for a node allowing for manual pairing to an additional fabric.
     * @param nodeId
     * @param timeout
     */
    async basicCommissioningWindow(nodeId: number | string, timeout = 900) {
        const node = this.controllerNode.getNode(nodeId);
        await node.openBasicCommissioningWindow(Seconds(timeout));
        logger.info(`Basic Commissioning Window for node ${nodeId} opened`);
    }

    /**
     * Opens an enhanced commissioning window for a node allowing for QR code pairing to an additional fabric.
     * @param nodeId
     * @param timeout
     * @returns
     */
    async enhancedCommissioningWindow(nodeId: number | string, timeout = 900) {
        const node = this.controllerNode.getNode(nodeId);
        const { qrPairingCode, manualPairingCode } = await node.openEnhancedCommissioningWindow(Seconds(timeout));

        logger.info(`Enhanced Commissioning Window for node ${nodeId} opened`);
        logger.debug(QrCode.get(qrPairingCode));
        logger.debug(`QR Code URL: https://project-chip.github.io/connectedhomeip/qrcode.html?data=${qrPairingCode}`);
        logger.info(`Manual pairing code: ${manualPairingCode}`);
        return { qrPairingCode, manualPairingCode };
    }

    /**
     * Logs the structure of a node
     * @param nodeId
     */
    async logNode(nodeId: number | string) {
        const node = this.controllerNode.getNode(nodeId);
        logger.info("Logging structure of Node ", nodeId.toString());
        logger.info(node);
    }

    async otaQueryForUpdates(nodeId: number | string, includeStoredUpdates: boolean = false) {
        const node = this.controllerNode.getNode(nodeId);
        // Query a specific node for updates
        const updates = await this.controllerNode.otaProvider.act(agent =>
            agent.get(SoftwareUpdateManager).queryUpdates({ peerToCheck: node, includeStoredUpdates }),
        );
        if (updates && updates.length > 0) {
            return updates[0].info;
        }
        return undefined;
    }

    async otaStartUpdate(nodeId: number | string): Promise<void> {
        const updateInfo = await this.otaQueryForUpdates(nodeId, true);
        if (updateInfo === undefined) {
            throw new Error(`No update info found for node ${nodeId}`);
        }
        const { vendorId, productId, softwareVersion, softwareVersionString } = updateInfo;
        logger.info(
            `Updating node ${nodeId} to version ${softwareVersionString} with vendorId ${vendorId} and productId ${productId}`,
        );
        const peerAddress = this.controllerNode.fabric.addressOf(NodeId(BigInt(nodeId)));
        await this.controllerNode.otaProvider.act(agent =>
            agent
                .get(SoftwareUpdateManager)
                .forceUpdate(peerAddress, { vendorId, productId, targetSoftwareVersion: softwareVersion }),
        );
    }

    async otaCancelUpdate(nodeId: number | string): Promise<void> {
        const peerAddress = this.controllerNode.fabric.addressOf(NodeId(BigInt(nodeId)));
        await this.controllerNode.otaProvider.act(agent => agent.get(SoftwareUpdateManager).removeConsent(peerAddress));
        logger.info(`Consent removed for node ${nodeId}`);
    }

    /**
     * Returns the check-in state of a sleepy device (ICD)
     * @param nodeId
     */
    async icdState(nodeId: number | string) {
        return this.controllerNode.icdState(this.controllerNode.getNode(nodeId));
    }

    /**
     * Asks a sleepy device (ICD) to stay awake, for example before sending it several commands. Only devices that
     * support long idle times accept this.
     * @param nodeId
     * @param duration requested time in milliseconds
     * @returns the time in milliseconds the device promised to stay awake
     */
    async icdStayActive(nodeId: number | string, duration: number) {
        return this.controllerNode.icdStayActive(this.controllerNode.getNode(nodeId), duration);
    }
}
