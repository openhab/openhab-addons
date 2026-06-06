import { ModeSelect } from "@matter/main/clusters";
import { ModeSelectServer } from "@matter/node/behaviors";
import { DeviceFunctions } from "../DeviceFunctions";

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
        if (this.endpoint.stateOf(CustomModeSelectServer).currentMode !== request.newMode) {
            await this.env
                .get(DeviceFunctions)
                .waitForStateUpdate(this.endpoint.id, "modeSelect", "currentMode", 15000);
        }
    }
}
