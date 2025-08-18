import { ColorControl, LevelControl } from "@matter/main/clusters";
import { Endpoint } from "@matter/node";
import { ExtendedColorLightDevice } from "@matter/node/devices/extended-color-light";
import { CustomColorControlServer, CustomLevelControlServer, CustomOnOffLocalServer } from "../behaviors";
import { BaseDeviceType } from "./BaseDeviceType";

export class ColorDeviceType extends BaseDeviceType {
    private normalizeValue(value: number, min: number, max: number): number {
        return Math.min(Math.max(value, min), max);
    }

    override createEndpoint(clusterValues: Record<string, any>) {
        const { colorControl } = clusterValues;
        const { colorTempPhysicalMinMireds, colorTempPhysicalMaxMireds } = colorControl;

        colorControl.colorTemperatureMireds = this.normalizeValue(
            colorControl.colorTemperatureMireds,
            colorTempPhysicalMinMireds,
            colorTempPhysicalMaxMireds,
        );

        colorControl.startUpColorTemperatureMireds = this.normalizeValue(
            colorControl.startUpColorTemperatureMireds,
            colorTempPhysicalMinMireds,
            colorTempPhysicalMaxMireds,
        );

        colorControl.coupleColorTempToLevelMinMireds = this.normalizeValue(
            colorControl.coupleColorTempToLevelMinMireds,
            colorTempPhysicalMinMireds,
            colorTempPhysicalMaxMireds,
        );

        colorControl.coupleColorTempToLevelMaxMireds = this.normalizeValue(
            colorControl.coupleColorTempToLevelMaxMireds,
            colorTempPhysicalMinMireds,
            colorTempPhysicalMaxMireds,
        );

        const endpoint = new Endpoint(
            ExtendedColorLightDevice.with(
                // set OnOff Locally to ensure moveToHueAndSaturationLogic gets triggered when the
                // device is switched on from openHAB.
                CustomOnOffLocalServer,
                CustomLevelControlServer.with(LevelControl.Feature.Lighting),
                CustomColorControlServer.with(
                    ColorControl.Feature.HueSaturation,
                    ColorControl.Feature.ColorTemperature,
                ),
                ...this.baseClusterServers,
            ),
            {
                ...this.endPointDefaults(),
                ...clusterValues,
            },
        );

        return endpoint;
    }

    override defaultClusterValues() {
        return {
            levelControl: { ...CustomLevelControlServer.DEFAULTS },
            onOff: { ...CustomOnOffLocalServer.DEFAULTS },
            colorControl: { ...CustomColorControlServer.DEFAULTS },
        };
    }
}
