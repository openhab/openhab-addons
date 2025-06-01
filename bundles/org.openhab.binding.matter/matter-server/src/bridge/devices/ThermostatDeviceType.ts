import { Thermostat } from "@matter/main/clusters";
import { Endpoint } from "@matter/node";
import { ThermostatServer } from "@matter/node/behaviors/thermostat";
import { ThermostatDevice } from "@matter/node/devices/thermostat";
import { GenericDeviceType } from "./GenericDeviceType";

export class ThermostatDeviceType extends GenericDeviceType {
    override createEndpoint(clusterValues: Record<string, any>) {
        let controlSequenceOfOperation = -1;
        const features: Thermostat.Feature[] = [];
        if (clusterValues.thermostat?.occupiedHeatingSetpoint != undefined) {
            features.push(Thermostat.Feature.Heating);
            controlSequenceOfOperation = 2;
        }
        if (clusterValues.thermostat?.occupiedCoolingSetpoint != undefined) {
            features.push(Thermostat.Feature.Cooling);
            controlSequenceOfOperation = 0;
        }
        if (features.indexOf(Thermostat.Feature.Heating) != -1 && features.indexOf(Thermostat.Feature.Cooling) != -1) {
            features.push(Thermostat.Feature.AutoMode);
            controlSequenceOfOperation = 4;
        }

        if (controlSequenceOfOperation < 0) {
            throw new Error("At least heating, cooling or both must be added");
        }

        clusterValues.thermostat.controlSequenceOfOperation = controlSequenceOfOperation;

        const endpoint = new Endpoint(
            ThermostatDevice.with(
                this.createOnOffServer().with(),
                ThermostatServer.with(...features),
                ...this.defaultClusterServers(),
            ),
            {
                ...this.endPointDefaults(),
                ...clusterValues,
            },
        );
        endpoint.events.thermostat.occupiedHeatingSetpoint$Changed?.on(value => {
            this.sendBridgeEvent("thermostat", "occupiedHeatingSetpoint", value);
        });
        endpoint.events.thermostat.occupiedCoolingSetpoint$Changed?.on(value => {
            this.sendBridgeEvent("thermostat", "occupiedCoolingSetpoint", value);
        });
        endpoint.events.thermostat.systemMode$Changed.on(value => {
            this.sendBridgeEvent("thermostat", "systemMode", value);
        });
        return endpoint;
    }

    override defaultClusterValues() {
        return {
            thermostat: {
                systemMode: 0,
                localTemperature: 0,
                minHeatSetpointLimit: 0,
                maxHeatSetpointLimit: 3500,
                absMinHeatSetpointLimit: 0,
                absMaxHeatSetpointLimit: 3500,
                minCoolSetpointLimit: 0,
                absMinCoolSetpointLimit: 0,
                maxCoolSetpointLimit: 3500,
                absMaxCoolSetpointLimit: 3500,
                minSetpointDeadBand: 0,
            },
            onOff: {
                onOff: false,
            },
        };
    }
}
