/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.avmfritz.internal.discovery;

import static org.junit.jupiter.api.Assertions.*;
import static org.openhab.binding.avmfritz.internal.AVMFritzBindingConstants.*;
import static org.openhab.core.thing.Thing.*;

import java.io.StringReader;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.avmfritz.internal.dto.AVMFritzBaseModel;
import org.openhab.binding.avmfritz.internal.dto.DeviceListModel;
import org.openhab.binding.avmfritz.internal.handler.AVMFritzThingHandlerOSGiTest;
import org.openhab.binding.avmfritz.internal.util.JAXBUtils;
import org.openhab.core.config.discovery.DiscoveryListener;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultFlag;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;

/**
 * Tests for {@link AVMFritzDiscoveryService}.
 *
 * @author Christoph Weitkamp - Initial contribution
 * @author Ulrich Mertin - Added support for HAN-FUN blinds
 */
@NonNullByDefault
public class AVMFritzDiscoveryServiceOSGiTest extends AVMFritzThingHandlerOSGiTest {

    private static final ThingUID BRIGE_THING_ID = new ThingUID("avmfritz:fritzbox:1");

    private @Nullable DiscoveryResult discoveryResult;
    private @NonNullByDefault({}) AVMFritzDiscoveryService discovery;

    private final DiscoveryListener listener = new DiscoveryListener() {
        @Override
        public void thingRemoved(DiscoveryService source, ThingUID thingUID) {
            discoveryResult = null;
        }

        @Override
        public void thingDiscovered(DiscoveryService source, DiscoveryResult result) {
            discoveryResult = result;
        }

        @Override
        public @Nullable Collection<ThingUID> removeOlderResults(DiscoveryService source, long timestamp,
                @Nullable Collection<ThingTypeUID> thingTypeUIDs, @Nullable ThingUID bridgeUID) {
            return List.of();
        }
    };

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
        discovery = new AVMFritzDiscoveryService();
        discovery.setThingHandler(bridgeHandler);
        discovery.addDiscoveryListener(listener);
    }

    @AfterEach
    public void cleanUp() {
        discoveryResult = null;
    }

    @Test
    public void correctSupportedTypes() {
        assertEquals(19, discovery.getSupportedThingTypes().size());
        assertTrue(discovery.getSupportedThingTypes().contains(DECT100_THING_TYPE));
        assertTrue(discovery.getSupportedThingTypes().contains(DECT200_THING_TYPE));
        assertTrue(discovery.getSupportedThingTypes().contains(DECT210_THING_TYPE));
        assertTrue(discovery.getSupportedThingTypes().contains(DECT300_THING_TYPE));
        assertTrue(discovery.getSupportedThingTypes().contains(DECT301_THING_TYPE));
        assertTrue(discovery.getSupportedThingTypes().contains(DECT302_THING_TYPE));
        assertTrue(discovery.getSupportedThingTypes().contains(DECT400_THING_TYPE));
        assertTrue(discovery.getSupportedThingTypes().contains(DECT440_THING_TYPE));
        assertTrue(discovery.getSupportedThingTypes().contains(DECT500_THING_TYPE));
        assertTrue(discovery.getSupportedThingTypes().contains(POWERLINE546E_THING_TYPE));
        assertTrue(discovery.getSupportedThingTypes().contains(COMETDECT_THING_TYPE));
        assertTrue(discovery.getSupportedThingTypes().contains(HAN_FUN_CONTACT_THING_TYPE));
        assertTrue(discovery.getSupportedThingTypes().contains(HAN_FUN_SWITCH_THING_TYPE));
        assertTrue(discovery.getSupportedThingTypes().contains(HAN_FUN_ON_OFF_THING_TYPE));
        assertTrue(discovery.getSupportedThingTypes().contains(HAN_FUN_BLINDS_THING_TYPE));
        assertTrue(discovery.getSupportedThingTypes().contains(HAN_FUN_COLOR_BULB_THING_TYPE));
        assertTrue(discovery.getSupportedThingTypes().contains(HAN_FUN_DIMMABLE_BULB_THING_TYPE));
        assertTrue(discovery.getSupportedThingTypes().contains(GROUP_HEATING_THING_TYPE));
        assertTrue(discovery.getSupportedThingTypes().contains(GROUP_SWITCH_THING_TYPE));
    }

    @SuppressWarnings("null")
    @Test
    public void invalidDiscoveryResult() throws JAXBException, XMLStreamException {
        // attribute productname is important for a valid discovery result
        //@formatter:off
        String xml =
                "<devicelist version=\"1\">" +
                    "<device identifier=\"08761 0954669\" id=\"20\" functionbitmask=\"1280\" fwversion=\"03.86\" manufacturer=\"AVM\" productname=\"\">" +
                        "<present>1</present>" +
                        "<name>FRITZ!DECT Repeater 100 #5</name>" +
                        "<temperature>" +
                            "<celsius>230</celsius>" +
                            "<offset>0</offset>" +
                        "</temperature>" +
                    "</device>" +
                "</devicelist>";
        //@formatter:on

        XMLStreamReader xsr = JAXBUtils.XMLINPUTFACTORY.createXMLStreamReader(new StringReader(xml));
        Unmarshaller u = JAXBUtils.JAXBCONTEXT_DEVICES.createUnmarshaller();
        DeviceListModel devices = u.unmarshal(xsr, DeviceListModel.class).getValue();

        assertNotNull(devices);
        assertEquals(1, devices.getDevicelist().size());

        AVMFritzBaseModel device = devices.getDevicelist().get(0);
        assertNotNull(device);

        discovery.onDeviceAdded(device);
        assertNull(discoveryResult);
    }

    @SuppressWarnings("null")
    @Test
    public void validDECTRepeater100Result() throws JAXBException, XMLStreamException {
        //@formatter:off
        final String xml =
                "<devicelist version=\"1\">" +
                    "<device identifier=\"08761 0954669\" id=\"20\" functionbitmask=\"1280\" fwversion=\"03.86\" manufacturer=\"AVM\" productname=\"FRITZ!DECT Repeater 100\">" +
                        "<present>1</present>" +
                        "<name>FRITZ!DECT Repeater 100 #5</name>" +
                        "<temperature>" +
                            "<celsius>230</celsius>" +
                            "<offset>0</offset>" +
                        "</temperature>" +
                    "</device>" +
                "</devicelist>";
        //@formatter:on

        XMLStreamReader xsr = JAXBUtils.XMLINPUTFACTORY.createXMLStreamReader(new StringReader(xml));
        Unmarshaller u = JAXBUtils.JAXBCONTEXT_DEVICES.createUnmarshaller();
        DeviceListModel devices = u.unmarshal(xsr, DeviceListModel.class).getValue();

        assertNotNull(devices);
        assertEquals(1, devices.getDevicelist().size());

        AVMFritzBaseModel device = devices.getDevicelist().get(0);
        assertNotNull(device);
        assertEquals(1, device.getPresent());

        discovery.onDeviceAdded(device);
        assertNotNull(discoveryResult);

        assertEquals(DiscoveryResultFlag.NEW, discoveryResult.getFlag());
        assertEquals(new ThingUID("avmfritz:FRITZ_DECT_Repeater_100:1:087610954669"), discoveryResult.getThingUID());
        assertEquals(DECT100_THING_TYPE, discoveryResult.getThingTypeUID());
        assertEquals(BRIGE_THING_ID, discoveryResult.getBridgeUID());
        assertEquals("087610954669", discoveryResult.getProperties().get(CONFIG_AIN));
        assertEquals("AVM", discoveryResult.getProperties().get(PROPERTY_VENDOR));
        assertEquals("FRITZ!DECT Repeater 100", discoveryResult.getProperties().get(PRODUCT_NAME));
        assertEquals("087610954669", discoveryResult.getProperties().get(PROPERTY_SERIAL_NUMBER));
        assertEquals("03.86", discoveryResult.getProperties().get(PROPERTY_FIRMWARE_VERSION));
        assertEquals(CONFIG_AIN, discoveryResult.getRepresentationProperty());

        final String deviceNotPresentXml = xml.replace("<present>1</present>", "<present>0</present>");
        devices = (DeviceListModel) u.unmarshal(new StringReader(deviceNotPresentXml));
        assertNotNull(devices);
        assertEquals(1, devices.getDevicelist().size());

        device = devices.getDevicelist().get(0);
        assertNotNull(device);
        assertEquals(0, device.getPresent());

        discovery.onDeviceAdded(device);
        assertNull(discoveryResult);
    }

    @SuppressWarnings("null")
    @Test
    public void validDECT200DiscoveryResult() throws JAXBException, XMLStreamException {
        //@formatter:off
        String xml =
                "<devicelist version=\"1\">" +
                    "<device identifier=\"08761 0000434\" id=\"17\" functionbitmask=\"2944\" fwversion=\"03.83\" manufacturer=\"AVM\" productname=\"FRITZ!DECT 200\">" +
                        "<present>1</present>" +
                        "<name>FRITZ!DECT 200 #1</name>" +
                        "<switch>" +
                            "<state>0</state>" +
                            "<mode>manuell</mode>" +
                            "<lock>0</lock>" +
                            "<devicelock>1</devicelock>" +
                        "</switch>" +
                        "<powermeter>" +
                            "<voltage>232850</voltage>" +
                            "<power>45</power>" +
                            "<energy>166</energy>" +
                        "</powermeter>" +
                        "<temperature>" +
                            "<celsius>255</celsius>" +
                            "<offset>0</offset>" +
                        "</temperature>" +
                     "</device>" +
                "</devicelist>";
        //@formatter:on

        XMLStreamReader xsr = JAXBUtils.XMLINPUTFACTORY.createXMLStreamReader(new StringReader(xml));
        Unmarshaller u = JAXBUtils.JAXBCONTEXT_DEVICES.createUnmarshaller();
        DeviceListModel devices = u.unmarshal(xsr, DeviceListModel.class).getValue();

        assertNotNull(devices);
        assertEquals(1, devices.getDevicelist().size());

        AVMFritzBaseModel device = devices.getDevicelist().get(0);
        assertNotNull(device);

        discovery.onDeviceAdded(device);
        assertNotNull(discoveryResult);

        assertEquals(DiscoveryResultFlag.NEW, discoveryResult.getFlag());
        assertEquals(new ThingUID("avmfritz:FRITZ_DECT_200:1:087610000434"), discoveryResult.getThingUID());
        assertEquals(DECT200_THING_TYPE, discoveryResult.getThingTypeUID());
        assertEquals(BRIGE_THING_ID, discoveryResult.getBridgeUID());
        assertEquals("087610000434", discoveryResult.getProperties().get(CONFIG_AIN));
        assertEquals("AVM", discoveryResult.getProperties().get(PROPERTY_VENDOR));
        assertEquals("FRITZ!DECT 200", discoveryResult.getProperties().get(PRODUCT_NAME));
        assertEquals("087610000434", discoveryResult.getProperties().get(PROPERTY_SERIAL_NUMBER));
        assertEquals("03.83", discoveryResult.getProperties().get(PROPERTY_FIRMWARE_VERSION));
        assertEquals(CONFIG_AIN, discoveryResult.getRepresentationProperty());
    }

    @SuppressWarnings("null")
    @Test
    public void validDECT210DiscoveryResult() throws JAXBException, XMLStreamException {
        //@formatter:off
        String xml =
                "<devicelist version=\"1\">" +
                    "<device identifier=\"08761 0000434\" id=\"17\" functionbitmask=\"2944\" fwversion=\"03.83\" manufacturer=\"AVM\" productname=\"FRITZ!DECT 210\">" +
                        "<present>1</present>" +
                        "<name>FRITZ!DECT 210 #1</name>" +
                        "<switch>" +
                            "<state>0</state>" +
                            "<mode>manuell</mode>" +
                            "<lock>0</lock>" +
                            "<devicelock>1</devicelock>" +
                        "</switch>" +
                        "<powermeter>" +
                            "<voltage>232850</voltage>" +
                            "<power>45</power>" +
                            "<energy>166</energy>" +
                        "</powermeter>" +
                        "<temperature>" +
                            "<celsius>255</celsius>" +
                            "<offset>0</offset>" +
                        "</temperature>" +
                    "</device>" +
                "</devicelist>";
        //@formatter:on

        XMLStreamReader xsr = JAXBUtils.XMLINPUTFACTORY.createXMLStreamReader(new StringReader(xml));
        Unmarshaller u = JAXBUtils.JAXBCONTEXT_DEVICES.createUnmarshaller();
        DeviceListModel devices = u.unmarshal(xsr, DeviceListModel.class).getValue();

        assertNotNull(devices);
        assertEquals(1, devices.getDevicelist().size());

        AVMFritzBaseModel device = devices.getDevicelist().get(0);
        assertNotNull(device);

        discovery.onDeviceAdded(device);
        assertNotNull(discoveryResult);

        assertEquals(DiscoveryResultFlag.NEW, discoveryResult.getFlag());
        assertEquals(new ThingUID("avmfritz:FRITZ_DECT_210:1:087610000434"), discoveryResult.getThingUID());
        assertEquals(DECT210_THING_TYPE, discoveryResult.getThingTypeUID());
        assertEquals(BRIGE_THING_ID, discoveryResult.getBridgeUID());
        assertEquals("087610000434", discoveryResult.getProperties().get(CONFIG_AIN));
        assertEquals("AVM", discoveryResult.getProperties().get(PROPERTY_VENDOR));
        assertEquals("FRITZ!DECT 210", discoveryResult.getProperties().get(PRODUCT_NAME));
        assertEquals("087610000434", discoveryResult.getProperties().get(PROPERTY_SERIAL_NUMBER));
        assertEquals("03.83", discoveryResult.getProperties().get(PROPERTY_FIRMWARE_VERSION));
        assertEquals(CONFIG_AIN, discoveryResult.getRepresentationProperty());
    }

    @SuppressWarnings("null")
    @Test
    public void validCometDECTDiscoveryResult() throws JAXBException, XMLStreamException {
        //@formatter:off
        String xml =
                "<devicelist version=\"1\">" +
                    "<device identifier=\"08761 0000435\" id=\"18\" functionbitmask=\"320\" fwversion=\"03.50\" manufacturer=\"AVM\" productname=\"Comet DECT\">" +
                        "<present>1</present>" +
                        "<name>Comet DECT #1</name>" +
                        "<temperature>" +
                            "<celsius>220</celsius>" +
                            "<offset>-10</offset>" +
                        "</temperature>" +
                        "<hkr>" +
                            "<tist>44</tist>" +
                            "<tsoll>42</tsoll>" +
                            "<absenk>28</absenk>" +
                            "<komfort>42</komfort>" +
                            "<lock>0</lock>" +
                            "<devicelock>0</devicelock>" +
                            "<errorcode>0</errorcode>" +
                            "<batterylow>0</batterylow>" +
                            "<nextchange>" +
                                "<endperiod>1484341200</endperiod>" +
                                "<tchange>28</tchange>" +
                            "</nextchange>" +
                        "</hkr>" +
                     "</device>" +
                 "</devicelist>";
        //@formatter:on

        XMLStreamReader xsr = JAXBUtils.XMLINPUTFACTORY.createXMLStreamReader(new StringReader(xml));
        Unmarshaller u = JAXBUtils.JAXBCONTEXT_DEVICES.createUnmarshaller();
        DeviceListModel devices = u.unmarshal(xsr, DeviceListModel.class).getValue();

        assertNotNull(devices);
        assertEquals(1, devices.getDevicelist().size());

        AVMFritzBaseModel device = devices.getDevicelist().get(0);
        assertNotNull(device);

        discovery.onDeviceAdded(device);
        assertNotNull(discoveryResult);

        assertEquals(DiscoveryResultFlag.NEW, discoveryResult.getFlag());
        assertEquals(new ThingUID("avmfritz:Comet_DECT:1:087610000435"), discoveryResult.getThingUID());
        assertEquals(COMETDECT_THING_TYPE, discoveryResult.getThingTypeUID());
        assertEquals(BRIGE_THING_ID, discoveryResult.getBridgeUID());
        assertEquals("087610000435", discoveryResult.getProperties().get(CONFIG_AIN));
        assertEquals("AVM", discoveryResult.getProperties().get(PROPERTY_VENDOR));
        assertEquals("Comet DECT", discoveryResult.getProperties().get(PRODUCT_NAME));
        assertEquals("087610000435", discoveryResult.getProperties().get(PROPERTY_SERIAL_NUMBER));
        assertEquals("03.50", discoveryResult.getProperties().get(PROPERTY_FIRMWARE_VERSION));
        assertEquals(CONFIG_AIN, discoveryResult.getRepresentationProperty());
    }

    @SuppressWarnings("null")
    @Test
    public void validDECT300DiscoveryResult() throws JAXBException, XMLStreamException {
        //@formatter:off
        String xml =
                "<devicelist version=\"1\">" +
                    "<device identifier=\"08761 0000435\" id=\"18\" functionbitmask=\"320\" fwversion=\"03.50\" manufacturer=\"AVM\" productname=\"FRITZ!DECT 300\">" +
                        "<present>1</present>" +
                        "<name>FRITZ!DECT 300 #1</name>" +
                        "<temperature>" +
                            "<celsius>220</celsius>" +
                            "<offset>-10</offset>" +
                        "</temperature>" +
                        "<hkr>" +
                            "<tist>44</tist>" +
                            "<tsoll>42</tsoll>" +
                            "<absenk>28</absenk>" +
                            "<komfort>42</komfort>" +
                            "<lock>0</lock>" +
                            "<devicelock>0</devicelock>" +
                            "<errorcode>0</errorcode>" +
                            "<batterylow>0</batterylow>" +
                            "<nextchange>" +
                                "<endperiod>1484341200</endperiod>" +
                                "<tchange>28</tchange>" +
                            "</nextchange>" +
                        "</hkr>" +
                    "</device>" +
                "</devicelist>";
        //@formatter:on

        XMLStreamReader xsr = JAXBUtils.XMLINPUTFACTORY.createXMLStreamReader(new StringReader(xml));
        Unmarshaller u = JAXBUtils.JAXBCONTEXT_DEVICES.createUnmarshaller();
        DeviceListModel devices = u.unmarshal(xsr, DeviceListModel.class).getValue();

        assertNotNull(devices);
        assertEquals(1, devices.getDevicelist().size());

        AVMFritzBaseModel device = devices.getDevicelist().get(0);
        assertNotNull(device);

        discovery.onDeviceAdded(device);
        assertNotNull(discoveryResult);

        assertEquals(DiscoveryResultFlag.NEW, discoveryResult.getFlag());
        assertEquals(new ThingUID("avmfritz:FRITZ_DECT_300:1:087610000435"), discoveryResult.getThingUID());
        assertEquals(DECT300_THING_TYPE, discoveryResult.getThingTypeUID());
        assertEquals(BRIGE_THING_ID, discoveryResult.getBridgeUID());
        assertEquals("087610000435", discoveryResult.getProperties().get(CONFIG_AIN));
        assertEquals("AVM", discoveryResult.getProperties().get(PROPERTY_VENDOR));
        assertEquals("FRITZ!DECT 300", discoveryResult.getProperties().get(PRODUCT_NAME));
        assertEquals("087610000435", discoveryResult.getProperties().get(PROPERTY_SERIAL_NUMBER));
        assertEquals("03.50", discoveryResult.getProperties().get(PROPERTY_FIRMWARE_VERSION));
        assertEquals(CONFIG_AIN, discoveryResult.getRepresentationProperty());
    }

    @SuppressWarnings("null")
    @Test
    public void validDECT301DiscoveryResult() throws JAXBException, XMLStreamException {
        //@formatter:off
        String xml =
                "<devicelist version=\"1\">" +
                    "<device identifier=\"08761 0000435\" id=\"18\" functionbitmask=\"320\" fwversion=\"03.50\" manufacturer=\"AVM\" productname=\"FRITZ!DECT 301\">" +
                        "<present>1</present>" +
                        "<name>FRITZ!DECT 301 #1</name>" +
                        "<temperature>" +
                        "<celsius>220</celsius>" +
                        "<offset>-10</offset>" +
                    "</temperature>" +
                    "<hkr>" +
                        "<tist>44</tist>" +
                        "<tsoll>42</tsoll>" +
                        "<absenk>28</absenk>" +
                        "<komfort>42</komfort>" +
                        "<lock>0</lock>" +
                        "<devicelock>0</devicelock>" +
                        "<errorcode>0</errorcode>" +
                        "<batterylow>0</batterylow>" +
                        "<nextchange>" +
                            "<endperiod>1484341200</endperiod>" +
                            "<tchange>28</tchange>" +
                        "</nextchange>" +
                    "</hkr>" +
                "</device>" +
            "</devicelist>";
        //@formatter:on

        XMLStreamReader xsr = JAXBUtils.XMLINPUTFACTORY.createXMLStreamReader(new StringReader(xml));
        Unmarshaller u = JAXBUtils.JAXBCONTEXT_DEVICES.createUnmarshaller();
        DeviceListModel devices = u.unmarshal(xsr, DeviceListModel.class).getValue();

        assertNotNull(devices);
        assertEquals(1, devices.getDevicelist().size());

        AVMFritzBaseModel device = devices.getDevicelist().get(0);
        assertNotNull(device);

        discovery.onDeviceAdded(device);
        assertNotNull(discoveryResult);

        assertEquals(DiscoveryResultFlag.NEW, discoveryResult.getFlag());
        assertEquals(new ThingUID("avmfritz:FRITZ_DECT_301:1:087610000435"), discoveryResult.getThingUID());
        assertEquals(DECT301_THING_TYPE, discoveryResult.getThingTypeUID());
        assertEquals(BRIGE_THING_ID, discoveryResult.getBridgeUID());
        assertEquals("087610000435", discoveryResult.getProperties().get(CONFIG_AIN));
        assertEquals("AVM", discoveryResult.getProperties().get(PROPERTY_VENDOR));
        assertEquals("FRITZ!DECT 301", discoveryResult.getProperties().get(PRODUCT_NAME));
        assertEquals("087610000435", discoveryResult.getProperties().get(PROPERTY_SERIAL_NUMBER));
        assertEquals("03.50", discoveryResult.getProperties().get(PROPERTY_FIRMWARE_VERSION));
        assertEquals(CONFIG_AIN, discoveryResult.getRepresentationProperty());
    }

    @SuppressWarnings("null")
    @Test
    public void validPowerline546EDiscoveryResult() throws JAXBException, XMLStreamException {
        //@formatter:off
        String xml =
                "<devicelist version=\"1\">" +
                    "<device identifier=\"5C:49:79:F0:A3:84\" id=\"19\" functionbitmask=\"640\" fwversion=\"06.92\" manufacturer=\"AVM\" productname=\"FRITZ!Powerline 546E\">" +
                        "<present>1</present>" +
                        "<name>FRITZ!Powerline 546E #1</name>" +
                        "<switch>" +
                            "<state>0</state>" +
                            "<mode>manuell</mode>" +
                            "<lock>0</lock>" +
                            "<devicelock>1</devicelock>" +
                        "</switch>" +
                        "<powermeter>" +
                            "<voltage>232850</voltage>" +
                            "<power>0</power>" +
                            "<energy>2087</energy>" +
                        "</powermeter>" +
                    "</device>" +
                "</devicelist>";
        //@formatter:on

        XMLStreamReader xsr = JAXBUtils.XMLINPUTFACTORY.createXMLStreamReader(new StringReader(xml));
        Unmarshaller u = JAXBUtils.JAXBCONTEXT_DEVICES.createUnmarshaller();
        DeviceListModel devices = u.unmarshal(xsr, DeviceListModel.class).getValue();

        assertNotNull(devices);
        assertEquals(1, devices.getDevicelist().size());

        AVMFritzBaseModel device = devices.getDevicelist().get(0);
        assertNotNull(device);

        discovery.onDeviceAdded(device);
        assertNotNull(discoveryResult);

        assertEquals(DiscoveryResultFlag.NEW, discoveryResult.getFlag());
        assertEquals(new ThingUID("avmfritz:FRITZ_Powerline_546E:1:5C_49_79_F0_A3_84"), discoveryResult.getThingUID());
        assertEquals(POWERLINE546E_THING_TYPE, discoveryResult.getThingTypeUID());
        assertEquals(BRIGE_THING_ID, discoveryResult.getBridgeUID());
        assertEquals("5C:49:79:F0:A3:84", discoveryResult.getProperties().get(CONFIG_AIN));
        assertEquals("AVM", discoveryResult.getProperties().get(PROPERTY_VENDOR));
        assertEquals("FRITZ!Powerline 546E", discoveryResult.getProperties().get(PRODUCT_NAME));
        assertEquals("5C:49:79:F0:A3:84", discoveryResult.getProperties().get(PROPERTY_SERIAL_NUMBER));
        assertEquals("06.92", discoveryResult.getProperties().get(PROPERTY_FIRMWARE_VERSION));
        assertEquals(CONFIG_AIN, discoveryResult.getRepresentationProperty());
    }

    @SuppressWarnings("null")
    @Test
    public void invalidHANFUNContactDiscoveryResult() throws JAXBException, XMLStreamException {
        //@formatter:off
        String xml =
                "<devicelist version=\"1\">" +
                    "<device identifier=\"11934 0059578\" id=\"406\" functionbitmask=\"1\" fwversion=\"00.00\" manufacturer=\"0x0feb\" productname=\"HAN-FUN\">" +
                        "<present>1</present>" +
                        "<name>HAN-FUN #2</name>" +
                    "</device>" +
                "</devicelist>";
        //@formatter:on

        XMLStreamReader xsr = JAXBUtils.XMLINPUTFACTORY.createXMLStreamReader(new StringReader(xml));
        Unmarshaller u = JAXBUtils.JAXBCONTEXT_DEVICES.createUnmarshaller();
        DeviceListModel devices = u.unmarshal(xsr, DeviceListModel.class).getValue();

        assertNotNull(devices);
        assertEquals(1, devices.getDevicelist().size());

        AVMFritzBaseModel device = devices.getDevicelist().get(0);
        assertNotNull(device);

        discovery.onDeviceAdded(device);
        assertNull(discoveryResult);
    }

    @SuppressWarnings("null")
    @Test
    public void validHANFUNMagneticContactDiscoveryResult() throws JAXBException, XMLStreamException {
        //@formatter:off
        String xml =
                "<devicelist version=\"1\">" +
                    "<device identifier=\"11934 0059578-1\" id=\"2000\" functionbitmask=\"8208\" fwversion=\"0.0\" manufacturer=\"0x0feb\" productname=\"HAN-FUN\">" +
                        "<present>1</present>" +
                        "<name>HAN-FUN #2: Unit #2</name>" +
                        "<etsiunitinfo>" +
                            "<etsideviceid>406</etsideviceid>" +
                            "<unittype>513</unittype>" +
                            "<interfaces>256</interfaces>" +
                        "</etsiunitinfo>" +
                        "<alert>" +
                            "<state/>" +
                        "</alert>" +
                    "</device>" +
                "</devicelist>";
        //@formatter:on

        XMLStreamReader xsr = JAXBUtils.XMLINPUTFACTORY.createXMLStreamReader(new StringReader(xml));
        Unmarshaller u = JAXBUtils.JAXBCONTEXT_DEVICES.createUnmarshaller();
        DeviceListModel devices = u.unmarshal(xsr, DeviceListModel.class).getValue();

        assertNotNull(devices);
        assertEquals(1, devices.getDevicelist().size());

        AVMFritzBaseModel device = devices.getDevicelist().get(0);
        assertNotNull(device);

        discovery.onDeviceAdded(device);
        assertNotNull(discoveryResult);

        assertEquals(DiscoveryResultFlag.NEW, discoveryResult.getFlag());
        assertEquals(new ThingUID("avmfritz:HAN_FUN_CONTACT:1:119340059578_1"), discoveryResult.getThingUID());
        assertEquals(HAN_FUN_CONTACT_THING_TYPE, discoveryResult.getThingTypeUID());
        assertEquals(BRIGE_THING_ID, discoveryResult.getBridgeUID());
        assertEquals("119340059578-1", discoveryResult.getProperties().get(CONFIG_AIN));
        assertEquals("0x0feb", discoveryResult.getProperties().get(PROPERTY_VENDOR));
        assertEquals("HAN-FUN", discoveryResult.getProperties().get(PRODUCT_NAME));
        assertEquals("119340059578-1", discoveryResult.getProperties().get(PROPERTY_SERIAL_NUMBER));
        assertEquals("0.0", discoveryResult.getProperties().get(PROPERTY_FIRMWARE_VERSION));
        assertEquals(CONFIG_AIN, discoveryResult.getRepresentationProperty());
    }

    @SuppressWarnings("null")
    @Test
    public void validHANFUNOpticalContactDiscoveryResult() throws JAXBException, XMLStreamException {
        //@formatter:off
        String xml =
                "<devicelist version=\"1\">" +
                    "<device identifier=\"11934 0059578-1\" id=\"2001\" functionbitmask=\"8208\" fwversion=\"0.0\" manufacturer=\"0x0feb\" productname=\"HAN-FUN\">" +
                        "<present>1</present>" +
                        "<name>HAN-FUN #3: Unit #3</name>" +
                        "<etsiunitinfo>" +
                            "<etsideviceid>406</etsideviceid>" +
                            "<unittype>514</unittype>" +
                            "<interfaces>256</interfaces>" +
                        "</etsiunitinfo>" +
                        "<alert>" +
                            "<state/>" +
                        "</alert>" +
                    "</device>" +
                "</devicelist>";
        //@formatter:on

        XMLStreamReader xsr = JAXBUtils.XMLINPUTFACTORY.createXMLStreamReader(new StringReader(xml));
        Unmarshaller u = JAXBUtils.JAXBCONTEXT_DEVICES.createUnmarshaller();
        DeviceListModel devices = u.unmarshal(xsr, DeviceListModel.class).getValue();

        assertNotNull(devices);
        assertEquals(1, devices.getDevicelist().size());

        AVMFritzBaseModel device = devices.getDevicelist().get(0);
        assertNotNull(device);

        discovery.onDeviceAdded(device);
        assertNotNull(discoveryResult);

        assertEquals(DiscoveryResultFlag.NEW, discoveryResult.getFlag());
        assertEquals(new ThingUID("avmfritz:HAN_FUN_CONTACT:1:119340059578_1"), discoveryResult.getThingUID());
        assertEquals(HAN_FUN_CONTACT_THING_TYPE, discoveryResult.getThingTypeUID());
        assertEquals(BRIGE_THING_ID, discoveryResult.getBridgeUID());
        assertEquals("119340059578-1", discoveryResult.getProperties().get(CONFIG_AIN));
        assertEquals("0x0feb", discoveryResult.getProperties().get(PROPERTY_VENDOR));
        assertEquals("HAN-FUN", discoveryResult.getProperties().get(PRODUCT_NAME));
        assertEquals("119340059578-1", discoveryResult.getProperties().get(PROPERTY_SERIAL_NUMBER));
        assertEquals("0.0", discoveryResult.getProperties().get(PROPERTY_FIRMWARE_VERSION));
        assertEquals(CONFIG_AIN, discoveryResult.getRepresentationProperty());
    }

    @SuppressWarnings("null")
    @Test
    public void validHANFUNMotionSensorDiscoveryResult() throws JAXBException, XMLStreamException {
        //@formatter:off
        String xml =
                "<devicelist version=\"1\">" +
                    "<device identifier=\"11934 0059578-1\" id=\"2002\" functionbitmask=\"8208\" fwversion=\"0.0\" manufacturer=\"0x0feb\" productname=\"HAN-FUN\">" +
                        "<present>1</present>" +
                        "<name>HAN-FUN #3: Unit #3</name>" +
                        "<etsiunitinfo>" +
                            "<etsideviceid>408</etsideviceid>" +
                            "<unittype>515</unittype>" +
                            "<interfaces>32513,256</interfaces>" +
                        "</etsiunitinfo>" +
                        "<alert>" +
                            "<state>0</state>" +
                        "</alert>" +
                    "</device>" +
                "</devicelist>";
        //@formatter:on

        XMLStreamReader xsr = JAXBUtils.XMLINPUTFACTORY.createXMLStreamReader(new StringReader(xml));
        Unmarshaller u = JAXBUtils.JAXBCONTEXT_DEVICES.createUnmarshaller();
        DeviceListModel devices = u.unmarshal(xsr, DeviceListModel.class).getValue();

        assertNotNull(devices);
        assertEquals(1, devices.getDevicelist().size());

        AVMFritzBaseModel device = devices.getDevicelist().get(0);
        assertNotNull(device);

        discovery.onDeviceAdded(device);
        assertNotNull(discoveryResult);

        assertEquals(DiscoveryResultFlag.NEW, discoveryResult.getFlag());
        assertEquals(new ThingUID("avmfritz:HAN_FUN_CONTACT:1:119340059578_1"), discoveryResult.getThingUID());
        assertEquals(HAN_FUN_CONTACT_THING_TYPE, discoveryResult.getThingTypeUID());
        assertEquals(BRIGE_THING_ID, discoveryResult.getBridgeUID());
        assertEquals("119340059578-1", discoveryResult.getProperties().get(CONFIG_AIN));
        assertEquals("0x0feb", discoveryResult.getProperties().get(PROPERTY_VENDOR));
        assertEquals("HAN-FUN", discoveryResult.getProperties().get(PRODUCT_NAME));
        assertEquals("119340059578-1", discoveryResult.getProperties().get(PROPERTY_SERIAL_NUMBER));
        assertEquals("0.0", discoveryResult.getProperties().get(PROPERTY_FIRMWARE_VERSION));
        assertEquals(CONFIG_AIN, discoveryResult.getRepresentationProperty());
    }

    @SuppressWarnings("null")
    @Test
    public void validHANFUNMSmokeDetectorDiscoveryResult() throws JAXBException, XMLStreamException {
        //@formatter:off
        String xml =
                "<devicelist version=\"1\">" +
                    "<device identifier=\"11324 0059952-1\" id=\"2003\" functionbitmask=\"8208\" fwversion=\"0.0\" manufacturer=\"0x2c3c\" productname=\"HAN-FUN\">" +
                        "<present>1</present>" +
                        "<name>HAN-FUN #4: Unit #4</name>" +
                        "<etsiunitinfo>" +
                            "<etsideviceid>407</etsideviceid>" +
                            "<unittype>516</unittype>" +
                            "<interfaces>256</interfaces>" +
                        "</etsiunitinfo>" +
                        "<alert>" +
                            "<state>0</state>" +
                        "</alert>" +
                    "</device>" +
                "</devicelist>";
        //@formatter:on

        XMLStreamReader xsr = JAXBUtils.XMLINPUTFACTORY.createXMLStreamReader(new StringReader(xml));
        Unmarshaller u = JAXBUtils.JAXBCONTEXT_DEVICES.createUnmarshaller();
        DeviceListModel devices = u.unmarshal(xsr, DeviceListModel.class).getValue();

        assertNotNull(devices);
        assertEquals(1, devices.getDevicelist().size());

        AVMFritzBaseModel device = devices.getDevicelist().get(0);
        assertNotNull(device);

        discovery.onDeviceAdded(device);
        assertNotNull(discoveryResult);

        assertEquals(DiscoveryResultFlag.NEW, discoveryResult.getFlag());
        assertEquals(new ThingUID("avmfritz:HAN_FUN_CONTACT:1:113240059952_1"), discoveryResult.getThingUID());
        assertEquals(HAN_FUN_CONTACT_THING_TYPE, discoveryResult.getThingTypeUID());
        assertEquals(BRIGE_THING_ID, discoveryResult.getBridgeUID());
        assertEquals("113240059952-1", discoveryResult.getProperties().get(CONFIG_AIN));
        assertEquals("0x2c3c", discoveryResult.getProperties().get(PROPERTY_VENDOR));
        assertEquals("HAN-FUN", discoveryResult.getProperties().get(PRODUCT_NAME));
        assertEquals("113240059952-1", discoveryResult.getProperties().get(PROPERTY_SERIAL_NUMBER));
        assertEquals("0.0", discoveryResult.getProperties().get(PROPERTY_FIRMWARE_VERSION));
        assertEquals(CONFIG_AIN, discoveryResult.getRepresentationProperty());
    }

    @SuppressWarnings("null")
    @Test
    public void validHANFUNSwitchtDiscoveryResult() throws JAXBException, XMLStreamException {
        //@formatter:off
        String xml =
                "<devicelist version=\"1\">" +
                    "<device identifier=\"11934 0059578-1\" id=\"2001\" functionbitmask=\"8200\" fwversion=\"0.0\" manufacturer=\"0x0feb\" productname=\"HAN-FUN\">" +
                        "<present>1</present>" +
                        "<name>HAN-FUN #2: Unit #2</name>" +
                        "<etsiunitinfo>" +
                            "<etsideviceid>412</etsideviceid>" +
                            "<unittype>273</unittype>" +
                            "<interfaces>772</interfaces>" +
                        "</etsiunitinfo>" +
                        "<button>" +
                            "<lastpressedtimestamp>1529590797</lastpressedtimestamp>" +
                        "</button>" +
                    "</device>" +
                "</devicelist>";
        //@formatter:on

        XMLStreamReader xsr = JAXBUtils.XMLINPUTFACTORY.createXMLStreamReader(new StringReader(xml));
        Unmarshaller u = JAXBUtils.JAXBCONTEXT_DEVICES.createUnmarshaller();
        DeviceListModel devices = u.unmarshal(xsr, DeviceListModel.class).getValue();

        assertNotNull(devices);
        assertEquals(1, devices.getDevicelist().size());

        AVMFritzBaseModel device = devices.getDevicelist().get(0);
        assertNotNull(device);

        discovery.onDeviceAdded(device);
        assertNotNull(discoveryResult);

        assertEquals(DiscoveryResultFlag.NEW, discoveryResult.getFlag());
        assertEquals(new ThingUID("avmfritz:HAN_FUN_SWITCH:1:119340059578_1"), discoveryResult.getThingUID());
        assertEquals(HAN_FUN_SWITCH_THING_TYPE, discoveryResult.getThingTypeUID());
        assertEquals(BRIGE_THING_ID, discoveryResult.getBridgeUID());
        assertEquals("119340059578-1", discoveryResult.getProperties().get(CONFIG_AIN));
        assertEquals("0x0feb", discoveryResult.getProperties().get(PROPERTY_VENDOR));
        assertEquals("HAN-FUN", discoveryResult.getProperties().get(PRODUCT_NAME));
        assertEquals("119340059578-1", discoveryResult.getProperties().get(PROPERTY_SERIAL_NUMBER));
        assertEquals("0.0", discoveryResult.getProperties().get(PROPERTY_FIRMWARE_VERSION));
        assertEquals(CONFIG_AIN, discoveryResult.getRepresentationProperty());
    }

    @SuppressWarnings("null")
    @Test
    public void validHANFUNBlindDiscoveryResult() throws JAXBException, XMLStreamException {
        //@formatter:off
        String xml =
                "<devicelist version=\"1\">" +
                    "<device identifier=\"14276 0503450-1\" id=\"2000\" functionbitmask=\"335888\" fwversion=\"0.0\" manufacturer=\"0x37c4\" productname=\"Rollotron 1213\">"+
                        "<present>1</present>"+
                        "<txbusy>0</txbusy>"+
                        "<name>Rollotron 1213 #1</name>"+
                        "<blind>"+
                            "<endpositionsset>1</endpositionsset>"+
                            "<mode>manuell</mode>"+
                        "</blind>"+
                        "<levelcontrol>"+
                            "<level>26</level>"+
                            "<levelpercentage>10</levelpercentage>"+
                        "</levelcontrol>"+
                        "<etsiunitinfo>"+
                            "<etsideviceid>406</etsideviceid>"+
                            "<unittype>281</unittype>"+
                            "<interfaces>256,513,516,517</interfaces>"+
                        "</etsiunitinfo>"+
                        "<alert>"+
                            "<state>0</state>"+
                            "<lastalertchgtimestamp></lastalertchgtimestamp>"+
                        "</alert>"+
                    "</device>" +
                "</devicelist>";
        //@formatter:on

        XMLStreamReader xsr = JAXBUtils.XMLINPUTFACTORY.createXMLStreamReader(new StringReader(xml));
        Unmarshaller u = JAXBUtils.JAXBCONTEXT_DEVICES.createUnmarshaller();
        DeviceListModel devices = u.unmarshal(xsr, DeviceListModel.class).getValue();

        assertNotNull(devices);
        assertEquals(1, devices.getDevicelist().size());

        AVMFritzBaseModel device = devices.getDevicelist().get(0);
        assertNotNull(device);

        discovery.onDeviceAdded(device);
        assertNotNull(discoveryResult);

        assertEquals(DiscoveryResultFlag.NEW, discoveryResult.getFlag());
        assertEquals(new ThingUID("avmfritz:HAN_FUN_BLINDS:1:142760503450_1"), discoveryResult.getThingUID());
        assertEquals(HAN_FUN_BLINDS_THING_TYPE, discoveryResult.getThingTypeUID());
        assertEquals(BRIGE_THING_ID, discoveryResult.getBridgeUID());
        assertEquals("142760503450-1", discoveryResult.getProperties().get(CONFIG_AIN));
        assertEquals("0x37c4", discoveryResult.getProperties().get(PROPERTY_VENDOR));
        assertEquals("Rollotron 1213", discoveryResult.getProperties().get(PRODUCT_NAME));
        assertEquals("142760503450-1", discoveryResult.getProperties().get(PROPERTY_SERIAL_NUMBER));
        assertEquals("0.0", discoveryResult.getProperties().get(PROPERTY_FIRMWARE_VERSION));
        assertEquals(CONFIG_AIN, discoveryResult.getRepresentationProperty());
    }

    @SuppressWarnings("null")
    @Test
    public void validHeatingGroupDiscoveryResult() throws JAXBException, XMLStreamException {
        //@formatter:off
        String xml =
                "<devicelist version=\"1\">" +
                    "<group identifier=\"F0:A3:7F-900\" id=\"20000\" functionbitmask=\"4160\" fwversion=\"1.0\" manufacturer=\"AVM\" productname=\"\">" +
                        "<present>1</present>" +
                        "<name>Schlafzimmer</name>" +
                        "<hkr>" +
                            "<tist>0</tist>" +
                            "<tsoll>253</tsoll>" +
                            "<absenk>33</absenk>" +
                            "<komfort>40</komfort>" +
                            "<lock>1</lock>" +
                            "<devicelock>0</devicelock>" +
                            "<errorcode>0</errorcode>" +
                            "<batterylow>0</batterylow>" +
                            "<windowopenactiv>0</windowopenactiv>" +
                            "<battery>1</battery>" +
                            "<nextchange>" +
                                "<endperiod>1546293600</endperiod>" +
                                "<tchange>33</tchange>" +
                            "</nextchange>" +
                            "<summeractive>1</summeractive>" +
                            "<holidayactive>0</holidayactive>" +
                        "</hkr>" +
                        "<groupinfo>" +
                            "<masterdeviceid>1000</masterdeviceid>" +
                            "<members>20000</members>" +
                        "</groupinfo>" +
                    "</group>" +
                "</devicelist>";
        //@formatter:on

        XMLStreamReader xsr = JAXBUtils.XMLINPUTFACTORY.createXMLStreamReader(new StringReader(xml));
        Unmarshaller u = JAXBUtils.JAXBCONTEXT_DEVICES.createUnmarshaller();
        DeviceListModel devices = u.unmarshal(xsr, DeviceListModel.class).getValue();

        assertNotNull(devices);
        assertEquals(1, devices.getDevicelist().size());

        AVMFritzBaseModel device = devices.getDevicelist().get(0);
        assertNotNull(device);

        discovery.onDeviceAdded(device);
        assertNotNull(discoveryResult);

        assertEquals(DiscoveryResultFlag.NEW, discoveryResult.getFlag());
        assertEquals(new ThingUID("avmfritz:FRITZ_GROUP_HEATING:1:F0_A3_7F_900"), discoveryResult.getThingUID());
        assertEquals(GROUP_HEATING_THING_TYPE, discoveryResult.getThingTypeUID());
        assertEquals(BRIGE_THING_ID, discoveryResult.getBridgeUID());
        assertEquals("F0:A3:7F-900", discoveryResult.getProperties().get(CONFIG_AIN));
        assertEquals("AVM", discoveryResult.getProperties().get(PROPERTY_VENDOR));
        assertEquals("", discoveryResult.getProperties().get(PRODUCT_NAME));
        assertEquals("F0:A3:7F-900", discoveryResult.getProperties().get(PROPERTY_SERIAL_NUMBER));
        assertEquals("1.0", discoveryResult.getProperties().get(PROPERTY_FIRMWARE_VERSION));
        assertEquals("1000", discoveryResult.getProperties().get(PROPERTY_MASTER));
        assertEquals("20000", discoveryResult.getProperties().get(PROPERTY_MEMBERS));
        assertEquals(CONFIG_AIN, discoveryResult.getRepresentationProperty());
    }

    @SuppressWarnings("null")
    @Test
    public void validSwitchGroupDiscoveryResult() throws JAXBException, XMLStreamException {
        //@formatter:off
        String xml =
                "<devicelist version=\"1\">" +
                    "<group identifier=\"F0:A3:7F-900\" id=\"20001\" functionbitmask=\"6784\" fwversion=\"1.0\" manufacturer=\"AVM\" productname=\"\">" +
                        "<present>1</present>" +
                        "<name>Schlafzimmer</name>" +
                        "<switch>" +
                            "<state>1</state>" +
                            "<mode>manuell</mode>" +
                            "<lock>0</lock>" +
                            "<devicelock>0</devicelock>" +
                        "</switch>" +
                        "<powermeter>" +
                            "<voltage>232850</voltage>" +
                            "<power>0</power>" +
                            "<energy>2087</energy>" +
                        "</powermeter>" +
                        "<groupinfo>" +
                            "<masterdeviceid>1000</masterdeviceid>" +
                            "<members>20000</members>" +
                        "</groupinfo>" +
                    "</group>" +
                "</devicelist>";
        //@formatter:on

        XMLStreamReader xsr = JAXBUtils.XMLINPUTFACTORY.createXMLStreamReader(new StringReader(xml));
        Unmarshaller u = JAXBUtils.JAXBCONTEXT_DEVICES.createUnmarshaller();
        DeviceListModel devices = u.unmarshal(xsr, DeviceListModel.class).getValue();

        assertNotNull(devices);
        assertEquals(1, devices.getDevicelist().size());

        AVMFritzBaseModel device = devices.getDevicelist().get(0);
        assertNotNull(device);

        discovery.onDeviceAdded(device);
        assertNotNull(discoveryResult);

        assertEquals(DiscoveryResultFlag.NEW, discoveryResult.getFlag());
        assertEquals(new ThingUID("avmfritz:FRITZ_GROUP_SWITCH:1:F0_A3_7F_900"), discoveryResult.getThingUID());
        assertEquals(GROUP_SWITCH_THING_TYPE, discoveryResult.getThingTypeUID());
        assertEquals(BRIGE_THING_ID, discoveryResult.getBridgeUID());
        assertEquals("F0:A3:7F-900", discoveryResult.getProperties().get(CONFIG_AIN));
        assertEquals("AVM", discoveryResult.getProperties().get(PROPERTY_VENDOR));
        assertEquals("", discoveryResult.getProperties().get(PRODUCT_NAME));
        assertEquals("F0:A3:7F-900", discoveryResult.getProperties().get(PROPERTY_SERIAL_NUMBER));
        assertEquals("1.0", discoveryResult.getProperties().get(PROPERTY_FIRMWARE_VERSION));
        assertEquals("1000", discoveryResult.getProperties().get(PROPERTY_MASTER));
        assertEquals("20000", discoveryResult.getProperties().get(PROPERTY_MEMBERS));
        assertEquals(CONFIG_AIN, discoveryResult.getRepresentationProperty());
    }
}
