import { Endpoint } from "@matter/node";
import { DoorLockDevice } from "@matter/node/devices/door-lock";
import { CustomDoorLockServer } from "../behaviors";
import { BaseDeviceType } from "./BaseDeviceType";

export class DoorLockDeviceType extends BaseDeviceType {
    override createEndpoint(clusterValues: Record<string, any>) {
        const endpoint = new Endpoint(DoorLockDevice.with(...this.baseClusterServers, CustomDoorLockServer), {
            ...this.endPointDefaults(),
            ...clusterValues,
        });
        return endpoint;
    }

    override defaultClusterValues() {
        return {
            doorLock: CustomDoorLockServer.DEFAULTS,
        };
    }
}
