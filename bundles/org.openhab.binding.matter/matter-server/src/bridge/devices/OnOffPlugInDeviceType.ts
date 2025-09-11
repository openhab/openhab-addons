import { Endpoint } from "@matter/node";
import { OnOffPlugInUnitDevice } from "@matter/node/devices/on-off-plug-in-unit";
import { CustomOnOffServer } from "../behaviors";
import { BaseDeviceType } from "./BaseDeviceType";

export class OnOffPlugInDeviceType extends BaseDeviceType {
    override createEndpoint(clusterValues: Record<string, any>) {
        const endpoint = new Endpoint(OnOffPlugInUnitDevice.with(...this.baseClusterServers, CustomOnOffServer), {
            ...this.endPointDefaults(),
            ...clusterValues,
        });
        return endpoint;
    }

    override defaultClusterValues() {
        return {
            onOff: CustomOnOffServer.DEFAULTS,
        };
    }
}
