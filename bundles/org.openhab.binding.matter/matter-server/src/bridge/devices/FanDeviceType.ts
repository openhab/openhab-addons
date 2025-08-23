import { Endpoint } from "@matter/node";
import { FanDevice } from "@matter/node/devices/fan";
import { CustomFanControlServer, CustomOnOffServer } from "../behaviors";
import { BaseDeviceType } from "./BaseDeviceType";

export class FanDeviceType extends BaseDeviceType {
    override createEndpoint(clusterValues: Record<string, any>) {
        const fanControlServer = CustomFanControlServer.with(
            ...CustomFanControlServer.features(clusterValues.fanControl),
        );

        const endpoint = new Endpoint(
            FanDevice.with(
                fanControlServer,
                ...(clusterValues.onOff?.onOff !== undefined ? [CustomOnOffServer] : []),
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
            fanControl: { ...CustomFanControlServer.DEFAULTS },
            onOff: { ...CustomOnOffServer.DEFAULTS },
        };
    }
}
