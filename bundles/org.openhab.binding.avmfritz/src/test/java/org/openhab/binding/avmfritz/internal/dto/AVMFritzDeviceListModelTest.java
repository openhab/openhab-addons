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
package org.openhab.binding.avmfritz.internal.dto;

import static org.junit.jupiter.api.Assertions.*;
import static org.openhab.binding.avmfritz.internal.AVMFritzBindingConstants.*;

import java.io.StringReader;
import java.math.BigDecimal;
import java.util.Optional;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.avmfritz.internal.util.JAXBUtils;

/**
 * Tests for {@link DeviceListModel}.
 *
 * @author Christoph Weitkamp - Initial contribution
 * @author Ulrich Mertin - Added support for HAN-FUN blinds
 * @author Fabian Girgert - Fixed incorrect state of dimmable bulb when switched off
 */
@NonNullByDefault
public class AVMFritzDeviceListModelTest {

    private @NonNullByDefault({}) DeviceListModel devices;

    @SuppressWarnings("null")
    @BeforeEach
    public void setUp() throws JAXBException, XMLStreamException {
        //@formatter:off
        final String xml =
                """
                <devicelist version="1">\
                <group identifier="F0:A3:7F-900" id="20000" functionbitmask="6784" fwversion="1.0" manufacturer="AVM" productname=""><present>1</present><name>Schlafzimmer</name><switch><state>1</state><mode>manuell</mode><lock>0</lock><devicelock>0</devicelock></switch><powermeter><voltage>230051</voltage><power>0</power><energy>2087</energy></powermeter><groupinfo><masterdeviceid>17</masterdeviceid><members>17,18</members></groupinfo></group>\
                <group identifier="F0:A3:7F-901" id="20001" functionbitmask="4160" fwversion="1.0" manufacturer="AVM" productname=""><present>1</present><name>Schlafzimmer</name><temperature><celsius>220</celsius><offset>-10</offset></temperature><hkr><tist>44</tist><tsoll>42</tsoll><absenk>28</absenk><komfort>42</komfort><lock>1</lock><devicelock>1</devicelock><errorcode>0</errorcode><windowopenactiv>0</windowopenactiv><windowopenactiveendtime>0</windowopenactiveendtime><boostactive>0</boostactive><boostactiveendtime>0</boostactiveendtime><batterylow>0</batterylow><battery>100</battery><nextchange><endperiod>1484341200</endperiod><tchange>28</tchange></nextchange></hkr><groupinfo><masterdeviceid>0</masterdeviceid><members>20,21,22</members></groupinfo></group>\
                <device identifier="08761 0000434" id="17" functionbitmask="35712" fwversion="03.83" manufacturer="AVM" productname="FRITZ!DECT 200"><present>1</present><name>FRITZ!DECT 200 #1</name><switch><state>1</state><mode>manuell</mode><lock>0</lock><devicelock>0</devicelock></switch><powermeter><voltage>230051</voltage><power>0</power><energy>2087</energy></powermeter><temperature><celsius>255</celsius><offset>0</offset></temperature></device>\
                <device identifier="08761 0000438" id="18" functionbitmask="35712" fwversion="03.83" manufacturer="AVM" productname="FRITZ!DECT 210"><present>1</present><name>FRITZ!DECT 210 #8</name><switch><state>1</state><mode>manuell</mode><lock>0</lock><devicelock>0</devicelock></switch><powermeter><voltage>230051</voltage><power>0</power><energy>2087</energy></powermeter><temperature><celsius>255</celsius><offset>0</offset></temperature></device>\
                <device identifier="08761 0000437" id="20" functionbitmask="320" fwversion="03.50" manufacturer="AVM" productname="FRITZ!DECT 300"><present>0</present><name>FRITZ!DECT 300 #1</name><temperature><celsius>220</celsius><offset>-10</offset></temperature><hkr><tist>44</tist><tsoll>42</tsoll><absenk>28</absenk><komfort>42</komfort><lock>1</lock><devicelock>1</devicelock><errorcode>0</errorcode><windowopenactiv>0</windowopenactiv><windowopenactiveendtime>0</windowopenactiveendtime><boostactive>0</boostactive><boostactiveendtime>0</boostactiveendtime><batterylow>0</batterylow><battery>100</battery><nextchange><endperiod>1484341200</endperiod><tchange>28</tchange></nextchange></hkr></device>\
                <device identifier="08761 0000436" id="21" functionbitmask="320" fwversion="03.50" manufacturer="AVM" productname="FRITZ!DECT 301"><present>0</present><name>FRITZ!DECT 301 #1</name><temperature><celsius>220</celsius><offset>-10</offset></temperature><hkr><tist>44</tist><tsoll>42</tsoll><absenk>28</absenk><komfort>42</komfort><lock>1</lock><devicelock>1</devicelock><errorcode>0</errorcode><windowopenactiv>0</windowopenactiv><windowopenactiveendtime>0</windowopenactiveendtime><boostactive>0</boostactive><boostactiveendtime>0</boostactiveendtime><batterylow>0</batterylow><battery>100</battery><nextchange><endperiod>1484341200</endperiod><tchange>28</tchange></nextchange></hkr></device>\
                <device identifier="08761 0000435" id="22" functionbitmask="320" fwversion="03.50" manufacturer="AVM" productname="Comet DECT"><present>0</present><name>Comet DECT #1</name><temperature><celsius>220</celsius><offset>-10</offset></temperature><hkr><tist>44</tist><tsoll>42</tsoll><absenk>28</absenk><komfort>42</komfort><lock>1</lock><devicelock>1</devicelock><errorcode>0</errorcode><windowopenactiv>0</windowopenactiv><windowopenactiveendtime>0</windowopenactiveendtime><boostactive>0</boostactive><boostactiveendtime>0</boostactiveendtime><batterylow>0</batterylow><battery>100</battery><nextchange><endperiod>1484341200</endperiod><tchange>28</tchange></nextchange></hkr></device>\
                <device identifier="5C:49:79:F0:A3:84" id="30" functionbitmask="640" fwversion="06.92" manufacturer="AVM" productname="FRITZ!Powerline 546E"><present>1</present><name>FRITZ!Powerline 546E #1</name><switch><state>0</state><mode>manuell</mode><lock>0</lock><devicelock>1</devicelock></switch><powermeter><voltage>230051</voltage><power>0</power><energy>2087</energy></powermeter></device>\
                <device identifier="08761 0000439" id="40" functionbitmask="1280" fwversion="03.86" manufacturer="AVM" productname="FRITZ!DECT Repeater 100"><present>1</present><name>FRITZ!DECT Repeater 100 #5</name><temperature><celsius>230</celsius><offset>0</offset></temperature></device>\
                <device identifier="11934 0059978-1" id="2000" functionbitmask="8208" fwversion="0.0" manufacturer="0x0feb" productname="HAN-FUN"><present>0</present><name>HAN-FUN #2: Unit #2</name><etsiunitinfo><etsideviceid>406</etsideviceid><unittype>514</unittype><interfaces>256</interfaces></etsiunitinfo><alert><state>1</state></alert></device>\
                <device identifier="11934 0059979-1" id="2001" functionbitmask="8200" fwversion="0.0" manufacturer="0x0feb" productname="HAN-FUN"><present>0</present><name>HAN-FUN #2: Unit #2</name><etsiunitinfo><etsideviceid>412</etsideviceid><unittype>273</unittype><interfaces>772</interfaces></etsiunitinfo><button><lastpressedtimestamp>1529590797</lastpressedtimestamp></button></device>\
                <device identifier="13096 0007307" id="29" functionbitmask="32" fwversion="04.90" manufacturer="AVM" productname="FRITZ!DECT 400"><present>1</present><name>FRITZ!DECT 400 #14</name><battery>100</battery><batterylow>0</batterylow><button identifier="13096 0007307-0" id="5000"><name>FRITZ!DECT 400 #14: kurz</name><lastpressedtimestamp>1549195586</lastpressedtimestamp></button><button identifier="13096 0007307-9" id="5001"><name>FRITZ!DECT 400 #14: lang</name><lastpressedtimestamp>1549195595</lastpressedtimestamp></button></device>\
                <device identifier="13096 0007308" id="30" functionbitmask="1048864" fwversion="05.10" manufacturer="AVM" productname="FRITZ!DECT 440"><present>1</present><name>FRITZ!DECT 440 #15</name><temperature><celsius>230</celsius><offset>0</offset></temperature><humidity><rel_humidity>43</rel_humidity></humidity><battery>100</battery><batterylow>0</batterylow><button identifier="13096 0007308-1" id="5000"><name>FRITZ!DECT 440 #15: Oben rechts</name><lastpressedtimestamp>1549195586</lastpressedtimestamp></button><button identifier="13096 0007308-3" id="5001"><name>FRITZ!DECT 440 #15: Unten rechts</name><lastpressedtimestamp>1549195595</lastpressedtimestamp></button><button identifier="13096 0007308-5" id="5002"><name>FRITZ!DECT 440 #15: Unten links</name><lastpressedtimestamp>1549195586</lastpressedtimestamp></button><button identifier="13096 0007308-7" id="5003"><name>FRITZ!DECT 440 #15: Oben links</name><lastpressedtimestamp>1549195595</lastpressedtimestamp></button></device>\
                <device identifier="14276 0503450-1" id="2000" functionbitmask="335888" fwversion="0.0" manufacturer="0x37c4" productname="Rollotron 1213"><present>1</present><txbusy>0</txbusy><name>Rollotron 1213 #1</name><blind><endpositionsset>1</endpositionsset><mode>manuell</mode></blind><levelcontrol><level>26</level><levelpercentage>10</levelpercentage></levelcontrol><etsiunitinfo><etsideviceid>406</etsideviceid><unittype>281</unittype><interfaces>256,513,516,517</interfaces></etsiunitinfo><alert><state>0</state><lastalertchgtimestamp></lastalertchgtimestamp></alert></device>\
                <device identifier="11324 0824499-1" id="2002" functionbitmask="40960" fwversion="0.0" manufacturer="0x2c3c" productname="HAN-FUN">
                    <present>1</present>
                    <txbusy>0</txbusy>
                    <name>Steckdose innen</name>
                    <simpleonoff>
                        <state>0</state>
                    </simpleonoff>
                    <etsiunitinfo>
                        <etsideviceid>408</etsideviceid>
                        <unittype>263</unittype>
                        <interfaces>512,768</interfaces>
                    </etsiunitinfo>
                </device>\
                <device identifier="11324 0584796-1" id="2001" functionbitmask="40960" fwversion="0.0" manufacturer="0x2c3c" productname="HAN-FUN">
                    <present>1</present>
                    <txbusy>0</txbusy>
                    <name>Steckdose außen</name>
                    <simpleonoff>
                        <state>0</state>
                    </simpleonoff>
                    <etsiunitinfo>
                        <etsideviceid>407</etsideviceid>
                        <unittype>262</unittype>
                        <interfaces>512</interfaces>
                    </etsiunitinfo>
                </device>\
                <device identifier="12701 0027533-1" id="2002" functionbitmask="237572" fwversion="0.0" manufacturer="0x319d" productname="HAN-FUN">
                    <present>0</present>
                    <txbusy>0</txbusy>
                    <name>SmartHome LED-Lampe #1</name>
                    <simpleonoff>
                        <state>0</state>
                    </simpleonoff>
                    <levelcontrol>
                       <level>26</level>
                       <levelpercentage>10</levelpercentage>
                    </levelcontrol>
                    <colorcontrol supported_modes="0" current_mode="" fullcolorsupport="0" mapped="0">
                        <hue>254</hue>
                        <saturation>100</saturation>
                        <unmapped_hue></unmapped_hue>
                        <unmapped_saturation></unmapped_saturation>
                        <temperature>2700</temperature>
                    </colorcontrol>
                    <etsiunitinfo>
                        <etsideviceid>407</etsideviceid>
                        <unittype>278</unittype>
                        <interfaces>512,514,513</interfaces>
                    </etsiunitinfo>
                </device>\
                <device identifier="Z001788011D4B55D30B" id="2038" functionbitmask="106500" fwversion="0.0" manufacturer="0x100b" productname="Signify Netherlands B.V. LWG004">
                    <present>1</present>
                    <txbusy>0</txbusy>
                    <name>Zigbee dimmable bulb</name>
                    <simpleonoff>
                        <state>0</state>
                    </simpleonoff>
                    <levelcontrol>
                        <level>255</level>
                        <levelpercentage>100</levelpercentage>
                    </levelcontrol>
                    <etsiunitinfo>
                        <etsideviceid>20029</etsideviceid>
                        <unittype>265</unittype>
                        <interfaces>512,513</interfaces>
                    </etsiunitinfo>
                </device>\
            </devicelist>\
                """;
        //@formatter:on
        XMLStreamReader xsr = JAXBUtils.XMLINPUTFACTORY.createXMLStreamReader(new StringReader(xml));
        Unmarshaller u = JAXBUtils.JAXBCONTEXT_DEVICES.createUnmarshaller();
        devices = u.unmarshal(xsr, DeviceListModel.class).getValue();
    }

    @Test
    public void validateDeviceListModel() {
        assertNotNull(devices);
        assertEquals(18, devices.getDevicelist().size());
        assertEquals("1", devices.getXmlApiVersion());
    }

    @Test
    public void validateDECTRepeater100Model() {
        Optional<AVMFritzBaseModel> optionalDevice = findModelByIdentifier("087610000439");
        assertTrue(optionalDevice.isPresent());
        assertTrue(optionalDevice.get() instanceof DeviceModel);

        DeviceModel device = (DeviceModel) optionalDevice.get();
        assertEquals("FRITZ!DECT Repeater 100", device.getProductName());
        assertEquals("087610000439", device.getIdentifier());
        assertEquals("40", device.getDeviceId());
        assertEquals("03.86", device.getFirmwareVersion());
        assertEquals("AVM", device.getManufacturer());

        assertEquals(1, device.getPresent());
        assertEquals("FRITZ!DECT Repeater 100 #5", device.getName());

        assertFalse(device.isButton());
        assertFalse(device.isHANFUNButton());
        assertFalse(device.isHANFUNAlarmSensor());
        assertTrue(device.isDectRepeater());
        assertFalse(device.isSwitchableOutlet());
        assertTrue(device.isTemperatureSensor());
        assertFalse(device.isHumiditySensor());
        assertFalse(device.isPowermeter());
        assertFalse(device.isHeatingThermostat());
        assertFalse(device.isHANFUNBlinds());

        assertNull(device.getSwitch());

        assertNotNull(device.getTemperature());
        assertEquals(new BigDecimal("23.0"), device.getTemperature().getCelsius());
        assertEquals(new BigDecimal("0.0"), device.getTemperature().getOffset());

        assertNull(device.getPowermeter());

        assertNull(device.getHkr());

        assertNull(device.getLevelControlModel());
    }

    @Test
    public void validateDECT200Model() {
        Optional<AVMFritzBaseModel> optionalDevice = findModel("FRITZ!DECT 200");
        assertTrue(optionalDevice.isPresent());
        assertTrue(optionalDevice.get() instanceof DeviceModel);

        DeviceModel device = (DeviceModel) optionalDevice.get();
        assertEquals("FRITZ!DECT 200", device.getProductName());
        assertEquals("087610000434", device.getIdentifier());
        assertEquals("17", device.getDeviceId());
        assertEquals("03.83", device.getFirmwareVersion());
        assertEquals("AVM", device.getManufacturer());

        assertEquals(1, device.getPresent());
        assertEquals("FRITZ!DECT 200 #1", device.getName());

        assertFalse(device.isHANFUNButton());
        assertFalse(device.isHANFUNAlarmSensor());
        assertFalse(device.isButton());
        assertFalse(device.isHeatingThermostat());
        assertTrue(device.isPowermeter());
        assertTrue(device.isTemperatureSensor());
        assertTrue(device.isSwitchableOutlet());
        assertFalse(device.isDectRepeater());
        assertTrue(device.hasMicrophone());
        assertFalse(device.isHANFUNUnit());
        assertTrue(device.isHANFUNOnOff());
        assertFalse(device.isHANFUNBlinds());
        assertFalse(device.isHumiditySensor());

        assertNotNull(device.getSwitch());
        assertEquals(SwitchModel.ON, device.getSwitch().getState());
        assertEquals(MODE_MANUAL, device.getSwitch().getMode());
        assertEquals(BigDecimal.ZERO, device.getSwitch().getLock());
        assertEquals(BigDecimal.ZERO, device.getSwitch().getDevicelock());

        assertNotNull(device.getTemperature());
        assertEquals(new BigDecimal("25.5"), device.getTemperature().getCelsius());
        assertEquals(new BigDecimal("0.0"), device.getTemperature().getOffset());

        validatePowerMeter(device.getPowermeter());

        assertNull(device.getHkr());

        assertNull(device.getLevelControlModel());
    }

    @Test
    public void validateDECT210Model() {
        Optional<AVMFritzBaseModel> optionalDevice = findModel("FRITZ!DECT 210");
        assertTrue(optionalDevice.isPresent());
        assertTrue(optionalDevice.get() instanceof DeviceModel);

        DeviceModel device = (DeviceModel) optionalDevice.get();
        assertEquals("FRITZ!DECT 210", device.getProductName());
        assertEquals("087610000438", device.getIdentifier());
        assertEquals("18", device.getDeviceId());
        assertEquals("03.83", device.getFirmwareVersion());
        assertEquals("AVM", device.getManufacturer());

        assertEquals(1, device.getPresent());
        assertEquals("FRITZ!DECT 210 #8", device.getName());

        assertFalse(device.isHANFUNButton());
        assertFalse(device.isHANFUNAlarmSensor());
        assertFalse(device.isButton());
        assertFalse(device.isHeatingThermostat());
        assertTrue(device.isPowermeter());
        assertTrue(device.isTemperatureSensor());
        assertTrue(device.isSwitchableOutlet());
        assertFalse(device.isDectRepeater());
        assertTrue(device.hasMicrophone());
        assertFalse(device.isHANFUNUnit());
        assertTrue(device.isHANFUNOnOff());
        assertFalse(device.isHANFUNBlinds());
        assertFalse(device.isHumiditySensor());

        assertNotNull(device.getSwitch());
        assertEquals(SwitchModel.ON, device.getSwitch().getState());
        assertEquals(MODE_MANUAL, device.getSwitch().getMode());
        assertEquals(BigDecimal.ZERO, device.getSwitch().getLock());
        assertEquals(BigDecimal.ZERO, device.getSwitch().getDevicelock());

        assertNotNull(device.getTemperature());
        assertEquals(new BigDecimal("25.5"), device.getTemperature().getCelsius());
        assertEquals(new BigDecimal("0.0"), device.getTemperature().getOffset());

        validatePowerMeter(device.getPowermeter());

        assertNull(device.getHkr());

        assertNull(device.getLevelControlModel());
    }

    @Test
    public void validateDECT300Model() {
        Optional<AVMFritzBaseModel> optionalDevice = findModel("FRITZ!DECT 300");
        assertTrue(optionalDevice.isPresent());
        assertTrue(optionalDevice.get() instanceof DeviceModel);

        DeviceModel device = (DeviceModel) optionalDevice.get();
        assertEquals("FRITZ!DECT 300", device.getProductName());
        assertEquals("087610000437", device.getIdentifier());
        assertEquals("20", device.getDeviceId());
        assertEquals("03.50", device.getFirmwareVersion());
        assertEquals("AVM", device.getManufacturer());

        assertEquals(0, device.getPresent());
        assertEquals("FRITZ!DECT 300 #1", device.getName());

        assertFalse(device.isButton());
        assertFalse(device.isHANFUNButton());
        assertFalse(device.isHANFUNAlarmSensor());
        assertFalse(device.isDectRepeater());
        assertFalse(device.isSwitchableOutlet());
        assertTrue(device.isTemperatureSensor());
        assertFalse(device.isHumiditySensor());
        assertFalse(device.isPowermeter());
        assertTrue(device.isHeatingThermostat());
        assertFalse(device.isHANFUNBlinds());

        assertNull(device.getSwitch());

        assertNotNull(device.getTemperature());
        assertEquals(new BigDecimal("22.0"), device.getTemperature().getCelsius());
        assertEquals(new BigDecimal("-1.0"), device.getTemperature().getOffset());

        assertNull(device.getPowermeter());

        validateHeatingModel(device.getHkr());
    }

    @Test
    public void validateDECT301Model() {
        Optional<AVMFritzBaseModel> optionalDevice = findModel("FRITZ!DECT 301");
        assertTrue(optionalDevice.isPresent());
        assertTrue(optionalDevice.get() instanceof DeviceModel);

        DeviceModel device = (DeviceModel) optionalDevice.get();
        assertEquals("FRITZ!DECT 301", device.getProductName());
        assertEquals("087610000436", device.getIdentifier());
        assertEquals("21", device.getDeviceId());
        assertEquals("03.50", device.getFirmwareVersion());
        assertEquals("AVM", device.getManufacturer());

        assertEquals(0, device.getPresent());
        assertEquals("FRITZ!DECT 301 #1", device.getName());

        assertFalse(device.isButton());
        assertFalse(device.isHANFUNButton());
        assertFalse(device.isHANFUNAlarmSensor());
        assertFalse(device.isDectRepeater());
        assertFalse(device.isSwitchableOutlet());
        assertTrue(device.isTemperatureSensor());
        assertFalse(device.isHumiditySensor());
        assertFalse(device.isPowermeter());
        assertTrue(device.isHeatingThermostat());
        assertFalse(device.isHANFUNBlinds());

        assertNull(device.getSwitch());

        assertNotNull(device.getTemperature());
        assertEquals(new BigDecimal("22.0"), device.getTemperature().getCelsius());
        assertEquals(new BigDecimal("-1.0"), device.getTemperature().getOffset());

        assertNull(device.getPowermeter());

        validateHeatingModel(device.getHkr());
    }

    @Test
    public void validateCometDECTModel() {
        Optional<AVMFritzBaseModel> optionalDevice = findModel("Comet DECT");
        assertTrue(optionalDevice.isPresent());
        assertTrue(optionalDevice.get() instanceof DeviceModel);

        DeviceModel device = (DeviceModel) optionalDevice.get();
        assertEquals("Comet DECT", device.getProductName());
        assertEquals("087610000435", device.getIdentifier());
        assertEquals("22", device.getDeviceId());
        assertEquals("03.50", device.getFirmwareVersion());
        assertEquals("AVM", device.getManufacturer());

        assertEquals(0, device.getPresent());
        assertEquals("Comet DECT #1", device.getName());

        assertFalse(device.isButton());
        assertFalse(device.isHANFUNButton());
        assertFalse(device.isHANFUNAlarmSensor());
        assertFalse(device.isDectRepeater());
        assertFalse(device.isSwitchableOutlet());
        assertTrue(device.isTemperatureSensor());
        assertFalse(device.isHumiditySensor());
        assertFalse(device.isPowermeter());
        assertTrue(device.isHeatingThermostat());
        assertFalse(device.isHANFUNBlinds());

        assertNull(device.getSwitch());

        assertNotNull(device.getTemperature());
        assertEquals(new BigDecimal("22.0"), device.getTemperature().getCelsius());
        assertEquals(new BigDecimal("-1.0"), device.getTemperature().getOffset());

        assertNull(device.getPowermeter());

        validateHeatingModel(device.getHkr());
    }

    @Test
    public void validateDECT400Model() {
        Optional<AVMFritzBaseModel> optionalDevice = findModelByIdentifier("130960007307");
        assertTrue(optionalDevice.isPresent());
        assertTrue(optionalDevice.get() instanceof DeviceModel);

        DeviceModel device = (DeviceModel) optionalDevice.get();
        assertEquals("FRITZ!DECT 400", device.getProductName());
        assertEquals("130960007307", device.getIdentifier());
        assertEquals("29", device.getDeviceId());
        assertEquals("04.90", device.getFirmwareVersion());
        assertEquals("AVM", device.getManufacturer());

        assertEquals(1, device.getPresent());
        assertEquals("FRITZ!DECT 400 #14", device.getName());

        assertTrue(device.isButton());
        assertFalse(device.isHANFUNButton());
        assertFalse(device.isHANFUNAlarmSensor());
        assertFalse(device.isDectRepeater());
        assertFalse(device.isSwitchableOutlet());
        assertFalse(device.isTemperatureSensor());
        assertFalse(device.isHumiditySensor());
        assertFalse(device.isPowermeter());
        assertFalse(device.isHeatingThermostat());
        assertFalse(device.isHANFUNBlinds());

        assertEquals(new BigDecimal("100"), device.getBattery());
        assertEquals(BatteryModel.BATTERY_OFF, device.getBatterylow());

        assertEquals(2, device.getButtons().size());
        assertEquals("FRITZ!DECT 400 #14: kurz", device.getButtons().get(0).getName());
        assertEquals(1549195586, device.getButtons().get(0).getLastpressedtimestamp());
        assertEquals("FRITZ!DECT 400 #14: lang", device.getButtons().get(1).getName());
        assertEquals(1549195595, device.getButtons().get(1).getLastpressedtimestamp());

        assertNull(device.getAlert());

        assertNull(device.getSwitch());

        assertNull(device.getTemperature());

        assertNull(device.getPowermeter());

        assertNull(device.getHkr());

        assertNull(device.getLevelControlModel());
    }

    @Test
    public void validateDECT440Model() {
        Optional<AVMFritzBaseModel> optionalDevice = findModelByIdentifier("130960007308");
        assertTrue(optionalDevice.isPresent());
        assertTrue(optionalDevice.get() instanceof DeviceModel);

        DeviceModel device = (DeviceModel) optionalDevice.get();
        assertEquals("FRITZ!DECT 440", device.getProductName());
        assertEquals("130960007308", device.getIdentifier());
        assertEquals("30", device.getDeviceId());
        assertEquals("05.10", device.getFirmwareVersion());
        assertEquals("AVM", device.getManufacturer());

        assertEquals(1, device.getPresent());
        assertEquals("FRITZ!DECT 440 #15", device.getName());

        assertTrue(device.isButton());
        assertFalse(device.isHANFUNButton());
        assertFalse(device.isHANFUNAlarmSensor());
        assertFalse(device.isDectRepeater());
        assertFalse(device.isSwitchableOutlet());
        assertTrue(device.isTemperatureSensor());
        assertTrue(device.isHumiditySensor());
        assertFalse(device.isPowermeter());
        assertFalse(device.isHeatingThermostat());
        assertFalse(device.isHANFUNBlinds());

        assertEquals(new BigDecimal("100"), device.getBattery());
        assertEquals(BatteryModel.BATTERY_OFF, device.getBatterylow());

        assertEquals(4, device.getButtons().size());
        final ButtonModel topRight = device.getButtons().get(0);
        assertEquals("130960007308-1", topRight.getIdentifier());
        assertEquals("5000", topRight.getButtonId());
        assertEquals("FRITZ!DECT 440 #15: Oben rechts", topRight.getName());
        assertEquals(1549195586, topRight.getLastpressedtimestamp());
        final ButtonModel bottomRight = device.getButtons().get(1);
        assertEquals("130960007308-3", bottomRight.getIdentifier());
        assertEquals("5001", bottomRight.getButtonId());
        assertEquals("FRITZ!DECT 440 #15: Unten rechts", bottomRight.getName());
        assertEquals(1549195595, bottomRight.getLastpressedtimestamp());
        final ButtonModel bottomLeft = device.getButtons().get(2);
        assertEquals("130960007308-5", bottomLeft.getIdentifier());
        assertEquals("5002", bottomLeft.getButtonId());
        assertEquals("FRITZ!DECT 440 #15: Unten links", bottomLeft.getName());
        assertEquals(1549195586, bottomLeft.getLastpressedtimestamp());
        final ButtonModel topLeft = device.getButtons().get(3);
        assertEquals("130960007308-7", topLeft.getIdentifier());
        assertEquals("5003", topLeft.getButtonId());
        assertEquals("FRITZ!DECT 440 #15: Oben links", topLeft.getName());
        assertEquals(1549195595, topLeft.getLastpressedtimestamp());

        assertNull(device.getAlert());

        assertNull(device.getSwitch());

        assertNotNull(device.getTemperature());
        assertEquals(new BigDecimal("23.0"), device.getTemperature().getCelsius());
        assertEquals(new BigDecimal("0.0"), device.getTemperature().getOffset());

        assertNotNull(device.getHumidity());
        assertEquals(new BigDecimal("43"), device.getHumidity().getRelativeHumidity());

        assertNull(device.getPowermeter());

        assertNull(device.getHkr());

        assertNull(device.getLevelControlModel());
    }

    @Test
    public void validatePowerline546EModel() {
        Optional<AVMFritzBaseModel> optionalDevice = findModel("FRITZ!Powerline 546E");
        assertTrue(optionalDevice.isPresent());
        assertTrue(optionalDevice.get() instanceof DeviceModel);

        DeviceModel device = (DeviceModel) optionalDevice.get();
        assertEquals("FRITZ!Powerline 546E", device.getProductName());
        assertEquals("5C:49:79:F0:A3:84", device.getIdentifier());
        assertEquals("30", device.getDeviceId());
        assertEquals("06.92", device.getFirmwareVersion());
        assertEquals("AVM", device.getManufacturer());

        assertEquals(1, device.getPresent());
        assertEquals("FRITZ!Powerline 546E #1", device.getName());

        assertFalse(device.isButton());
        assertFalse(device.isHANFUNButton());
        assertFalse(device.isHANFUNAlarmSensor());
        assertFalse(device.isDectRepeater());
        assertTrue(device.isSwitchableOutlet());
        assertFalse(device.isTemperatureSensor());
        assertFalse(device.isHumiditySensor());
        assertTrue(device.isPowermeter());
        assertFalse(device.isHeatingThermostat());
        assertFalse(device.isHANFUNBlinds());

        assertNotNull(device.getSwitch());
        assertEquals(SwitchModel.OFF, device.getSwitch().getState());
        assertEquals(MODE_MANUAL, device.getSwitch().getMode());
        assertEquals(BigDecimal.ZERO, device.getSwitch().getLock());
        assertEquals(BigDecimal.ONE, device.getSwitch().getDevicelock());

        assertNull(device.getTemperature());

        validatePowerMeter(device.getPowermeter());

        assertNull(device.getHkr());

        assertNull(device.getLevelControlModel());
    }

    @Test
    public void validateHANFUNContactModel() {
        Optional<AVMFritzBaseModel> optionalDevice = findModelByIdentifier("119340059978-1");
        assertTrue(optionalDevice.isPresent());
        assertTrue(optionalDevice.get() instanceof DeviceModel);

        DeviceModel device = (DeviceModel) optionalDevice.get();
        assertEquals("HAN-FUN", device.getProductName());
        assertEquals("119340059978-1", device.getIdentifier());
        assertEquals("2000", device.getDeviceId());
        assertEquals("0.0", device.getFirmwareVersion());
        assertEquals("0x0feb", device.getManufacturer());

        assertEquals(0, device.getPresent());
        assertEquals("HAN-FUN #2: Unit #2", device.getName());

        assertFalse(device.isButton());
        assertFalse(device.isHANFUNButton());
        assertTrue(device.isHANFUNAlarmSensor());
        assertFalse(device.isDectRepeater());
        assertFalse(device.isSwitchableOutlet());
        assertFalse(device.isTemperatureSensor());
        assertFalse(device.isHumiditySensor());
        assertFalse(device.isPowermeter());
        assertFalse(device.isHeatingThermostat());
        assertFalse(device.isHANFUNBlinds());

        assertTrue(device.getButtons().isEmpty());

        assertNotNull(device.getAlert());
        assertEquals(BigDecimal.ONE, device.getAlert().getState());

        assertNull(device.getSwitch());

        assertNull(device.getTemperature());

        assertNull(device.getPowermeter());

        assertNull(device.getHkr());

        assertNull(device.getLevelControlModel());
    }

    @Test
    public void validateHANFUNSwitchModel() {
        Optional<AVMFritzBaseModel> optionalDevice = findModelByIdentifier("119340059979-1");
        assertTrue(optionalDevice.isPresent());
        assertTrue(optionalDevice.get() instanceof DeviceModel);

        DeviceModel device = (DeviceModel) optionalDevice.get();
        assertEquals("HAN-FUN", device.getProductName());
        assertEquals("119340059979-1", device.getIdentifier());
        assertEquals("2001", device.getDeviceId());
        assertEquals("0.0", device.getFirmwareVersion());
        assertEquals("0x0feb", device.getManufacturer());

        assertEquals(0, device.getPresent());
        assertEquals("HAN-FUN #2: Unit #2", device.getName());

        assertFalse(device.isButton());
        assertTrue(device.isHANFUNButton());
        assertFalse(device.isHANFUNAlarmSensor());
        assertFalse(device.isDectRepeater());
        assertFalse(device.isSwitchableOutlet());
        assertFalse(device.isTemperatureSensor());
        assertFalse(device.isHumiditySensor());
        assertFalse(device.isPowermeter());
        assertFalse(device.isHeatingThermostat());
        assertFalse(device.isHANFUNBlinds());

        assertEquals(1, device.getButtons().size());
        assertEquals(1529590797, device.getButtons().get(0).getLastpressedtimestamp());

        assertNull(device.getAlert());

        assertNull(device.getSwitch());

        assertNull(device.getTemperature());

        assertNull(device.getPowermeter());

        assertNull(device.getHkr());

        assertNull(device.getLevelControlModel());
    }

    @Test
    public void validateHANFUNBlindModel() {
        Optional<AVMFritzBaseModel> optionalDevice = findModelByIdentifier("142760503450-1");
        assertTrue(optionalDevice.isPresent());
        assertTrue(optionalDevice.get() instanceof DeviceModel);

        DeviceModel device = (DeviceModel) optionalDevice.get();
        assertEquals("Rollotron 1213", device.getProductName());
        assertEquals("142760503450-1", device.getIdentifier());
        assertEquals("2000", device.getDeviceId());
        assertEquals("0.0", device.getFirmwareVersion());
        assertEquals("0x37c4", device.getManufacturer());

        assertEquals(1, device.getPresent());
        assertEquals("Rollotron 1213 #1", device.getName());

        assertFalse(device.isHANFUNDevice());
        assertFalse(device.isHANFUNButton());
        assertTrue(device.isHANFUNAlarmSensor());
        assertFalse(device.isButton());
        assertFalse(device.isSwitchableOutlet());
        assertFalse(device.isTemperatureSensor());
        assertFalse(device.isHumiditySensor());
        assertFalse(device.isPowermeter());
        assertFalse(device.isDectRepeater());
        assertFalse(device.isHeatingThermostat());
        assertFalse(device.hasMicrophone());
        assertTrue(device.isHANFUNUnit());
        assertTrue(device.isHANFUNOnOff());
        assertTrue(device.isDimmableLight());
        assertFalse(device.isColorLight());
        assertTrue(device.isHANFUNBlinds());

        assertTrue(device.getButtons().isEmpty());

        assertNotNull(device.getAlert());
        assertEquals(BigDecimal.ZERO, device.getAlert().getState());

        assertNull(device.getSwitch());

        assertNull(device.getTemperature());

        assertNull(device.getPowermeter());

        assertNull(device.getHkr());

        LevelControlModel levelcontrol = device.getLevelControlModel();
        assertNotNull(levelcontrol);
        assertEquals(BigDecimal.valueOf(26L), levelcontrol.getLevel());
        assertEquals(BigDecimal.valueOf(10L), levelcontrol.getLevelPercentage());
    }

    @Test
    public void validateHANFUNColorLightModel() {
        Optional<AVMFritzBaseModel> optionalDevice = findModelByIdentifier("127010027533-1");
        assertTrue(optionalDevice.isPresent());
        assertTrue(optionalDevice.get() instanceof DeviceModel);

        DeviceModel device = (DeviceModel) optionalDevice.get();
        assertEquals("HAN-FUN", device.getProductName());
        assertEquals("127010027533-1", device.getIdentifier());
        assertEquals("2002", device.getDeviceId());
        assertEquals("0.0", device.getFirmwareVersion());
        assertEquals("0x319d", device.getManufacturer());

        assertEquals(0, device.getPresent());
        assertEquals("SmartHome LED-Lampe #1", device.getName());

        assertFalse(device.isHANFUNDevice());
        assertFalse(device.isHANFUNButton());
        assertFalse(device.isHANFUNAlarmSensor());
        assertFalse(device.isButton());
        assertFalse(device.isSwitchableOutlet());
        assertFalse(device.isTemperatureSensor());
        assertFalse(device.isHumiditySensor());
        assertFalse(device.isPowermeter());
        assertFalse(device.isDectRepeater());
        assertFalse(device.isHeatingThermostat());
        assertFalse(device.hasMicrophone());
        assertTrue(device.isHANFUNUnit());
        assertTrue(device.isHANFUNOnOff());
        assertTrue(device.isDimmableLight());
        assertTrue(device.isColorLight());
        assertFalse(device.isHANFUNBlinds());

        assertTrue(device.getButtons().isEmpty());

        assertNull(device.getAlert());

        assertNull(device.getSwitch());

        assertNull(device.getTemperature());

        assertNull(device.getPowermeter());

        assertNull(device.getHkr());

        LevelControlModel levelcontrol = device.getLevelControlModel();
        assertNotNull(levelcontrol);
        assertEquals(BigDecimal.valueOf(26L), levelcontrol.getLevel());
        assertEquals(BigDecimal.valueOf(10L), levelcontrol.getLevelPercentage());

        ColorControlModel colorModel = device.getColorControlModel();
        assertNotNull(colorModel);
        assertEquals(254, colorModel.hue);
        assertEquals(100, colorModel.saturation);
        assertEquals(0, colorModel.unmappedHue);
        assertEquals(0, colorModel.unmappedSaturation);
        assertEquals(2700, colorModel.temperature);
    }

    @Test
    public void validateHANFUNDimmableLightModel() {
        Optional<AVMFritzBaseModel> optionalDevice = findModelByIdentifier("Z001788011D4B55D30B");
        assertTrue(optionalDevice.isPresent());
        assertTrue(optionalDevice.get() instanceof DeviceModel);

        DeviceModel device = (DeviceModel) optionalDevice.get();
        assertEquals("Signify Netherlands B.V. LWG004", device.getProductName());
        assertEquals("Z001788011D4B55D30B", device.getIdentifier());
        assertEquals("2038", device.getDeviceId());
        assertEquals("0.0", device.getFirmwareVersion());
        assertEquals("0x100b", device.getManufacturer());

        assertEquals(1, device.getPresent());
        assertEquals("Zigbee dimmable bulb", device.getName());

        assertFalse(device.isHANFUNDevice());
        assertFalse(device.isHANFUNButton());
        assertFalse(device.isHANFUNAlarmSensor());
        assertFalse(device.isButton());
        assertFalse(device.isSwitchableOutlet());
        assertFalse(device.isTemperatureSensor());
        assertFalse(device.isHumiditySensor());
        assertFalse(device.isPowermeter());
        assertFalse(device.isDectRepeater());
        assertFalse(device.isHeatingThermostat());
        assertFalse(device.hasMicrophone());
        assertTrue(device.isHANFUNUnit());
        assertTrue(device.isHANFUNOnOff());
        assertTrue(device.isDimmableLight());
        assertFalse(device.isColorLight());
        assertFalse(device.isHANFUNBlinds());

        assertTrue(device.getButtons().isEmpty());

        assertNull(device.getAlert());

        assertNull(device.getSwitch());

        assertNull(device.getTemperature());

        assertNull(device.getPowermeter());

        assertNull(device.getHkr());

        LevelControlModel levelcontrol = device.getLevelControlModel();
        assertNotNull(levelcontrol);
        assertEquals(BigDecimal.valueOf(255L), levelcontrol.getLevel());
        assertEquals(BigDecimal.valueOf(100L), levelcontrol.getLevelPercentage());
    }

    @Test
    public void validateHANFUNOnOffModel() {
        Optional<AVMFritzBaseModel> optionalDevice = findModelByIdentifier("113240824499-1");
        assertTrue(optionalDevice.isPresent());
        assertTrue(optionalDevice.get() instanceof DeviceModel);

        DeviceModel device = (DeviceModel) optionalDevice.get();
        assertEquals("HAN-FUN", device.getProductName());
        assertEquals("113240824499-1", device.getIdentifier());
        assertEquals("2002", device.getDeviceId());
        assertEquals("0.0", device.getFirmwareVersion());
        assertEquals("0x2c3c", device.getManufacturer());

        assertEquals(1, device.getPresent());
        assertEquals("Steckdose innen", device.getName());

        assertFalse(device.isHANFUNButton());
        assertFalse(device.isHANFUNAlarmSensor());
        assertFalse(device.isButton());
        assertFalse(device.isHeatingThermostat());
        assertFalse(device.isPowermeter());
        assertFalse(device.isTemperatureSensor());
        assertFalse(device.isSwitchableOutlet());
        assertFalse(device.isDectRepeater());
        assertFalse(device.hasMicrophone());
        assertTrue(device.isHANFUNUnit());
        assertTrue(device.isHANFUNOnOff());
        assertFalse(device.isHANFUNBlinds());
        assertFalse(device.isHumiditySensor());

        assertTrue(device.getButtons().isEmpty());

        assertNull(device.getAlert());

        assertNull(device.getSwitch());

        assertNull(device.getTemperature());

        SimpleOnOffModel model = device.getSimpleOnOffUnit();
        assertNotNull(model);
        assertEquals(false, model.state);

        assertNull(device.getPowermeter());

        assertNull(device.getHkr());

        assertNull(device.getLevelControlModel());
    }

    @Test
    public void validateHeatingGroupModel() {
        Optional<AVMFritzBaseModel> optionalGroup = findModelByIdentifier("F0:A3:7F-901");
        assertTrue(optionalGroup.isPresent());
        assertTrue(optionalGroup.get() instanceof GroupModel);

        GroupModel group = (GroupModel) optionalGroup.get();
        assertEquals("", group.getProductName());
        assertEquals("F0:A3:7F-901", group.getIdentifier());
        assertEquals("20001", group.getDeviceId());
        assertEquals("1.0", group.getFirmwareVersion());
        assertEquals("AVM", group.getManufacturer());

        assertEquals(1, group.getPresent());
        assertEquals("Schlafzimmer", group.getName());

        assertFalse(group.isButton());
        assertFalse(group.isHANFUNButton());
        assertFalse(group.isHANFUNAlarmSensor());
        assertFalse(group.isDectRepeater());
        assertFalse(group.isSwitchableOutlet());
        assertFalse(group.isTemperatureSensor());
        assertFalse(group.isHumiditySensor());
        assertFalse(group.isPowermeter());
        assertTrue(group.isHeatingThermostat());
        assertFalse(group.isHANFUNBlinds());

        assertNull(group.getSwitch());

        assertNull(group.getPowermeter());

        validateHeatingModel(group.getHkr());

        assertNotNull(group.getGroupinfo());
        assertEquals("0", group.getGroupinfo().getMasterdeviceid());
        assertEquals("20,21,22", group.getGroupinfo().getMembers());
    }

    @Test
    public void validateSwitchGroupModel() {
        Optional<AVMFritzBaseModel> optionalGroup = findModelByIdentifier("F0:A3:7F-900");
        assertTrue(optionalGroup.isPresent());
        assertTrue(optionalGroup.get() instanceof GroupModel);

        GroupModel group = (GroupModel) optionalGroup.get();
        assertEquals("", group.getProductName());
        assertEquals("F0:A3:7F-900", group.getIdentifier());
        assertEquals("20000", group.getDeviceId());
        assertEquals("1.0", group.getFirmwareVersion());
        assertEquals("AVM", group.getManufacturer());

        assertEquals(1, group.getPresent());
        assertEquals("Schlafzimmer", group.getName());

        assertFalse(group.isButton());
        assertFalse(group.isHANFUNButton());
        assertFalse(group.isHANFUNAlarmSensor());
        assertFalse(group.isDectRepeater());
        assertTrue(group.isSwitchableOutlet());
        assertFalse(group.isTemperatureSensor());
        assertFalse(group.isHumiditySensor());
        assertTrue(group.isPowermeter());
        assertFalse(group.isHeatingThermostat());
        assertFalse(group.isHANFUNBlinds());

        assertNotNull(group.getSwitch());
        assertEquals(SwitchModel.ON, group.getSwitch().getState());
        assertEquals(MODE_MANUAL, group.getSwitch().getMode());
        assertEquals(BigDecimal.ZERO, group.getSwitch().getLock());
        assertEquals(BigDecimal.ZERO, group.getSwitch().getDevicelock());

        validatePowerMeter(group.getPowermeter());

        assertNull(group.getHkr());

        assertNotNull(group.getGroupinfo());
        assertEquals("17", group.getGroupinfo().getMasterdeviceid());
        assertEquals("17,18", group.getGroupinfo().getMembers());
    }

    private Optional<AVMFritzBaseModel> findModel(String name) {
        return devices.getDevicelist().stream().filter(it -> name.equals(it.getProductName())).findFirst();
    }

    private Optional<AVMFritzBaseModel> findModelByIdentifier(String identifier) {
        return devices.getDevicelist().stream().filter(it -> identifier.equals(it.getIdentifier())).findFirst();
    }

    private void validatePowerMeter(PowerMeterModel model) {
        assertNotNull(model);
        assertEquals(new BigDecimal("230.051"), model.getVoltage());
        assertEquals(new BigDecimal("0.000"), model.getPower());
        assertEquals(new BigDecimal("2087"), model.getEnergy());
    }

    private void validateHeatingModel(HeatingModel model) {
        assertNotNull(model);
        assertEquals(new BigDecimal(44), model.getTist());
        assertEquals(new BigDecimal(42), model.getTsoll());
        assertEquals(new BigDecimal(28), model.getAbsenk());
        assertEquals(new BigDecimal(42), model.getKomfort());
        assertEquals(BigDecimal.ONE, model.getLock());
        assertEquals(BigDecimal.ONE, model.getDevicelock());
        assertEquals(BigDecimal.ZERO, model.getWindowopenactiv());
        assertEquals(BigDecimal.ZERO, model.getBoostactive());
        assertEquals(new BigDecimal("100"), model.getBattery());
        assertEquals(BatteryModel.BATTERY_OFF, model.getBatterylow());
        assertEquals(MODE_AUTO, model.getMode());
        assertEquals(MODE_COMFORT, model.getRadiatorMode());

        assertNotNull(model.getNextchange());
        assertEquals(1484341200, model.getNextchange().getEndperiod());
        assertEquals(new BigDecimal(28), model.getNextchange().getTchange());
    }
}
