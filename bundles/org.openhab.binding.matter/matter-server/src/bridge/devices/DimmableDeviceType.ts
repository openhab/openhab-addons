import { LevelControl, OnOff } from "@matter/main/clusters";
import { Endpoint } from "@matter/node";
import { DimmableLightDevice } from "@matter/node/devices/dimmable-light";
import { CustomLevelControlServer, CustomOnOffServer } from "../behaviors";
import { BaseDeviceType } from "./BaseDeviceType";

export class DimmableDeviceType extends BaseDeviceType {
    override createEndpoint(clusterValues: Record<string, any>) {
        const endpoint = new Endpoint(
            // Supplying a behavior replaces the one the device type defines, so the features it specializes have to
            // be repeated here. DimmableLightDevice requires OnOff with Lighting and LevelControl with Lighting and
            // OnOff; without the latter LevelControlServer.couple() never runs and a MoveToLevelWithOnOff command
            // sets the level without turning the light on.
            DimmableLightDevice.with(
                CustomOnOffServer.with(OnOff.Feature.Lighting),
                CustomLevelControlServer.with(LevelControl.Feature.Lighting, LevelControl.Feature.OnOff),
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
