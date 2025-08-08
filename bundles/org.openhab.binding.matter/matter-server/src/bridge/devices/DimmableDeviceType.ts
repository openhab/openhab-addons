import { LevelControlServer } from "@matter/main/behaviors";
import { LevelControl } from "@matter/main/clusters";
import { Endpoint } from "@matter/node";
import { DimmableLightDevice } from "@matter/node/devices/dimmable-light";
import { GenericDeviceType } from "./GenericDeviceType";

const LevelControlType = LevelControlServer.with(LevelControl.Feature.Lighting);

export class DimmableDeviceType extends GenericDeviceType {
    override createEndpoint(clusterValues: Record<string, any>) {
        const endpoint = new Endpoint(
            DimmableLightDevice.with(
                this.createOnOffServer(),
                this.createLevelControlServer().with(LevelControl.Feature.Lighting),
                ...this.defaultClusterServers(),
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
            levelControl: {
                currentLevel: 254,
            },
            onOff: {
                onOff: false,
            },
        };
    }
}
