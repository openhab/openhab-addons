import { Logger } from "@matter/main";
import { DoorLockServer } from "@matter/main/behaviors";
import { DoorLock } from "@matter/main/clusters";
import { DeviceFunctions } from "../DeviceFunctions";

const logger = Logger.get("CustomDoorLockServer");

export class CustomDoorLockServer extends DoorLockServer {
    static readonly DEFAULTS = {
        lockState: 0,
        lockType: 2,
        actuatorEnabled: true,
        doorState: 1,
        maxPinCodeLength: 10,
        minPinCodeLength: 1,
        wrongCodeEntryLimit: 5,
        userCodeTemporaryDisableTime: 10,
        operatingMode: 0,
    } as const;

    override async lockDoor(request: DoorLock.LockDoorRequest) {
        await this.sendLockState(DoorLock.LockState.Locked);
        return super.lockDoor(request);
    }

    override async unlockDoor(request: DoorLock.UnlockDoorRequest) {
        await this.sendLockState(DoorLock.LockState.Unlocked);
        return super.unlockDoor(request);
    }

    /**
     * Waits for openHAB to confirm the new lockState (longer timeout since locks can be slow).
     */
    private async sendLockState(lockState: DoorLock.LockState) {
        this.env.get(DeviceFunctions).sendAttributeChangedEvent(this.endpoint.id, "doorLock", "lockState", lockState);
        if (this.state.lockState !== lockState) {
            let result: DoorLock.LockState | undefined;
            try {
                result = await this.env
                    .get(DeviceFunctions)
                    .waitForStateUpdate(this.endpoint.id, "doorLock", "lockState", 30000);
            } catch {
                logger.debug(`No lockState confirmation from openHAB for ${this.endpoint.id}, proceeding`);
                return;
            }
            if (result !== lockState) {
                throw new Error("Lock state failed", { cause: result });
            }
        }
    }
}
