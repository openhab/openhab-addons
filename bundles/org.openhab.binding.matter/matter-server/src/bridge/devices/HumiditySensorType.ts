import { Endpoint } from "@matter/node";
import { HumiditySensorDevice } from "@matter/node/devices/humidity-sensor";
import { BaseDeviceType } from "./BaseDeviceType";

export class HumiditySensorType extends BaseDeviceType {
    override createEndpoint(clusterValues: Record<string, any>) {
        const endpoint = new Endpoint(HumiditySensorDevice.with(...this.baseClusterServers), {
            ...this.endPointDefaults(),
            ...clusterValues,
        });
        return endpoint;
    }

    override defaultClusterValues() {
        return {
            relativeHumidityMeasurement: {
                measuredValue: 0,
            },
        };
    }
}
