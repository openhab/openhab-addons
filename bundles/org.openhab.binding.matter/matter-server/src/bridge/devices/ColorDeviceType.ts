import { ColorControlServer } from "@matter/main/behaviors";
import { ColorControl, LevelControl, OnOff } from "@matter/main/clusters";
import { Endpoint } from "@matter/node";
import { ExtendedColorLightDevice } from "@matter/node/devices/extended-color-light";
import { GenericDeviceType } from "./GenericDeviceType"; // Adjust the path as needed

export class ColorDeviceType extends GenericDeviceType {
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
                //setLocally=true for createOnOffServer otherwise moveToHueAndSaturationLogic will not be called b/c matter.js thinks the device is OFF.
                this.createOnOffServer(true).with(OnOff.Feature.Lighting),
                this.createLevelControlServer().with(LevelControl.Feature.Lighting),
                this.createColorControlServer().with(
                    ColorControl.Feature.HueSaturation,
                    ColorControl.Feature.ColorTemperature,
                ),
                ...this.defaultClusterServers(),
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
            levelControl: {
                currentLevel: 0,
            },
            onOff: {
                onOff: false,
            },
            colorControl: {
                colorMode: 0,
                currentHue: 0,
                currentSaturation: 0,
                colorTemperatureMireds: 154,
                startUpColorTemperatureMireds: 154,
                colorTempPhysicalMinMireds: 154,
                colorTempPhysicalMaxMireds: 667,
                coupleColorTempToLevelMinMireds: 154,
                coupleColorTempToLevelMaxMireds: 667,
            },
        };
    }

    protected createColorControlServer(): typeof ColorControlServer {
        const parent = this;
        return class extends ColorControlServer {
            override async moveToColorTemperatureLogic(targetMireds: number, transitionTime: number) {
                parent.sendBridgeEvent("colorControl", "colorTemperatureMireds", targetMireds);
                return super.moveToColorTemperatureLogic(targetMireds, transitionTime);
            }

            override async moveToHueAndSaturationLogic(
                targetHue: number,
                targetSaturation: number,
                transitionTime: number,
            ) {
                parent.sendBridgeEvent("colorControl", "currentHue", targetHue);
                parent.sendBridgeEvent("colorControl", "currentSaturation", targetSaturation);
            }
        };
    }
}
