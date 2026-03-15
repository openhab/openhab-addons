import { CustomOnOffServer } from "./CustomOnOffServer";

/**
 * This class is used to send on/off events to the bridge.
 * The OnOff state will be set locally immediately and does not wait for openHAB to send a state change event.
 */
export class CustomOnOffLocalServer extends CustomOnOffServer {
    override async on() {
        this.sendOnOff(true);
        await super.on();
    }
    override async off() {
        this.sendOnOff(false);
        await super.off();
    }
}
