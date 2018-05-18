/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.avmfritz.internal.discovery;

import static org.eclipse.smarthome.core.thing.Thing.*;
import static org.junit.Assert.*;
import static org.openhab.binding.avmfritz.BindingConstants.*;

import java.io.StringReader;
import java.util.Collection;
import java.util.Collections;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.eclipse.smarthome.config.discovery.DiscoveryListener;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultFlag;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openhab.binding.avmfritz.handler.AVMFritzThingHandlerOSGiTest;
import org.openhab.binding.avmfritz.internal.ahamodel.AVMFritzBaseModel;
import org.openhab.binding.avmfritz.internal.ahamodel.DevicelistModel;
import org.openhab.binding.avmfritz.internal.util.JAXBUtils;

/**
 * Tests for {@link AVMFritzDiscoveryService}.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
public class AVMFritzDiscoveryServiceTest extends AVMFritzThingHandlerOSGiTest {

    private static final ThingUID BRIGE_THING_ID = new ThingUID("avmfritz:fritzbox:1");

    private DiscoveryListener listener;
    private DiscoveryResult discoveryResult;

    private AVMFritzDiscoveryService discovery;

    @Override
    @Before
    public void setUp() {
        super.setUp();
        discovery = new AVMFritzDiscoveryService(bridgeHandler);
        listener = new DiscoveryListener() {
            @Override
            public void thingRemoved(DiscoveryService source, ThingUID thingUID) {
            }

            @Override
            public void thingDiscovered(DiscoveryService source, DiscoveryResult result) {
                discoveryResult = result;
            }

            @Override
            public Collection<ThingUID> removeOlderResults(DiscoveryService source, long timestamp,
                    Collection<ThingTypeUID> thingTypeUIDs, ThingUID bridgeUID) {
                return Collections.emptyList();
            }
        };
        discovery.addDiscoveryListener(listener);
    }

    @After
    public void cleanUp() {
        discoveryResult = null;
    }

    @Test
    public void correctSupportedTypes() {
        assertEquals(9, discovery.getSupportedThingTypes().size());
        assertTrue(discovery.getSupportedThingTypes().contains(DECT100_THING_TYPE));
        assertTrue(discovery.getSupportedThingTypes().contains(DECT200_THING_TYPE));
        assertTrue(discovery.getSupportedThingTypes().contains(DECT210_THING_TYPE));
        assertTrue(discovery.getSupportedThingTypes().contains(DECT300_THING_TYPE));
        assertTrue(discovery.getSupportedThingTypes().contains(DECT301_THING_TYPE));
        assertTrue(discovery.getSupportedThingTypes().contains(PL546E_THING_TYPE));
        assertTrue(discovery.getSupportedThingTypes().contains(COMETDECT_THING_TYPE));
        assertTrue(discovery.getSupportedThingTypes().contains(GROUP_HEATING_THING_TYPE));
        assertTrue(discovery.getSupportedThingTypes().contains(GROUP_SWITCH_THING_TYPE));
    }

    @Test
    public void invalidDiscoveryResult() throws JAXBException {
        String xml = "<devicelist version=\"1\"><device identifier=\"11934 0110051-1\" id=\"2000\" functionbitmask=\"8208\" fwversion=\"0.0\" manufacturer=\"0x0feb\" productname=\"HAN-FUN\"><present>1</present><name>HAN-FUN #1</name><etsiunitinfo><etsideviceid>411</etsideviceid><unittype>514</unittype><interfaces>256</interfaces></etsiunitinfo><alert><state>0</state></alert></device></devicelist>";

        Unmarshaller u = JAXBUtils.JAXBCONTEXT.createUnmarshaller();
        DevicelistModel devices = (DevicelistModel) u.unmarshal(new StringReader(xml));
        assertNotNull(devices);
        assertEquals(1, devices.getDevicelist().size());

        AVMFritzBaseModel device = devices.getDevicelist().get(0);
        assertNotNull(device);

        discovery.onDeviceAddedInternal(device);
        assertNull(discoveryResult);
    }

    @Test
    public void validDECTRepeater100Result() throws JAXBException {
        String xml = "<devicelist version=\"1\"><device identifier=\"08761 0954669\" id=\"20\" functionbitmask=\"1280\" fwversion=\"03.86\" manufacturer=\"AVM\" productname=\"FRITZ!DECT Repeater 100\"><present>1</present><name>FRITZ!DECT Repeater 100 #5</name><temperature><celsius>230</celsius><offset>0</offset></temperature></device></devicelist>";

        Unmarshaller u = JAXBUtils.JAXBCONTEXT.createUnmarshaller();
        DevicelistModel devices = (DevicelistModel) u.unmarshal(new StringReader(xml));
        assertNotNull(devices);
        assertEquals(1, devices.getDevicelist().size());

        AVMFritzBaseModel device = devices.getDevicelist().get(0);
        assertNotNull(device);

        discovery.onDeviceAddedInternal(device);
        assertNotNull(discoveryResult);

        assertEquals(DiscoveryResultFlag.NEW, discoveryResult.getFlag());
        assertEquals(new ThingUID("avmfritz:FRITZ_DECT_Repeater_100:1:087610954669"), discoveryResult.getThingUID());
        assertEquals(DECT100_THING_TYPE, discoveryResult.getThingTypeUID());
        assertEquals(BRIGE_THING_ID, discoveryResult.getBridgeUID());
        assertEquals("087610954669", discoveryResult.getProperties().get(THING_AIN));
        assertEquals("AVM", discoveryResult.getProperties().get(PROPERTY_VENDOR));
        assertEquals("20", discoveryResult.getProperties().get(PROPERTY_MODEL_ID));
        assertEquals("087610954669", discoveryResult.getProperties().get(PROPERTY_SERIAL_NUMBER));
        assertEquals("03.86", discoveryResult.getProperties().get(PROPERTY_FIRMWARE_VERSION));
        assertEquals(THING_AIN, discoveryResult.getRepresentationProperty());
    }

    @Test
    public void validDECT200DiscoveryResult() throws JAXBException {
        String xml = "<devicelist version=\"1\"><device identifier=\"08761 0000434\" id=\"17\" functionbitmask=\"2944\" fwversion=\"03.83\" manufacturer=\"AVM\" productname=\"FRITZ!DECT 200\"><present>1</present><name>FRITZ!DECT 200 #1</name><switch><state>0</state><mode>manuell</mode><lock>0</lock><devicelock>1</devicelock></switch><powermeter><power>45</power><energy>166</energy></powermeter><temperature><celsius>255</celsius><offset>0</offset></temperature></device></devicelist>";

        Unmarshaller u = JAXBUtils.JAXBCONTEXT.createUnmarshaller();
        DevicelistModel devices = (DevicelistModel) u.unmarshal(new StringReader(xml));
        assertNotNull(devices);
        assertEquals(1, devices.getDevicelist().size());

        AVMFritzBaseModel device = devices.getDevicelist().get(0);
        assertNotNull(device);

        discovery.onDeviceAddedInternal(device);
        assertNotNull(discoveryResult);

        assertEquals(DiscoveryResultFlag.NEW, discoveryResult.getFlag());
        assertEquals(new ThingUID("avmfritz:FRITZ_DECT_200:1:087610000434"), discoveryResult.getThingUID());
        assertEquals(DECT200_THING_TYPE, discoveryResult.getThingTypeUID());
        assertEquals(BRIGE_THING_ID, discoveryResult.getBridgeUID());
        assertEquals("087610000434", discoveryResult.getProperties().get(THING_AIN));
        assertEquals("AVM", discoveryResult.getProperties().get(PROPERTY_VENDOR));
        assertEquals("17", discoveryResult.getProperties().get(PROPERTY_MODEL_ID));
        assertEquals("087610000434", discoveryResult.getProperties().get(PROPERTY_SERIAL_NUMBER));
        assertEquals("03.83", discoveryResult.getProperties().get(PROPERTY_FIRMWARE_VERSION));
        assertEquals(THING_AIN, discoveryResult.getRepresentationProperty());
    }

    @Test
    public void validCometDECTDiscoveryResult() throws JAXBException {
        String xml = "<devicelist version=\"1\"><device identifier=\"08761 0000435\" id=\"18\" functionbitmask=\"320\" fwversion=\"03.50\" manufacturer=\"AVM\" productname=\"Comet DECT\"><present>1</present><name>Comet DECT #1</name><temperature><celsius>220</celsius><offset>-10</offset></temperature><hkr><tist>44</tist><tsoll>42</tsoll><absenk>28</absenk><komfort>42</komfort><lock>0</lock><devicelock>0</devicelock><errorcode>0</errorcode><batterylow>0</batterylow><nextchange><endperiod>1484341200</endperiod><tchange>28</tchange></nextchange></hkr></device></devicelist>";

        Unmarshaller u = JAXBUtils.JAXBCONTEXT.createUnmarshaller();
        DevicelistModel devices = (DevicelistModel) u.unmarshal(new StringReader(xml));
        assertNotNull(devices);
        assertEquals(1, devices.getDevicelist().size());

        AVMFritzBaseModel device = devices.getDevicelist().get(0);
        assertNotNull(device);

        discovery.onDeviceAddedInternal(device);
        assertNotNull(discoveryResult);

        assertEquals(DiscoveryResultFlag.NEW, discoveryResult.getFlag());
        assertEquals(new ThingUID("avmfritz:Comet_DECT:1:087610000435"), discoveryResult.getThingUID());
        assertEquals(COMETDECT_THING_TYPE, discoveryResult.getThingTypeUID());
        assertEquals(BRIGE_THING_ID, discoveryResult.getBridgeUID());
        assertEquals("087610000435", discoveryResult.getProperties().get(THING_AIN));
        assertEquals("AVM", discoveryResult.getProperties().get(PROPERTY_VENDOR));
        assertEquals("18", discoveryResult.getProperties().get(PROPERTY_MODEL_ID));
        assertEquals("087610000435", discoveryResult.getProperties().get(PROPERTY_SERIAL_NUMBER));
        assertEquals("03.50", discoveryResult.getProperties().get(PROPERTY_FIRMWARE_VERSION));
        assertEquals(THING_AIN, discoveryResult.getRepresentationProperty());
    }

    @Test
    public void validPowerline546EDiscoveryResult() throws JAXBException {
        String xml = "<devicelist version=\"1\"><device identifier=\"5C:49:79:F0:A3:84\" id=\"19\" functionbitmask=\"640\" fwversion=\"06.92\" manufacturer=\"AVM\" productname=\"FRITZ!Powerline 546E\"><present>1</present><name>FRITZ!Powerline 546E #1</name><switch><state>0</state><mode>manuell</mode><lock>0</lock><devicelock>1</devicelock></switch><powermeter><power>0</power><energy>2087</energy></powermeter></device></devicelist>";

        Unmarshaller u = JAXBUtils.JAXBCONTEXT.createUnmarshaller();
        DevicelistModel devices = (DevicelistModel) u.unmarshal(new StringReader(xml));
        assertNotNull(devices);
        assertEquals(1, devices.getDevicelist().size());

        AVMFritzBaseModel device = devices.getDevicelist().get(0);
        assertNotNull(device);

        discovery.onDeviceAddedInternal(device);
        assertNotNull(discoveryResult);

        assertEquals(DiscoveryResultFlag.NEW, discoveryResult.getFlag());
        assertEquals(new ThingUID("avmfritz:FRITZ_Powerline_546E:1:5C_49_79_F0_A3_84"), discoveryResult.getThingUID());
        assertEquals(PL546E_THING_TYPE, discoveryResult.getThingTypeUID());
        assertEquals(BRIGE_THING_ID, discoveryResult.getBridgeUID());
        assertEquals("5C:49:79:F0:A3:84", discoveryResult.getProperties().get(THING_AIN));
        assertEquals("AVM", discoveryResult.getProperties().get(PROPERTY_VENDOR));
        assertEquals("19", discoveryResult.getProperties().get(PROPERTY_MODEL_ID));
        assertEquals("5C:49:79:F0:A3:84", discoveryResult.getProperties().get(PROPERTY_SERIAL_NUMBER));
        assertEquals("06.92", discoveryResult.getProperties().get(PROPERTY_FIRMWARE_VERSION));
        assertEquals(THING_AIN, discoveryResult.getRepresentationProperty());
    }

    @Test
    public void validSwitchGroupDiscoveryResult() throws JAXBException {
        String xml = "<devicelist version=\"1\"><group identifier=\"F0:A3:7F-900\" id=\"20001\" functionbitmask=\"640\" fwversion=\"1.0\" manufacturer=\"AVM\" productname=\"\"><present>1</present><name>Schlafzimmer</name><switch><state>1</state><mode>manuell</mode><lock>0</lock><devicelock>0</devicelock></switch><powermeter><power>0</power><energy>2087</energy></powermeter><groupinfo><masterdeviceid>1000</masterdeviceid><members>20000</members></groupinfo></group></devicelist>";

        Unmarshaller u = JAXBUtils.JAXBCONTEXT.createUnmarshaller();
        DevicelistModel devices = (DevicelistModel) u.unmarshal(new StringReader(xml));
        assertNotNull(devices);
        assertEquals(1, devices.getDevicelist().size());

        AVMFritzBaseModel device = devices.getDevicelist().get(0);
        assertNotNull(device);

        discovery.onDeviceAddedInternal(device);
        assertNotNull(discoveryResult);

        assertEquals(DiscoveryResultFlag.NEW, discoveryResult.getFlag());
        assertEquals(new ThingUID("avmfritz:FRITZ_GROUP_SWITCH:1:F0_A3_7F_900"), discoveryResult.getThingUID());
        assertEquals(GROUP_SWITCH_THING_TYPE, discoveryResult.getThingTypeUID());
        assertEquals(BRIGE_THING_ID, discoveryResult.getBridgeUID());
        assertEquals("F0:A3:7F-900", discoveryResult.getProperties().get(THING_AIN));
        assertEquals("AVM", discoveryResult.getProperties().get(PROPERTY_VENDOR));
        assertEquals("20001", discoveryResult.getProperties().get(PROPERTY_MODEL_ID));
        assertEquals("F0:A3:7F-900", discoveryResult.getProperties().get(PROPERTY_SERIAL_NUMBER));
        assertEquals("1.0", discoveryResult.getProperties().get(PROPERTY_FIRMWARE_VERSION));
        assertEquals("1000", discoveryResult.getProperties().get(PROPERTY_MASTER));
        assertEquals("20000", discoveryResult.getProperties().get(PROPERTY_MEMBERS));
        assertEquals(THING_AIN, discoveryResult.getRepresentationProperty());
    }
}
