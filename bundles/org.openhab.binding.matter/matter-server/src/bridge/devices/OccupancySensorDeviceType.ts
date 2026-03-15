import { OccupancySensing } from "@matter/main/clusters";
import { Endpoint } from "@matter/node";
import { OccupancySensingServer } from "@matter/node/behaviors";
import { OccupancySensorDevice } from "@matter/node/devices/occupancy-sensor";
import { BaseDeviceType } from "./BaseDeviceType";

/**
 * This is the device type for the occupancy sensor.
 */
export class OccupancySensorDeviceType extends BaseDeviceType {
    override createEndpoint(clusterValues: Record<string, any>) {
        const endpoint = new Endpoint(
            OccupancySensorDevice.with(
                OccupancySensingServer.with(OccupancySensing.Feature.PassiveInfrared),
                ...this.baseClusterServers,
            ),
            {
                ...this.endPointDefaults(),
                ...clusterValues,
            },
        );
        return endpoint;
    }

    override defaultClusterValues() {
        return {
            occupancySensing: {
                occupancy: {
                    occupied: false,
                },
                occupancySensorType: OccupancySensing.OccupancySensorType.Pir,
            },
        };
    }
}
