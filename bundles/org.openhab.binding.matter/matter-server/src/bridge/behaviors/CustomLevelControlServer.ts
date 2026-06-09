import { Logger, MaybePromise } from "@matter/main";
import { LevelControlServer } from "@matter/main/behaviors";
import { LevelControl } from "@matter/main/clusters";
import { DeviceFunctions } from "../DeviceFunctions";

const logger = Logger.get("CustomLevelControlServer");

export class CustomLevelControlServer extends LevelControlServer {
    static readonly DEFAULTS = { currentLevel: 254 } as const;

    override initialize(): MaybePromise {
        if (this.features.lighting) {
            const minLevel = this.state.minLevel ?? 1;
            if (this.state.currentLevel === 0 || this.state.currentLevel === null) {
                this.state.currentLevel = minLevel;
            }
            if (this.state.onLevel === 0) {
                this.state.onLevel = null;
            }
        }
        return super.initialize();
    }

    override async moveToLevelLogic(
        level: number,
        transitionTime: number | null,
        withOnOff: boolean,
        options: LevelControl.Options = {},
    ) {
        this.env
            .get(DeviceFunctions)
            .sendAttributeChangedEvent(this.endpoint.id, "levelControl", "currentLevel", level);
        if (this.state.currentLevel !== level) {
            try {
                await this.env
                    .get(DeviceFunctions)
                    .waitForStateUpdate(this.endpoint.id, "levelControl", "currentLevel", 15000);
            } catch {
                logger.debug(`No currentLevel confirmation from openHAB for ${this.endpoint.id}, proceeding`);
            }
        }
        return super.moveToLevelLogic(level, transitionTime, withOnOff, options);
    }
}
