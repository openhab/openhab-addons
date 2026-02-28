import { WindowCovering } from "@matter/main/clusters";
import { Endpoint } from "@matter/node";
import { WindowCoveringDevice } from "@matter/node/devices/window-covering";
import { CustomWindowCoveringServer } from "../behaviors";
import { BaseDeviceType } from "./BaseDeviceType";

export class WindowCoveringDeviceType extends BaseDeviceType {
    override createEndpoint(clusterValues: Record<string, any>) {
        const features: WindowCovering.Feature[] = [
            WindowCovering.Feature.Lift,
            WindowCovering.Feature.PositionAwareLift,
        ];
        const endpoint = new Endpoint(
            WindowCoveringDevice.with(CustomWindowCoveringServer.with(...features), ...this.baseClusterServers),
            {
                ...this.endPointDefaults(),
                ...clusterValues,
            },
        );
        return endpoint;
    }

    override defaultClusterValues() {
        return {
            windowCovering: { ...CustomWindowCoveringServer.DEFAULTS },
        };
    }
}
