import { Endpoint } from "@matter/node";
import { OnOffLightDevice } from "@matter/node/devices/on-off-light";
import { CustomOnOffServer } from "../behaviors";
import { BaseDeviceType } from "./BaseDeviceType";

export class OnOffLightDeviceType extends BaseDeviceType {
    override createEndpoint(clusterValues: Record<string, any>) {
        const endpoint = new Endpoint(OnOffLightDevice.with(...this.baseClusterServers, CustomOnOffServer), {
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
