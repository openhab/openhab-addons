import { LevelControl } from "@matter/main/clusters";
import { Endpoint } from "@matter/node";
import { DimmableLightDevice } from "@matter/node/devices/dimmable-light";
import { CustomLevelControlServer, CustomOnOffServer } from "../behaviors";
import { BaseDeviceType } from "./BaseDeviceType";

export class DimmableDeviceType extends BaseDeviceType {
    override createEndpoint(clusterValues: Record<string, any>) {
        const endpoint = new Endpoint(
            DimmableLightDevice.with(
                CustomOnOffServer,
                CustomLevelControlServer.with(LevelControl.Feature.Lighting),
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
            levelControl: { ...CustomLevelControlServer.DEFAULTS },
            onOff: { ...CustomOnOffServer.DEFAULTS },
        };
    }
}
