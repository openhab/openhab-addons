package org.openhab.binding.omnilink.discovery;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.openhab.binding.omnilink.OmnilinkBindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;

/**
 *
 * @author Craig Hamilton
 *
 */
public class OmnilinkBridgeDiscoveryService extends AbstractDiscoveryService {
    private static final Logger logger = LoggerFactory.getLogger(OmnilinkBridgeDiscoveryService.class);
    private static final int DISCOVER_TIMEOUT_SECONDS = 30;

    public OmnilinkBridgeDiscoveryService() {
        super(ImmutableSet.of(new ThingTypeUID(OmnilinkBindingConstants.BINDING_ID, "-")), DISCOVER_TIMEOUT_SECONDS,
                false);
    }

    @Override
    protected void startScan() {
        logger.debug("start scan called for omnilink bridge");

    }
}