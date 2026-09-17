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
        // A move with on/off to the minimum level is how a client turns the light off, while the same level from a
        // plain move is the dimmest the light can be. Only we can tell those apart, so openHAB is sent the meaning
        // rather than a level it would have to guess at.
        const off = withOnOff && level === this.minLevel;
        const clusterName = off ? "onOff" : "levelControl";
        const attributeName = off ? "onOff" : "currentLevel";
        const functions = this.env.get(DeviceFunctions);
        functions.sendAttributeChangedEvent(this.endpoint.id, clusterName, attributeName, off ? false : level);
        if (this.state.currentLevel !== level) {
            try {
                await functions.waitForStateUpdate(this.endpoint.id, clusterName, attributeName, 15000);
            } catch {
                logger.debug(`No ${attributeName} confirmation from openHAB for ${this.endpoint.id}, proceeding`);
            }
        }
        return super.moveToLevelLogic(level, transitionTime, withOnOff, options);
    }
}
