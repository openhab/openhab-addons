import { Logger } from "@matter/main";
import { ModeSelect } from "@matter/main/clusters";
import { ModeSelectServer } from "@matter/node/behaviors";
import { DeviceFunctions } from "../DeviceFunctions";

const logger = Logger.get("CustomModeSelectServer");

export class CustomModeSelectServer extends ModeSelectServer {
    static readonly DEFAULTS = {
        modeSelect: {
            mode: 0,
        },
    } as const;

    override async changeToMode(request: ModeSelect.ChangeToModeRequest) {
        this.env
            .get(DeviceFunctions)
            .sendAttributeChangedEvent(this.endpoint.id, "modeSelect", "currentMode", request.newMode);
        if (this.state.currentMode !== request.newMode) {
            try {
                await this.env
                    .get(DeviceFunctions)
                    .waitForStateUpdate(this.endpoint.id, "modeSelect", "currentMode", 15000);
            } catch {
                logger.debug(`No currentMode confirmation from openHAB for ${this.endpoint.id}, proceeding`);
            }
        }
    }
}
