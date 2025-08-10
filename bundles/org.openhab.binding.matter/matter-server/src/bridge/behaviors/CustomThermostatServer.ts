import { ActionContext, MaybePromise } from "@matter/main";
import { Thermostat } from "@matter/main/clusters";
import { ThermostatServer } from "@matter/node/behaviors/thermostat";
import { DeviceFunctions } from "../DeviceFunctions";

export class CustomThermostatServer extends ThermostatServer {
    static readonly DEFAULTS = {
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
    } as const;

    static features(values: any): Thermostat.Feature[] {
        const feats: Thermostat.Feature[] = [];
        let controlSeq = -1;
        if (values?.occupiedHeatingSetpoint !== undefined) {
            feats.push(Thermostat.Feature.Heating);
            controlSeq = 2;
        }
        if (values?.occupiedCoolingSetpoint !== undefined) {
            feats.push(Thermostat.Feature.Cooling);
            controlSeq = controlSeq < 0 ? 0 : controlSeq;
        }
        if (feats.includes(Thermostat.Feature.Heating) && feats.includes(Thermostat.Feature.Cooling)) {
            feats.push(Thermostat.Feature.AutoMode);
            controlSeq = 4;
        }
        if (controlSeq < 0) throw new Error("At least heating or cooling must be provided");
        values.controlSequenceOfOperation = controlSeq;
        return feats;
    }

    override initialize(_options?: {}): MaybePromise {
        super.initialize(_options);
        const ev: any = this.endpoint.events;
        ev.thermostat.occupiedHeatingSetpoint$Changed?.on((v: any, _o: any, ctx?: ActionContext) => {
            this.env
                .get(DeviceFunctions)
                .attributeChanged(this.endpoint.id, "thermostat", "occupiedHeatingSetpoint", v, ctx);
        });
        ev.thermostat.occupiedCoolingSetpoint$Changed?.on((v: any, _o: any, ctx?: ActionContext) => {
            this.env
                .get(DeviceFunctions)
                .attributeChanged(this.endpoint.id, "thermostat", "occupiedCoolingSetpoint", v, ctx);
        });
        ev.thermostat.systemMode$Changed.on((v: any, _o: any, ctx?: ActionContext) => {
            this.env.get(DeviceFunctions).attributeChanged(this.endpoint.id, "thermostat", "systemMode", v, ctx);
        });
    }
}
