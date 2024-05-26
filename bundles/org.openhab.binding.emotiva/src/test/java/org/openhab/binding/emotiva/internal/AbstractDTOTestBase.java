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
package org.openhab.binding.emotiva.internal;

import javax.xml.bind.JAXBException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.emotiva.internal.protocol.EmotivaXmlUtils;

/**
 * Abstract helper class for unit tests.
 *
 * @author Espen Fossen - Initial contribution
 */
@NonNullByDefault
public class AbstractDTOTestBase {

    protected EmotivaXmlUtils xmlUtils = new EmotivaXmlUtils();

    protected String emotivaAckPowerOff = """
            <?xml version="1.0"?>
            <emotivaAck>
              <power_off status="ack"/>
            </emotivaAck>""";

    protected String emotivaAckPowerOffAndNotRealCommand = """
            <?xml version="1.0"?>
            <emotivaAck>
              <power_off status="ack"/>
              <not_a_real_command status="ack"/>
            </emotivaAck>""";

    protected String emotivaAckPowerOffAndVolume = """
            <?xml version="1.0"?>
            <emotivaAck>
              <power_off status="ack"/>
              <volume status="ack"/>
            </emotivaAck>""";

    protected String emotivaCommandoPowerOn = """
            <power_on status="ack"/>""";

    protected String emotivaNotifyEmotivaPropertyPower = """
            <property name="tuner_channel" value="FM 106.50MHz" visible="true"/>""";

    protected String emotivaUpdateEmotivaPropertyPower = """
            <property name="power" value="On" visible="true" status="ack"/>""";

    protected String emotivaControlVolume = """
            <emotivaControl>
              <volume value="-1" ack="no" />
            </emotivaControl>""";

    protected String emotivaNotifyV2KeepAlive = """
            <?xml version="1.0"?>
            <emotivaNotify sequence="54062">
              <keepAlive value="7500" visible="true"/>
            </emotivaNotify>""";

    protected String emotivaNotifyV2UnknownTag = """
            <?xml version="1.0"?>
            <emotivaNotify sequence="54062">
              <unknownTag value="0" visible="false"/>
            </emotivaNotify>""";

    protected String emotivaNotifyV2KeepAliveSequence = "54062";

    protected String emotivaNotifyV3KeepAlive = """
            <?xml version="1.0"?>
            <emotivaNotify sequence="54062">
              <property name="keepAlive" value="7500" visible="true"/>
            </emotivaNotify>""";

    protected String emotivaNotifyV3EmptyMenuValue = """
            <?xml version="1.0"?>
            <emotivaNotify sequence="23929">
              <property name="menu" value="" visible="true"/>
            </emotivaNotify>
            """;

    protected String emotivaUpdateRequest = """
            <?xml version="1.0" encoding="utf-8"?>
            <emotivaUpdate protocol="3.0">
              <power />
              <source />
              <volume />
              <audio_bitstream />
              <audio_bits />
              <video_input />
              <video_format />
              <video_space />
            </emotivaUpdate>""";

    protected String emotivaMenuNotify = """
            <?xml version="1.0"?>
            <emotivaMenuNotify sequence="2378">
              <row number="0">
                <col number="0" value="" fixed="no" highlight="no" arrow="no"/>
                <col number="1" value="Left Display" fixed="no" highlight="no" arrow="up"/>
                <col number="2" value="Full Status" fixed="no" highlight="no" arrow="no"/>
              </row>
              <row number="1">
                <col number="0" value="" fixed="no" highlight="no" arrow="no"/>
                <col number="1" value="Right Display" fixed="no" highlight="no" arrow="no"/>
                <col number="2" value="Volume" fixed="no" highlight="no" arrow="no"/>
              </row>
              <row number="2">
                <col number="0" value="" fixed="no" highlight="no" arrow="no"/>
                <col number="1" value="Menu Display" fixed="no" highlight="no" arrow="no"/>
                <col number="2" value="Right" fixed="no" highlight="no" arrow="no"/>
              </row>
              <row number="3">
                <col number="0" value="" fixed="no" highlight="no" arrow="no"/>
                <col number="1" value="OSD Transparent" fixed="no" highlight="no" arrow="no"/>
                <col number="2" value=" 37.5%" fixed="no" highlight="no" arrow="no"/>
              </row>
              <row number="4">
                <col number="0" value="" fixed="no" highlight="no" arrow="no"/>
                <col number="1" value="Friendly Name" fixed="no" highlight="no" arrow="up"/>
                <col number="2" value="RMC-1" fixed="no" highlight="no" arrow="no"/>
              </row>
              <row number="5">
                <col number="0" value="Preferences" fixed="no" highlight="no" arrow="left"/>
                <col number="1" value="OSD Popups" fixed="no" highlight="yes" arrow="no"/>
                <col number="2" value="All" fixed="no" highlight="no" arrow="right"/>
              </row>
              <row number="6">
                <col number="0" value="" fixed="no" highlight="no" arrow="no"/>
                <col number="1" value="LFE Level" fixed="no" highlight="no" arrow="down"/>
                <col number="2" value="  0.0dB" fixed="no" highlight="no" arrow="no"/>
              </row>
              <row number="7">
                <col number="0" value="" fixed="no" highlight="no" arrow="no"/>
                <col number="1" value="Turn-On Input" fixed="no" highlight="no" arrow="no"/>
                <col number="2" value="Last Used" fixed="no" highlight="no" arrow="no"/>
              </row>
              <row number="8">
                <col number="0" value="" fixed="no" highlight="no" arrow="no"/>
                <col number="1" value="Turn-On Volume" fixed="no" highlight="no" arrow="no"/>
                <col number="2" value="Last Used" fixed="no" highlight="no" arrow="no"/>
              </row>
              <row number="9">
                <col number="0" value="" fixed="no" highlight="no" arrow="no"/>
                <col number="1" value="Max Volume" fixed="no" highlight="no" arrow="no"/>
                <col number="2" value=" 11.0dB" fixed="no" highlight="no" arrow="no"/>
              </row>
              <row number="10">
                <col number="0" value="" fixed="no" highlight="no" arrow="no"/>
                <col number="1" value="Front Bright" fixed="no" highlight="no" arrow="no"/>
                <col number="2" value="100%" fixed="no" highlight="no" arrow="no"/>
              </row>
            </emotivaMenuNotify>""";

    protected String emotivaMenuNotifyWithCheckBox = """
            <?xml version="1.0" encoding="UTF-8"?>
            <emotivaMenuNotify sequence="12129">
              <row number="0">
                <col number="0" value="" fixedWidth="false" highlight="false" arrow="no"/>
                <col number="1" value="" fixedWidth="false" highlight="false" arrow="up"/>
                <col number="2" value="" fixedWidth="false" highlight="false" arrow="no"/>
              </row>
              <row number="1">
                <col number="0" value="" fixedWidth="false" highlight="false" arrow="no"/>
                <col number="1" value="" fixedWidth="false" highlight="false" arrow="no"/>
                <col number="2" value="" fixedWidth="false" highlight="false" arrow="no"/>
              </row>
              <row number="2">
                <col number="0" value="" fixedWidth="false" highlight="false" arrow="no"/>
                <col number="1" value="" fixedWidth="false" highlight="false" arrow="no"/>
                <col number="2" value="" fixedWidth="false" highlight="false" arrow="no"/>
              </row>
              <row number="3">
                <col number="0" value="" fixedWidth="false" highlight="false" arrow="no"/>
                <col number="1" value="Input change" fixedWidth="false" highlight="false" arrow="no"/>
                <col number="2" checkbox="off" highlight="false" arrow="no"/>
              </row>
              <row number="4">
                <col number="0" value="" fixedWidth="false" highlight="false" arrow="no"/>
                <col number="1" value="Volume" fixedWidth="false" highlight="false" arrow="up"/>
                <col number="2" checkbox="on" highlight="false" arrow="no"/>
              </row>
              <row number="5">
                <col number="0" value="HDMI CEC" fixedWidth="false" highlight="false" arrow="left"/>
                <col number="1" value="Enable" fixedWidth="false" highlight="true" arrow="no"/>
                <col number="2" checkbox="on" highlight="false" arrow="right"/>
              </row>
              <row number="6">
                <col number="0" value="" fixedWidth="false" highlight="false" arrow="no"/>
                <col number="1" value="Audio to TV" fixedWidth="false" highlight="false" arrow="down"/>
                <col number="2" checkbox="off" highlight="false" arrow="no"/>
              </row>
              <row number="7">
                <col number="0" value="" fixedWidth="false" highlight="false" arrow="no"/>
                <col number="1" value="Power On" fixedWidth="false" highlight="false" arrow="no"/>
                <col number="2" checkbox="on" highlight="false" arrow="no"/>
              </row>
              <row number="8">
                <col number="0" value="" fixedWidth="false" highlight="false" arrow="no"/>
                <col number="1" value="Power Off" fixedWidth="false" highlight="false" arrow="no"/>
                <col number="2" checkbox="on" highlight="false" arrow="no"/>
              </row>
              <row number="9">
                <col number="0" value="" fixedWidth="false" highlight="false" arrow="no"/>
                <col number="1" value="" fixedWidth="false" highlight="false" arrow="no"/>
                <col number="2" value="" fixedWidth="false" highlight="false" arrow="no"/>
              </row>
              <row number="10">
                <col number="0" value="" fixedWidth="false" highlight="false" arrow="no"/>
                <col number="1" value="" fixedWidth="false" highlight="false" arrow="no"/>
                <col number="2" value="" fixedWidth="false" highlight="false" arrow="no"/>
              </row>
            </emotivaMenuNotify>""";

    protected String emotivaMenuNotifyProgress = """
            <?xml version="1.0"?>
            <emotivaMenuNotify sequence="2405">
              <progress time="15"/>
            </emotivaMenuNotify>""";

    protected String emotivaUpdateResponseV2 = """
            <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
            <emotivaUpdate protocol="2.0">
              <power value="On" visible="true" status="ack"/>
              <source value="HDMI 1" visible="true" status="nak"/>
              <noKnownTag ack="nak"/>
            </emotivaUpdate>""";

    protected String emotivaUpdateResponseV3 = """
            <?xml version="1.0" encoding="utf-8"?>
            <emotivaUpdate protocol="3.0">
              <property name="power" value="On" visible="true" status="ack"/>
              <property name="source" value="HDMI 1" visible="true" status="nak"/>
              <property name="noKnownTag" ack="nak"/>
            </emotivaUpdate>""";

    protected String emotivaBarNotifyBigText = """
            <?xml version="1.0" encoding="UTF-8"?>
            <emotivaBarNotify sequence="98">
              <bar text="XBox One" type="bigText"/>
            </emotivaBarNotify>""";

    protected String emotivaSubscriptionRequest = """
            <emotivaSubscription>
              <selected_mode />
              <power />
              <noKnownTag />
            </emotivaSubscription>""";

    protected String emotivaSubscriptionResponse = """
            <?xml version="1.0"?>
            <emotivaSubscription>
              <power status="ack"/>
              <source value="SHIELD    " visible="true" status="ack"/>
              <menu value="Off" visible="true" status="ack"/>
              <treble ack="yes" value="+ 1.5" visible="true" status="ack"/>
              <noKnownTag ack="no"/>
            </emotivaSubscription>""";

    protected String emotivaPingV2 = """
            <?xml version="1.0" encoding="utf-8"?>
            <emotivaPing />""";

    protected String emotivaPingV3 = """
            <?xml version="1.0" encoding="utf-8" ?>
            <emotivaPing protocol="3.0"/>""";

    protected String emotivaTransponderResponseV2 = """
            <?xml version="1.0"?>
            <emotivaTransponder>
              <model>XMC-1</model>
              <revision>2.0</revision>
              <name>Living Room</name>
              <control>
                <version>2.0</version>
                <controlPort>7002</controlPort>
                <notifyPort>7003</notifyPort>
                <infoPort>7004</infoPort>
                <setupPortTCP>7100</setupPortTCP>
                <keepAlive>10000</keepAlive>
              </control>
            </emotivaTransponder>""";

    protected String emotivaTransponderResponseV3 = """
            <?xml version="1.0"?>
            <emotivaTransponder>
              <model>XMC-2</model>
              <revision>3.0</revision>
              <name>Living Room</name>
              <control>
                <version>3.0</version>
                <controlPort>7002</controlPort>
                <notifyPort>7003</notifyPort>
                <infoPort>7004</infoPort>
                <setupPortTCP>7100</setupPortTCP>
                <keepAlive>10000</keepAlive>
              </control>
            </emotivaTransponder>""";

    public AbstractDTOTestBase() throws JAXBException {
    }
}
