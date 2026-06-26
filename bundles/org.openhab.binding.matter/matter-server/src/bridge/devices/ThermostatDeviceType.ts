import { Endpoint } from "@matter/node";
import { ThermostatDevice } from "@matter/node/devices/thermostat";
import { CustomThermostatServer } from "../behaviors";
import { BaseDeviceType } from "./BaseDeviceType";

export class ThermostatDeviceType extends BaseDeviceType {
    override createEndpoint(clusterValues: Record<string, any>) {
        const thermostatServer = CustomThermostatServer.with(
            ...CustomThermostatServer.selectFeatures(clusterValues.thermostat),
        );

        const endpoint = new Endpoint(ThermostatDevice.with(thermostatServer, ...this.baseClusterServers), {
            ...this.endPointDefaults(),
            ...clusterValues,
        });

        return endpoint;
    }

    override defaultClusterValues(userValues: Record<string, any>) {
        const thermostat = userValues.thermostat ?? {};
        const hasHeating = thermostat.occupiedHeatingSetpoint !== undefined;
        const hasCooling = thermostat.occupiedCoolingSetpoint !== undefined;

        const thermostatDefaults: Record<string, any> = {
            ...CustomThermostatServer.DEFAULTS_COMMON,
        };
        if (hasHeating) {
            Object.assign(thermostatDefaults, CustomThermostatServer.DEFAULTS_HEAT);
        }
        if (hasCooling) {
            Object.assign(thermostatDefaults, CustomThermostatServer.DEFAULTS_COOL);
        }
        if (hasHeating && hasCooling) {
            Object.assign(thermostatDefaults, CustomThermostatServer.DEFAULTS_AUTO);
        }

        return {
            thermostat: thermostatDefaults,
        };
    }
}
