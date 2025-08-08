import { Endpoint } from "@matter/node";
import { OnOffPlugInUnitDevice } from "@matter/node/devices/on-off-plug-in-unit";
import { GenericDeviceType } from "./GenericDeviceType";

export class OnOffPlugInDeviceType extends GenericDeviceType {
    override createEndpoint(clusterValues: Record<string, any>) {
        const endpoint = new Endpoint(
            OnOffPlugInUnitDevice.with(...this.defaultClusterServers(), this.createOnOffServer()),
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
