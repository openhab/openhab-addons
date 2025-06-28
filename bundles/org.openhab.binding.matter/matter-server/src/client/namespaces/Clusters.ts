import { Logger } from "@matter/general";
import { ClusterId, ValidationError } from "@matter/main/types";
import { ClusterModel, MatterModel } from "@matter/model";
import { SupportedAttributeClient } from "@matter/protocol";
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
     * This method retrieves the cluster client for the device at the given node and endpoint, checks the existence
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
        const device = this.controllerNode.getNode(nodeId).getDeviceById(endpointId);
        if (device == undefined) {
            throw new Error(`Endpoint ${endpointId} not found`);
        }

        const cluster = this.#clusterForName(clusterName);
        if (cluster.id === undefined) {
            throw new Error(`Cluster ID for ${clusterName} not found`);
        }

        const clusterClient = device.getClusterClientById(ClusterId(cluster.id));
        if (clusterClient === undefined) {
            throw new Error(`Cluster client for ${clusterName} not found`);
        }

        const uppercaseName = capitalize(commandName);
        const command = cluster.commands.find(c => c.name === uppercaseName);
        if (command == undefined) {
            throw new Error(`Cluster Function ${commandName} not found`);
        }

        let convertedArgs: any = undefined;
        if (args !== undefined && Object.keys(args).length > 0) {
            convertedArgs = convertJsonDataWithModel(command, args);
        }

        return clusterClient.commands[commandName](convertedArgs);
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

        const device = this.controllerNode.getNode(nodeId).getDeviceById(endpointId);
        if (device == undefined) {
            throw new Error(`Endpoint ${endpointId} not found`);
        }

        const cluster = this.#clusterForName(clusterName);
        if (cluster.id === undefined) {
            throw new Error(`Cluster ID for ${clusterName} not found`);
        }

        const clusterClient = device.getClusterClientById(ClusterId(cluster.id));
        if (clusterClient === undefined) {
            throw new Error(`Cluster client for ${clusterName} not found`);
        }

        const attributeClient = clusterClient.attributes[attributeName];
        if (!(attributeClient instanceof SupportedAttributeClient)) {
            throw new Error(`Attribute ${nodeId}/${endpointId}/${clusterName}/${attributeName} not supported.`);
        }

        const uppercaseName = capitalize(attributeName);
        const attribute = cluster.attributes.find(c => c.name === uppercaseName);
        if (attribute == undefined) {
            throw new Error(`Attribute ${attributeName} not found`);
        }

        try {
            parsedValue = convertJsonDataWithModel(attribute, parsedValue);
            await attributeClient.set(parsedValue);
            console.log(
                `Attribute ${attributeName} ${nodeId}/${endpointId}/${clusterName}/${attributeName} set to ${toJSON(value)}`,
            );
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
        const device = this.controllerNode.getNode(nodeId).getDeviceById(endpointId);
        if (device == undefined) {
            throw new Error(`Endpoint ${endpointId} not found`);
        }

        const cluster = this.#clusterForName(clusterName);
        if (cluster.id === undefined) {
            throw new Error(`Cluster ID for ${clusterName} not found`);
        }

        const clusterClient = device.getClusterClientById(ClusterId(cluster.id));
        if (clusterClient === undefined) {
            throw new Error(`Cluster client for ${clusterName} not found`);
        }

        const attributeClient = clusterClient.attributes[attributeName];
        if (!(attributeClient instanceof SupportedAttributeClient)) {
            throw new Error(`Attribute ${nodeId}/${endpointId}/${clusterName}/${attributeName} not supported.`);
        }

        const uppercaseName = capitalize(attributeName);
        const attribute = cluster.attributes.find(c => c.name === uppercaseName);
        if (attribute == undefined) {
            throw new Error(`Attribute ${attributeName} not found`);
        }

        return await attributeClient.get(true);
    }

    /**
     * Requests all attributes data for a single endpoint and its children
     * @param nodeId
     * @param endpointId
     * @returns
     */
    async readCluster(nodeId: string | number, endpointId: number, clusterNameOrId: string | number) {
        const device = this.controllerNode.getNode(nodeId).getDeviceById(endpointId);
        if (device === undefined) {
            throw new Error(`Endpoint ${endpointId} not found`);
        }

        const clusterId =
            typeof clusterNameOrId === "string" ? this.#clusterForName(clusterNameOrId).id : clusterNameOrId;
        if (clusterId === undefined) {
            throw new Error(`Cluster ID for ${clusterNameOrId} not found`);
        }

        const clusterClient = device.getClusterClientById(ClusterId(clusterId));
        if (clusterClient === undefined) {
            throw new Error(`Cluster client for ${clusterNameOrId} not found`);
        }

        const clusterData: any = {
            id: clusterClient.id,
            name: clusterClient.name,
        };

        // Serialize attributes
        for (const attributeName in clusterClient.attributes) {
            // Skip numeric referenced attributes
            if (/^\d+$/.test(attributeName)) continue;
            const attribute = clusterClient.attributes[attributeName];
            if (!attribute) continue;
            const attributeValue = await attribute.get();
            logger.debug(`Attribute ${attributeName} value: ${attributeValue}`);
            if (attributeValue !== undefined) {
                clusterData[attributeName] = attributeValue;
            }
        }

        return clusterData;
    }

    #clusterForName(clusterName: string): ClusterModel {
        const cluster = MatterModel.standard.clusters.find(c => c.name === clusterName);
        if (cluster == null) {
            throw new Error(`Cluster ${clusterName} not found`);
        }
        return cluster;
    }
}
