package org.openhab.binding.lutron.internal.hw;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.lutron.internal.LutronHandlerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HwDiscoveryService extends AbstractDiscoveryService {
    private Logger logger = LoggerFactory.getLogger(HwDiscoveryService.class);

    private final HwSerialBridgeHandler handler;

    public HwDiscoveryService(HwSerialBridgeHandler handler) {
        super(LutronHandlerFactory.HW_DISCOVERABLE_DEVICE_TYPES_UIDS, 10);
        this.handler = handler;
    }

    @Override
    protected void startScan() {
        // Scan for dimmers
        try {
            for (int m = 1; m <= 8; m++) { // Modules
                for (int o = 1; o <= 4; o++) { // Outputs
                    String address = String.format("[01:01:00:%02d:%02d]", m, o);
                    handler.sendCommand("RDL, " + address);
                    Thread.sleep(5);
                }
            }
        } catch (InterruptedException e) {
            logger.warn("Scan interrupted");
        }
    }

    /**
     * Called by the bridge when it receives a status update for a dimmer that is not registered.
     */
    public void declareUnknownDimmer(String address) {
        if (address == null) {
            logger.info("Discovered HomeWorks dimmer with no address");
            return;
        }
        String addressUid = address.replaceAll("[\\[\\]]", "").replaceAll(":", "-");
        ThingUID bridgeUID = this.handler.getThing().getUID();
        ThingUID uid = new ThingUID(HwConstants.THING_TYPE_HWDIMMER, bridgeUID, addressUid);

        Map<String, Object> props = new HashMap<>();

        // TODO put this in a constant
        props.put("address", address);
        DiscoveryResult result = DiscoveryResultBuilder.create(uid).withBridge(bridgeUID).withProperties(props)
                .withRepresentationProperty("address").build();

        thingDiscovered(result);

        logger.debug("Discovered {}", uid);
    }
}
