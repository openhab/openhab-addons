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
 * Test class for {@link OpenWebNetBridgeHandler#ownID} and ThingID
 * calculation using {@link OpenWebNetBridgeHandler}
 * methods: normalizeWhere(), ownIdFromWhoWhere(), ownIdFromMessage(), thingIdFromWhere()
 *
 * @author Massimo Valla - Initial contribution
 * @author Andrea Conte - Energy management
 * @author Giovanni Fabiani - AAuxiliary message support
 */
@NonNullByDefault
public class OwnIdTest {

    private final Logger logger = LoggerFactory.getLogger(OwnIdTest.class);

 // @formatter:off
    /**
     *
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
     * BUS AUX                4               4                    9.4            4
     */
// @formatter:on

    public enum TEST {
        // @formatter:off
        zb_switch(new WhereZigBee("789309801#9"), Who.fromValue(1), "*1*1*789309801#9##", "789309800h9", "1.789309800h9", "789309800h9"),
        zb_switch_2u_1(new WhereZigBee("789301201#9"), Who.fromValue(1), "*1*1*789301201#9##", "789301200h9", "1.789301200h9", "789301200h9"),
        zb_switch_2u_2(new WhereZigBee("789301202#9"), Who.fromValue(1), "*1*1*789301202#9##", "789301200h9", "1.789301200h9", "789301200h9"),
        bus_switch(new WhereLightAutom("51"), Who.fromValue(1), "*1*1*51##", "51", "1.51", "51"),
        bus_localbus(new WhereLightAutom("25#4#01"), Who.fromValue(1), "*1*1*25#4#01##", "25h4h01", "1.25h4h01", "25h4h01"),
        bus_autom(new WhereLightAutom("93"), Who.fromValue(2), "*2*0*93##", "93", "2.93", "93"),
        bus_thermo_via_cu(new WhereThermo("#1"), Who.fromValue(4),"*#4*#1*0*0020##" ,"1", "4.1", "1"),
        bus_thermo(new WhereThermo("1"), Who.fromValue(4),"*#4*1*0*0020##" , "1", "4.1", "1"),
        bus_thermo_act(new WhereThermo("1#2"), Who.fromValue(4),"*#4*1#2*20*0##" ,"1", "4.1", "1"),
        bus_tempSensor(new WhereThermo("500"), Who.fromValue(4), "*#4*500*15*1*0020*0001##", "500", "4.500", "500"),
        bus_energy(new WhereEnergyManagement("51"), Who.fromValue(18), "*#18*51*113##", "51", "18.51", "51"),
        bus_cen(new WhereCEN("51"), Who.fromValue(15), "*15*31*51##", "51", "15.51", "51"),
        bus_cen_plus(new WhereCEN("212"), Who.fromValue(25), "*25*21#31*212##", "212", "25.212", "212"),
        bus_drycontact(new WhereCEN("399"), Who.fromValue(25), "*25*32#1*399##", "399", "25.399", "399"),
        bus_aux(new WhereAuxiliary("4"), Who.fromValue(9), "*9*1*4##","4","9.4","4");


        // @formatter:on

        private final Logger logger = LoggerFactory.getLogger(TEST.class);

        public final Where where;
        public final Who who;
        public final @Nullable BaseOpenMessage msg;
        public final String norm, ownId, thingId;

        private TEST(Where where, Who who, String msg, String norm, String ownId, String thingId) {
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
            logger.info("testing where={}", test.where);
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
