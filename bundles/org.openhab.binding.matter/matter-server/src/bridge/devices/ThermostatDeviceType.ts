import { Endpoint } from "@matter/node";
import { ThermostatDevice } from "@matter/node/devices/thermostat";
import { CustomThermostatServer } from "../behaviors";
import { BaseDeviceType } from "./BaseDeviceType";

export class ThermostatDeviceType extends BaseDeviceType {
    override createEndpoint(clusterValues: Record<string, any>) {
        const thermostatServer = CustomThermostatServer.with(
            ...CustomThermostatServer.features(clusterValues.thermostat),
        );

        const endpoint = new Endpoint(ThermostatDevice.with(thermostatServer, ...this.baseClusterServers), {
            ...this.endPointDefaults(),
            ...clusterValues,
        });

        return endpoint;
    }

    override defaultClusterValues(userValues: Record<string, any>) {
        const defaults: Record<string, any> = {
            thermostat: CustomThermostatServer.DEFAULTS,
        };
        return defaults;
    }
}
