import { DoorLockBaseServer, DoorLockServer, LockOperationFailedError } from "@matter/main/behaviors";
import { DoorLock } from "@matter/main/clusters";
import { DeviceFunctions } from "../DeviceFunctions";

export class CustomDoorLockServer extends DoorLockServer {
    static readonly DEFAULTS = {
        lockState: 0,
        lockType: 2,
        actuatorEnabled: true,
        operatingMode: 0,
    } as const;

    // Controllers give up on a command after about 10 seconds, so answer before that
    static readonly CONFIRM_TIMEOUT_MS = 8000;

    /**
     * matter.js calls this once it has checked that the command is allowed. Waits for openHAB to confirm the new
     * lock state, and fails the command if openHAB does not, so paired apps never show a state the lock may not be in.
     */
    override async handleLockOperation({ actuation }: DoorLockBaseServer.LockOperation) {
        const lockState = actuation === "lock" ? DoorLock.LockState.Locked : DoorLock.LockState.Unlocked;
        const deviceFunctions = this.env.get(DeviceFunctions);
        deviceFunctions.sendAttributeChangedEvent(this.endpoint.id, "doorLock", "lockState", lockState);
        if (this.state.lockState === lockState) {
            return;
        }
        let result: DoorLock.LockState | undefined;
        try {
            result = await deviceFunctions.waitForStateUpdate(
                this.endpoint.id,
                "doorLock",
                "lockState",
                CustomDoorLockServer.CONFIRM_TIMEOUT_MS,
            );
        } catch (e) {
            throw new LockOperationFailedError(DoorLock.OperationError.Unspecified, "Lock state not confirmed", {
                cause: e,
            });
        }
        if (result !== lockState) {
            throw new LockOperationFailedError(DoorLock.OperationError.Unspecified, "Lock state failed", {
                cause: result,
            });
        }
    }
}
