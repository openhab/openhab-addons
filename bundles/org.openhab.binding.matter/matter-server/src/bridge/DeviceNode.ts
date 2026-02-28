// Include this first to auto-register Crypto, Network and Time Node.js implementations
import "@matter/node";

import { Environment, Logger, MatterAggregateError, StorageService } from "@matter/general";
import { Endpoint, ServerNode } from "@matter/node";
import { BasicInformationServer } from "@matter/node/behaviors";
import { AggregatorEndpoint } from "@matter/node/endpoints";
import { DeviceCommissioner, FabricManager } from "@matter/protocol";
import { FabricIndex, VendorId } from "@matter/types";
import { BridgeEvent, BridgeEventType, EventType } from "../MessageTypes";
import { BridgeController } from "./BridgeController";
import { DeviceFunctions } from "./DeviceFunctions";

import {
    BaseDeviceType,
    ColorDeviceType,
    ContactSensorDeviceType,
    DimmableDeviceType,
    DoorLockDeviceType,
    FanDeviceType,
    HumiditySensorType,
    OccupancySensorDeviceType,
    OnOffLightDeviceType,
    OnOffPlugInDeviceType,
    TemperatureSensorType,
    ThermostatDeviceType,
    WindowCoveringDeviceType,
} from "./devices";
import { ModeSelectDeviceType } from "./devices/ModeSelectDeviceType";

type DeviceType =
    | OnOffLightDeviceType
    | OnOffPlugInDeviceType
    | DimmableDeviceType
    | ThermostatDeviceType
    | WindowCoveringDeviceType
    | DoorLockDeviceType
    | TemperatureSensorType
    | HumiditySensorType
    | OccupancySensorDeviceType
    | ContactSensorDeviceType
    | FanDeviceType
    | ColorDeviceType
    | ModeSelectDeviceType;

const logger = Logger.get("DeviceNode");

/**
 * This represents the root device node for the Matter Bridge, creates the initial aggregator endpoint, adds devices to the bridge, and manages storage
 */
export class DeviceNode {
    static DEFAULT_NODE_ID = "oh-bridge";

    private server: ServerNode | null = null;
    #environment: Environment = Environment.default;

    private aggregator: Endpoint<AggregatorEndpoint> | null = null;
    private devices: Map<string, BaseDeviceType> = new Map();
    private storageService: StorageService;
    private inCommissioning: boolean = false;
    private deviceFunctions: DeviceFunctions;

    constructor(
        private bridgeController: BridgeController,
        private storagePath: string,
        private deviceName: string,
        private vendorName: string,
        private passcode: number,
        private discriminator: number,
        private vendorId: number,
        private productName: string,
        private productId: number,
        private port: number,
    ) {
        logger.info(`Device Node Storage location: ${this.storagePath} (Directory)`);
        this.#environment.vars.set("storage.path", this.storagePath);
        this.storageService = this.#environment.get(StorageService);
        this.deviceFunctions = new DeviceFunctions(this.bridgeController);
        this.#environment.set(DeviceFunctions, this.deviceFunctions);
    }

    //public methods

    public async initializeBridge(resetStorage: boolean = false) {
        await this.close();
        logger.info(`Initializing bridge`);
        await this.#init();
        if (resetStorage) {
            logger.info(`!!! Erasing ServerNode Storage !!!`);
            await this.server?.erase();
            await this.close();
            // generate a new uniqueId for the bridge (bridgeBasicInformation.uniqueId)
            const ohStorage = await this.#ohBridgeStorage();
            await ohStorage.set("basicInformation.uniqueId", BasicInformationServer.createUniqueId());
            logger.info(`Initializing bridge again`);
            await this.#init();
        }
        logger.info(`Bridge initialized`);
    }

    public async startBridge() {
        if (this.devices.size === 0) {
            throw new Error("No devices added, not starting");
        }
        if (!this.server) {
            throw new Error("Server not initialized, not starting");
        }
        if (this.server.lifecycle.isOnline) {
            throw new Error("Server is already started, not starting");
        }
        this.server.lifecycle.online.once(() => {
            logger.info(`Bridge online`);
            this.server?.events.commissioning.enabled$Changed.on(async () => {
                logger.info(`Commissioning state changed to ${this.server?.state.commissioning.enabled}`);
                this.inCommissioning = this.server?.state.commissioning.enabled ?? false;
                this.#sendCommissioningStatus();
            });
            this.#sendCommissioningStatus();
        });
        logger.info(this.server);
        logger.info(`Starting bridge`);
        await this.server.start();
        logger.info(`Bridge started`);
        const ohStorage = await this.#ohBridgeStorage();
        await ohStorage.set("lastStart", Date.now());
    }

    public async close() {
        await this.server?.close();
        this.server = null;
        this.devices.clear();
    }

    public async addEndpoint(
        deviceType: string,
        id: string,
        nodeLabel: string,
        productName: string,
        productLabel: string,
        serialNumber: string,
        attributeMap: { [key: string]: any },
    ) {
        if (this.devices.has(id)) {
            throw new Error(`Device ${id} already exists!`);
        }

        if (!this.aggregator) {
            throw new Error(`Aggregator not initialized, aborting.`);
        }

        // little hack to get the correct device class and initialize it with the correct parameters once
        const deviceTypeMap: {
            [key: string]: new (
                bridgeController: BridgeController,
                attributeMap: { [key: string]: any },
                id: string,
                nodeLabel: string,
                productName: string,
                productLabel: string,
                serialNumber: string,
            ) => DeviceType;
        } = {
            OnOffLight: OnOffLightDeviceType,
            OnOffPlugInUnit: OnOffPlugInDeviceType,
            DimmableLight: DimmableDeviceType,
            Thermostat: ThermostatDeviceType,
            WindowCovering: WindowCoveringDeviceType,
            DoorLock: DoorLockDeviceType,
            TemperatureSensor: TemperatureSensorType,
            HumiditySensor: HumiditySensorType,
            OccupancySensor: OccupancySensorDeviceType,
            ContactSensor: ContactSensorDeviceType,
            Fan: FanDeviceType,
            ColorLight: ColorDeviceType,
            ModeSelect: ModeSelectDeviceType,
        };

        const DeviceClass = deviceTypeMap[deviceType];
        if (!DeviceClass) {
            throw new Error(`Unsupported device type ${deviceType}`);
        }
        const device = new DeviceClass(
            this.bridgeController,
            attributeMap,
            id,
            nodeLabel,
            productName,
            productLabel,
            serialNumber,
        );
        this.devices.set(id, device);
        try {
            await this.aggregator.add(device.endpoint);
        } catch (e) {
            logger.error(`Error adding device ${id} to aggregator: ${e}`);
            if (e instanceof MatterAggregateError) {
                const errors = e.errors;
                logger.error(`Name: ${e.name} Message: ${e.message} Cause: ${e.cause}`);
                let errorMessage = "";
                for (const error of errors) {
                    errorMessage += `Error: ${error.message} Cause: ${error.cause}\n`;
                }
                logger.error(errorMessage);
                throw new Error(errorMessage);
            }
            throw e;
        }
    }

    public async setEndpointStates(
        endpointId: string,
        states: { clusterName: string; attributeName: string; state: any }[],
    ) {
        const device = this.devices.get(endpointId);
        if (device) {
            // First notify waiters about the incoming states
            this.deviceFunctions.notifyStateWaiters(endpointId, states);
            void device.updateStates(states);
        }
    }

    public async openCommissioningWindow() {
        const dc = this.#getStartedServer().env.get(DeviceCommissioner);
        logger.debug("opening basic commissioning window");
        await dc.allowBasicCommissioning(() => {
            logger.debug("commissioning window closed");
        });
        logger.debug("basic commissioning window open");
    }

    public async closeCommissioningWindow() {
        const server = this.#getStartedServer();
        if (!server.state.commissioning.commissioned) {
            throw new Error("Bridge is not commissioned, not closing commissioning window");
        }
        const dc = server.env.get(DeviceCommissioner);
        logger.debug("closing basic commissioning window");
        await dc.endCommissioning();
    }

    public getCommissioningState() {
        const server = this.#getStartedServer();
        return {
            pairingCodes: {
                manualPairingCode: server.state.commissioning.pairingCodes.manualPairingCode,
                qrPairingCode: server.state.commissioning.pairingCodes.qrPairingCode,
            },
            commissioningWindowOpen: !server.state.commissioning.commissioned || this.inCommissioning,
        };
    }

    public getFabrics() {
        const fabricManager = this.#getStartedServer().env.get(FabricManager);
        return fabricManager.fabrics;
    }

    public async removeFabric(fabricIndex: number) {
        const fabricManager = this.#getStartedServer().env.get(FabricManager);
        await fabricManager.removeFabric(FabricIndex(fabricIndex));
    }

    //private methods

    async #init() {
        const ohStorage = await this.#ohBridgeStorage();
        const uniqueId = await this.#uniqueIdForBridge();
        logger.info(`Unique ID: ${uniqueId}`);
        /**
         * Create a Matter ServerNode, which contains the Root Endpoint and all relevant data and configuration
         */
        try {
            this.server = await ServerNode.create({
                // Required: Give the Node a unique ID which is used to store the state of this node
                id: DeviceNode.DEFAULT_NODE_ID,

                // Provide Network relevant configuration like the port
                // Optional when operating only one device on a host, Default port is 5540
                network: {
                    port: this.port,
                },

                // Provide Commissioning relevant settings
                // Optional for development/testing purposes
                commissioning: {
                    passcode: this.passcode,
                    discriminator: this.discriminator,
                },

                // Provide Node announcement settings
                // Optional: If Ommitted some development defaults are used
                productDescription: {
                    name: this.deviceName,
                    deviceType: AggregatorEndpoint.deviceType,
                },

                // Provide defaults for the BasicInformation cluster on the Root endpoint
                // Optional: If Omitted some development defaults are used
                basicInformation: {
                    vendorName: this.vendorName,
                    vendorId: VendorId(this.vendorId),
                    nodeLabel: this.productName,
                    productName: this.productName,
                    productLabel: this.productName,
                    productId: this.productId,
                    uniqueId: uniqueId,
                },
            });
            this.aggregator = new Endpoint(AggregatorEndpoint, { id: "aggregator" });
            await this.server.add(this.aggregator);
            await ohStorage.set("basicInformation.uniqueId", uniqueId);
            logger.info(`ServerNode created with uniqueId: ${uniqueId}`);
        } catch (e) {
            logger.error(`Error starting server: ${e}`);
            throw e;
        }
    }

    #getStartedServer() {
        if (!this.server || !this.server.lifecycle.isOnline) {
            throw new Error("Server not ready");
        }
        return this.server;
    }

    async #ohBridgeStorage() {
        return (await this.storageService.open(DeviceNode.DEFAULT_NODE_ID)).createContext("openhab");
    }

    async #uniqueIdForBridge() {
        const rootContext = await this.#ohBridgeStorage();
        return rootContext.get("basicInformation.uniqueId", BasicInformationServer.createUniqueId());
    }

    #sendCommissioningStatus() {
        const state = this.getCommissioningState();
        const be: BridgeEvent = {
            type: BridgeEventType.EventTriggered,
            data: {
                eventName: state.commissioningWindowOpen ? "commissioningWindowOpen" : "commissioningWindowClosed",
                data: state.pairingCodes,
            },
        };
        this.bridgeController.ws.sendEvent(EventType.BridgeEvent, be);
    }
}
