import { OnOff } from "@matter/main/clusters";
import { Endpoint } from "@matter/node";
import { OnOffLightDevice } from "@matter/node/devices/on-off-light";
import { CustomOnOffServer } from "../behaviors";
import { BaseDeviceType } from "./BaseDeviceType";

export class OnOffLightDeviceType extends BaseDeviceType {
    override createEndpoint(clusterValues: Record<string, any>) {
        // OnOffLightDevice requires OnOff with Lighting; supplying our own server replaces the device type's and
        // drops that feature unless it is repeated here.
        const endpoint = new Endpoint(
            OnOffLightDevice.with(...this.baseClusterServers, CustomOnOffServer.with(OnOff.Feature.Lighting)),
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
