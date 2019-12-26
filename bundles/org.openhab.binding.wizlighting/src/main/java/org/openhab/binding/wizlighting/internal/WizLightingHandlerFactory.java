/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.wizlighting.internal;

import java.util.Collections;
import java.util.Set;

import org.eclipse.smarthome.core.net.NetworkAddressService;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.wizlighting.internal.WizLightingBindingConstants;
import org.openhab.binding.wizlighting.internal.handler.WizLightingHandler;
import org.openhab.binding.wizlighting.internal.handler.WizLightingMediator;
import org.openhab.binding.wizlighting.internal.exceptions.MacAddressNotValidException;
import org.openhab.binding.wizlighting.internal.utils.ValidationUtils;
import org.openhab.binding.wizlighting.internal.utils.NetworkUtils;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link WizLightingHandlerFactory} is responsible for creating things and
 * thing handlers.
 *
 * @author Sriram Balakrishnan - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.wizlighting", service = ThingHandlerFactory.class)
public class WizLightingHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections
            .singleton(WizLightingBindingConstants.THING_TYPE_WIZ_BULB);

    private final Logger logger = LoggerFactory.getLogger(WizLightingHandlerFactory.class);

    private @Nullable WizLightingMediator mediator;

    private @Nullable NetworkAddressService networkAddressService;

    private @Nullable String myIpAddress;

    private @Nullable String myMacAddress;

    @Override
    protected void activate(ComponentContext componentContext) {
        super.activate(componentContext);
    };

    /**
     * Used by OSGI to inject the mediator in the handler factory.
     *
     * @param mediator the mediator
     */
    @Reference(cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.STATIC)
    public void setMediator(final WizLightingMediator mediator) {
        logger.trace("Mediator has been injected on handler factory service.");
        this.mediator = mediator;
    }

    /**
     * Used by OSGI to unsets the mediator from the handler factory.
     *
     * @param mediator the mediator
     */
    public void unsetMediator(final WizLightingMediator mitsubishiMediator) {
        logger.trace("Mediator has been unsetted from handler factory service.");
        this.mediator = null;
    }

    @Override
    public boolean supportsThingType(final ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(final Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        logger.trace("Creating Handler Request for {}", thingTypeUID);

        if (thingTypeUID.equals(WizLightingBindingConstants.THING_TYPE_WIZ_BULB)) {
            WizLightingHandler handler;

            String myIp = getMyIpAddress();
            String myMac = getMyMacAddress();
            if (myIp != null && myMac != null) {
                logger.debug("Creating a new WizLightingHandler...");
                try {
                    handler = new WizLightingHandler(thing, myIp, myMac);
                    logger.debug("WizLightingMediator will register the handler.");

                    WizLightingMediator mediator = this.mediator;
                    if (mediator != null) {
                        mediator.registerThingAndWizBulbHandler(thing, handler);
                    } else {
                        logger.error(
                                "The mediator is missing on Handler factory. Without one mediator the handler cannot work!");
                        return null;
                    }
                    return handler;
                } catch (MacAddressNotValidException e) {
                    logger.debug("The mac address passed by configurations is not valid.");
                }
            } else {
                logger.error("No IP or MAC have been returned by the network service, cannot create handler!");
                return null;
            }
        }
        return null;
    }

    private @Nullable String getMyIpAddress() {
        NetworkAddressService networkAddressService = this.networkAddressService;
        String ohIpAddress = String.valueOf(WizLightingBindingConstants.OH_IP_ADDRESS_ARG);
        if (myIpAddress != null) {
            return myIpAddress;
        } else if (ohIpAddress != null) {
            return ohIpAddress;
        } else if (networkAddressService != null) {
            myIpAddress = networkAddressService.getPrimaryIpv4HostAddress();
            if (myIpAddress == null) {
                logger.warn("No network interface could be found.  IP of OpenHab device is unknown");
                return null;
            }
            logger.info("IP of OpenHab device is {}.", myIpAddress);
            return myIpAddress;
        } else {
            return null;
        }
    }

    private @Nullable String getMyMacAddress() {
        String ohMacAddress = String.valueOf(WizLightingBindingConstants.OH_MAC_ADDRESS_ARG);
        if (myMacAddress != null) {
            return myMacAddress;
        } else if (ohMacAddress != null && ValidationUtils.isMacValid(ohMacAddress)) {
            logger.info("Mac Address of OpenHab device is {}.", ohMacAddress);
            return ohMacAddress;
        } else {
            try {
                myMacAddress = NetworkUtils.getMyMacAddress();
                if (myMacAddress == null) {
                    logger.warn("No network interface could be found.  Mac of OpenHab device is unknown.");
                    return null;
                }
            } catch (Exception e) {
                logger.warn("Mac Address of OpenHab device is invalid.");
                return null;
            }
            logger.info("Mac Address of OpenHab device is {}.", myMacAddress);
            return myMacAddress;
        }
    }

    @Override
    public void unregisterHandler(final Thing thing) {
        WizLightingMediator mediator = this.mediator;
        if (mediator != null) {
            mediator.unregisterWizBulbHandlerByThing(thing);
        }
        super.unregisterHandler(thing);
    }

    @Reference
    protected void setNetworkAddressService(NetworkAddressService networkAddressService) {
        this.networkAddressService = networkAddressService;
    }

    protected void unsetNetworkAddressService(NetworkAddressService networkAddressService) {
        this.networkAddressService = null;
    }
}
