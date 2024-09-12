/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.fsinternetradio.test;

import static org.junit.jupiter.api.Assertions.*;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jupnp.model.ValidationException;
import org.jupnp.model.meta.DeviceDetails;
import org.jupnp.model.meta.ManufacturerDetails;
import org.jupnp.model.meta.ModelDetails;
import org.jupnp.model.meta.RemoteDevice;
import org.jupnp.model.meta.RemoteDeviceIdentity;
import org.jupnp.model.meta.RemoteService;
import org.jupnp.model.types.DeviceType;
import org.jupnp.model.types.UDN;
import org.openhab.binding.fsinternetradio.internal.FSInternetRadioBindingConstants;
import org.openhab.binding.fsinternetradio.internal.FSInternetRadioDiscoveryParticipant;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.upnp.UpnpDiscoveryParticipant;
import org.openhab.core.thing.ThingUID;

/**
 * OSGi tests for the {@link FSInternetRadioDiscoveryParticipant}.
 *
 * @author Mihaela Memova - Initial contribution
 * @author Markus Rathgeb - Migrated from Groovy to pure Java test, made more robust
 * @author Velin Yordanov - Migrated to mockito
 *
 */
public class FSInternetRadioDiscoveryParticipantJavaTest {
    UpnpDiscoveryParticipant discoveryParticipant;

    // default device variables used in the tests
    DeviceType DEFAULT_TYPE = new DeviceType("namespace", "type");
    String DEFAULT_UPC = "upc";
    URI DEFAULT_URI = null;

    // default radio variables used in most of the tests
    private static final RemoteDeviceIdentity DEFAULT_RADIO_IDENTITY;
    private static final URL DEFAULT_RADIO_BASE_URL;
    String DEFAULT_RADIO_NAME = "HamaRadio";
    static {
        try {
            DEFAULT_RADIO_IDENTITY = new RemoteDeviceIdentity(new UDN("radioUDN"), 60,
                    new URL("http://radioDescriptiveURL"), null, null);
            DEFAULT_RADIO_BASE_URL = new URL("http://radioBaseURL");
        } catch (final MalformedURLException ex) {
            throw new Error("Initialization error", ex);
        }
    }

    /*
     * The default radio is chosen from the {@link FrontierSiliconRadioDiscoveryParticipant}'s
     * set of supported radios
     */
    String DEFAULT_RADIO_MANIFACTURER = "HAMA";
    String DEFAULT_RADIO_MODEL_NAME = "IR";
    String DEFAULT_RADIO_MODEL_DESCRIPTION = "IR Radio";
    String DEFAULT_RADIO_MODEL_NUMBER = "IR100";
    String DEFAULT_RADIO_SERIAL_NUMBER = "serialNumber123";

    String RADIO_BINDING_ID = "fsinternetradio"; // taken from the binding.xml file
    String RADIO_THING_TYPE_ID = "radio"; // taken from the thing-types.xml file
    String DEFAULT_RADIO_THING_UID = String.format("%s:%s:%s", RADIO_BINDING_ID, RADIO_THING_TYPE_ID,
            DEFAULT_RADIO_SERIAL_NUMBER);

    @BeforeEach
    public void setUp() {
        discoveryParticipant = new FSInternetRadioDiscoveryParticipant();
    }

    /**
     * Verify correct supported types.
     */
    @Test
    public void correctSupportedTypes() {
        assertEquals(1, discoveryParticipant.getSupportedThingTypeUIDs().size());
        assertEquals(FSInternetRadioBindingConstants.THING_TYPE_RADIO,
                discoveryParticipant.getSupportedThingTypeUIDs().iterator().next());
    }

    /**
     * Verify valid DiscoveryResult with completeFSInterntRadioDevice.
     *
     * @throws ValidationException
     */
    @Test
    public void validDiscoveryResultWithComplete() throws ValidationException {
        RemoteDevice completeFSInternetRadioDevice = createDefaultFSInternetRadioDevice(DEFAULT_RADIO_BASE_URL);
        final DiscoveryResult result = discoveryParticipant.createResult(completeFSInternetRadioDevice);
        assertEquals(new ThingUID(DEFAULT_RADIO_THING_UID), result.getThingUID());
        assertEquals(FSInternetRadioBindingConstants.THING_TYPE_RADIO, result.getThingTypeUID());
        assertEquals(DEFAULT_RADIO_MANIFACTURER,
                result.getProperties().get(FSInternetRadioBindingConstants.PROPERTY_MANUFACTURER));
        assertEquals(DEFAULT_RADIO_MODEL_NUMBER,
                result.getProperties().get(FSInternetRadioBindingConstants.PROPERTY_MODEL));
    }

    /**
     * Verify no discovery result for unknown device.
     *
     * @throws ValidationException
     * @throws MalformedURLException
     */
    @Test
    public void noDiscoveryResultIfUnknown() throws MalformedURLException, ValidationException {
        RemoteDevice unknownRemoteDevice = createUnknownRemoteDevice();
        assertNull(discoveryParticipant.createResult(unknownRemoteDevice));
    }

    /**
     * Verify valid DiscoveryResult with FSInterntRadio device without base URL.
     *
     * @throws ValidationException
     */
    @Test
    public void validDiscoveryResultIfWithoutBaseUrl() throws ValidationException {
        RemoteDevice fsInternetRadioDeviceWithoutUrl = createDefaultFSInternetRadioDevice(null);
        final DiscoveryResult result = discoveryParticipant.createResult(fsInternetRadioDeviceWithoutUrl);
        assertEquals(new ThingUID(DEFAULT_RADIO_THING_UID), result.getThingUID());
        assertEquals(FSInternetRadioBindingConstants.THING_TYPE_RADIO, result.getThingTypeUID());
        assertEquals(DEFAULT_RADIO_MANIFACTURER,
                result.getProperties().get(FSInternetRadioBindingConstants.PROPERTY_MANUFACTURER));
        assertEquals(DEFAULT_RADIO_MODEL_NUMBER,
                result.getProperties().get(FSInternetRadioBindingConstants.PROPERTY_MODEL));
    }

    private RemoteDevice createDefaultFSInternetRadioDevice(URL baseURL) throws ValidationException {
        ManufacturerDetails manifacturerDetails = new ManufacturerDetails(DEFAULT_RADIO_MANIFACTURER);
        ModelDetails modelDetails = new ModelDetails(DEFAULT_RADIO_MODEL_NAME, DEFAULT_RADIO_MODEL_DESCRIPTION,
                DEFAULT_RADIO_MODEL_NUMBER);
        DeviceDetails deviceDetails = new DeviceDetails(baseURL, DEFAULT_RADIO_NAME, manifacturerDetails, modelDetails,
                DEFAULT_RADIO_SERIAL_NUMBER, DEFAULT_UPC, DEFAULT_URI);

        final RemoteService remoteService = null;
        return new RemoteDevice(DEFAULT_RADIO_IDENTITY, DEFAULT_TYPE, deviceDetails, remoteService);
    }

    private RemoteDevice createUnknownRemoteDevice() throws ValidationException, MalformedURLException {
        int deviceIdentityMaxAgeSeconds = 60;
        RemoteDeviceIdentity identity = new RemoteDeviceIdentity(new UDN("unknownUDN"), deviceIdentityMaxAgeSeconds,
                new URL("http://unknownDescriptorURL"), null, null);
        URL anotherBaseURL = new URL("http://unknownBaseUrl");
        String friendlyName = "Unknown remote device";
        ManufacturerDetails manifacturerDetails = new ManufacturerDetails("UnknownManifacturer");
        ModelDetails modelDetails = new ModelDetails("unknownModel");
        String serialNumber = "unknownSerialNumber";

        DeviceDetails deviceDetails = new DeviceDetails(anotherBaseURL, friendlyName, manifacturerDetails, modelDetails,
                serialNumber, DEFAULT_UPC, DEFAULT_URI);

        final RemoteService remoteService = null;
        return new RemoteDevice(identity, DEFAULT_TYPE, deviceDetails, remoteService);
    }
}
