import { Logger } from "@matter/main";
import { Endpoint } from "@matter/node";
import { ModeSelectDevice } from "@matter/node/devices/mode-select";
import { CustomModeSelectServer } from "../behaviors";
import { BaseDeviceType } from "./BaseDeviceType";
const logger = Logger.get("ModeSelectDevice");

export class ModeSelectDeviceType extends BaseDeviceType {
    override createEndpoint(clusterValues: Record<string, any>) {
        const cv = clusterValues;
        cv.modeSelect.supportedModes = cv.modeSelect.supportedModes.map((mode: any) => {
            return {
                label: mode.label,
                mode: mode.mode,
                semanticTags: mode.semanticTags.map((tag: any) => {
                    if (tag.mfgCode === undefined) {
                        tag.mfgCode = 0;
                    }
                    return {
                        mfgCode: tag.mfgCode,
                        value: tag.value,
                    };
                }),
            };
        });
        logger.debug(`ModeSelect Values: ${JSON.stringify(cv)}`);
        const endpoint = new Endpoint(ModeSelectDevice.with(...this.baseClusterServers, CustomModeSelectServer), {
            ...this.endPointDefaults(),
            ...clusterValues,
        });
        return endpoint;
    }

    override defaultClusterValues() {
        return {
            modeSelect: CustomModeSelectServer.DEFAULTS,
        };
    }
}
