import { ActionContext } from "@matter/node";
import { BridgeEvent, BridgeEventType, EventType } from "../MessageTypes";
import { BridgeController } from "./BridgeController";

/**
 * This class is intended as a singleton for use by Device Types and Cluster Behaviors.
 * It is injected into the environment of the Root DeviceNode
 */
export class DeviceFunctions {
    // Waiters that wait for specific state updates
    private stateWaiters: {
        endpointId: string;
        clusterName: string;
        attributeName: string;
        resolve: (value: any) => void;
        timer: NodeJS.Timeout;
    }[] = [];

    constructor(private bridgeController: BridgeController) {}

    /**
     * This method is used to send an attributeChanged event to the bridge controller. Prevents loopback events.
     * @param clusterName - The cluster name of the attribute that changed.
     * @param attributeName - The attribute name that changed.
     * @param attributeValue - The value of the attribute that changed.
     * @param context - The context of the attribute that changed.
     */
    public attributeChanged(
        endpointId: string,
        clusterName: string,
        attributeName: string,
        attributeValue: any,
        context?: ActionContext,
    ) {
        // if the context is undefined or the context is offline, do not send the event as this was openHAB initiated (prevents loopback)
        if (context === undefined || context.offline === true) {
            return;
        }
        this.sendAttributeChangedEvent(endpointId, clusterName, attributeName, attributeValue);
    }
    /**
     * This method is used to send an attributeChanged event to the bridge controller.
     * @param clusterName - The cluster name of the attribute that changed.
     * @param attributeName - The attribute name that changed.
     * @param attributeValue - The value of the attribute that changed.
     * @param context - The context of the attribute that changed.
     */
    public sendAttributeChangedEvent(
        endpointId: string,
        clusterName: string,
        attributeName: string,
        attributeValue: any,
    ) {
        const be: BridgeEvent = {
            type: BridgeEventType.AttributeChanged,
            data: {
                endpointId: endpointId,
                clusterName: clusterName,
                attributeName: attributeName,
                data: attributeValue,
            },
        };
        this.bridgeController.ws.sendEvent(EventType.BridgeEvent, be);
    }

    /**
     * Wait for the next incoming state update matching the given endpoint/cluster/attribute.
     * Resolves with the new value or rejects after the given timeout (ms).
     */
    public waitForStateUpdate(
        endpointId: string,
        clusterName: string,
        attributeName: string,
        timeout: number,
    ): Promise<any> {
        return new Promise<any>((resolve, reject) => {
            const timer = setTimeout(() => {
                this.stateWaiters = this.stateWaiters.filter(w => w !== waiter);
                reject(new Error(`Timed out waiting for state ${endpointId}.${clusterName}.${attributeName}`));
            }, timeout);

            const waiter = {
                endpointId,
                clusterName,
                attributeName,
                resolve: (value: any) => {
                    clearTimeout(timer);
                    resolve(value);
                },
                timer,
            } as {
                endpointId: string;
                clusterName: string;
                attributeName: string;
                resolve: (value: any) => void;
                timer: NodeJS.Timeout;
            };

            this.stateWaiters.push(waiter);
        });
    }

    /**
     * Resolve any stored waiters that match the provided state updates.
     */
    notifyStateWaiters(endpointId: string, states: { clusterName: string; attributeName: string; state: any }[]) {
        if (this.stateWaiters.length === 0) {
            return;
        }

        const remaining: typeof this.stateWaiters = [];
        for (const waiter of this.stateWaiters) {
            if (waiter.endpointId !== endpointId) {
                remaining.push(waiter);
                continue;
            }

            const matched = states.find(
                s => s.clusterName === waiter.clusterName && s.attributeName === waiter.attributeName,
            );
            if (matched) {
                waiter.resolve(matched.state);
                clearTimeout(waiter.timer);
            } else {
                remaining.push(waiter);
            }
        }
        this.stateWaiters = remaining;
    }
}
