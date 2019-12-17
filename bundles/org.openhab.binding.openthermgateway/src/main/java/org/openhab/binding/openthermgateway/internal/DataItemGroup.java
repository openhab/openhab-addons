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
package org.openhab.binding.openthermgateway.internal;

import java.util.HashMap;

/**
 * @author Arjen Korevaar - Initial contribution
 */
public class DataItemGroup {
    private int id;
    private DataItem[] dataItems;

    public int getID() {
        return id;
    }

    public void setID(int id) {
        this.id = id;
    }

    public DataItem[] getDataItems() {
        return dataItems;
    }

    public void setDataItem(DataItem[] dataItems) {
        this.dataItems = dataItems;
    }

    public DataItemGroup(int id, DataItem... dataItems) {
        this.id = id;
        this.dataItems = dataItems;
    }

    public static final HashMap<Integer, DataItem[]> dataItemGroups = createDataItemGroups();

    private static HashMap<Integer, DataItem[]> createDataItemGroups() {
        HashMap<Integer, DataItem[]> g = new HashMap<Integer, DataItem[]>();

        g.put(0, new DataItem[] { new DataItem(0, Msg.Read, ByteType.HighByte, DataType.Flags, 0, "ch_enable"),
                new DataItem(0, Msg.Read, ByteType.HighByte, DataType.Flags, 1, "dhw_enable"),
                new DataItem(0, Msg.Read, ByteType.HighByte, DataType.Flags, 2, "cooling_enabled"),
                new DataItem(0, Msg.Read, ByteType.HighByte, DataType.Flags, 3, "otc_active"),
                new DataItem(0, Msg.Read, ByteType.HighByte, DataType.Flags, 4, "ch2_enable"),
                new DataItem(0, Msg.Read, ByteType.HighByte, DataType.Flags, 5, "0x00:5"),
                new DataItem(0, Msg.Read, ByteType.HighByte, DataType.Flags, 6, "0x00:6"),
                new DataItem(0, Msg.Read, ByteType.HighByte, DataType.Flags, 7, "0x00:7"),
                new DataItem(0, Msg.Read, ByteType.LowByte, DataType.Flags, 0, "fault"),
                new DataItem(0, Msg.Read, ByteType.LowByte, DataType.Flags, 1, "ch_mode"),
                new DataItem(0, Msg.Read, ByteType.LowByte, DataType.Flags, 2, "dhw_mode"),
                new DataItem(0, Msg.Read, ByteType.LowByte, DataType.Flags, 3, "flame"),
                new DataItem(0, Msg.Read, ByteType.LowByte, DataType.Flags, 4, "cooling"),
                new DataItem(0, Msg.Read, ByteType.LowByte, DataType.Flags, 5, "ch2E"),
                new DataItem(0, Msg.Read, ByteType.LowByte, DataType.Flags, 6, "diag"),
                new DataItem(0, Msg.Read, ByteType.LowByte, DataType.Flags, 7, "0x00:7") });
        g.put(1, new DataItem[] { new DataItem(1, Msg.Write, ByteType.Both, DataType.Float, 0, "controlsetpoint") });
        g.put(2, new DataItem[] { new DataItem(2, Msg.Write, ByteType.HighByte, DataType.Flags, 0, "0x02:0"),
                new DataItem(2, Msg.Write, ByteType.HighByte, DataType.Flags, 1, "0x02:1"),
                new DataItem(2, Msg.Write, ByteType.HighByte, DataType.Flags, 2, "0x02:2"),
                new DataItem(2, Msg.Write, ByteType.HighByte, DataType.Flags, 3, "0x02:3"),
                new DataItem(2, Msg.Write, ByteType.HighByte, DataType.Flags, 4, "0x02:4"),
                new DataItem(2, Msg.Write, ByteType.HighByte, DataType.Flags, 5, "0x02:5"),
                new DataItem(2, Msg.Write, ByteType.HighByte, DataType.Flags, 6, "0x02:6"),
                new DataItem(2, Msg.Write, ByteType.HighByte, DataType.Flags, 7, "0x02:7"),
                new DataItem(2, Msg.Write, ByteType.LowByte, DataType.Uint8, 0, "mastermemberid") });
        g.put(3, new DataItem[] { new DataItem(3, Msg.Read, ByteType.HighByte, DataType.Flags, 0, "dhwpresent"),
                new DataItem(3, Msg.Read, ByteType.HighByte, DataType.Flags, 1, "controltype"),
                new DataItem(3, Msg.Read, ByteType.HighByte, DataType.Flags, 2, "coolingsupport"),
                new DataItem(3, Msg.Read, ByteType.HighByte, DataType.Flags, 3, "dhwconfig"),
                new DataItem(3, Msg.Read, ByteType.HighByte, DataType.Flags, 4, "masterlowoff"),
                new DataItem(3, Msg.Read, ByteType.HighByte, DataType.Flags, 5, "ch2present"),
                new DataItem(3, Msg.Read, ByteType.HighByte, DataType.Flags, 6, "0x03:6"),
                new DataItem(3, Msg.Read, ByteType.HighByte, DataType.Flags, 7, "0x03:7"),
                new DataItem(3, Msg.Read, ByteType.LowByte, DataType.Uint8, 0, "slavememberid") });
        g.put(4, new DataItem[] { new DataItem(4, Msg.Write, ByteType.HighByte, DataType.Uint8, 0, "commandcode"),
                new DataItem(4, Msg.Read, ByteType.LowByte, DataType.Uint8, 0, "commandresponse") });
        g.put(5, new DataItem[] { new DataItem(5, Msg.Read, ByteType.HighByte, DataType.Flags, 0, "servicerequest"),
                new DataItem(5, Msg.Read, ByteType.HighByte, DataType.Flags, 1, "lockout-reset"),
                new DataItem(5, Msg.Read, ByteType.HighByte, DataType.Flags, 2, "lowwaterpress"),
                new DataItem(5, Msg.Read, ByteType.HighByte, DataType.Flags, 3, "gasflamefault"),
                new DataItem(5, Msg.Read, ByteType.HighByte, DataType.Flags, 4, "airpressfault"),
                new DataItem(5, Msg.Read, ByteType.HighByte, DataType.Flags, 5, "waterovtemp"),
                new DataItem(5, Msg.Read, ByteType.HighByte, DataType.Flags, 6, "0x05:6"),
                new DataItem(5, Msg.Read, ByteType.HighByte, DataType.Flags, 7, "0x05:7"),
                new DataItem(5, Msg.Read, ByteType.LowByte, DataType.Uint8, 0, "oemfaultcode") });
        g.put(6, new DataItem[] { new DataItem(6, Msg.Read, ByteType.LowByte, DataType.Flags, 0, "0x06:l0"),
                new DataItem(6, Msg.Read, ByteType.LowByte, DataType.Flags, 1, "0x06:l1"),
                new DataItem(6, Msg.Read, ByteType.LowByte, DataType.Flags, 2, "0x06:l2"),
                new DataItem(6, Msg.Read, ByteType.LowByte, DataType.Flags, 3, "0x06:l3"),
                new DataItem(6, Msg.Read, ByteType.LowByte, DataType.Flags, 4, "0x06:l4"),
                new DataItem(6, Msg.Read, ByteType.LowByte, DataType.Flags, 5, "0x06:l5"),
                new DataItem(6, Msg.Read, ByteType.LowByte, DataType.Flags, 6, "0x06:l6"),
                new DataItem(6, Msg.Read, ByteType.LowByte, DataType.Flags, 7, "0x06:l7"),
                new DataItem(6, Msg.Read, ByteType.HighByte, DataType.Flags, 0, "0x06:h0"),
                new DataItem(6, Msg.Read, ByteType.HighByte, DataType.Flags, 1, "0x06:h1"),
                new DataItem(6, Msg.Read, ByteType.HighByte, DataType.Flags, 2, "0x06:h2"),
                new DataItem(6, Msg.Read, ByteType.HighByte, DataType.Flags, 3, "0x06:h3"),
                new DataItem(6, Msg.Read, ByteType.HighByte, DataType.Flags, 4, "0x06:h4"),
                new DataItem(6, Msg.Read, ByteType.HighByte, DataType.Flags, 5, "0x06:h5"),
                new DataItem(6, Msg.Read, ByteType.HighByte, DataType.Flags, 6, "0x06:h6"),
                new DataItem(6, Msg.Read, ByteType.HighByte, DataType.Flags, 7, "0x06:h7") });
        g.put(7, new DataItem[] { new DataItem(7, Msg.Write, ByteType.Both, DataType.Float, 0, "0x07") });
        g.put(8, new DataItem[] { new DataItem(8, Msg.Write, ByteType.Both, DataType.Float, 0, "controlsetpoint2") });
        g.put(9, new DataItem[] { new DataItem(9, Msg.Read, ByteType.Both, DataType.Float, 0, "overridesetpoint") });
        g.put(10, new DataItem[] { new DataItem(10, Msg.Write, ByteType.HighByte, DataType.Uint8, 0, "0x0a:h"),
                new DataItem(10, Msg.Write, ByteType.LowByte, DataType.Uint8, 0, "0x0a:l") });
        g.put(11, new DataItem[] { new DataItem(11, Msg.ReadWrite, ByteType.HighByte, DataType.Uint8, 0, "tspindex"),
                new DataItem(11, Msg.ReadWrite, ByteType.LowByte, DataType.Uint8, 0, "tspvalue") });
        g.put(12, new DataItem[] { new DataItem(12, Msg.Read, ByteType.HighByte, DataType.Uint8, 0, "0x0c:h"),
                new DataItem(12, Msg.Read, ByteType.LowByte, DataType.Uint8, 0, "0x0c:l") });
        g.put(13, new DataItem[] { new DataItem(13, Msg.Read, ByteType.HighByte, DataType.Uint8, 0, "0x0d:h"),
                new DataItem(13, Msg.Read, ByteType.LowByte, DataType.Uint8, 0, "0x0d:l") });
        g.put(14, new DataItem[] { new DataItem(14, Msg.Read, ByteType.LowByte, DataType.Float, 0, "maxrelmdulevel") });
        g.put(15, new DataItem[] { new DataItem(15, Msg.Read, ByteType.HighByte, DataType.Uint8, 0, "maxcapkw"),
                new DataItem(15, Msg.Read, ByteType.LowByte, DataType.Uint8, 0, "maxcapprc") });
        g.put(16, new DataItem[] { new DataItem(16, Msg.Write, ByteType.Both, DataType.Float, 0, "roomsetpoint") });
        g.put(17, new DataItem[] { new DataItem(17, Msg.Read, ByteType.Both, DataType.Float, 0, "modulevel") });
        g.put(18, new DataItem[] { new DataItem(18, Msg.Read, ByteType.Both, DataType.Float, 0, "waterpressure") });
        g.put(19, new DataItem[] { new DataItem(19, Msg.Read, ByteType.Both, DataType.Float, 0, "dhwflow") });
        g.put(20, new DataItem[] { new DataItem(20, Msg.ReadWrite, ByteType.Both, DataType.DoWToD, 0, "dowtod") });
        g.put(21, new DataItem[] { new DataItem(21, Msg.ReadWrite, ByteType.HighByte, DataType.Uint8, 0, "month"),
                new DataItem(21, Msg.ReadWrite, ByteType.LowByte, DataType.Uint8, 0, "dom") });
        g.put(22, new DataItem[] { new DataItem(22, Msg.ReadWrite, ByteType.LowByte, DataType.Uint8, 0, "year") });
        g.put(23, new DataItem[] { new DataItem(23, Msg.Write, ByteType.Both, DataType.Float, 0, "setpointch2") });
        g.put(24, new DataItem[] { new DataItem(24, Msg.Write, ByteType.Both, DataType.Float, 0, "roomtemp") });
        g.put(25, new DataItem[] { new DataItem(25, Msg.Read, ByteType.Both, DataType.Float, 0, "flowtemp") });
        g.put(26, new DataItem[] { new DataItem(26, Msg.Read, ByteType.Both, DataType.Float, 0, "dhwtemp") });
        g.put(27, new DataItem[] { new DataItem(27, Msg.Read, ByteType.Both, DataType.Float, 0, "outsidetemp") });
        g.put(28, new DataItem[] { new DataItem(28, Msg.Read, ByteType.Both, DataType.Float, 0, "returntemp") });
        g.put(29, new DataItem[] { new DataItem(29, Msg.Read, ByteType.Both, DataType.Float, 0, "solstortemp") });
        g.put(30, new DataItem[] { new DataItem(30, Msg.Read, ByteType.Both, DataType.Float, 0, "solcolltemp") });
        g.put(31, new DataItem[] { new DataItem(31, Msg.Read, ByteType.Both, DataType.Float, 0, "flowtemp2") });
        g.put(32, new DataItem[] { new DataItem(32, Msg.Read, ByteType.Both, DataType.Float, 0, "dhw2temp") });
        g.put(33, new DataItem[] { new DataItem(33, Msg.Read, ByteType.Both, DataType.Int16, 0, "exhausttemp") });
        g.put(48, new DataItem[] { new DataItem(48, Msg.Read, ByteType.HighByte, DataType.Int8, 0, "tdhwsetu"),
                new DataItem(48, Msg.Read, ByteType.LowByte, DataType.Int8, 0, "tdhwsetl") });
        g.put(49, new DataItem[] { new DataItem(49, Msg.Read, ByteType.HighByte, DataType.Int8, 0, "maxchu"),
                new DataItem(49, Msg.Read, ByteType.LowByte, DataType.Int8, 0, "maxchl") });
        g.put(50, new DataItem[] { new DataItem(50, Msg.Read, ByteType.HighByte, DataType.Int8, 0, "otcu"),
                new DataItem(50, Msg.Read, ByteType.LowByte, DataType.Int8, 0, "otcl") });
        g.put(56, new DataItem[] { new DataItem(56, Msg.ReadWrite, ByteType.Both, DataType.Float, 0, "tdhwset") });
        g.put(57, new DataItem[] { new DataItem(57, Msg.ReadWrite, ByteType.Both, DataType.Float, 0, "tchmax") });
        g.put(58, new DataItem[] { new DataItem(58, Msg.ReadWrite, ByteType.Both, DataType.Float, 0, "otchcratio") });
        g.put(100,
                new DataItem[] { new DataItem(100, Msg.Read, ByteType.HighByte, DataType.Flags, 0, "rof0"),
                        new DataItem(100, Msg.Read, ByteType.HighByte, DataType.Flags, 1, "rof1"),
                        new DataItem(100, Msg.Read, ByteType.HighByte, DataType.Flags, 2, "rof2"),
                        new DataItem(100, Msg.Read, ByteType.HighByte, DataType.Flags, 3, "rof3"),
                        new DataItem(100, Msg.Read, ByteType.HighByte, DataType.Flags, 4, "rof4"),
                        new DataItem(100, Msg.Read, ByteType.HighByte, DataType.Flags, 5, "rof5"),
                        new DataItem(100, Msg.Read, ByteType.HighByte, DataType.Flags, 6, "rof6"),
                        new DataItem(100, Msg.Read, ByteType.HighByte, DataType.Flags, 7, "rof7") });
        g.put(115, new DataItem[] { new DataItem(115, Msg.Read, ByteType.Both, DataType.Uint16, 0, "oemdiagcode") });
        g.put(116, new DataItem[] { new DataItem(116, Msg.Read, ByteType.Both, DataType.Uint16, 0, "burnerstarts") });
        g.put(117, new DataItem[] { new DataItem(117, Msg.Read, ByteType.Both, DataType.Uint16, 0, "chpumpstarts") });
        g.put(118, new DataItem[] { new DataItem(118, Msg.Read, ByteType.Both, DataType.Uint16, 0, "dhwpvstarts") });
        g.put(119,
                new DataItem[] { new DataItem(119, Msg.Read, ByteType.Both, DataType.Uint16, 0, "dhwburnerstarts") });
        g.put(120, new DataItem[] { new DataItem(120, Msg.Read, ByteType.Both, DataType.Uint16, 0, "burnerhours") });
        g.put(121, new DataItem[] { new DataItem(121, Msg.Read, ByteType.Both, DataType.Uint16, 0, "chpumphours") });
        g.put(122, new DataItem[] { new DataItem(122, Msg.Read, ByteType.Both, DataType.Uint16, 0, "dhwpvhours") });
        g.put(123, new DataItem[] { new DataItem(123, Msg.Read, ByteType.Both, DataType.Uint16, 0, "dhwburnerhours") });
        g.put(124,
                new DataItem[] { new DataItem(124, Msg.Write, ByteType.Both, DataType.Float, 0, "masterotversion") });
        g.put(125, new DataItem[] { new DataItem(125, Msg.Read, ByteType.Both, DataType.Float, 0, "slaveotversion") });
        g.put(126,
                new DataItem[] {
                        new DataItem(126, Msg.Write, ByteType.HighByte, DataType.Uint8, 0, "masterproducttype"),
                        new DataItem(126, Msg.Write, ByteType.LowByte, DataType.Uint8, 0, "masterproductversion") });
        g.put(127,
                new DataItem[] { new DataItem(127, Msg.Read, ByteType.HighByte, DataType.Uint8, 0, "slaveproducttype"),
                        new DataItem(127, Msg.Read, ByteType.LowByte, DataType.Uint8, 0, "slaveproductversion") });

        return g;
    }
}
