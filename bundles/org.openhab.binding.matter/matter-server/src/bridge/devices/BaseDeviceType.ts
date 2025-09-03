import { Endpoint, Logger } from "@matter/main";
import { FixedLabelServer } from "@matter/main/behaviors";
import { BridgedDeviceBasicInformationServer } from "@matter/node/behaviors/bridged-device-basic-information";
import { BridgeController } from "../BridgeController";

const logger = Logger.get("GenericDevice");

/**
 * This is the base class for all matter device types.
 */
export abstract class BaseDeviceType {
    protected updateLocks = new Set<string>();
    endpoint: Endpoint;

    constructor(
        protected bridgeController: BridgeController,
        protected attributeMap: Record<string, any>,
        protected endpointId: string,
        protected nodeLabel: string,
        protected productName: string,
        protected productLabel: string,
        protected serialNumber: string,
    ) {
        this.nodeLabel = this.#truncateString(nodeLabel);
        this.productLabel = this.#truncateString(productLabel);
        this.productName = this.#truncateString(productName);
        this.serialNumber = this.#truncateString(serialNumber);
        this.endpoint = this.createEndpoint(
            this.#generateAttributes(this.defaultClusterValues(attributeMap), attributeMap),
        );
        logger.debug(
            `New Device: label: ${this.nodeLabel} name: ${this.productName} product label: ${this.productLabel} serial: ${this.serialNumber}`,
        );
    }

    /**
     * This method is used to generate the default cluster values for the device.
     * @param userValues - The user provided values for the device, which will override the default values returned here. Useful if cluster need to be included or excluded based on user values.
     * @returns The default cluster values for the device that will be used as a base for the user provided values to override.
     */
    abstract defaultClusterValues(userValues: Record<string, any>): Record<string, any>;

    /**
     * This method is used to create the endpoint for the device.
     * @param clusterValues - The cluster values for the device.
     * @returns The endpoint for the device.
     */
    abstract createEndpoint(clusterValues: Record<string, any>): Endpoint;

    /**
     * This method is used to update the states for the device.
     * @param states - The states to update for the device.
     */
    public async updateStates(states: { clusterName: string; attributeName: string; state: any }[]) {
        const args = {} as { [key: string]: any };
        states.forEach(state => {
            if (args[state.clusterName] === undefined) {
                args[state.clusterName] = {} as { [key: string]: any };
            }
            args[state.clusterName][state.attributeName] = state.state;
        });
        logger.debug(`Updating states: ${JSON.stringify(args)}`);
        await this.endpoint.set(args);
    }

    protected endPointDefaults() {
        return {
            id: this.endpointId,
            bridgedDeviceBasicInformation: {
                nodeLabel: this.nodeLabel,
                productName: this.productName,
                productLabel: this.productLabel,
                serialNumber: this.serialNumber,
                reachable: true,
            },
        };
    }

    protected get baseClusterServers() {
        return [BridgedDeviceBasicInformationServer, FixedLabelServer];
    }

    #truncateString(str: string, maxLength: number = 32): string {
        return str.slice(0, maxLength);
    }

    #generateAttributes<T extends Record<string, any>, U extends Partial<T>>(defaults: T, overrides: U): T {
        const alwaysAdd = ["fixedLabel"];
        const entries = this.#mergeWithDefaults(defaults, overrides);
        // Ensure entries include the values from overrides for the keys in alwaysAdd
        alwaysAdd.forEach(key => {
            if (key in overrides) {
                entries[key as keyof T] = overrides[key as keyof T]!;
            }
        });
        return entries;
    }

    #mergeWithDefaults<T extends Record<string, any>, U extends Partial<T>>(defaults: T, overrides: U): T {
        function isPlainObject(value: any): value is Record<string, any> {
            return value && typeof value === "object" && !Array.isArray(value);
        }
        // Get unique keys from both objects
        const allKeys = [...new Set([...Object.keys(defaults), ...Object.keys(overrides)])];

        return allKeys.reduce(
            (result, key) => {
                const defaultValue = defaults[key];
                const overrideValue = overrides[key];

                // If both values exist and are objects, merge them recursively
                if (isPlainObject(defaultValue) && isPlainObject(overrideValue)) {
                    result[key] = this.#mergeWithDefaults(defaultValue, overrideValue);
                } else {
                    // Use override value if it exists, otherwise use default value
                    result[key] = key in overrides ? overrideValue : defaultValue;
                }

                return result;
            },
            {} as Record<string, any>,
        ) as T;
    }
}
