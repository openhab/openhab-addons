import { Logger } from "@matter/general";
import { ControllerNode } from "../ControllerNode";
import { convertJsonDataWithModel } from "../../util/Json";
import { ValidationError } from "@matter/main/types";
import * as MatterClusters from "@matter/types/clusters";
import { SupportedAttributeClient } from "@matter/protocol"

const logger = Logger.get("Clusters");

/**
 * This class is used for websocket clients interacting with Matter Clusters to send commands like OnOff, LevelControl, etc... 
 */
export class Clusters {
    constructor(private theNode: ControllerNode) {
    }

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
    async command(nodeId: number, endpointId: number, clusterName: string, commandName: string, args: any[]) {
        logger.debug(`command ${nodeId} ${endpointId} ${clusterName} ${commandName} ${Logger.toJSON(args)}`);
        const device = this.theNode.getEndpoint(await this.theNode.getNode(nodeId), endpointId);
        if (device == undefined) {
            throw new Error(`Endpoint ${endpointId} not found`);
        }
        const cluster = (MatterClusters as any)[`${clusterName}Cluster`];
        const clusterClient: any = device.getClusterClient(cluster);
        if (clusterClient === undefined) {
            throw new Error(`Cluster ${clusterName} not found`);
        }
        if (typeof clusterClient[commandName] !== 'function') {
            throw new Error(`Cluster Function ${commandName} not found`);
        }
        if (args == null || Object.keys(args).length === 0) {
            return clusterClient[commandName]();
        } else {
            return clusterClient[commandName](args);
        }
    }

    /**
     * Writes an attribute to a device (not all attributes are writable)
     * @param nodeId 
     * @param endpointId 
     * @param clusterName 
     * @param attributeName 
     * @param value 
     */
    async writeAttribute(nodeId: number, endpointId: number, clusterName: string, attributeName: string, value: string) {
        let parsedValue: any;
        try {
            parsedValue = JSON.parse(value);
        } catch (error) {
            try {
                parsedValue = JSON.parse(`"${value}"`);
            } catch (innerError) {
                throw new Error(`ERROR: Could not parse value ${value} as JSON.`)
            }
        }

        const node = await this.theNode.getNode(nodeId);

        const device = this.theNode.getEndpoint(await this.theNode.getNode(nodeId), endpointId);
        if (device == undefined) {
            throw new Error(`Endpoint ${endpointId} not found`);
        }
        const cluster = (MatterClusters as any)[`${clusterName}Cluster`];
        const clusterClient: any = device.getClusterClient(cluster);
        if (clusterClient === undefined) {
            throw new Error(`Cluster ${clusterName} not found`);
        }
        const attributeClient = clusterClient.attributes[attributeName];
        if (!(attributeClient instanceof SupportedAttributeClient)) {
            throw new Error(`Attribute ${node.nodeId.toString()}/${endpointId}/${clusterName}/${attributeName} not supported.`)
        }

        try {
            parsedValue = convertJsonDataWithModel(cluster, parsedValue);
            await attributeClient.set(parsedValue);
            console.log(
                `Attribute ${attributeName} ${node.nodeId.toString()}/${endpointId}/${clusterName}/${attributeName} set to ${Logger.toJSON(value)}`,
            );
        } catch (error) {
            if (error instanceof ValidationError) {
                throw new Error(`Could not validate data for attribute ${attributeName} to ${Logger.toJSON(parsedValue)}: ${error}${error.fieldName !== undefined ? ` in field ${error.fieldName}` : ""}`,
                )
            } else {
                throw new Error(`Could not set attribute ${attributeName} to ${Logger.toJSON(parsedValue)}: ${error}`)
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
        const node = await this.theNode.getNode(nodeId);

        const device = this.theNode.getEndpoint(await this.theNode.getNode(nodeId), endpointId);
        if (device == undefined) {
            throw new Error(`Endpoint ${endpointId} not found`);
        }
        const cluster = (MatterClusters as any)[`${clusterName}Cluster`];
        const clusterClient: any = device.getClusterClient(cluster);
        if (clusterClient === undefined) {
            throw new Error(`Cluster ${clusterName} not found`);
        }
        const attributeClient = clusterClient.attributes[attributeName];
        if (!(attributeClient instanceof SupportedAttributeClient)) {
            throw new Error(`Attribute ${node.nodeId.toString()}/${endpointId}/${clusterName}/${attributeName} not supported.`)
        }
        return await attributeClient.get(true);
    }
}