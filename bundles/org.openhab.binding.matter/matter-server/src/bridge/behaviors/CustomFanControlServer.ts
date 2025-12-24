import { ActionContext, MaybePromise } from "@matter/main";
import { FanControl } from "@matter/main/clusters";
import { FanControlServer } from "@matter/node/behaviors";
import { DeviceFunctions } from "../DeviceFunctions";

export class CustomFanControlServer extends FanControlServer {
    static readonly DEFAULTS = {
        featureMap: {
            auto: false,
            step: false,
            multiSpeed: false,
            airflowDirection: false,
            rocking: false,
            wind: false,
        },
        fanMode: 0,
        fanModeSequence: 5,
        percentCurrent: 0,
        percentSetting: 0,
    } as const;

    static features(fanControlValues: any): FanControl.Feature[] {
        const feats: FanControl.Feature[] = [];
        if (fanControlValues?.featureMap?.step) feats.push(FanControl.Feature.Step);
        switch (fanControlValues?.fanModeSequence) {
            case FanControl.FanModeSequence.OffLowMedHighAuto:
            case FanControl.FanModeSequence.OffLowHighAuto:
            case FanControl.FanModeSequence.OffHighAuto:
                feats.push(FanControl.Feature.Auto);
                if (fanControlValues?.featureMap) fanControlValues.featureMap.auto = true;
                break;
            default:
                if (fanControlValues?.featureMap) fanControlValues.featureMap.auto = false;
                break;
        }
        return feats;
    }

    override initialize(_options?: {}): MaybePromise {
        super.initialize(_options);
        const events: any = this.endpoint.events;
        events.fanControl.fanMode$Changed.on((v: any, _o: any, ctx?: ActionContext) => {
            this.env.get(DeviceFunctions).attributeChanged(this.endpoint.id, "fanControl", "fanMode", v, ctx);
        });
        events.fanControl.percentSetting$Changed.on((v: any, _o: any, ctx?: ActionContext) => {
            this.env.get(DeviceFunctions).attributeChanged(this.endpoint.id, "fanControl", "percentSetting", v, ctx);
        });
    }
}
