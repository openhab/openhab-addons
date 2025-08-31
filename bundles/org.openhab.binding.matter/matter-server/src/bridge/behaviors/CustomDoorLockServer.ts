import { DoorLockServer } from "@matter/main/behaviors";
import { DoorLock } from "@matter/main/clusters";
import { DeviceFunctions } from "../DeviceFunctions";

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

    override async lockDoor() {
        await this.sendLockState(DoorLock.LockState.Locked);
        return super.lockDoor();
    }

    override async unlockDoor() {
        await this.sendLockState(DoorLock.LockState.Unlocked);
        return super.unlockDoor();
    }

    /**
     * For Lock/Unlock, we need to wait for the state update to be sent to the bridge controller if the state is not already the desired state.
     * Locks can often take a while to lock/unlock so we need to wait longer for the state update to be sent to the bridge controller.
     */
    private async sendLockState(lockState: DoorLock.LockState) {
        this.env.get(DeviceFunctions).sendAttributeChangedEvent(this.endpoint.id, "doorLock", "lockState", lockState);
        if (this.endpoint.stateOf(CustomDoorLockServer).lockState !== lockState) {
            const result = await this.env
                .get(DeviceFunctions)
                .waitForStateUpdate(this.endpoint.id, "doorLock", "lockState", 30000);
            if (result !== lockState) {
                throw new Error("Lock state failed", { cause: result });
            }
        }
    }
}
