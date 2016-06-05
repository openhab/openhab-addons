package org.openhab.binding.silvercrest.discovery;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.silvercrest.SilvercrestBindingConstants;
import org.openhab.binding.silvercrest.handler.WifiSocketOutletMediator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the {@link DiscoveryService} for the Silvercrest Items.
 *
 * @author Jaime Vaz - Initial contribution
 *
 */
public class SilvercrestDiscoveryService extends AbstractDiscoveryService {

    private static final Logger LOG = LoggerFactory.getLogger(SilvercrestDiscoveryService.class);
    private final WifiSocketOutletMediator mediator;

    /**
     * Constructor of the discovery service.
     *
     * @throws IllegalArgumentException if the timeout < 0
     */
    public SilvercrestDiscoveryService() throws IllegalArgumentException {
        super(SilvercrestBindingConstants.SUPPORTED_THING_TYPES_UIDS,
                SilvercrestBindingConstants.DISCOVERY_TIMEOUT_SECONDS);
        LOG.debug("SilvercrestWifiSocketMediator is not initialized yet will create one mediator...");
        this.mediator = new WifiSocketOutletMediator(this);
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return SilvercrestBindingConstants.SUPPORTED_THING_TYPES_UIDS;
    }

    @Override
    protected void startScan() {
        LOG.debug("Don't need to start new scan... background scanning in progress by mediator.");
    }

    /**
     * Method called by mediator, when receive one packet from one unknown Wifi Socket Outlet.
     *
     * @param macAddress the mack address from the device.
     * @param hostAddress the host address from the device.
     */
    public void discoveredWifiSocketOutlet(final String macAddress, final String hostAddress) {
        Map<String, Object> properties = new HashMap<>(2);
        properties.put(SilvercrestBindingConstants.MAC_ADDRESS_ARG, macAddress);
        properties.put(SilvercrestBindingConstants.HOST_ADDRESS_ARG, hostAddress);

        ThingUID newThingId = this.getNewThingId(macAddress);
        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(newThingId).withProperties(properties)
                .withLabel("Wifi Socket Outlet " + newThingId.getId()).build();

        LOG.debug("Discovered new thing with mac address '{}' and host address '{}'", macAddress, hostAddress);

        this.thingDiscovered(discoveryResult);
    }

    /**
     * Gets one unrepeated {@link ThingUID} based on the existing {@link ThingUID} in {@link WifiSocketOutletMediator}.
     *
     * @param macAddress the mac address to generate the {@link ThingUID}.
     * @return the new unexisting {@link ThingUID}.
     */
    private ThingUID getNewThingId(final String macAddress) {
        ThingUID thingUID = new ThingUID(SilvercrestBindingConstants.THING_TYPE_SOCKET_OUTLET, macAddress);

        boolean thingUIDExists = this.thingUIDExistsInMediator(thingUID);

        if (thingUIDExists) {
            int index = 1;
            while (this.thingUIDExistsInMediator(thingUID)) {
                thingUID = new ThingUID(SilvercrestBindingConstants.THING_TYPE_SOCKET_OUTLET, macAddress + "_" + index);
            }
        }
        return thingUID;
    }

    /**
     * Check if the {@link ThingUID} exists in {@link WifiSocketOutletMediator}.
     *
     * @param thingUID the {@link ThingUID}.
     * @return true if the {@link ThingUID} already exists.
     */
    private boolean thingUIDExistsInMediator(final ThingUID thingUID) {
        boolean exists = false;
        for (Thing thing : this.getMediator().getAllThingsRegistred()) {
            if (thing.getUID().equals(thingUID)) {
                exists = true;
                break;
            }
        }
        return exists;
    }

    // SETTERS AND GETTERS
    /**
     * Gets the {@link WifiSocketOutletMediator} of this binding.
     *
     * @return {@link WifiSocketOutletMediator}.
     */
    public WifiSocketOutletMediator getMediator() {
        return this.mediator;
    }

}
