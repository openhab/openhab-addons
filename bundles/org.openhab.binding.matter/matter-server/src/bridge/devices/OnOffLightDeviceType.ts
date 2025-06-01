import { Endpoint } from "@matter/node";
import { OnOffLightDevice } from "@matter/node/devices/on-off-light";
import { GenericDeviceType } from "./GenericDeviceType";

export class OnOffLightDeviceType extends GenericDeviceType {
    override createEndpoint(clusterValues: Record<string, any>) {
        const endpoint = new Endpoint(
            OnOffLightDevice.with(...this.defaultClusterServers(), this.createOnOffServer()),
            {
                ...this.endPointDefaults(),
                ...clusterValues,
            },
        );
        return endpoint;
    }

    override defaultClusterValues() {
        return {
            onOff: {
                onOff: false,
            },
        };
    }
}
