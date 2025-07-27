import { LogFormat, Logger, LogLevel } from "@matter/general";
import { IncomingMessage } from "http";
import WebSocket, { Server } from "ws";
import yargs from "yargs";
import { hideBin } from "yargs/helpers";
import { BridgeController } from "./bridge/BridgeController";
import { ClientController } from "./client/ClientController";
import { Controller } from "./Controller";
import { Message, Request } from "./MessageTypes";
import { printError } from "./util/error";
import { toJSON } from "./util/Json";

const argv: any = yargs(hideBin(process.argv)).argv;

const logger = Logger.get("matter");
Logger.level = LogLevel.DEBUG;
Logger.format = LogFormat.PLAIN;

process.on("SIGINT", () => void shutdownHandler("SIGINT"));
process.on("SIGTERM", () => void shutdownHandler("SIGTERM"));
process.on("uncaughtException", function (err) {
    logger.error(`Caught exception: ${err} ${err.stack}`);
});

const parentPid = process.ppid;
setInterval(() => {
    try {
        // Try sending signal 0 to the parent process.
        // If the parent is dead, this will throw an error.
        // otherwise we stick around forever and eat 100% of a cpu core (?)
        process.kill(parentPid, 0);
    } catch (e) {
        console.error("Parent process exited. Shutting down Node.js...");
        process.exit(1);
    }
}, 5000);

const shutdownHandler = async (signal: string) => {
    logger.info(`Received ${signal}. Closing WebSocket connections...`);

    const closePromises: Promise<void>[] = [];

    wss.clients.forEach((client: WebSocket) => {
        if (client.readyState === WebSocket.OPEN) {
            closePromises.push(
                new Promise<void>(resolve => {
                    client.close(1000, "Server shutting down");
                    client.on("close", () => {
                        resolve();
                    });
                    client.on("error", err => {
                        console.error("Error while closing WebSocket connection:", err);
                        resolve();
                    });
                }),
            );
        }
    });

    await Promise.all(closePromises)
        .then(() => {
            logger.info("All WebSocket connections closed.");
            return new Promise<void>(resolve => wss.close(() => resolve()));
        })
        .then(() => {
            logger.info("WebSocket server closed.");
            process.exit(0);
        })
        .catch(err => {
            console.error("Error during shutdown:", err);
            process.exit(1);
        });
};

export interface WebSocketSession extends WebSocket {
    controller?: Controller;
    sendResponse(type: string, id: string, result?: any, error?: string, errorId?: string): void;
    sendEvent(type: string, data?: any): void;
}

const socketPort = argv.port ? parseInt(argv.port) : 8888;
const wss: Server = new WebSocket.Server({ port: socketPort, host: argv.host });

wss.on("connection", (ws: WebSocketSession, req: IncomingMessage) => {
    ws.sendResponse = (type: string, id: string, result?: any, error?: string, errorId?: string) => {
        const message: Message = {
            type: "response",
            message: {
                type,
                id,
                result,
                error,
                errorId,
            },
        };
        logger.debug(`Sending response: ${toJSON(message)}`);
        ws.send(toJSON(message));
    };

    ws.sendEvent = (type: string, data?: any) => {
        const message: Message = {
            type: "event",
            message: {
                type,
                data,
            },
        };
        logger.debug(`Sending event: ${toJSON(message)}`);
        ws.send(toJSON(message));
    };

    ws.on("open", () => {
        logger.info("WebSocket opened");
    });

    ws.on("message", (message: string) => {
        const request: Request = JSON.parse(message);
        if (ws.controller) {
            void ws.controller.handleRequest(request);
        }
    });

    ws.on("close", () => {
        logger.info("WebSocket closed");
        if (ws.controller) {
            void ws.controller.close().catch((error: Error) => {
                logger.error(`Error closing controller: ${error}`);
            });
        }
    });

    ws.on("error", (error: Error) => {
        logger.error(`WebSocket error: ${error} ${error.stack}`);
    });

    // Support async init in a non async function
    void (async () => {
        if (!req.url) {
            logger.error("No URL in the request");
            ws.close(1002, "No URL in the request");
            return;
        }

        const params = new URLSearchParams(req.url.slice(req.url.indexOf("?")));
        const service = params.get("service") === "bridge" ? "bridge" : "client";

        if (service === "client") {
            const controllerName = params.get("controllerName");
            try {
                if (controllerName == null) {
                    throw new Error("No controllerName parameter in the request");
                }
                wss.clients.forEach((client: WebSocket) => {
                    const session = client as WebSocketSession;
                    if (session.controller && session.controller.id() === `client-${controllerName}`) {
                        throw new Error(`Controller with name ${controllerName} already exists!`);
                    }
                });
                ws.controller = new ClientController(ws, params);
                try {
                    await ws.controller.init();
                    ws.sendEvent("ready", "Controller initialized");
                } catch (error: any) {
                    printError(logger, error, "ClientController.init()");
                    logger.error("returning error", error.message);
                    ws.close(1002, error.message);
                    return;
                }
            } catch (error: any) {
                printError(logger, error, "ClientController.init()");
                logger.error("returning error", error.message);
                ws.close(1002, error.message);
                return;
            }
        } else {
            // For now we only support one bridge
            const uniqueId = "0";
            try {
                wss.clients.forEach((client: WebSocket) => {
                    const session = client as WebSocketSession;
                    if (session.controller && session.controller.id() === `bridge-${uniqueId}`) {
                        throw new Error(`Bridge with uniqueId ${uniqueId} already exists!`);
                    }
                });
                ws.controller = new BridgeController(ws, params);
                try {
                    await ws.controller.init();
                    ws.sendEvent("ready", "Controller initialized");
                } catch (error: any) {
                    printError(logger, error, "BridgeController.init()");
                    logger.error("returning error", error.message);
                    ws.close(1002, error.message);
                    return;
                }
            } catch (error: any) {
                printError(logger, error, "BridgeController.init()");
                logger.error("returning error", error.message);
                ws.close(1002, error.message);
                return;
            }
        }
    })();
});

logger.info(`CHIP Controller Server listening on port ${socketPort}`);
