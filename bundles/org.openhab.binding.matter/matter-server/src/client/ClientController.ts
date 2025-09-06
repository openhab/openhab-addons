import { Logger } from "@matter/general";
import { WebSocketSession } from "../app";
import { Controller } from "../Controller";
import { toJSON } from "../util/Json";
import { ControllerNode } from "./ControllerNode";
import { Clusters, Nodes } from "./namespaces";

const logger = Logger.get("ClientController");

/**
 * This class exists to expose the "nodes" and "clusters" namespaces to websocket clients
 */
export class ClientController extends Controller {
    nodes?: Nodes;
    clusters?: Clusters;
    controllerNode: ControllerNode;
    controllerName: string;

    constructor(
        override ws: WebSocketSession,
        override params: URLSearchParams,
    ) {
        super(ws, params);
        const stringId = this.params.get("nodeId");
        const nodeId = stringId != null ? parseInt(stringId) : null;
        const storagePath = this.params.get("storagePath");
        const controllerName = this.params.get("controllerName");

        if (nodeId === null || storagePath === null || controllerName === null) {
            throw new Error("Missing required parameters in the request");
        }

        this.controllerName = controllerName;
        this.controllerNode = new ControllerNode(storagePath, controllerName, nodeId, ws);
    }

    id(): string {
        return "client-" + this.controllerName;
    }

    async init() {
        await this.controllerNode.initialize();
        logger.info(`Started Node`);
        // set up listeners to send events back to the client
        this.nodes = new Nodes(this.controllerNode);
        this.clusters = new Clusters(this.controllerNode);
    }

    async close() {
        logger.info(`Closing Node`);
        await this.controllerNode?.close();
        logger.info(`Node Closed`);
    }

    executeCommand(namespace: string, functionName: string, args: any[]): any | Promise<any> {
        logger.debug(`Executing function ${namespace}.${functionName}(${toJSON(args)})`);

        const controllerAny: any = this;

        if (typeof controllerAny[namespace] !== "object") {
            throw new Error(`Namespace ${namespace} not found`);
        }

        const baseObject = controllerAny[namespace];
        if (typeof baseObject[functionName] !== "function") {
            throw new Error(`Function ${functionName} not found`);
        }

        return baseObject[functionName](...args);
    }
}
