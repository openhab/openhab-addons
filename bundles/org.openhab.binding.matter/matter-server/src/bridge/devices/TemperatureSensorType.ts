import { Endpoint } from "@matter/node";
import { TemperatureSensorDevice } from "@matter/node/devices/temperature-sensor";
import { BaseDeviceType } from "./BaseDeviceType";

export class TemperatureSensorType extends BaseDeviceType {
    override createEndpoint(clusterValues: Record<string, any>) {
        const endpoint = new Endpoint(TemperatureSensorDevice.with(...this.baseClusterServers), {
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
