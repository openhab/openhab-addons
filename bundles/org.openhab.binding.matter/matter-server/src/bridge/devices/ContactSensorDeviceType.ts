import { Endpoint } from "@matter/node";
import { ContactSensorDevice } from "@matter/node/devices/contact-sensor";
import { GenericDeviceType } from "./GenericDeviceType"; // Adjust the path as needed

export class ContactSensorDeviceType extends GenericDeviceType {
    override createEndpoint(clusterValues: Record<string, any>) {
        const defaults = {
            booleanState: {
                stateValue: false,
            },
        };
        const endpoint = new Endpoint(ContactSensorDevice.with(...this.defaultClusterServers()), {
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
