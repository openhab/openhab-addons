package org.openhab.binding.systeminfo.discovery;

import static org.openhab.binding.systeminfo.SysteminfoBindingConstants.THING_TYPE_COMPUTER;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Set;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.systeminfo.SysteminfoBindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SysteminfoDiscoveryService extends AbstractDiscoveryService {
    private static final Logger logger = LoggerFactory.getLogger(SysteminfoDiscoveryService.class);
    public final static int DISCOVERY_TIME_SECONDS = 30;
    private final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_COMPUTER);

    public SysteminfoDiscoveryService() {
        super(SUPPORTED_THING_TYPES_UIDS, DISCOVERY_TIME_SECONDS);
    }

    @Override
    protected void startScan() {
        logger.debug("Starting system information discovery !");
        String hostname = "Unknown";

        try {
            InetAddress addr;
            addr = InetAddress.getLocalHost();
            hostname = addr.getHostName();
        } catch (UnknownHostException ex) {
            logger.info("Hostname can not be resolved. Computer name will be set to the default one: {}", hostname);
        }

        ThingTypeUID computerType = SysteminfoBindingConstants.THING_TYPE_COMPUTER;
        ThingUID computer = new ThingUID(computerType, hostname);
        thingDiscovered(DiscoveryResultBuilder.create(computer).withLabel("Local computer").build());
    }
}
