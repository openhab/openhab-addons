/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sony.discovery;

import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
import org.openhab.binding.sony.internal.net.HttpRequest;
import org.openhab.binding.sony.internal.net.NetUtilities;
import org.openhab.binding.sony.internal.scalarweb.ScalarWebConfig;
import org.openhab.binding.sony.internal.scalarweb.ScalarWebConstants;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebService;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebState;
import org.openhab.binding.sony.internal.simpleip.SimpleIpConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
/**
 * The Class ScalarWebDiscoveryParticipant.
 *
 * @author Tim Roberts - Initial contribution
 */
public class ScalarWebDiscoveryParticipant implements UpnpDiscoveryParticipant {

    /** The logger. */
    private Logger logger = LoggerFactory.getLogger(ScalarWebDiscoveryParticipant.class);

    /**
     * Returns the set of supported things {@link SimpleIpConstants#THING_TYPE_SIMPLEIP}.
     *
     * @return a singleton set to the ircc thing type
     */
    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return Collections.singleton(ScalarWebConstants.THING_TYPE_SCALAR);
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
                return createScalarWebDevice(device, uid);
            } catch (Exception e) {
                logger.error("Exception creating scalar web device: {}", e.getMessage(), e);
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Creates the scalar web device.
     *
     * @param device the device
     * @param uid the uid
     * @return the discovery result
     * @throws Exception the exception
     */
    private DiscoveryResult createScalarWebDevice(RemoteDevice device, ThingUID uid) throws Exception {
        final Map<String, Object> properties = new HashMap<>(100);

        final RemoteDeviceIdentity identity = device.getIdentity();
        final URL scalarURL = identity.getDescriptorURL();

        // Test to see if we can parse/initiate the state
        try (final ScalarWebState state = new ScalarWebState(scalarURL.toString())) {
            logger.debug("{}", state);

            if (state.getService(ScalarWebService.AccessControl) == null) {
                logger.debug("Implemented scalar web API but had no access control - rejected: {}", scalarURL);
                return null;
            }
        } catch (Exception e) {
            logger.debug("Unable to parse scalar services: {} ", e.getMessage(), e);
            return null;
        }

        int refreshSeconds = 5;

        try (final HttpRequest request = NetUtilities.createHttpRequest()) {

            String sysWolAddress = null;

            // TODO: get the system information and run with it
            // try {
            // try (final IrccState irccState = new IrccState(request, irccURL.toURI().toString())) {
            // final IrccSystemInformation systemInformation = irccState.getSystemInformation();
            // sysWolAddress = systemInformation.getWolMacAddress();
            //
            // // If it has a status URL - assume we can poll the status
            // final String getStatusUrl = irccState.getUrlForAction(IrccState.AN_GETSTATUS);
            // if (!StringUtils.isEmpty(getStatusUrl)) {
            // refreshSeconds = 2;
            // }
            // }
            // } catch (NotImplementedException | IOException | URISyntaxException | ParserConfigurationException
            // | SAXException e) {
            // // Couldn't get it the mac address from sys info - just allow sysWolAddress to be null
            // }

            if (!StringUtils.isEmpty(sysWolAddress)) {
                properties.put(ScalarWebConfig.DeviceMacAddress, sysWolAddress);
            } else {
                final String macAddress = NetUtilities.getMacAddress(identity.getWakeOnLANBytes());
                if (macAddress != null) {
                    properties.put(ScalarWebConfig.DeviceMacAddress, macAddress);
                }
            }

            properties.put(ScalarWebConfig.ScalarWebUri, scalarURL.toURI().toString());
            properties.put(ScalarWebConfig.AccessCode, "RQST");
            properties.put(ScalarWebConfig.CommandsMapFile, "scalar-" + uid.getId() + ".map");
            properties.put(ScalarWebConfig.Refresh, refreshSeconds);
            properties.put(ScalarWebConfig.IgnoreChannels, "analog");

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
                final RemoteService scalarWebService = device
                        .findService(new ServiceId("schemas-sony-com", "ScalarWebAPI"));
                if (scalarWebService != null) {
                    final RemoteDeviceIdentity identity = device.getIdentity();
                    if (identity != null) {

                        final UDN udn = device.getIdentity().getUdn();
                        logger.debug("Found Sony WebScalarAPI service: {}", udn);
                        return new ThingUID(ScalarWebConstants.THING_TYPE_SCALAR, udn.getIdentifierString());
                    } else {
                        logger.debug("Found Sony WebScalarAPI service but it had no identity!");
                    }
                }
            }
        }
        return null;
    }

}
