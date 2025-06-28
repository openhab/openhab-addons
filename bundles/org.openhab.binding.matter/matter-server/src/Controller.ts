import { Logger } from "@matter/general";
import { WebSocketSession } from "./app";
import { MessageType, Request } from "./MessageTypes";
import { printError } from "./util/error";
const logger = Logger.get("Controller");

export abstract class Controller {
    constructor(
        protected ws: WebSocketSession,
        protected params: URLSearchParams,
    ) {}

    /**
     * Initializes the controller
     */
    abstract init(): Promise<void>;

    /**
     * Closes the controller
     */
    abstract close(): Promise<void>;

    /**
     * Returns the unique identifier of the controller
     * @returns
     */
    abstract id(): string;

    /**
     * Executes a command, similar to a RPC call, on the controller implementor
     * @param namespace
     * @param functionName
     * @param args
     */
    abstract executeCommand(namespace: string, functionName: string, args: any[]): any | Promise<any>;

    /**
     * Handles a request from the client
     * @param request
     */
    async handleRequest(request: Request): Promise<void> {
        const { id, namespace, function: functionName, args } = request;
        logger.debug(`Received request: ${Logger.toJSON(request)}`);
        try {
            const result = this.executeCommand(namespace, functionName, args || []);
            if (result instanceof Promise) {
                const asyncResult = await result;
                this.ws.sendResponse(MessageType.ResultSuccess, id, asyncResult);
            } else {
                this.ws.sendResponse(MessageType.ResultSuccess, id, result);
            }
        } catch (error) {
            if (error instanceof Error) {
                printError(logger, error, functionName);
                let errorId: string | undefined;
                // instances of a MatterError has an id property
                if ("id" in error) {
                    errorId = (error as any).id;
                }
                this.ws.sendResponse(MessageType.ResultError, id, undefined, error.message, errorId);
            } else {
                logger.error(`Unexpected error executing function ${functionName}: ${error}`);
                this.ws.sendResponse(MessageType.ResultError, id, undefined, String(error));
            }
        }
    }
}
