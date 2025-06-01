import { Logger } from "@matter/main";
import { FanControl } from "@matter/main/clusters";
import { Endpoint } from "@matter/node";
import { FanControlServer } from "@matter/node/behaviors";
import { FanDevice } from "@matter/node/devices/fan";
import { GenericDeviceType } from "./GenericDeviceType";

const logger = Logger.get("FanDeviceType");

export class FanDeviceType extends GenericDeviceType {
    override createEndpoint(clusterValues: Record<string, any>) {
        const features: FanControl.Feature[] = [];
        if (clusterValues.fanControl.featureMap.step === true) {
            features.push(FanControl.Feature.Step);
        }

        switch (clusterValues.fanControl.fanModeSequence) {
            case FanControl.FanModeSequence.OffLowMedHighAuto:
            case FanControl.FanModeSequence.OffLowHighAuto:
            case FanControl.FanModeSequence.OffHighAuto:
                features.push(FanControl.Feature.Auto);
                clusterValues.fanControl.featureMap.auto = true;
                break;
            default:
                clusterValues.fanControl.featureMap.auto = false;
                break;
        }

        logger.debug(`createEndpoint values: ${JSON.stringify(clusterValues)}`);
        const endpoint = new Endpoint(
            FanDevice.with(
                ...this.defaultClusterServers(),
                FanControlServer.with(...features),
                ...(clusterValues.onOff?.onOff !== undefined ? [this.createOnOffServer()] : []),
            ),
            {
                ...this.endPointDefaults(),
                ...clusterValues,
            },
        );
        endpoint.events.fanControl.fanMode$Changed.on(value => {
            this.sendBridgeEvent("fanControl", "fanMode", value);
        });

        endpoint.events.fanControl.percentSetting$Changed.on(value => {
            this.sendBridgeEvent("fanControl", "percentSetting", value);
        });

        return endpoint;
    }

    override defaultClusterValues() {
        return {
            fanControl: {
                featureMap: {
                    auto: false,
                    step: false,
                    multiSpeed: false,
                    airflowDirection: false,
                    rocking: false,
                    wind: false,
                },
                fanMode: FanControl.FanMode.Off,
                fanModeSequence: FanControl.FanModeSequence.OffHigh,
                percentCurrent: 0,
                percentSetting: 0,
            },
        };
    }
}
