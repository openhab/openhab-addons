/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.openwebnet.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.core.thing.Bridge;
import org.openwebnet4j.message.Lighting;
import org.openwebnet4j.message.Where;
import org.openwebnet4j.message.WhereLightAutom;
import org.openwebnet4j.message.WhereZigBee;
import org.openwebnet4j.message.Who;

/**
 * Test class for {@link OpenWebNetBridgeHandler#ownID} and ThingID calculation using {@link OpenWebNetBridgeHandler}
 * methods: normalizeWhere(), ownIdFromWhoWhere(), ownIdFromMessage(), thingIdFromWhere()
 *
 * @author Massimo Valla - Initial contribution
 */
@NonNullByDefault
public class OwnIdTest {

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
     * BUS TempSensor       500             500                 4.500           500
     * BUS Energy           51              51                  18.51           51
     * BUS CEN              51              51                  15.51           51
     * BUS CEN+             212             212                 25.212          212
     * BUS DryContact       399             399                 25.399          399
     *
     */
// @formatter:on

    public enum TEST {
        zb_switch(new WhereZigBee("789309801#9"), Who.fromValue(1), "789309800h9", "1.789309800h9", "789309800h9"),
        zb_switch_2u_1(new WhereZigBee("789301201#9"), Who.fromValue(1), "789301200h9", "1.789301200h9", "789301200h9"),
        zb_switch_2u_2(new WhereZigBee("789301202#9"), Who.fromValue(1), "789301200h9", "1.789301200h9", "789301200h9"),
        bus_switch(new WhereLightAutom("51"), Who.fromValue(1), "51", "1.51", "51"),
        bus_localbus(new WhereLightAutom("25#4#01"), Who.fromValue(1), "25h4h01", "1.25h4h01", "25h4h01");
        // bus_thermo("#1", "4", "1", "4.1", "1"),
        // bus_tempSensor("500", "4", "500", "4.500", "500"),
        // bus_energy("51", "18", "51", "18.51", "51");

        public final Where where;
        public final Who who;
        public final String norm, ownId, thingId;

        private TEST(Where where, Who who, String norm, String ownId, String thingId) {
            this.where = where;
            this.who = who;
            this.norm = norm;
            this.ownId = ownId;
            this.thingId = thingId;
        }
    }

    @Test
    public void testOwnId() {
        Bridge mockBridge = mock(Bridge.class);
        OpenWebNetBridgeHandler brH = new OpenWebNetBridgeHandler(mockBridge);

        for (int i = 0; i < TEST.values().length; i++) {
            TEST test = TEST.values()[i];
            // System.out.println("testing where=" + test.where);
            assertEquals(test.norm, brH.normalizeWhere(test.where));
            assertEquals(test.ownId, brH.ownIdFromWhoWhere(test.who, test.where));
            assertEquals(test.ownId, brH.ownIdFromMessage(Lighting.requestTurnOn(test.where.value())));
            assertEquals(test.thingId, brH.thingIdFromWhere(test.where));
        }
    }
}
