import { DoorLockServer } from "@matter/main/behaviors";
import { DoorLock } from "@matter/main/clusters";
import { Endpoint } from "@matter/node";
import { DoorLockDevice } from "@matter/node/devices/door-lock";
import { GenericDeviceType } from "./GenericDeviceType";
import LockState = DoorLock.LockState;

export class DoorLockDeviceType extends GenericDeviceType {
    override createEndpoint(clusterValues: Record<string, any>) {
        const endpoint = new Endpoint(
            DoorLockDevice.with(...this.defaultClusterServers(), this.createDoorLockServer()),
            {
                ...this.endPointDefaults(),
                ...clusterValues,
            },
        );
        return endpoint;
    }

    override defaultClusterValues() {
        return {
            doorLock: {
                lockState: 0,
                lockType: 2,
                actuatorEnabled: true,
                doorState: 1,
                maxPinCodeLength: 10,
                minPinCodeLength: 1,
                wrongCodeEntryLimit: 5,
                userCodeTemporaryDisableTime: 10,
                operatingMode: 0,
            },
        };
    }

    protected createDoorLockServer(): typeof DoorLockServer {
        const parent = this;
        return class extends DoorLockServer {
            override async lockDoor() {
                parent.sendBridgeEvent("doorLock", "lockState", LockState.Locked);
            }

            override async unlockDoor() {
                parent.sendBridgeEvent("doorLock", "lockState", LockState.Unlocked);
            }
        };
    }
}
