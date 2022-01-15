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
package org.openhab.binding.openthermgateway.internal;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;

/**
 * The {@link DataItemGroup} represents a list of all possible DataItem messages within the OpenTherm specification.
 *
 * @author Arjen Korevaar - Initial contribution
 */
@NonNullByDefault
public class DataItemGroup {

    public static final Map<Integer, DataItem[]> dataItemGroups = createDataItemGroups();

    private static Map<Integer, DataItem[]> createDataItemGroups() {
        HashMap<Integer, DataItem[]> g = new HashMap<>();

        g.put(0, new DataItem[] {
                new DataItem(0, Msg.READ, ByteType.HIGHBYTE, DataType.FLAGS, 0, "ch_enable", CodeType.B),
                new DataItem(0, Msg.READ, ByteType.HIGHBYTE, DataType.FLAGS, 0, "ch_enablerequested", CodeType.T),
                new DataItem(0, Msg.READ, ByteType.HIGHBYTE, DataType.FLAGS, 0, "ch_enableoverride", CodeType.R),
                new DataItem(0, Msg.READ, ByteType.HIGHBYTE, DataType.FLAGS, 1, "dhw_enable", CodeType.B),
                new DataItem(0, Msg.READ, ByteType.HIGHBYTE, DataType.FLAGS, 2, "cooling_enabled", CodeType.B),
                new DataItem(0, Msg.READ, ByteType.HIGHBYTE, DataType.FLAGS, 3, "otc_active", CodeType.B),
                new DataItem(0, Msg.READ, ByteType.HIGHBYTE, DataType.FLAGS, 4, "ch2_enable", CodeType.B),
                new DataItem(0, Msg.READ, ByteType.HIGHBYTE, DataType.FLAGS, 4, "ch2_enablerequested", CodeType.T),
                new DataItem(0, Msg.READ, ByteType.HIGHBYTE, DataType.FLAGS, 4, "ch2_enableoverride", CodeType.R),
                new DataItem(0, Msg.READ, ByteType.HIGHBYTE, DataType.FLAGS, 5, "0x00:5", CodeType.B),
                new DataItem(0, Msg.READ, ByteType.HIGHBYTE, DataType.FLAGS, 6, "0x00:6", CodeType.B),
                new DataItem(0, Msg.READ, ByteType.HIGHBYTE, DataType.FLAGS, 7, "0x00:7", CodeType.B),
                new DataItem(0, Msg.READ, ByteType.LOWBYTE, DataType.FLAGS, 0, "fault", CodeType.B),
                new DataItem(0, Msg.READ, ByteType.LOWBYTE, DataType.FLAGS, 1, "ch_mode", CodeType.B),
                new DataItem(0, Msg.READ, ByteType.LOWBYTE, DataType.FLAGS, 2, "dhw_mode", CodeType.B),
                new DataItem(0, Msg.READ, ByteType.LOWBYTE, DataType.FLAGS, 3, "flame", CodeType.B),
                new DataItem(0, Msg.READ, ByteType.LOWBYTE, DataType.FLAGS, 4, "cooling", CodeType.B),
                new DataItem(0, Msg.READ, ByteType.LOWBYTE, DataType.FLAGS, 5, "ch2E", CodeType.B),
                new DataItem(0, Msg.READ, ByteType.LOWBYTE, DataType.FLAGS, 6, "diag", CodeType.B),
                new DataItem(0, Msg.READ, ByteType.LOWBYTE, DataType.FLAGS, 7, "0x00:7", CodeType.B) });
        g.put(1, new DataItem[] {
                new DataItem(1, Msg.WRITE, ByteType.BOTH, DataType.FLOAT, 0, "controlsetpoint", SIUnits.CELSIUS,
                        CodeType.B),
                new DataItem(1, Msg.WRITE, ByteType.BOTH, DataType.FLOAT, 0, "controlsetpointrequested",
                        SIUnits.CELSIUS, CodeType.T),
                new DataItem(1, Msg.WRITE, ByteType.BOTH, DataType.FLOAT, 0, "controlsetpointoverride", SIUnits.CELSIUS,
                        CodeType.R) });
        g.put(2, new DataItem[] { new DataItem(2, Msg.WRITE, ByteType.HIGHBYTE, DataType.FLAGS, 0, "0x02:0"),
                new DataItem(2, Msg.WRITE, ByteType.HIGHBYTE, DataType.FLAGS, 1, "0x02:1"),
                new DataItem(2, Msg.WRITE, ByteType.HIGHBYTE, DataType.FLAGS, 2, "0x02:2"),
                new DataItem(2, Msg.WRITE, ByteType.HIGHBYTE, DataType.FLAGS, 3, "0x02:3"),
                new DataItem(2, Msg.WRITE, ByteType.HIGHBYTE, DataType.FLAGS, 4, "0x02:4"),
                new DataItem(2, Msg.WRITE, ByteType.HIGHBYTE, DataType.FLAGS, 5, "0x02:5"),
                new DataItem(2, Msg.WRITE, ByteType.HIGHBYTE, DataType.FLAGS, 6, "0x02:6"),
                new DataItem(2, Msg.WRITE, ByteType.HIGHBYTE, DataType.FLAGS, 7, "0x02:7"),
                new DataItem(2, Msg.WRITE, ByteType.LOWBYTE, DataType.UINT8, 0, "mastermemberid") });
        g.put(3, new DataItem[] { new DataItem(3, Msg.READ, ByteType.HIGHBYTE, DataType.FLAGS, 0, "dhwpresent"),
                new DataItem(3, Msg.READ, ByteType.HIGHBYTE, DataType.FLAGS, 1, "controltype"),
                new DataItem(3, Msg.READ, ByteType.HIGHBYTE, DataType.FLAGS, 2, "coolingsupport"),
                new DataItem(3, Msg.READ, ByteType.HIGHBYTE, DataType.FLAGS, 3, "dhwconfig"),
                new DataItem(3, Msg.READ, ByteType.HIGHBYTE, DataType.FLAGS, 4, "masterlowoff"),
                new DataItem(3, Msg.READ, ByteType.HIGHBYTE, DataType.FLAGS, 5, "ch2present"),
                new DataItem(3, Msg.READ, ByteType.HIGHBYTE, DataType.FLAGS, 6, "0x03:6"),
                new DataItem(3, Msg.READ, ByteType.HIGHBYTE, DataType.FLAGS, 7, "0x03:7"),
                new DataItem(3, Msg.READ, ByteType.LOWBYTE, DataType.UINT8, 0, "slavememberid") });
        g.put(4, new DataItem[] { new DataItem(4, Msg.WRITE, ByteType.HIGHBYTE, DataType.UINT8, 0, "commandcode"),
                new DataItem(4, Msg.READ, ByteType.LOWBYTE, DataType.UINT8, 0, "commandresponse") });
        g.put(5, new DataItem[] { new DataItem(5, Msg.READ, ByteType.HIGHBYTE, DataType.FLAGS, 0, "servicerequest"),
                new DataItem(5, Msg.READ, ByteType.HIGHBYTE, DataType.FLAGS, 1, "lockout-reset"),
                new DataItem(5, Msg.READ, ByteType.HIGHBYTE, DataType.FLAGS, 2, "lowwaterpress"),
                new DataItem(5, Msg.READ, ByteType.HIGHBYTE, DataType.FLAGS, 3, "gasflamefault"),
                new DataItem(5, Msg.READ, ByteType.HIGHBYTE, DataType.FLAGS, 4, "airpressfault"),
                new DataItem(5, Msg.READ, ByteType.HIGHBYTE, DataType.FLAGS, 5, "waterovtemp"),
                new DataItem(5, Msg.READ, ByteType.HIGHBYTE, DataType.FLAGS, 6, "0x05:6"),
                new DataItem(5, Msg.READ, ByteType.HIGHBYTE, DataType.FLAGS, 7, "0x05:7"),
                new DataItem(5, Msg.READ, ByteType.LOWBYTE, DataType.UINT8, 0, "oemfaultcode") });
        g.put(6, new DataItem[] { new DataItem(6, Msg.READ, ByteType.LOWBYTE, DataType.FLAGS, 0, "0x06:l0"),
                new DataItem(6, Msg.READ, ByteType.LOWBYTE, DataType.FLAGS, 1, "0x06:l1"),
                new DataItem(6, Msg.READ, ByteType.LOWBYTE, DataType.FLAGS, 2, "0x06:l2"),
                new DataItem(6, Msg.READ, ByteType.LOWBYTE, DataType.FLAGS, 3, "0x06:l3"),
                new DataItem(6, Msg.READ, ByteType.LOWBYTE, DataType.FLAGS, 4, "0x06:l4"),
                new DataItem(6, Msg.READ, ByteType.LOWBYTE, DataType.FLAGS, 5, "0x06:l5"),
                new DataItem(6, Msg.READ, ByteType.LOWBYTE, DataType.FLAGS, 6, "0x06:l6"),
                new DataItem(6, Msg.READ, ByteType.LOWBYTE, DataType.FLAGS, 7, "0x06:l7"),
                new DataItem(6, Msg.READ, ByteType.HIGHBYTE, DataType.FLAGS, 0, "0x06:h0"),
                new DataItem(6, Msg.READ, ByteType.HIGHBYTE, DataType.FLAGS, 1, "0x06:h1"),
                new DataItem(6, Msg.READ, ByteType.HIGHBYTE, DataType.FLAGS, 2, "0x06:h2"),
                new DataItem(6, Msg.READ, ByteType.HIGHBYTE, DataType.FLAGS, 3, "0x06:h3"),
                new DataItem(6, Msg.READ, ByteType.HIGHBYTE, DataType.FLAGS, 4, "0x06:h4"),
                new DataItem(6, Msg.READ, ByteType.HIGHBYTE, DataType.FLAGS, 5, "0x06:h5"),
                new DataItem(6, Msg.READ, ByteType.HIGHBYTE, DataType.FLAGS, 6, "0x06:h6"),
                new DataItem(6, Msg.READ, ByteType.HIGHBYTE, DataType.FLAGS, 7, "0x06:h7") });
        g.put(7, new DataItem[] { new DataItem(7, Msg.WRITE, ByteType.BOTH, DataType.FLOAT, 0, "0x07") });
        g.put(8, new DataItem[] {
                new DataItem(8, Msg.WRITE, ByteType.BOTH, DataType.FLOAT, 0, "controlsetpoint2", SIUnits.CELSIUS,
                        CodeType.B),
                new DataItem(8, Msg.WRITE, ByteType.BOTH, DataType.FLOAT, 0, "controlsetpoint2requested",
                        SIUnits.CELSIUS, CodeType.T),
                new DataItem(8, Msg.WRITE, ByteType.BOTH, DataType.FLOAT, 0, "controlsetpoint2override",
                        SIUnits.CELSIUS, CodeType.R) });
        g.put(9, new DataItem[] { new DataItem(9, Msg.READ, ByteType.BOTH, DataType.FLOAT, 0, "overridesetpoint") });
        g.put(10, new DataItem[] { new DataItem(10, Msg.WRITE, ByteType.HIGHBYTE, DataType.UINT8, 0, "0x0a:h"),
                new DataItem(10, Msg.WRITE, ByteType.LOWBYTE, DataType.UINT8, 0, "0x0a:l") });
        g.put(11, new DataItem[] { new DataItem(11, Msg.READWRITE, ByteType.HIGHBYTE, DataType.UINT8, 0, "tspindex"),
                new DataItem(11, Msg.READWRITE, ByteType.LOWBYTE, DataType.UINT8, 0, "tspvalue") });
        g.put(12, new DataItem[] { new DataItem(12, Msg.READ, ByteType.HIGHBYTE, DataType.UINT8, 0, "0x0c:h"),
                new DataItem(12, Msg.READ, ByteType.LOWBYTE, DataType.UINT8, 0, "0x0c:l") });
        g.put(13, new DataItem[] { new DataItem(13, Msg.READ, ByteType.HIGHBYTE, DataType.UINT8, 0, "0x0d:h"),
                new DataItem(13, Msg.READ, ByteType.LOWBYTE, DataType.UINT8, 0, "0x0d:l") });
        g.put(14, new DataItem[] {
                new DataItem(14, Msg.READ, ByteType.LOWBYTE, DataType.FLOAT, 0, "maxrelmdulevel", Units.PERCENT) });
        g.put(15, new DataItem[] { new DataItem(15, Msg.READ, ByteType.HIGHBYTE, DataType.UINT8, 0, "maxcapkw"),
                new DataItem(15, Msg.READ, ByteType.LOWBYTE, DataType.UINT8, 0, "maxcapprc") });
        g.put(16, new DataItem[] {
                new DataItem(16, Msg.WRITE, ByteType.BOTH, DataType.FLOAT, 0, "roomsetpoint", SIUnits.CELSIUS) });
        g.put(17, new DataItem[] {
                new DataItem(17, Msg.READ, ByteType.BOTH, DataType.FLOAT, 0, "modulevel", Units.PERCENT) });
        g.put(18, new DataItem[] {
                new DataItem(18, Msg.READ, ByteType.BOTH, DataType.FLOAT, 0, "waterpressure", Units.BAR) });
        g.put(19, new DataItem[] { new DataItem(19, Msg.READ, ByteType.BOTH, DataType.FLOAT, 0, "dhwflow") });
        g.put(20, new DataItem[] { new DataItem(20, Msg.READWRITE, ByteType.BOTH, DataType.DOWTOD, 0, "dowtod") });
        g.put(21, new DataItem[] { new DataItem(21, Msg.READWRITE, ByteType.HIGHBYTE, DataType.UINT8, 0, "month"),
                new DataItem(21, Msg.READWRITE, ByteType.LOWBYTE, DataType.UINT8, 0, "dom") });
        g.put(22, new DataItem[] { new DataItem(22, Msg.READWRITE, ByteType.LOWBYTE, DataType.UINT8, 0, "year") });
        g.put(23, new DataItem[] { new DataItem(23, Msg.WRITE, ByteType.BOTH, DataType.FLOAT, 0, "setpointch2") });
        g.put(24, new DataItem[] {
                new DataItem(24, Msg.WRITE, ByteType.BOTH, DataType.FLOAT, 0, "roomtemp", SIUnits.CELSIUS) });
        g.put(25, new DataItem[] {
                new DataItem(25, Msg.READ, ByteType.BOTH, DataType.FLOAT, 0, "flowtemp", SIUnits.CELSIUS) });
        g.put(26, new DataItem[] {
                new DataItem(26, Msg.READ, ByteType.BOTH, DataType.FLOAT, 0, "dhwtemp", SIUnits.CELSIUS) });
        g.put(27, new DataItem[] {
                new DataItem(27, Msg.READ, ByteType.BOTH, DataType.FLOAT, 0, "outsidetemp", SIUnits.CELSIUS) });
        g.put(28, new DataItem[] {
                new DataItem(28, Msg.READ, ByteType.BOTH, DataType.FLOAT, 0, "returntemp", SIUnits.CELSIUS) });
        g.put(29, new DataItem[] { new DataItem(29, Msg.READ, ByteType.BOTH, DataType.FLOAT, 0, "solstortemp") });
        g.put(30, new DataItem[] { new DataItem(30, Msg.READ, ByteType.BOTH, DataType.FLOAT, 0, "solcolltemp") });
        g.put(31, new DataItem[] { new DataItem(31, Msg.READ, ByteType.BOTH, DataType.FLOAT, 0, "flowtemp2") });
        g.put(32, new DataItem[] { new DataItem(32, Msg.READ, ByteType.BOTH, DataType.FLOAT, 0, "dhw2temp") });
        g.put(33, new DataItem[] { new DataItem(33, Msg.READ, ByteType.BOTH, DataType.INT16, 0, "exhausttemp") });
        g.put(48, new DataItem[] { new DataItem(48, Msg.READ, ByteType.HIGHBYTE, DataType.INT8, 0, "tdhwsetu"),
                new DataItem(48, Msg.READ, ByteType.LOWBYTE, DataType.INT8, 0, "tdhwsetl") });
        g.put(49, new DataItem[] { new DataItem(49, Msg.READ, ByteType.HIGHBYTE, DataType.INT8, 0, "maxchu"),
                new DataItem(49, Msg.READ, ByteType.LOWBYTE, DataType.INT8, 0, "maxchl") });
        g.put(50, new DataItem[] { new DataItem(50, Msg.READ, ByteType.HIGHBYTE, DataType.INT8, 0, "otcu"),
                new DataItem(50, Msg.READ, ByteType.LOWBYTE, DataType.INT8, 0, "otcl") });
        g.put(56, new DataItem[] {
                new DataItem(56, Msg.READWRITE, ByteType.BOTH, DataType.FLOAT, 0, "tdhwset", SIUnits.CELSIUS) });
        g.put(57, new DataItem[] { new DataItem(57, Msg.READWRITE, ByteType.BOTH, DataType.FLOAT, 0, "tchmax") });
        g.put(58, new DataItem[] { new DataItem(58, Msg.READWRITE, ByteType.BOTH, DataType.FLOAT, 0, "otchcratio") });
        g.put(100,
                new DataItem[] { new DataItem(100, Msg.READ, ByteType.HIGHBYTE, DataType.FLAGS, 0, "rof0"),
                        new DataItem(100, Msg.READ, ByteType.HIGHBYTE, DataType.FLAGS, 1, "rof1"),
                        new DataItem(100, Msg.READ, ByteType.HIGHBYTE, DataType.FLAGS, 2, "rof2"),
                        new DataItem(100, Msg.READ, ByteType.HIGHBYTE, DataType.FLAGS, 3, "rof3"),
                        new DataItem(100, Msg.READ, ByteType.HIGHBYTE, DataType.FLAGS, 4, "rof4"),
                        new DataItem(100, Msg.READ, ByteType.HIGHBYTE, DataType.FLAGS, 5, "rof5"),
                        new DataItem(100, Msg.READ, ByteType.HIGHBYTE, DataType.FLAGS, 6, "rof6"),
                        new DataItem(100, Msg.READ, ByteType.HIGHBYTE, DataType.FLAGS, 7, "rof7") });
        g.put(113, new DataItem[] {
                new DataItem(113, Msg.READ, ByteType.BOTH, DataType.UINT16, 0, "unsuccessfulburnerstarts") });
        g.put(115, new DataItem[] { new DataItem(115, Msg.READ, ByteType.BOTH, DataType.UINT16, 0, "oemdiagcode") });
        g.put(116, new DataItem[] { new DataItem(116, Msg.READ, ByteType.BOTH, DataType.UINT16, 0, "burnerstarts") });
        g.put(117, new DataItem[] { new DataItem(117, Msg.READ, ByteType.BOTH, DataType.UINT16, 0, "chpumpstarts") });
        g.put(118, new DataItem[] { new DataItem(118, Msg.READ, ByteType.BOTH, DataType.UINT16, 0, "dhwpvstarts") });
        g.put(119,
                new DataItem[] { new DataItem(119, Msg.READ, ByteType.BOTH, DataType.UINT16, 0, "dhwburnerstarts") });
        g.put(120, new DataItem[] {
                new DataItem(120, Msg.READ, ByteType.BOTH, DataType.UINT16, 0, "burnerhours", Units.HOUR) });
        g.put(121, new DataItem[] {
                new DataItem(121, Msg.READ, ByteType.BOTH, DataType.UINT16, 0, "chpumphours", Units.HOUR) });
        g.put(122, new DataItem[] {
                new DataItem(122, Msg.READ, ByteType.BOTH, DataType.UINT16, 0, "dhwpvhours", Units.HOUR) });
        g.put(123, new DataItem[] {
                new DataItem(123, Msg.READ, ByteType.BOTH, DataType.UINT16, 0, "dhwburnerhours", Units.HOUR) });
        g.put(124,
                new DataItem[] { new DataItem(124, Msg.WRITE, ByteType.BOTH, DataType.FLOAT, 0, "masterotversion") });
        g.put(125, new DataItem[] { new DataItem(125, Msg.READ, ByteType.BOTH, DataType.FLOAT, 0, "slaveotversion") });
        g.put(126,
                new DataItem[] {
                        new DataItem(126, Msg.WRITE, ByteType.HIGHBYTE, DataType.UINT8, 0, "masterproducttype"),
                        new DataItem(126, Msg.WRITE, ByteType.LOWBYTE, DataType.UINT8, 0, "masterproductversion") });
        g.put(127,
                new DataItem[] { new DataItem(127, Msg.READ, ByteType.HIGHBYTE, DataType.UINT8, 0, "slaveproducttype"),
                        new DataItem(127, Msg.READ, ByteType.LOWBYTE, DataType.UINT8, 0, "slaveproductversion") });

        return g;
    }
}
