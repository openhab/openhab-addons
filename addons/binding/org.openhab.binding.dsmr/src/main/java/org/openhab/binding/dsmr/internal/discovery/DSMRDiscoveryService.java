package org.openhab.binding.dsmr.internal.discovery;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.dsmr.DSMRBindingConstants;
import org.openhab.binding.dsmr.device.DSMRDevice;
import org.openhab.binding.dsmr.device.discovery.DSMRMeterDiscoveryListener;
import org.openhab.binding.dsmr.meter.DSMRMeterConstants;
import org.openhab.binding.dsmr.meter.DSMRMeterDescriptor;
import org.openhab.binding.dsmr.meter.DSMRMeterType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This implements the discovery service for detecting new DSMR Meters
 *
 * @author M. Volaart
 * @since 2.0.0
 */
public class DSMRDiscoveryService extends AbstractDiscoveryService implements DSMRMeterDiscoveryListener {
    // Logger
    private final Logger logger = LoggerFactory.getLogger(DSMRDiscoveryService.class);

    // The Bridge ThingUID
    private ThingUID dsmrBridgeUID = null;

    /**
     * Constructs a new DSMRDiscoveryService with the specified DSMR Bridge ThingUID
     *
     * @param dsmrBridgeUID ThingUID for the DSMR Bridges
     */
    public DSMRDiscoveryService(ThingUID dsmrBridgeUID) {
        super(DSMRMeterType.METER_THING_TYPES, DSMRBindingConstants.DSMR_DISCOVERY_TIMEOUT, false);
        this.dsmrBridgeUID = dsmrBridgeUID;
    }

    /**
     * Starts a new discovery scan.
     *
     * Since {@link DSMRDevice} will always notify when a new meter is found
     * nothing specific has to be done except just wait till new data is processed.
     *
     * Since new data is available every 10 seconds (< DSMR V5) or every second (DSMRV5)
     * will we just wait DSMR_DISCOVERY_TIMEOUT ({@link DSMRBindingConstants}
     */
    @Override
    protected void startScan() {
        /*
         * There is no specific action needed to do a discovery.
         * DSMRDevice will always notify new discovered devices
         */
        logger.debug("Started Discovery Scan (no specific action is needed for this binding)");
    }

    /**
     * Callback when a new meter is discovered
     * The new meter is described by the {@link DSMRMeterDescriptor}
     *
     * There will be a DiscoveryResult created and sent to the framework.
     *
     * At this moment there are no reasons why a new meter will not be accepted.
     *
     * Therefor this callback will always return true.
     *
     * @param meterDescriptor the descriptor of the new detected meter
     * @return true (meter is always accepted)
     */
    @Override
    public boolean meterDiscovered(DSMRMeterDescriptor meterDescriptor) {
        DSMRMeterType meterType = meterDescriptor.getMeterType();

        ThingTypeUID thingTypeUID = meterType.getThingTypeUID();
        String thingId = "dsmr:" + meterType.name().toLowerCase() + ":"
                + (meterDescriptor.getChannel() == DSMRMeterConstants.UNKNOWN_CHANNEL ? "default"
                        : meterDescriptor.getChannel())
                + "_" + meterDescriptor.getIdString();
        ThingUID thingUID = new ThingUID(thingId);

        // Construct the configuration for this meter
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("meterType", meterType.name());
        properties.put("channel", meterDescriptor.getChannel());
        properties.put("idString", meterDescriptor.getIdString());

        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withThingType(thingTypeUID)
                .withBridge(dsmrBridgeUID).withProperties(properties).withLabel(meterType.meterKind.toString()).build();

        logger.debug("{} for meterDescriptor {}", discoveryResult, meterDescriptor);
        thingDiscovered(discoveryResult);

        return true;
    }
}