import { Logger } from "@matter/general";
import { CommissioningServer, NodeLifecycle } from "@matter/node";
import { DeviceCommissioner, FabricManager } from "@matter/protocol";
import { DeviceFunctions } from "../DeviceFunctions";

const logger = Logger.get("BridgeCommissioningServer");

/**
 * Notifies openHAB when the bridge opens its own commissioning window, at startup or after a factory reset, and keeps
 * that window open until the bridge is paired.
 */
export class BridgeCommissioningServer extends CommissioningServer {
    /**
     * matter.js closes the window an unpaired node opens after 48 hours. Reopen it each time, so the bridge stays
     * pairable until it is paired.
     */
    override async enterCommissionableMode() {
        const { env, endpoint } = this;
        const reopenWhileUnpaired = async () => {
            if (!(endpoint.lifecycle as NodeLifecycle).isOnline || env.get(FabricManager).fabrics.length > 0) {
                return;
            }
            try {
                await env.get(DeviceCommissioner).allowBasicCommissioning({ onClose: reopenWhileUnpaired });
                logger.debug("Reopened the commissioning window of the unpaired bridge");
            } catch (error) {
                // the commissioner refuses to open a window while the node shuts down
                logger.debug(`Could not reopen the commissioning window: ${error}`);
            }
        };
        await env.get(DeviceCommissioner).allowBasicCommissioning({ onClose: reopenWhileUnpaired });
        this.initiateCommissioning();
    }

    protected override initiateCommissioning() {
        super.initiateCommissioning();
        this.env.get(DeviceFunctions).commissionable.emit();
    }
}
