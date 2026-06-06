import { ColorControlServer } from "@matter/main/behaviors";
import { DeviceFunctions } from "../DeviceFunctions";

export class CustomColorControlServer extends ColorControlServer {
    static readonly DEFAULTS = {
        colorMode: 0,
        currentHue: 0,
        currentSaturation: 0,
        colorTemperatureMireds: 154,
        startUpColorTemperatureMireds: 154,
        colorTempPhysicalMinMireds: 154,
        colorTempPhysicalMaxMireds: 667,
        coupleColorTempToLevelMinMireds: 154,
        coupleColorTempToLevelMaxMireds: 667,
    } as const;

    override async moveToColorTemperatureLogic(targetMireds: number, transitionTime: number) {
        this.env
            .get(DeviceFunctions)
            .sendAttributeChangedEvent(this.endpoint.id, "colorControl", "colorTemperatureMireds", targetMireds);
        return super.moveToColorTemperatureLogic(targetMireds, transitionTime);
    }

    override async moveToHueAndSaturationLogic(h: number, s: number, t: number) {
        const deviceFunctions = this.env.get(DeviceFunctions);
        deviceFunctions.sendAttributeChangedEvent(this.endpoint.id, "colorControl", "currentHue", h);
        deviceFunctions.sendAttributeChangedEvent(this.endpoint.id, "colorControl", "currentSaturation", s);
        return super.moveToHueAndSaturationLogic(h, s, t);
    }
}
