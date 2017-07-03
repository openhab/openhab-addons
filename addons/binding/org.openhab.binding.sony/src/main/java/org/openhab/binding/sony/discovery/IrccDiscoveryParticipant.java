/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sony.discovery;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.UpnpDiscoveryParticipant;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.jupnp.model.meta.DeviceDetails;
import org.jupnp.model.meta.RemoteDevice;
import org.jupnp.model.meta.RemoteDeviceIdentity;
import org.jupnp.model.meta.RemoteService;
import org.jupnp.model.types.ServiceId;
import org.jupnp.model.types.UDN;
import org.openhab.binding.sony.internal.ircc.IrccConfig;
import org.openhab.binding.sony.internal.ircc.IrccConstants;
import org.openhab.binding.sony.internal.ircc.models.IrccState;
import org.openhab.binding.sony.internal.ircc.models.IrccSystemInformation;
import org.openhab.binding.sony.internal.net.HttpRequest;
import org.openhab.binding.sony.internal.net.NetUtilities;
import org.openhab.binding.sony.internal.simpleip.SimpleIpConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

// TODO: Auto-generated Javadoc
/**
 * The Class IrccDiscoveryParticipant.
 *
 * @author Tim Roberts - Initial contribution
 */
public class IrccDiscoveryParticipant implements UpnpDiscoveryParticipant {

    /** The logger. */
    private Logger logger = LoggerFactory.getLogger(IrccDiscoveryParticipant.class);

    /**
     * Returns the set of supported things {@link SimpleIpConstants#THING_TYPE_SIMPLEIP}.
     *
     * @return a singleton set to the ircc thing type
     */
    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return Collections.singleton(IrccConstants.THING_TYPE_IRCC);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.smarthome.config.discovery.UpnpDiscoveryParticipant#createResult(org.jupnp.model.meta.RemoteDevice)
     */
    @Override
    public DiscoveryResult createResult(RemoteDevice device) {
        final ThingUID uid = getThingUID(device);
        if (uid != null) {
            try {
                return createIrccDevice(device, uid);
            } catch (Exception e) {
                logger.error("Exception creating IRCC device: {}", e.getMessage(), e);
            }
        }
        return null;
    }

    /**
     * Creates the ircc device.
     *
     * @param device the device
     * @param uid the uid
     * @return the discovery result
     * @throws Exception the exception
     */
    private DiscoveryResult createIrccDevice(RemoteDevice device, ThingUID uid) throws Exception {
        final Map<String, Object> properties = new HashMap<>(100);

        final RemoteDeviceIdentity identity = device.getIdentity();
        final URL irccURL = identity.getDescriptorURL();

        // Test to see if it supports the IRCC actions (X_CERS_ActionList_URL)
        try (final HttpRequest request = NetUtilities.createHttpRequest()) {
            try (final IrccState irccState = new IrccState(request, irccURL.toURI().toString())) {
                final String actionUrl = irccState.getUnrDeviceInformation().getActionListUrl();
                if (StringUtils.isEmpty(actionUrl)) {
                    logger.debug("IRCC device didn't implement a valid actionURL - ignoring: {}", identity);
                    return null;
                }
            }
        } catch (Exception e) {
            logger.debug("Exception getting device info: {}", e.getMessage(), e);
            return null;
        }

        int refreshSeconds = -1; // assume not capable of polling
        try (final HttpRequest request = NetUtilities.createHttpRequest()) {

            String sysWolAddress = null;
            try {
                try (final IrccState irccState = new IrccState(request, irccURL.toURI().toString())) {
                    final IrccSystemInformation systemInformation = irccState.getSystemInformation();
                    sysWolAddress = systemInformation.getWolMacAddress();

                    // If it has a status URL - assume we can poll the status
                    final String getStatusUrl = irccState.getUrlForAction(IrccState.AN_GETSTATUS);
                    if (!StringUtils.isEmpty(getStatusUrl)) {
                        refreshSeconds = 2;
                    }
                }
            } catch (NotImplementedException | IOException | URISyntaxException | ParserConfigurationException
                    | SAXException e) {
                // Couldn't get it the mac address from sys info - just allow sysWolAddress to be null
            }

            if (!StringUtils.isEmpty(sysWolAddress)) {
                properties.put(IrccConfig.DeviceMacAddress, sysWolAddress);
            } else {
                final String macAddress = NetUtilities.getMacAddress(identity.getWakeOnLANBytes());
                if (macAddress != null) {
                    properties.put(IrccConfig.DeviceMacAddress, macAddress);
                }
            }

            properties.put(IrccConfig.IrccUri, irccURL.toURI().toString());
            properties.put(IrccConfig.AccessCode, "RQST");
            properties.put(IrccConfig.CommandsMapFile, "ircc-" + uid.getId() + ".map");
            properties.put(IrccConfig.Refresh, refreshSeconds);

            final DiscoveryResult result = DiscoveryResultBuilder.create(uid).withProperties(properties)
                    .withLabel(device.getDetails().getFriendlyName()).build();
            return result;
        }
    }

    /**
     * Returns the {@link ThingUID} representing a Sony Ircc device or null if the {@link RemoteDevice} is not a Sony
     * ircc device.
     *
     * @param device a possibly null device to check
     * @return a {@link ThingUID} for the device or null if it's not a sony ircc device
     */
    @Override
    public ThingUID getThingUID(RemoteDevice device) {
        if (device == null) {
            return null;
        }

        final DeviceDetails details = device.getDetails();

        if (details != null) {
            final String manufacturer = details.getManufacturerDetails() == null ? null
                    : details.getManufacturerDetails().getManufacturer();
            final String modelName = details.getModelDetails() == null ? null
                    : details.getModelDetails().getModelName();

            if (manufacturer == null || modelName == null) {
                return null;
            }

            if (manufacturer.toLowerCase().contains("sony")) {
                final RemoteService irccService = device.findService(new ServiceId("schemas-sony-com", "IRCC"));
                if (irccService != null) {
                    final RemoteDeviceIdentity identity = device.getIdentity();
                    if (identity != null) {
                        final UDN udn = device.getIdentity().getUdn();
                        logger.debug("Found Sony IRCC service: {}", udn);
                        return new ThingUID(IrccConstants.THING_TYPE_IRCC, udn.getIdentifierString());
                    } else {
                        logger.debug("Found Sony IRCC service but it had no identity!");
                    }
                } else {
                    logger.debug("Could not find the IRCC service for device: {}", details);
                }
            }
        }
        return null;
    }

}
