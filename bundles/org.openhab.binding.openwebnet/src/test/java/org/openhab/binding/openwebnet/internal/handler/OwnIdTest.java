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
package org.openhab.binding.openwebnet.internal.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.openhab.core.thing.Bridge;
import org.openwebnet4j.message.BaseOpenMessage;
import org.openwebnet4j.message.FrameException;
import org.openwebnet4j.message.Where;
import org.openwebnet4j.message.WhereAlarm;
import org.openwebnet4j.message.WhereAuxiliary;
import org.openwebnet4j.message.WhereCEN;
import org.openwebnet4j.message.WhereEnergyManagement;
import org.openwebnet4j.message.WhereLightAutom;
import org.openwebnet4j.message.WhereThermo;
import org.openwebnet4j.message.WhereZigBee;
import org.openwebnet4j.message.Who;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test class for {@link OpenWebNetBridgeHandler#ownID} and ThingID calculation
 * using {@link OpenWebNetBridgeHandler} methods: normalizeWhere(),
 * ownIdFromWhoWhere(), ownIdFromMessage(), thingIdFromWhere()
 *
 * @author Massimo Valla - Initial contribution, updates
 * @author Andrea Conte - Energy management
 * @author Giovanni Fabiani - Auxiliary message support
 */
@NonNullByDefault
public class OwnIdTest {

    private final Logger logger = LoggerFactory.getLogger(OwnIdTest.class);

 // @formatter:off
    /**
     *                                      deviceWhere
     *                                      (DevAddrParam)
     * TYPE                 WHERE           = normalizeWhere()  ownId           ThingID
     * ---------------------------------------------------------------------------------
     * Zigbee Switch        789309801#9     789309800h9         1.789309800h9   789309800h9
     * Zigbee Switch_2u u1  789301201#9     789301200h9         1.789309800h9   789309800h9
     * Zigbee Switch_2u u2  789301202#9     789301200h9         1.789309800h9   789309800h9
     * BUS Switch           51              51                  1.51            51
     * BUS Local Bus        25#4#01         25h4h01             1.25h4h01       25h4h01
     * BUS Autom            93              93                  2.93            93
     * BUS Thermo           #1 or 1         1                   4.1             1
     * BUS Thermo actuator  1#2             1                   4.1             1
     * BUS TempSensor       500             500                 4.500           500
     * BUS Energy           51              51                  18.51           51
     * BUS CEN              51              51                  15.51           51
     * BUS CEN+             212             212                 25.212          212
     * BUS DryContact       399             399                 25.399          399
     * BUS AUX              4               4                   9.4             4
     * BUS Scenario         05              05                  0.05            05
     * BUS Alarm Zone       #2 or 2         2                   5.2             2
     * BUS Alarm silent     0               0                   5.0             0
     * BUS Alarm system     null            0                   5.0             0
     */
// @formatter:on

    public enum TEST {
        // @formatter:off
        // msg, who, where, normW, ownId, thingId
        zb_switch("*1*1*789309801#9##", Who.fromValue(1), new WhereZigBee("789309801#9"), "789309800h9", "1.789309800h9", "789309800h9"),
        zb_switch_2u_1("*1*1*789301201#9##", Who.fromValue(1), new WhereZigBee("789301201#9"), "789301200h9", "1.789301200h9", "789301200h9"),
        zb_switch_2u_2("*1*1*789301202#9##", Who.fromValue(1),    new WhereZigBee("789301202#9"), "789301200h9", "1.789301200h9", "789301200h9"),
        bus_switch("*1*1*51##", Who.fromValue(1), new WhereLightAutom("51"),"51", "1.51", "51"),
        bus_localbus("*1*1*25#4#01##",  Who.fromValue(1), new WhereLightAutom("25#4#01"), "25h4h01", "1.25h4h01", "25h4h01"),
        bus_autom("*2*0*93##",Who.fromValue(2),  new WhereLightAutom("93"), "93", "2.93", "93"),
        bus_thermo_via_cu("*#4*#1*0*0020##",  Who.fromValue(4), new WhereThermo("#1"), "1", "4.1", "1"),
        bus_thermo("*#4*1*0*0020##", Who.fromValue(4),  new WhereThermo("1"),  "1", "4.1", "1"),
        bus_thermo_act("*#4*1#2*20*0##",   Who.fromValue(4), new WhereThermo("1#2") ,"1", "4.1", "1"),
        bus_tempSensor("*#4*500*15*1*0020*0001##",Who.fromValue(4),  new WhereThermo("500"), "500", "4.500", "500"),
        bus_energy("*#18*51*113##",   Who.fromValue(18), new WhereEnergyManagement("51"),  "51", "18.51", "51"),
        bus_cen("*15*31*51##", Who.fromValue(15),  new WhereCEN("51"),  "51", "15.51", "51"),
        bus_cen_plus("*25*21#31*212##", Who.fromValue(25),  new WhereCEN("212"), "212", "25.212", "212"),
        bus_drycontact("*25*32#1*399##", Who.fromValue(25),   new WhereCEN("399"), "399", "25.399", "399"),
        bus_aux( "*9*1*4##", Who.fromValue(9),  new WhereAuxiliary("4"),"4","9.4","4"),
        bus_scenario( "*0*2*05##", Who.fromValue(0), new WhereLightAutom("05"), "05","0.05","05"),
        bus_alarm_zh("*#5*#2##",  Who.fromValue(5), new WhereAlarm("#2"),  "2", "5.2", "2"),
        bus_alarm_silent("*5*2*0##",  Who.fromValue(5), new WhereAlarm("0"),  "0", "5.0", "0"),
        bus_alarm_system( "*5*7*##", Who.fromValue(5),new WhereAlarm("0"), "0", "5.0", "0");

        // @formatter:on

        private final Logger logger = LoggerFactory.getLogger(TEST.class);

        public final Where where;
        public final Who who;
        public final @Nullable BaseOpenMessage msg;
        public final String norm, ownId, thingId;

        private TEST(String msg, Who who, Where where, String norm, String ownId, String thingId) {
            this.where = where;
            this.who = who;
            BaseOpenMessage bmsg = null;
            try {
                bmsg = (BaseOpenMessage) BaseOpenMessage.parse(msg);
            } catch (FrameException e) {
                logger.warn("something is wrong in the test table ({}). ownIdFromMessage test will be skipped",
                        e.getMessage());
            }
            this.msg = bmsg;
            this.norm = norm;
            this.ownId = ownId;
            this.thingId = thingId;
        }
    }

    @Test
    public void testOwnId() {
        Bridge mockBridge = mock(Bridge.class);
        OpenWebNetBridgeHandler brH = new OpenWebNetBridgeHandler(mockBridge);
        BaseOpenMessage bmsg;
        for (int i = 0; i < TEST.values().length; i++) {
            TEST test = TEST.values()[i];
            logger.info("testing {} (who={} where={})", test.msg, test.who, test.where);
            assertEquals(test.norm, brH.normalizeWhere(test.where));
            assertEquals(test.ownId, brH.ownIdFromWhoWhere(test.who, test.where));
            bmsg = test.msg;
            if (bmsg != null) {
                assertEquals(test.ownId, brH.ownIdFromMessage(bmsg));
            }
            assertEquals(test.thingId, brH.thingIdFromWhere(test.where));
        }
    }
}
