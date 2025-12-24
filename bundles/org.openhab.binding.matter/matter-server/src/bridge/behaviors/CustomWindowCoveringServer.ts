import { WindowCovering } from "@matter/main/clusters";
import { MovementDirection, MovementType, WindowCoveringServer } from "@matter/node/behaviors/window-covering";
import { DeviceFunctions } from "../DeviceFunctions";

export class CustomWindowCoveringServer extends WindowCoveringServer {
    static readonly DEFAULTS = {
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
    } as const;

    override async handleMovement(
        type: MovementType,
        reversed: boolean,
        direction: MovementDirection,
        targetPercent100ths?: number,
    ): Promise<void> {
        if (targetPercent100ths != null) {
            this.env
                .get(DeviceFunctions)
                .sendAttributeChangedEvent(
                    this.endpoint.id,
                    "windowCovering",
                    "targetPositionLiftPercent100ths",
                    targetPercent100ths,
                );
        }
        return super.handleMovement(type, reversed, direction, targetPercent100ths);
    }
    override async handleStopMovement() {
        this.env
            .get(DeviceFunctions)
            .sendAttributeChangedEvent(this.endpoint.id, "windowCovering", "operationalStatus", {
                global: WindowCovering.MovementStatus.Stopped,
                lift: WindowCovering.MovementStatus.Stopped,
                tilt: WindowCovering.MovementStatus.Stopped,
            });
        return super.handleStopMovement();
    }
}
