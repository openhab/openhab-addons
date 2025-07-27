import { Endpoint } from "@matter/node";
import { TemperatureSensorDevice } from "@matter/node/devices/temperature-sensor";
import { GenericDeviceType } from "./GenericDeviceType";

export class TemperatureSensorType extends GenericDeviceType {
    override createEndpoint(clusterValues: Record<string, any>) {
        const endpoint = new Endpoint(TemperatureSensorDevice.with(...this.defaultClusterServers()), {
            ...this.endPointDefaults(),
            ...clusterValues,
        });
        return endpoint;
    }
    override defaultClusterValues() {
        return {
            temperatureMeasurement: {
                measuredValue: 0,
            },
        };
    }
}
