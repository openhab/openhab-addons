import { OnOffServer } from "@matter/main/behaviors";
import { DeviceFunctions } from "../DeviceFunctions";

/**
 * This class is used to send on/off events to the bridge.
 * The OnOff state will not be set until openHAB sends a state change event.
 */
export class CustomOnOffServer extends OnOffServer {
    static readonly DEFAULTS = { onOff: false } as const;

    override async on() {
        // we only wait for ON/OFF if waiting for a different state
        if (this.endpoint.stateOf(CustomOnOffServer).onOff !== true) {
            await this.sendOnOffAndWait(true);
        } else {
            this.sendOnOff(true);
        }
        return super.on();
    }
    override async off() {
        if (this.endpoint.stateOf(CustomOnOffServer).onOff !== false) {
            await this.sendOnOffAndWait(false);
        } else {
            this.sendOnOff(false);
        }
        return super.off();
    }

    protected sendOnOff(onOff: boolean) {
        this.env.get(DeviceFunctions).sendAttributeChangedEvent(this.endpoint.id, "onOff", "onOff", onOff);
    }

    protected async sendOnOffAndWait(onOff: boolean) {
        this.sendOnOff(onOff);
        const result = await this.env
            .get(DeviceFunctions)
            .waitForStateUpdate(this.endpoint.id, "onOff", "onOff", 15000);
        if (result !== onOff) {
            throw new Error("On/Off failed", { cause: result });
        }
    }
}
