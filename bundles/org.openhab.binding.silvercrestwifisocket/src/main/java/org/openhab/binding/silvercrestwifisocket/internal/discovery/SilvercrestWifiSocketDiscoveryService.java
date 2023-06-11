/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.silvercrestwifisocket.internal.discovery;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.openhab.binding.silvercrestwifisocket.internal.SilvercrestWifiSocketBindingConstants;
import org.openhab.binding.silvercrestwifisocket.internal.handler.SilvercrestWifiSocketMediator;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the {@link DiscoveryService} for the Silvercrest Items.
 *
 * @author Jaime Vaz - Initial contribution
 *
 */
@Component(service = DiscoveryService.class, configurationPid = "discovery.silvercrestwifisocket")
public class SilvercrestWifiSocketDiscoveryService extends AbstractDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(SilvercrestWifiSocketDiscoveryService.class);
    private SilvercrestWifiSocketMediator mediator;

    /**
     * Used by OSGI to inject the mediator in the discovery service.
     *
     * @param mediator the mediator
     */
    @Reference
    public void setMediator(final SilvercrestWifiSocketMediator mediator) {
        logger.debug("Mediator has been injected on discovery service.");
        this.mediator = mediator;
        mediator.setDiscoveryService(this);
    }

    /**
     * Used by OSGI to unset the mediator in the discovery service.
     *
     * @param mediator the mediator
     */
    public void unsetMediator(final SilvercrestWifiSocketMediator mitsubishiMediator) {
        logger.debug("Mediator has been unsetted from discovery service.");
        this.mediator.setDiscoveryService(null);
        this.mediator = null;
    }

    /**
     * Constructor of the discovery service.
     *
     * @throws IllegalArgumentException if the timeout < 0
     */
    public SilvercrestWifiSocketDiscoveryService() throws IllegalArgumentException {
        super(SilvercrestWifiSocketBindingConstants.SUPPORTED_THING_TYPES_UIDS,
                SilvercrestWifiSocketBindingConstants.DISCOVERY_TIMEOUT_SECONDS);
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return SilvercrestWifiSocketBindingConstants.SUPPORTED_THING_TYPES_UIDS;
    }

    @Override
    protected void startScan() {
        logger.debug("Don't need to start new scan... background scanning in progress by mediator.");
    }

    /**
     * Method called by mediator, when receive one packet from one unknown Wifi Socket.
     *
     * @param macAddress the mack address from the device.
     * @param hostAddress the host address from the device.
     */
    public void discoveredWifiSocket(final String macAddress, final String hostAddress) {
        Map<String, Object> properties = new HashMap<>(2);
        properties.put(SilvercrestWifiSocketBindingConstants.MAC_ADDRESS_ARG, macAddress);
        properties.put(SilvercrestWifiSocketBindingConstants.HOST_ADDRESS_ARG, hostAddress);

        ThingUID newThingId = new ThingUID(SilvercrestWifiSocketBindingConstants.THING_TYPE_WIFI_SOCKET, macAddress);
        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(newThingId).withProperties(properties)
                .withLabel("Silvercrest Wifi Socket").withRepresentationProperty(macAddress).build();

        logger.debug("Discovered new thing with mac address '{}' and host address '{}'", macAddress, hostAddress);

        this.thingDiscovered(discoveryResult);
    }

    // SETTERS AND GETTERS
    /**
     * Gets the {@link SilvercrestWifiSocketMediator} of this binding.
     *
     * @return {@link SilvercrestWifiSocketMediator}.
     */
    public SilvercrestWifiSocketMediator getMediator() {
        return this.mediator;
    }
}
