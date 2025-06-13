import { WindowCovering } from "@matter/main/clusters";
import { Endpoint } from "@matter/node";
import { MovementDirection, MovementType, WindowCoveringServer } from "@matter/node/behaviors/window-covering";
import { WindowCoveringDevice } from "@matter/node/devices/window-covering";
import { GenericDeviceType } from "./GenericDeviceType";

export class WindowCoveringDeviceType extends GenericDeviceType {
    override createEndpoint(clusterValues: Record<string, any>) {
        const features: WindowCovering.Feature[] = [];
        features.push(WindowCovering.Feature.Lift);
        features.push(WindowCovering.Feature.PositionAwareLift);
        const endpoint = new Endpoint(
            WindowCoveringDevice.with(
                this.createWindowCoveringServer().with(...features),
                ...this.defaultClusterServers(),
            ),
            {
                ...this.endPointDefaults(),
                ...clusterValues,
            },
        );
        endpoint.events.windowCovering.operationalStatus$Changed.on(value => {
            this.sendBridgeEvent("windowCovering", "operationalStatus", value);
        });
        return endpoint;
    }

    override defaultClusterValues() {
        return {
            windowCovering: {
                currentPositionLiftPercent100ths: 0,
                configStatus: {
                    operational: true,
                    onlineReserved: false,
                    liftMovementReversed: false,
                    liftPositionAware: true,
                    tiltPositionAware: false,
                    liftEncoderControlled: true,
                    tiltEncoderControlled: false,
                },
            },
        };
    }

    // this allows us to get all commands to move the device, not just if it thinks the position has changed
    private createWindowCoveringServer(): typeof WindowCoveringServer {
        const parent = this;
        return class extends WindowCoveringServer {
            override async handleMovement(
                type: MovementType,
                reversed: boolean,
                direction: MovementDirection,
                targetPercent100ths?: number,
            ): Promise<void> {
                if (targetPercent100ths != null) {
                    parent.sendBridgeEvent("windowCovering", "targetPositionLiftPercent100ths", targetPercent100ths);
                }
            }
            override async handleStopMovement() {
                parent.sendBridgeEvent("windowCovering", "operationalStatus", {
                    global: WindowCovering.MovementStatus.Stopped,
                    lift: WindowCovering.MovementStatus.Stopped,
                    tilt: WindowCovering.MovementStatus.Stopped,
                });
            }
        };
    }
}
