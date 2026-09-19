import { OnOff } from "@matter/main/clusters";
import { Endpoint } from "@matter/node";
import { OnOffPlugInUnitDevice } from "@matter/node/devices/on-off-plug-in-unit";
import { CustomOnOffServer } from "../behaviors";
import { BaseDeviceType } from "./BaseDeviceType";

export class OnOffPlugInDeviceType extends BaseDeviceType {
    override createEndpoint(clusterValues: Record<string, any>) {
        // OnOffPlugInUnitDevice requires OnOff with Lighting; supplying our own server replaces the device type's
        // and drops that feature unless it is repeated here.
        const endpoint = new Endpoint(
            OnOffPlugInUnitDevice.with(...this.baseClusterServers, CustomOnOffServer.with(OnOff.Feature.Lighting)),
            {
                ...this.endPointDefaults(),
                ...clusterValues,
            },
        );
        return endpoint;
    }

    override defaultClusterValues() {
        return {
            onOff: CustomOnOffServer.DEFAULTS,
        };
    }
}
