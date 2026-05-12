import { MaybePromise } from "@matter/main";
import { LevelControlServer } from "@matter/main/behaviors";
import { LevelControl } from "@matter/main/clusters";
import { DeviceFunctions } from "../DeviceFunctions";

export class CustomLevelControlServer extends LevelControlServer {
    static readonly DEFAULTS = { currentLevel: 254 } as const;

    /**
     * Sanitize persisted state before matter.js's post-initialize validation runs.
     *
     * matter.js 0.17 enforces the Matter spec constraint `minLevel ≤ currentLevel ≤ maxLevel`
     * (minLevel = 1 when the Lighting feature is enabled) at the end of behavior initialization.
     * Bridge endpoints persist their state across restarts, and any installation that ran on
     * matter.js 0.16 (which did not enforce this constraint) may have a stored `currentLevel = 0`
     * from a previously-off light. Without sanitization the bridge fails to start with
     * "Validating ...levelControl.state.currentLevel: Constraint 'minLevel to maxLevel': Value 0
     * is not within bounds".
     *
     * We coerce stored `currentLevel = 0` (or null) to `minLevel` when the Lighting feature is
     * active. The "off" state is already represented by `onOff = false`; per Matter spec
     * `currentLevel` should reflect the last-known dim level, never 0. `onLevel` shares the same
     * constraint but is nullable — coerce stored 0 to `null` (its "unset" value).
     */
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
        if (this.endpoint.stateOf(CustomLevelControlServer).currentLevel !== level) {
            await this.env
                .get(DeviceFunctions)
                .waitForStateUpdate(this.endpoint.id, "levelControl", "currentLevel", 15000);
        }
        return super.moveToLevelLogic(level, transitionTime, withOnOff, options);
    }
}
