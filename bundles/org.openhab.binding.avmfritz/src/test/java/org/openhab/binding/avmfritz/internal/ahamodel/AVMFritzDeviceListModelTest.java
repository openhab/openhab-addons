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
package org.openhab.binding.avmfritz.internal.ahamodel;

import static org.junit.Assert.*;
import static org.openhab.binding.avmfritz.internal.BindingConstants.*;

import java.io.StringReader;
import java.math.BigDecimal;
import java.util.Optional;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.junit.Before;
import org.junit.Test;
import org.openhab.binding.avmfritz.internal.util.JAXBUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for {@link DeviceListModel}.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
public class AVMFritzDeviceListModelTest {

    private final Logger logger = LoggerFactory.getLogger(AVMFritzDeviceListModelTest.class);

    private DeviceListModel devices;

    @Before
    public void setUp() {
        //@formatter:off
        String xml =
                "<devicelist version=\"1\">" +
                    "<group identifier=\"F0:A3:7F-900\" id=\"20000\" functionbitmask=\"6784\" fwversion=\"1.0\" manufacturer=\"AVM\" productname=\"\"><present>1</present><name>Schlafzimmer</name><switch><state>1</state><mode>manuell</mode><lock>0</lock><devicelock>0</devicelock></switch><powermeter><voltage>230051</voltage><power>0</power><energy>2087</energy></powermeter><groupinfo><masterdeviceid>17</masterdeviceid><members>17,18</members></groupinfo></group>" +
                    "<group identifier=\"F0:A3:7F-901\" id=\"20001\" functionbitmask=\"4160\" fwversion=\"1.0\" manufacturer=\"AVM\" productname=\"\"><present>1</present><name>Schlafzimmer</name><temperature><celsius>220</celsius><offset>-10</offset></temperature><hkr><tist>44</tist><tsoll>42</tsoll><absenk>28</absenk><komfort>42</komfort><lock>1</lock><devicelock>1</devicelock><errorcode>0</errorcode><batterylow>0</batterylow><battery>100</battery><nextchange><endperiod>1484341200</endperiod><tchange>28</tchange></nextchange></hkr><groupinfo><masterdeviceid>0</masterdeviceid><members>20,21,22</members></groupinfo></group>" +
                    "<device identifier=\"08761 0000434\" id=\"17\" functionbitmask=\"2944\" fwversion=\"03.83\" manufacturer=\"AVM\" productname=\"FRITZ!DECT 200\"><present>1</present><name>FRITZ!DECT 200 #1</name><switch><state>1</state><mode>manuell</mode><lock>0</lock><devicelock>0</devicelock></switch><powermeter><voltage>230051</voltage><power>0</power><energy>2087</energy></powermeter><temperature><celsius>255</celsius><offset>0</offset></temperature></device>" +
                    "<device identifier=\"08761 0000438\" id=\"18\" functionbitmask=\"2944\" fwversion=\"03.83\" manufacturer=\"AVM\" productname=\"FRITZ!DECT 210\"><present>1</present><name>FRITZ!DECT 210 #8</name><switch><state>1</state><mode>manuell</mode><lock>0</lock><devicelock>0</devicelock></switch><powermeter><voltage>230051</voltage><power>0</power><energy>2087</energy></powermeter><temperature><celsius>255</celsius><offset>0</offset></temperature></device>" +
                    "<device identifier=\"08761 0000437\" id=\"20\" functionbitmask=\"320\" fwversion=\"03.50\" manufacturer=\"AVM\" productname=\"FRITZ!DECT 300\"><present>0</present><name>FRITZ!DECT 300 #1</name><temperature><celsius>220</celsius><offset>-10</offset></temperature><hkr><tist>44</tist><tsoll>42</tsoll><absenk>28</absenk><komfort>42</komfort><lock>1</lock><devicelock>1</devicelock><errorcode>0</errorcode><batterylow>0</batterylow><battery>100</battery><nextchange><endperiod>1484341200</endperiod><tchange>28</tchange></nextchange></hkr></device>" +
                    "<device identifier=\"08761 0000436\" id=\"21\" functionbitmask=\"320\" fwversion=\"03.50\" manufacturer=\"AVM\" productname=\"FRITZ!DECT 301\"><present>0</present><name>FRITZ!DECT 301 #1</name><temperature><celsius>220</celsius><offset>-10</offset></temperature><hkr><tist>44</tist><tsoll>42</tsoll><absenk>28</absenk><komfort>42</komfort><lock>1</lock><devicelock>1</devicelock><errorcode>0</errorcode><batterylow>0</batterylow><battery>100</battery><nextchange><endperiod>1484341200</endperiod><tchange>28</tchange></nextchange></hkr></device>" +
                    "<device identifier=\"08761 0000435\" id=\"22\" functionbitmask=\"320\" fwversion=\"03.50\" manufacturer=\"AVM\" productname=\"Comet DECT\"><present>0</present><name>Comet DECT #1</name><temperature><celsius>220</celsius><offset>-10</offset></temperature><hkr><tist>44</tist><tsoll>42</tsoll><absenk>28</absenk><komfort>42</komfort><lock>1</lock><devicelock>1</devicelock><errorcode>0</errorcode><batterylow>0</batterylow><battery>100</battery><nextchange><endperiod>1484341200</endperiod><tchange>28</tchange></nextchange></hkr></device>" +
                    "<device identifier=\"5C:49:79:F0:A3:84\" id=\"30\" functionbitmask=\"640\" fwversion=\"06.92\" manufacturer=\"AVM\" productname=\"FRITZ!Powerline 546E\"><present>1</present><name>FRITZ!Powerline 546E #1</name><switch><state>0</state><mode>manuell</mode><lock>0</lock><devicelock>1</devicelock></switch><powermeter><voltage>230051</voltage><power>0</power><energy>2087</energy></powermeter></device>" +
                    "<device identifier=\"08761 0000439\" id=\"40\" functionbitmask=\"1280\" fwversion=\"03.86\" manufacturer=\"AVM\" productname=\"FRITZ!DECT Repeater 100\"><present>1</present><name>FRITZ!DECT Repeater 100 #5</name><temperature><celsius>230</celsius><offset>0</offset></temperature></device>" +
                    "<device identifier=\"11934 0059978-1\" id=\"2000\" functionbitmask=\"8208\" fwversion=\"0.0\" manufacturer=\"0x0feb\" productname=\"HAN-FUN\"><present>0</present><name>HAN-FUN #2: Unit #2</name><etsiunitinfo><etsideviceid>406</etsideviceid><unittype>514</unittype><interfaces>256</interfaces></etsiunitinfo><alert><state>1</state></alert></device>" +
                    "<device identifier=\"11934 0059979-1\" id=\"2001\" functionbitmask=\"8200\" fwversion=\"0.0\" manufacturer=\"0x0feb\" productname=\"HAN-FUN\"><present>0</present><name>HAN-FUN #2: Unit #2</name><etsiunitinfo><etsideviceid>412</etsideviceid><unittype>273</unittype><interfaces>772</interfaces></etsiunitinfo><button><lastpressedtimestamp>1529590797</lastpressedtimestamp></button></device>" +
                "</devicelist>";
        //@formatter:off

        try {
            Unmarshaller u = JAXBUtils.JAXBCONTEXT_DEVICES.createUnmarshaller();
            devices = (DeviceListModel) u.unmarshal(new StringReader(xml));
        } catch (JAXBException e) {
            logger.error("Exception creating Unmarshaller: {}", e.getLocalizedMessage(), e);
        }
    }

    @Test
    public void validateDeviceListModel() {
        assertNotNull(devices);
        assertEquals(11, devices.getDevicelist().size());
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
        assertFalse(device.isAlarmSensor());
        assertTrue(device.isDectRepeater());
        assertFalse(device.isSwitchableOutlet());
        assertTrue(device.isTempSensor());
        assertFalse(device.isPowermeter());
        assertFalse(device.isHeatingThermostat());

        assertNull(device.getSwitch());

        assertNotNull(device.getTemperature());
        assertEquals(new BigDecimal("23.0"), device.getTemperature().getCelsius());
        assertEquals(new BigDecimal("0.0"), device.getTemperature().getOffset());

        assertNull(device.getPowermeter());

        assertNull(device.getHkr());
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

        assertFalse(device.isButton());
        assertFalse(device.isAlarmSensor());
        assertFalse(device.isDectRepeater());
        assertTrue(device.isSwitchableOutlet());
        assertTrue(device.isTempSensor());
        assertTrue(device.isPowermeter());
        assertFalse(device.isHeatingThermostat());

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

        assertFalse(device.isButton());
        assertFalse(device.isAlarmSensor());
        assertFalse(device.isDectRepeater());
        assertTrue(device.isSwitchableOutlet());
        assertTrue(device.isTempSensor());
        assertTrue(device.isPowermeter());
        assertFalse(device.isHeatingThermostat());

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
        assertFalse(device.isAlarmSensor());
        assertFalse(device.isDectRepeater());
        assertFalse(device.isSwitchableOutlet());
        assertTrue(device.isTempSensor());
        assertFalse(device.isPowermeter());
        assertTrue(device.isHeatingThermostat());

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
        assertFalse(device.isAlarmSensor());
        assertFalse(device.isDectRepeater());
        assertFalse(device.isSwitchableOutlet());
        assertTrue(device.isTempSensor());
        assertFalse(device.isPowermeter());
        assertTrue(device.isHeatingThermostat());

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
        assertFalse(device.isAlarmSensor());
        assertFalse(device.isDectRepeater());
        assertFalse(device.isSwitchableOutlet());
        assertTrue(device.isTempSensor());
        assertFalse(device.isPowermeter());
        assertTrue(device.isHeatingThermostat());

        assertNull(device.getSwitch());

        assertNotNull(device.getTemperature());
        assertEquals(new BigDecimal("22.0"), device.getTemperature().getCelsius());
        assertEquals(new BigDecimal("-1.0"), device.getTemperature().getOffset());

        assertNull(device.getPowermeter());

        validateHeatingModel(device.getHkr());
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
        assertFalse(device.isAlarmSensor());
        assertFalse(device.isDectRepeater());
        assertTrue(device.isSwitchableOutlet());
        assertFalse(device.isTempSensor());
        assertTrue(device.isPowermeter());
        assertFalse(device.isHeatingThermostat());

        assertNotNull(device.getSwitch());
        assertEquals(SwitchModel.OFF, device.getSwitch().getState());
        assertEquals(MODE_MANUAL, device.getSwitch().getMode());
        assertEquals(BigDecimal.ZERO, device.getSwitch().getLock());
        assertEquals(BigDecimal.ONE, device.getSwitch().getDevicelock());

        assertNull(device.getTemperature());

        validatePowerMeter(device.getPowermeter());

        assertNull(device.getHkr());
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
        assertTrue(device.isAlarmSensor());
        assertFalse(device.isDectRepeater());
        assertFalse(device.isSwitchableOutlet());
        assertFalse(device.isTempSensor());
        assertFalse(device.isPowermeter());
        assertFalse(device.isHeatingThermostat());

        assertNull(device.getButton());

        assertNotNull(device.getAlert());
        assertEquals(BigDecimal.ONE, device.getAlert().getState());

        assertNull(device.getSwitch());

        assertNull(device.getTemperature());

        assertNull(device.getPowermeter());

        assertNull(device.getHkr());
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

        assertTrue(device.isButton());
        assertFalse(device.isAlarmSensor());
        assertFalse(device.isDectRepeater());
        assertFalse(device.isSwitchableOutlet());
        assertFalse(device.isTempSensor());
        assertFalse(device.isPowermeter());
        assertFalse(device.isHeatingThermostat());

        assertNotNull(device.getButton());
        assertEquals(1529590797, device.getButton().getLastpressedtimestamp());

        assertNull(device.getAlert());

        assertNull(device.getSwitch());

        assertNull(device.getTemperature());

        assertNull(device.getPowermeter());

        assertNull(device.getHkr());
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
        assertFalse(group.isAlarmSensor());
        assertFalse(group.isDectRepeater());
        assertFalse(group.isSwitchableOutlet());
        assertFalse(group.isTempSensor());
        assertFalse(group.isPowermeter());
        assertTrue(group.isHeatingThermostat());

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
        assertFalse(group.isAlarmSensor());
        assertFalse(group.isDectRepeater());
        assertTrue(group.isSwitchableOutlet());
        assertFalse(group.isTempSensor());
        assertTrue(group.isPowermeter());
        assertFalse(group.isHeatingThermostat());

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
        assertEquals(new BigDecimal("100"), model.getBattery());
        assertEquals(HeatingModel.BATTERY_OFF, model.getBatterylow());
        assertEquals(MODE_AUTO, model.getMode());
        assertEquals(MODE_COMFORT, model.getRadiatorMode());

        assertNotNull(model.getNextchange());
        assertEquals(1484341200, model.getNextchange().getEndperiod());
        assertEquals(new BigDecimal(28), model.getNextchange().getTchange());
    }
}
