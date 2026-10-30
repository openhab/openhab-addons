import { Logger } from "@matter/general";
import { AttributeId, ClusterId, EndpointNumber, ValidationError } from "@matter/main/types";
import { ClusterModel, MatterModel } from "@matter/model";
import { ClusterBehavior, Endpoint } from "@matter/node";
import { convertJsonDataWithModel, toJSON } from "../../util/Json";
import { capitalize } from "../../util/String";
import { ControllerNode } from "../ControllerNode";

const logger = Logger.get("Clusters");

/**
 * This class is used for websocket clients interacting with Matter Clusters to send commands like OnOff, LevelControl, etc...
 * Methods not marked as private are intended to be exposed to websocket clients
 */
export class Clusters {
    constructor(private controllerNode: ControllerNode) {}

    /**
     * Dynamically executes a command on a specified cluster within a device node.
     * This method retrieves the cluster for the device at the given node and endpoint, checks the existence
     * of the command on the cluster, and calls it with any provided arguments.
     *
     * @param nodeId Identifier for the node containing the target device.
     * @param endpointId Endpoint on the node where the command is directed.
     * @param clusterName Name of the cluster targeted by the command.
     * @param commandName Specific command to be executed on the cluster.
     * @param args Optional arguments for executing the command.
     * @throws Error if the cluster or command is not found on the device.
     */
    async command(nodeId: number, endpointId: number, clusterName: string, commandName: string, args: any) {
        logger.debug(`command ${nodeId} ${endpointId} ${clusterName} ${commandName} ${toJSON(args)}`);
        const { endpoint, cluster, type } = this.#resolve(nodeId, endpointId, clusterName);

        const command = cluster.commands.find(c => c.name === capitalize(commandName));
        if (command === undefined || !endpoint.behaviors.elementsOf(type).commands.has(commandName)) {
            throw new Error(`Cluster Function ${commandName} not found`);
        }

        let convertedArgs: any = undefined;
        if (args !== undefined && Object.keys(args).length > 0) {
            convertedArgs = convertJsonDataWithModel(command, args);
        }

        const commands = endpoint.commandsOf(type) as Record<string, (request?: any) => Promise<any>>;
        return commands[commandName](convertedArgs);
    }

    /**
     * Writes an attribute to a device (not all attributes are writable)
     * @param nodeId
     * @param endpointId
     * @param clusterName
     * @param attributeName
     * @param value
     */
    async writeAttribute(
        nodeId: number,
        endpointId: number,
        clusterName: string,
        attributeName: string,
        value: string,
    ) {
        let parsedValue: any;
        try {
            parsedValue = JSON.parse(value);
        } catch (error) {
            try {
                parsedValue = JSON.parse(`"${value}"`);
            } catch (innerError) {
                throw new Error(`ERROR: Could not parse value ${value} as JSON.`);
            }
        }

        const { endpoint, cluster, type } = this.#resolve(nodeId, endpointId, clusterName);
        const attribute = this.#supportedAttribute(endpoint, cluster, type, attributeName);
        if (!attribute.writable) {
            throw new Error(`Attribute ${attributeName} is not writable`);
        }

        try {
            parsedValue = convertJsonDataWithModel(attribute, parsedValue);
            // Setting state on a device's cluster writes the attribute to the device
            await endpoint.setStateOf(type, { [attributeName]: parsedValue });
            logger.debug(`Attribute ${nodeId}/${endpointId}/${clusterName}/${attributeName} set to ${toJSON(value)}`);
        } catch (error) {
            if (error instanceof ValidationError) {
                throw new Error(
                    `Could not validate data for attribute ${attributeName} to ${toJSON(parsedValue)}: ${error}${error.fieldName !== undefined ? ` in field ${error.fieldName}` : ""}`,
                );
            } else {
                throw new Error(`Could not set attribute ${attributeName} to ${toJSON(parsedValue)}: ${error}`);
            }
        }
    }

    /**
     * Reads an attribute from a device
     * @param nodeId
     * @param endpointId
     * @param clusterName
     * @param attributeName
     */
    async readAttribute(nodeId: number, endpointId: number, clusterName: string, attributeName: string) {
        const values = await this.readAttributes(nodeId, endpointId, clusterName, [attributeName]);
        return values[attributeName];
    }

    /**
     * Reads several attributes of one cluster from a device in a single read interaction. Devices only have to serve a
     * few concurrent reads, so separate requests can be rejected as busy.
     * @param nodeId
     * @param endpointId
     * @param clusterName
     * @param attributeNames
     * @returns the values by attribute name, attributes the device did not report are left out
     */
    async readAttributes(nodeId: number, endpointId: number, clusterName: string, attributeNames: string[]) {
        const node = this.controllerNode.getNode(nodeId);
        const { endpoint, cluster, type } = this.#resolve(nodeId, endpointId, clusterName);
        const clusterId = ClusterId(type.cluster.id);

        const namesByKey = new Map<string, string>();
        const paths = attributeNames.map(attributeName => {
            const attribute = this.#supportedAttribute(endpoint, cluster, type, attributeName);
            const attributeId = AttributeId(attribute.id);
            namesByKey.set(`${endpointId}/${clusterId}/${attributeId}`, attributeName);
            return { endpointId: EndpointNumber(endpointId), clusterId, attributeId };
        });

        const values: Record<string, unknown> = {};
        for (const [key, value] of await this.controllerNode.readAttributes(node, paths)) {
            const name = namesByKey.get(key);
            if (name !== undefined) {
                values[name] = value;
            }
        }
        return values;
    }

    /**
     * Returns the cached attribute values of a cluster
     * @param nodeId
     * @param endpointId
     * @returns
     */
    async readCluster(nodeId: string | number, endpointId: number, clusterNameOrId: string | number) {
        const node = this.controllerNode.getNode(nodeId);
        const endpoint = this.controllerNode.endpointFor(node, endpointId);
        const clusterId =
            typeof clusterNameOrId === "string" ? this.#clusterForName(clusterNameOrId).id : clusterNameOrId;
        const type = clusterId !== undefined ? endpoint.behaviors.forCluster(ClusterId(clusterId)) : undefined;
        if (type === undefined) {
            throw new Error(`Cluster client for ${clusterNameOrId} not found`);
        }
        return this.controllerNode.serializeCluster(endpoint, type);
    }

    #resolve(nodeId: string | number, endpointId: number, clusterName: string) {
        const node = this.controllerNode.getNode(nodeId);
        const endpoint = this.controllerNode.endpointFor(node, endpointId);
        const cluster = this.#clusterForName(clusterName);
        if (cluster.id === undefined) {
            throw new Error(`Cluster ID for ${clusterName} not found`);
        }
        const type = endpoint.behaviors.forCluster(ClusterId(cluster.id));
        if (type === undefined) {
            throw new Error(`Cluster client for ${clusterName} not found`);
        }
        return { endpoint, cluster, type };
    }

    #supportedAttribute(endpoint: Endpoint, cluster: ClusterModel, type: ClusterBehavior.Type, attributeName: string) {
        const attribute = cluster.attributes.find(c => c.name === capitalize(attributeName));
        if (attribute === undefined) {
            throw new Error(`Attribute ${attributeName} not found`);
        }
        if (!endpoint.behaviors.elementsOf(type).attributes.has(attributeName)) {
            throw new Error(`Attribute ${endpoint}/${cluster.name}/${attributeName} not supported.`);
        }
        return attribute;
    }

    #clusterForName(clusterName: string): ClusterModel {
        const cluster = MatterModel.standard.clusters.find(c => c.name === clusterName);
        if (cluster == null) {
            throw new Error(`Cluster ${clusterName} not found`);
        }
        return cluster;
    }
}
