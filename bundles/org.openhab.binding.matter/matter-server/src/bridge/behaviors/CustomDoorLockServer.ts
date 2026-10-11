import { Logger } from "@matter/main";
import { DoorLockBaseServer, DoorLockServer, LockOperationFailedError } from "@matter/main/behaviors";
import { DoorLock } from "@matter/main/clusters";
import { DeviceFunctions } from "../DeviceFunctions";

const logger = Logger.get("CustomDoorLockServer");

export class CustomDoorLockServer extends DoorLockServer {
    static readonly DEFAULTS = {
        lockState: 0,
        lockType: 2,
        actuatorEnabled: true,
        operatingMode: 0,
    } as const;

    /**
     * matter.js calls this once it has checked that the command is allowed. Waits for openHAB to confirm the new
     * lock state, with a longer timeout because locks can be slow.
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
            result = await deviceFunctions.waitForStateUpdate(this.endpoint.id, "doorLock", "lockState", 30000);
        } catch {
            logger.debug(`No lockState confirmation from openHAB for ${this.endpoint.id}, proceeding`);
            return;
        }
        if (result !== lockState) {
            throw new LockOperationFailedError(DoorLock.OperationError.Unspecified, "Lock state failed", {
                cause: result,
            });
        }
    }
}
