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

    public static final Map<Integer, DataItem[]> DATAITEMGROUPS = createDataItemGroups();

    private static Map<Integer, DataItem[]> createDataItemGroups() {
        HashMap<Integer, DataItem[]> g = new HashMap<>();

        g.put(0, new DataItem[] { new FlagDataItem(Msg.READ, ByteType.HIGHBYTE, 0, "ch_enable", CodeType.B),
                new FlagDataItem(Msg.READ, ByteType.HIGHBYTE, 0, "ch_enablerequested", CodeType.T),
                new FlagDataItem(Msg.READ, ByteType.HIGHBYTE, 0, "ch_enableoverride", CodeType.R),
                new FlagDataItem(Msg.READ, ByteType.HIGHBYTE, 1, "dhw_enable", CodeType.B),
                new FlagDataItem(Msg.READ, ByteType.HIGHBYTE, 2, "cooling_enabled", CodeType.B),
                new FlagDataItem(Msg.READ, ByteType.HIGHBYTE, 3, "otc_active", CodeType.B),
                new FlagDataItem(Msg.READ, ByteType.HIGHBYTE, 4, "ch2_enable", CodeType.B),
                new FlagDataItem(Msg.READ, ByteType.HIGHBYTE, 4, "ch2_enablerequested", CodeType.T),
                new FlagDataItem(Msg.READ, ByteType.HIGHBYTE, 4, "ch2_enableoverride", CodeType.R),
                new FlagDataItem(Msg.READ, ByteType.HIGHBYTE, 5, "0x00:5", CodeType.B),
                new FlagDataItem(Msg.READ, ByteType.HIGHBYTE, 6, "0x00:6", CodeType.B),
                new FlagDataItem(Msg.READ, ByteType.HIGHBYTE, 7, "0x00:7", CodeType.B),
                new FlagDataItem(Msg.READ, ByteType.LOWBYTE, 0, "fault", CodeType.B),
                new FlagDataItem(Msg.READ, ByteType.LOWBYTE, 1, "ch_mode", CodeType.B),
                new FlagDataItem(Msg.READ, ByteType.LOWBYTE, 2, "dhw_mode", CodeType.B),
                new FlagDataItem(Msg.READ, ByteType.LOWBYTE, 3, "flame", CodeType.B),
                new FlagDataItem(Msg.READ, ByteType.LOWBYTE, 4, "cooling", CodeType.B),
                new FlagDataItem(Msg.READ, ByteType.LOWBYTE, 5, "ch2E", CodeType.B),
                new FlagDataItem(Msg.READ, ByteType.LOWBYTE, 6, "diag", CodeType.B),
                new FlagDataItem(Msg.READ, ByteType.LOWBYTE, 7, "0x00:7", CodeType.B) });
        g.put(1, new DataItem[] { new FloatDataItem(Msg.WRITE, "controlsetpoint", SIUnits.CELSIUS, CodeType.B),
                new FloatDataItem(Msg.WRITE, "controlsetpointrequested", SIUnits.CELSIUS, CodeType.T),
                new FloatDataItem(Msg.WRITE, "controlsetpointoverride", SIUnits.CELSIUS, CodeType.R) });
        g.put(2, new DataItem[] { new FlagDataItem(Msg.WRITE, ByteType.HIGHBYTE, 0, "0x02:0"),
                new FlagDataItem(Msg.WRITE, ByteType.HIGHBYTE, 1, "0x02:1"),
                new FlagDataItem(Msg.WRITE, ByteType.HIGHBYTE, 2, "0x02:2"),
                new FlagDataItem(Msg.WRITE, ByteType.HIGHBYTE, 3, "0x02:3"),
                new FlagDataItem(Msg.WRITE, ByteType.HIGHBYTE, 4, "0x02:4"),
                new FlagDataItem(Msg.WRITE, ByteType.HIGHBYTE, 5, "0x02:5"),
                new FlagDataItem(Msg.WRITE, ByteType.HIGHBYTE, 6, "0x02:6"),
                new FlagDataItem(Msg.WRITE, ByteType.HIGHBYTE, 7, "0x02:7"),
                new UIntDataItem(Msg.WRITE, ByteType.LOWBYTE, "mastermemberid") });
        g.put(3, new DataItem[] { new FlagDataItem(Msg.READ, ByteType.HIGHBYTE, 0, "dhwpresent"),
                new FlagDataItem(Msg.READ, ByteType.HIGHBYTE, 1, "controltype"),
                new FlagDataItem(Msg.READ, ByteType.HIGHBYTE, 2, "coolingsupport"),
                new FlagDataItem(Msg.READ, ByteType.HIGHBYTE, 3, "dhwconfig"),
                new FlagDataItem(Msg.READ, ByteType.HIGHBYTE, 4, "masterlowoff"),
                new FlagDataItem(Msg.READ, ByteType.HIGHBYTE, 5, "ch2present"),
                new FlagDataItem(Msg.READ, ByteType.HIGHBYTE, 6, "0x03:6"),
                new FlagDataItem(Msg.READ, ByteType.HIGHBYTE, 7, "0x03:7"),
                new UIntDataItem(Msg.READ, ByteType.LOWBYTE, "slavememberid") });
        g.put(4, new DataItem[] { new UIntDataItem(Msg.WRITE, ByteType.HIGHBYTE, "commandcode"),
                new UIntDataItem(Msg.READ, ByteType.LOWBYTE, "commandresponse") });
        g.put(5, new DataItem[] { new FlagDataItem(Msg.READ, ByteType.HIGHBYTE, 0, "servicerequest"),
                new FlagDataItem(Msg.READ, ByteType.HIGHBYTE, 1, "lockout-reset"),
                new FlagDataItem(Msg.READ, ByteType.HIGHBYTE, 2, "lowwaterpress"),
                new FlagDataItem(Msg.READ, ByteType.HIGHBYTE, 3, "gasflamefault"),
                new FlagDataItem(Msg.READ, ByteType.HIGHBYTE, 4, "airpressfault"),
                new FlagDataItem(Msg.READ, ByteType.HIGHBYTE, 5, "waterovtemp"),
                new FlagDataItem(Msg.READ, ByteType.HIGHBYTE, 6, "0x05:6"),
                new FlagDataItem(Msg.READ, ByteType.HIGHBYTE, 7, "0x05:7"),
                new IntDataItem(Msg.READ, ByteType.LOWBYTE, "oemfaultcode") });
        g.put(6, new DataItem[] { new FlagDataItem(Msg.READ, ByteType.HIGHBYTE, 0, "0x06:h0"),
                new FlagDataItem(Msg.READ, ByteType.HIGHBYTE, 1, "0x06:h1"),
                new FlagDataItem(Msg.READ, ByteType.HIGHBYTE, 2, "0x06:h2"),
                new FlagDataItem(Msg.READ, ByteType.HIGHBYTE, 3, "0x06:h3"),
                new FlagDataItem(Msg.READ, ByteType.HIGHBYTE, 4, "0x06:h4"),
                new FlagDataItem(Msg.READ, ByteType.HIGHBYTE, 5, "0x06:h5"),
                new FlagDataItem(Msg.READ, ByteType.HIGHBYTE, 6, "0x06:h6"),
                new FlagDataItem(Msg.READ, ByteType.HIGHBYTE, 7, "0x06:h7"),
                new FlagDataItem(Msg.READ, ByteType.LOWBYTE, 0, "0x06:l0"),
                new FlagDataItem(Msg.READ, ByteType.LOWBYTE, 1, "0x06:l1"),
                new FlagDataItem(Msg.READ, ByteType.LOWBYTE, 2, "0x06:l2"),
                new FlagDataItem(Msg.READ, ByteType.LOWBYTE, 3, "0x06:l3"),
                new FlagDataItem(Msg.READ, ByteType.LOWBYTE, 4, "0x06:l4"),
                new FlagDataItem(Msg.READ, ByteType.LOWBYTE, 5, "0x06:l5"),
                new FlagDataItem(Msg.READ, ByteType.LOWBYTE, 6, "0x06:l6"),
                new FlagDataItem(Msg.READ, ByteType.LOWBYTE, 7, "0x06:l7") });
        g.put(7, new DataItem[] { new FloatDataItem(Msg.WRITE, "coolingcontrolsignal") });
        g.put(8, new DataItem[] { new FloatDataItem(Msg.WRITE, "controlsetpoint2", SIUnits.CELSIUS, CodeType.B),
                new FloatDataItem(Msg.WRITE, "controlsetpoint2requested", SIUnits.CELSIUS, CodeType.T),
                new FloatDataItem(Msg.WRITE, "controlsetpoint2override", SIUnits.CELSIUS, CodeType.R) });
        g.put(9, new DataItem[] { new FloatDataItem(Msg.READ, "overridesetpoint") });
        g.put(10, new DataItem[] { new TspFhbSizeDataItem(Msg.WRITE, ByteType.HIGHBYTE, 11, "tspnumber") });
        g.put(11, new DataItem[] { new TspFhbValueDataItem(Msg.READWRITE, "tspentry") });
        g.put(12, new DataItem[] { new TspFhbSizeDataItem(Msg.READ, ByteType.HIGHBYTE, 13, "fhbnumber") });
        g.put(13, new DataItem[] { new TspFhbValueDataItem(Msg.READ, "fhbentry") });
        g.put(14, new DataItem[] { new FloatDataItem(Msg.READ, "maxrelmdulevel", Units.PERCENT) });
        g.put(15, new DataItem[] { new UIntDataItem(Msg.READ, ByteType.HIGHBYTE, "maxcapkw"),
                new UIntDataItem(Msg.READ, ByteType.LOWBYTE, "maxcapprc") });
        g.put(16, new DataItem[] { new FloatDataItem(Msg.WRITE, "roomsetpoint", SIUnits.CELSIUS) });
        g.put(17, new DataItem[] { new FloatDataItem(Msg.READ, "modulevel", Units.PERCENT) });
        g.put(18, new DataItem[] { new FloatDataItem(Msg.READ, "waterpressure", Units.BAR) });
        g.put(19, new DataItem[] { new FloatDataItem(Msg.READ, "dhwflow") });
        g.put(20, new DataItem[] { new UIntDataItem(Msg.READWRITE, ByteType.BOTH, "dowtod") });
        g.put(21, new DataItem[] { new UIntDataItem(Msg.READWRITE, ByteType.HIGHBYTE, "month"),
                new UIntDataItem(Msg.READWRITE, ByteType.LOWBYTE, "day") });
        g.put(22, new DataItem[] { new UIntDataItem(Msg.READWRITE, ByteType.BOTH, "year") });
        g.put(23, new DataItem[] { new FloatDataItem(Msg.WRITE, "setpointch2") });
        g.put(24, new DataItem[] { new FloatDataItem(Msg.WRITE, "roomtemp", SIUnits.CELSIUS) });
        g.put(25, new DataItem[] { new FloatDataItem(Msg.READ, "flowtemp", SIUnits.CELSIUS) });
        g.put(26, new DataItem[] { new FloatDataItem(Msg.READ, "dhwtemp", SIUnits.CELSIUS) });
        g.put(27, new DataItem[] { new FloatDataItem(Msg.READ, "outsidetemp", SIUnits.CELSIUS) });
        g.put(28, new DataItem[] { new FloatDataItem(Msg.READ, "returntemp", SIUnits.CELSIUS) });

        g.put(29, new DataItem[] { new FloatDataItem(Msg.READ, "ss_temperature") });
        g.put(30, new DataItem[] { new FloatDataItem(Msg.READ, "ss_collectortemperature") });

        g.put(31, new DataItem[] { new FloatDataItem(Msg.READ, "flowtemp2") });
        g.put(32, new DataItem[] { new FloatDataItem(Msg.READ, "dhw2temp") });
        g.put(33, new DataItem[] { new UIntDataItem(Msg.READ, ByteType.BOTH, "exhausttemp") });
        g.put(48, new DataItem[] { new IntDataItem(Msg.READ, ByteType.HIGHBYTE, "tdhwsetu"),
                new IntDataItem(Msg.READ, ByteType.LOWBYTE, "tdhwsetl") });
        g.put(49, new DataItem[] { new IntDataItem(Msg.READ, ByteType.HIGHBYTE, "maxchu"),
                new IntDataItem(Msg.READ, ByteType.LOWBYTE, "maxchl") });
        g.put(50, new DataItem[] { new IntDataItem(Msg.READ, ByteType.HIGHBYTE, "otcu"),
                new IntDataItem(Msg.READ, ByteType.LOWBYTE, "otcl") });
        g.put(56, new DataItem[] { new FloatDataItem(Msg.READWRITE, "tdhwset", SIUnits.CELSIUS) });
        g.put(57, new DataItem[] { new FloatDataItem(Msg.READWRITE, "tchmax") });
        g.put(58, new DataItem[] { new FloatDataItem(Msg.READWRITE, "otchcratio") });
        g.put(70,
                new DataItem[] { new FlagDataItem(Msg.READ, ByteType.HIGHBYTE, 0, "vh_ventilationenable"),
                        new FlagDataItem(Msg.READ, ByteType.HIGHBYTE, 1, "vh_bypassposition"),
                        new FlagDataItem(Msg.READ, ByteType.HIGHBYTE, 2, "vh_bypassmode"),
                        new FlagDataItem(Msg.READ, ByteType.HIGHBYTE, 3, "vh_freeventilationmode"),
                        new FlagDataItem(Msg.READ, ByteType.LOWBYTE, 0, "vh_faultindication"),
                        new FlagDataItem(Msg.READ, ByteType.LOWBYTE, 1, "vh_ventilationmode"),
                        new FlagDataItem(Msg.READ, ByteType.LOWBYTE, 2, "vh_bypassstatus"),
                        new FlagDataItem(Msg.READ, ByteType.LOWBYTE, 3, "vh_bypassautomaticstatus"),
                        new FlagDataItem(Msg.READ, ByteType.LOWBYTE, 4, "vh_freeventilationstatus"),
                        new FlagDataItem(Msg.READ, ByteType.LOWBYTE, 5, "vh_filtercheck"),
                        new FlagDataItem(Msg.READ, ByteType.LOWBYTE, 6, "vh_diagnosticindication") });
        g.put(71, new DataItem[] { new UIntDataItem(Msg.WRITE, ByteType.LOWBYTE, "vh_controlsetpoint") });
        g.put(72,
                new DataItem[] { new FlagDataItem(Msg.READ, ByteType.HIGHBYTE, 0, "vh_servicerequest"),
                        new FlagDataItem(Msg.READ, ByteType.HIGHBYTE, 1, "vh_exhaustfanfault"),
                        new FlagDataItem(Msg.READ, ByteType.HIGHBYTE, 2, "vh_inletfanfault"),
                        new FlagDataItem(Msg.READ, ByteType.HIGHBYTE, 3, "vh_frostprotection"),
                        new UIntDataItem(Msg.READ, ByteType.LOWBYTE, "vh_faultcode") });
        g.put(73, new DataItem[] { new UIntDataItem(Msg.READ, ByteType.BOTH, "vh_diagnosticcode") });
        g.put(74,
                new DataItem[] { new FlagDataItem(Msg.READ, ByteType.HIGHBYTE, 0, "vh_systemtype"),
                        new FlagDataItem(Msg.READ, ByteType.HIGHBYTE, 1, "vh_bypass"),
                        new FlagDataItem(Msg.READ, ByteType.HIGHBYTE, 2, "vh_speedcontrol"),
                        new UIntDataItem(Msg.READ, ByteType.LOWBYTE, "vh_memberid") });
        g.put(75, new DataItem[] { new FloatDataItem(Msg.READ, "vh_openthermversion") });
        g.put(76, new DataItem[] { new UIntDataItem(Msg.READ, ByteType.BOTH, "vh_versiontype") });
        g.put(77, new DataItem[] { new UIntDataItem(Msg.READ, ByteType.BOTH, "vh_relativeventilation") });
        g.put(78, new DataItem[] { new UIntDataItem(Msg.READ, ByteType.BOTH, "vh_relativehumidity") });
        g.put(79, new DataItem[] { new UIntDataItem(Msg.READ, ByteType.BOTH, "vh_co2level") });
        g.put(80, new DataItem[] { new FloatDataItem(Msg.READ, "vh_supplyinlettemp") });
        g.put(81, new DataItem[] { new FloatDataItem(Msg.READ, "vh_supplyoutlettemp") });
        g.put(82, new DataItem[] { new FloatDataItem(Msg.READ, "vh_exhaustinlettemp") });
        g.put(83, new DataItem[] { new FloatDataItem(Msg.READ, "vh_exhaustoutlettemp") });
        g.put(84, new DataItem[] { new UIntDataItem(Msg.READ, ByteType.BOTH, "vh_actualexhaustfanspeed") });
        g.put(85, new DataItem[] { new UIntDataItem(Msg.READ, ByteType.BOTH, "vh_actualinletfanspeed") });
        g.put(86, new DataItem[] { new FlagDataItem(Msg.READ, ByteType.HIGHBYTE, 0, "vh_nominalventenable"),
                new FlagDataItem(Msg.READ, ByteType.LOWBYTE, 0, "vh_nominalventrw") });
        g.put(87, new DataItem[] { new UIntDataItem(Msg.READ, ByteType.HIGHBYTE, "vh_nominalventilationvalue") });
        g.put(88, new DataItem[] { new TspFhbSizeDataItem(Msg.READ, ByteType.HIGHBYTE, 89, "vh_tspnumber") });
        g.put(89, new DataItem[] { new TspFhbValueDataItem(Msg.READ, "vh_tspentry") });
        g.put(90, new DataItem[] { new TspFhbSizeDataItem(Msg.READ, ByteType.HIGHBYTE, 91, "vh_fhbnumber") });
        g.put(91, new DataItem[] { new TspFhbValueDataItem(Msg.READ, "vh_fhbentry") });
        g.put(100,
                new DataItem[] { new FlagDataItem(Msg.READ, ByteType.HIGHBYTE, 0, "rof0"),
                        new FlagDataItem(Msg.READ, ByteType.HIGHBYTE, 1, "rof1"),
                        new FlagDataItem(Msg.READ, ByteType.HIGHBYTE, 2, "rof2"),
                        new FlagDataItem(Msg.READ, ByteType.HIGHBYTE, 3, "rof3"),
                        new FlagDataItem(Msg.READ, ByteType.HIGHBYTE, 4, "rof4"),
                        new FlagDataItem(Msg.READ, ByteType.HIGHBYTE, 5, "rof5"),
                        new FlagDataItem(Msg.READ, ByteType.HIGHBYTE, 6, "rof6"),
                        new FlagDataItem(Msg.READ, ByteType.HIGHBYTE, 7, "rof7") });

        g.put(101,
                new DataItem[] { new FlagDataItem(Msg.READ, ByteType.HIGHBYTE, 0, "rof0"),
                        new FlagDataItem(Msg.READ, ByteType.HIGHBYTE, 1, "rof1"),
                        new FlagDataItem(Msg.READ, ByteType.HIGHBYTE, 2, "rof2"),
                        new FlagDataItem(Msg.READ, ByteType.HIGHBYTE, 3, "rof3"),
                        new FlagDataItem(Msg.READ, ByteType.HIGHBYTE, 4, "rof4"),
                        new FlagDataItem(Msg.READ, ByteType.HIGHBYTE, 5, "rof5"),
                        new FlagDataItem(Msg.READ, ByteType.HIGHBYTE, 6, "rof6"),
                        new FlagDataItem(Msg.READ, ByteType.HIGHBYTE, 7, "rof7") });
        g.put(102, new DataItem[] {});

        g.put(105, new DataItem[] { new TspFhbSizeDataItem(Msg.READ, ByteType.HIGHBYTE, 106, "ss_tspnumber") });
        g.put(106, new DataItem[] { new TspFhbValueDataItem(Msg.READ, "ss_tspentry") });

        g.put(107, new DataItem[] { new TspFhbSizeDataItem(Msg.READ, ByteType.HIGHBYTE, 108, "ss_fhbnumber") });
        g.put(108, new DataItem[] { new TspFhbValueDataItem(Msg.READ, "ss_fhbentry") });
        g.put(113, new DataItem[] { new UIntDataItem(Msg.READ, ByteType.BOTH, "unsuccessfulburnerstarts") });
        g.put(115, new DataItem[] { new UIntDataItem(Msg.READ, ByteType.BOTH, "oemdiagcode") });
        g.put(116, new DataItem[] { new UIntDataItem(Msg.READ, ByteType.BOTH, "burnerstarts") });
        g.put(117, new DataItem[] { new UIntDataItem(Msg.READ, ByteType.BOTH, "chpumpstarts") });
        g.put(118, new DataItem[] { new UIntDataItem(Msg.READ, ByteType.BOTH, "dhwpvstarts") });
        g.put(119, new DataItem[] { new UIntDataItem(Msg.READ, ByteType.BOTH, "dhwburnerstarts") });
        g.put(120, new DataItem[] { new UIntDataItem(Msg.READ, ByteType.BOTH, "burnerhours", Units.HOUR) });
        g.put(121, new DataItem[] { new UIntDataItem(Msg.READ, ByteType.BOTH, "chpumphours", Units.HOUR) });
        g.put(122, new DataItem[] { new UIntDataItem(Msg.READ, ByteType.BOTH, "dhwpvhours", Units.HOUR) });
        g.put(123, new DataItem[] { new UIntDataItem(Msg.READ, ByteType.BOTH, "dhwburnerhours", Units.HOUR) });
        g.put(124, new DataItem[] { new FloatDataItem(Msg.WRITE, "masterotversion") });
        g.put(125, new DataItem[] { new FloatDataItem(Msg.READ, "slaveotversion") });
        g.put(126, new DataItem[] { new UIntDataItem(Msg.WRITE, ByteType.HIGHBYTE, "masterproducttype"),
                new UIntDataItem(Msg.WRITE, ByteType.LOWBYTE, "masterproductversion") });
        g.put(127, new DataItem[] { new UIntDataItem(Msg.READ, ByteType.HIGHBYTE, "slaveproducttype"),
                new UIntDataItem(Msg.READ, ByteType.LOWBYTE, "slaveproductversion") });

        return g;
    }
}
