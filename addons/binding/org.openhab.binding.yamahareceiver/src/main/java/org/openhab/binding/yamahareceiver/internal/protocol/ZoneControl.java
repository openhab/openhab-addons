/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.yamahareceiver.internal.protocol;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * The zone protocol class is used to control one zone of a Yamaha receiver with HTTP/xml.
 * No state will be saved in here, but in {@link ZoneControl.State} instead.
 *
 * @author David Gräff <david.graeff@tu-dortmund.de>
 * @author Eric Thill
 * @author Ben Jones
 */
public class ZoneControl {
    /**
     * The state of a specific zone of a Yamaha receiver.
     *
     * @author David Graeff <david.graeff@web.de>
     */
    public static class State {
        public boolean power = false;
        // User visible name of the input channel for the current zone
        public String inputName = "";
        // The ID of the input channel that is used as xml tags (for example NET_RADIO, HDMI_1).
        // This may differ from what the AVR returns in Input/Input_Sel ("NET RADIO", "HDMI1")
        public String inputID = "";
        public String surroundProgram = "";
        public float volume = 0.0f; // volume in percent
        public boolean mute = false;
    }

    public static class AvailableInputState {
        // List of inputs with <Input ID, Input Name>
        public Map<String, String> availableInputs = new TreeMap<String, String>();
    }

    public interface Listener {
        void zoneStateChanged(State msg);

        void availableInputsChanged(AvailableInputState msg);
    }

    private Listener observer;
    private Logger logger = LoggerFactory.getLogger(ZoneControl.class);

    /**
     * The names of this enum are part of the protocol!
     * Receivers have different capabilities, some have 2 zones, some up to 4.
     * Every receiver has a "Main_Zone".
     */
    public enum Zone {
        Main_Zone,
        Zone_2,
        Zone_3,
        Zone_4;
    }

    /**
     * The volume min and max is the same for all supported devices.
     */
    public static final int VOLUME_MIN = -80;
    public static final int VOLUME_MAX = 12;
    public static final int VOLUME_RANGE = -VOLUME_MIN + VOLUME_MAX;

    // Menu navigation timeouts
    public static final int MENU_RETRY_DELAY = 500;
    public static final int MENU_MAX_WAITING_TIME = 5000;

    // The address of the receiver.
    private final WeakReference<HttpXMLSendReceive> comReference;
    private final Zone zone;

    // Creates a yamaha protol connection object.
    // All commands always refer to a zone. A ZoneControl object
    // therefore consists of a AVR connection and a zone.
    public ZoneControl(HttpXMLSendReceive xml, Zone zone, Listener observer) {
        this.comReference = new WeakReference<HttpXMLSendReceive>(xml);
        this.zone = zone;
        this.observer = observer;
    }

    /**
     * Return the zone
     */
    public Zone getZone() {
        return zone;
    }

    /**
     * Wraps the XML message with the zone tags. Example with zone=Main_Zone:
     * <Main_Zone>message</Main_Zone>.
     *
     * @param message XML message
     * @return
     */
    protected String wrZone(String message) {
        return "<" + zone.name() + ">" + message + "</" + zone.name() + ">";
    }

    public void setPower(boolean on) throws IOException, ParserConfigurationException, SAXException {
        if (on) {
            comReference.get().postPut(wrZone("<Power_Control><Power>On</Power></Power_Control>"));
        } else {
            comReference.get().postPut(wrZone("<Power_Control><Power>Standby</Power></Power_Control>"));
        }
        updateState();
    }

    /**
     * Sets the absolute volume in decibel.
     *
     * @param volume Absolute value in decibel ([-80,+12]).
     * @throws IOException
     */
    public void setVolumeDB(float volume) throws IOException, ParserConfigurationException, SAXException {
        if (volume < VOLUME_MIN) {
            volume = VOLUME_MIN;
        }
        if (volume > VOLUME_MAX) {
            volume = VOLUME_MAX;
        }
        int vol = (int) volume * 10;
        comReference.get().postPut(wrZone(
                "<Volume><Lvl><Val>" + String.valueOf(vol) + "</Val><Exp>1</Exp><Unit>dB</Unit></Lvl></Volume>"));

        updateState();
    }

    /**
     * Sets the volume in percent
     *
     * @param volume
     * @throws IOException
     */
    public void setVolume(float volume) throws IOException, ParserConfigurationException, SAXException {
        if (volume < 0) {
            volume = 0;
        }
        if (volume > 100) {
            volume = 100;
        }
        // Compute value in db
        setVolumeDB(volume * ZoneControl.VOLUME_RANGE / 100.0f + ZoneControl.VOLUME_MIN);
    }

    /**
     * Increase or decrease the volume by the given percentage.
     *
     * @param percent
     * @throws IOException
     */
    public void setVolumeRelative(State state, float percent)
            throws IOException, ParserConfigurationException, SAXException {
        setVolume(state.volume + percent);
    }

    public void setMute(boolean mute) throws IOException, ParserConfigurationException, SAXException {
        if (mute) {
            comReference.get().postPut(wrZone("<Volume><Mute>On</Mute></Volume>"));
        } else {
            comReference.get().postPut(wrZone("<Volume><Mute>Off</Mute></Volume>"));
        }
        updateState();
    }

    public void setInput(String name) throws IOException, ParserConfigurationException, SAXException {
        comReference.get().postPut(wrZone("<Input><Input_Sel>" + name + "</Input_Sel></Input>"));
        updateState();
    }

    public void setSurroundProgram(String name) throws IOException, ParserConfigurationException, SAXException {
        if (name.toLowerCase().equals("straight")) {
            comReference.get().postPut(wrZone(
                    "<Surround><Program_Sel><Current><Straight>On</Straight></Current></Program_Sel></Surround>"));
        } else {
            comReference.get().postPut(wrZone("<Surround><Program_Sel><Current><Sound_Program>" + name
                    + "</Sound_Program></Current></Program_Sel></Surround>"));
        }

        updateState();
    }

    private String convertNameToID(String name) {
        // Replace whitespace with an underscore. The ID is what is used for xml tags and the AVR doesn't like
        // whitespace in xml tags.
        name = name.replace(" ", "_").toUpperCase();
        // Workaround if the receiver returns "HDMI2" instead of "HDMI_2". We can't really change the input IDs in the
        // thing type description, because we still need to send "HDMI_2" for an input change to the receiver.
        if (name.length() >= 5 && name.startsWith("HDMI") && name.charAt(4) != '_') {
            // Adds the missing underscore.
            name = name.replace("HDMI", "HDMI_");
        }
        return name;
    }

    public void updateState() throws IOException, ParserConfigurationException, SAXException {
        if (observer == null) {
            return;
        }

        HttpXMLSendReceive com = comReference.get();
        String response = com.post(wrZone("<Basic_Status>GetParam</Basic_Status>"));
        Document doc = com.xml(response);
        if (doc.getFirstChild() == null) {
            throw new SAXException("<Basic_Status>GetParam failed: " + response);
        }
        Node basicStatus = HttpXMLSendReceive.getNode(doc.getFirstChild(), zone + "/Basic_Status");

        Node node;
        String value;

        State state = new State();

        node = HttpXMLSendReceive.getNode(basicStatus, "Power_Control/Power");
        value = node != null ? node.getTextContent() : "";
        state.power = "On".equalsIgnoreCase(value);

        node = HttpXMLSendReceive.getNode(basicStatus, "Input/Input_Sel");
        value = node != null ? node.getTextContent() : "";
        state.inputID = convertNameToID(value);

        // Some receivers may use Src_Name instead?
        node = HttpXMLSendReceive.getNode(basicStatus, "Input/Input_Sel_Item_Info/Title");
        value = node != null ? node.getTextContent() : "";
        state.inputName = value;

        node = HttpXMLSendReceive.getNode(basicStatus, "Surround/Program_Sel/Current/Sound_Program");
        value = node != null ? node.getTextContent() : "";
        state.surroundProgram = value;

        node = HttpXMLSendReceive.getNode(basicStatus, "Volume/Lvl/Val");
        value = node != null ? node.getTextContent() : String.valueOf(VOLUME_MIN);
        state.volume = Float.parseFloat(value) * .1f; // in DB
        state.volume = (state.volume + -ZoneControl.VOLUME_MIN) * 100.0f / ZoneControl.VOLUME_RANGE; // in percent
        if (state.volume < 0 || state.volume > 100) {
            logger.error("Received volume is out of range: {}", state.volume);
            state.volume = 0;
        }

        node = HttpXMLSendReceive.getNode(basicStatus, "Volume/Mute");
        value = node != null ? node.getTextContent() : "";
        state.mute = "On".equalsIgnoreCase(value);

        observer.zoneStateChanged(state);
    }

    public void fetchAvailableInputs() throws IOException, ParserConfigurationException, SAXException {
        if (observer == null) {
            return;
        }

        HttpXMLSendReceive com = comReference.get();
        String response = com.post(wrZone("<Input><Input_Sel_Item>GetParam</Input_Sel_Item></Input>"));
        Document doc = com.xml(response);
        if (doc.getFirstChild() == null) {
            throw new SAXException("<Input><Input_Sel_Item>GetParam failed: " + response);
        }
        Node inputSelItem = HttpXMLSendReceive.getNode(doc.getFirstChild(), zone + "/Input/Input_Sel_Item");
        NodeList items = inputSelItem.getChildNodes();

        AvailableInputState state = new AvailableInputState();

        for (int i = 0; i < items.getLength(); i++) {
            Element item = (Element) items.item(i);
            String name = item.getElementsByTagName("Param").item(0).getTextContent();
            boolean writable = item.getElementsByTagName("RW").item(0).getTextContent().contains("W");
            if (writable) {
                state.availableInputs.put(convertNameToID(name), name);
            }
        }

        observer.availableInputsChanged(state);
    }

}
