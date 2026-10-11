import { CommissioningServer } from "@matter/node";
import { DeviceFunctions } from "../DeviceFunctions";

/**
 * Notifies openHAB when the bridge opens its own commissioning window, at startup or after a factory reset.
 */
export class BridgeCommissioningServer extends CommissioningServer {
    protected override initiateCommissioning() {
        super.initiateCommissioning();
        this.env.get(DeviceFunctions).commissionable.emit();
    }
}
