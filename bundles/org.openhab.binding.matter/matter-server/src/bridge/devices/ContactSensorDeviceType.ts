import { Endpoint } from "@matter/node";
import { ContactSensorDevice } from "@matter/node/devices/contact-sensor";
import { BaseDeviceType } from "./BaseDeviceType"; // Adjust the path as needed

export class ContactSensorDeviceType extends BaseDeviceType {
    override createEndpoint(clusterValues: Record<string, any>) {
        const defaults = {
            booleanState: {
                stateValue: false,
            },
        };
        const endpoint = new Endpoint(ContactSensorDevice.with(...this.baseClusterServers), {
            ...this.endPointDefaults(),
            ...clusterValues,
        });

        return endpoint;
    }

    override defaultClusterValues() {
        return {
            booleanState: {
                stateValue: false,
            },
        };
    }
}
