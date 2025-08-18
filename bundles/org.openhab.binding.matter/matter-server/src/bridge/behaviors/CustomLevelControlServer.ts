import { LevelControlServer } from "@matter/main/behaviors";
import { LevelControl } from "@matter/main/clusters";
import { TypeFromPartialBitSchema } from "@matter/main/types";
import { DeviceFunctions } from "../DeviceFunctions";

export class CustomLevelControlServer extends LevelControlServer {
    static readonly DEFAULTS = { currentLevel: 254 } as const;

    override async moveToLevelLogic(
        level: number,
        transitionTime: number | null,
        withOnOff: boolean,
        options: TypeFromPartialBitSchema<typeof LevelControl.Options>,
    ) {
        this.env
            .get(DeviceFunctions)
            .sendAttributeChangedEvent(this.endpoint.id, "levelControl", "currentLevel", level);
        if (this.endpoint.stateOf(CustomLevelControlServer).currentLevel !== level) {
            await this.env
                .get(DeviceFunctions)
                .waitForStateUpdate(this.endpoint.id, "levelControl", "currentLevel", 15000);
        }
        return super.moveToLevelLogic(level, transitionTime, withOnOff, options);
    }
}
