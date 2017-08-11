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
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.google.common.collect.Sets;

/**
 * This class implements the Yamaha Receiver protocol related to navigation functionally. USB, NET_RADIO, IPOD and
 * other inputs are using the same way of playback control.
 *
 * The XML nodes <Play_Info> and <Play_Control> are used.
 *
 * Example:
 *
 * InputWithPlayControl menu = new InputWithPlayControl("NET_RADIO", comObject);
 * menu.goToPath(menuDir);
 * menu.selectItem(stationName);
 *
 * No state will be saved in here, but in {@link InputWithPlayControl.PlayInfoState} and
 * {@link InputWithPlayControl.PlayControlState} instead.
 *
 * @author David Graeff
 */
public class InputWithPlayControl {
    protected final WeakReference<HttpXMLSendReceive> comReference;

    protected final String inputID;

    public static Set<String> supportedInputs = Sets.newHashSet("TUNER", "NET_RADIO", "USB", "DOCK", "iPOD_USB", "PC",
            "Napster", "Pandora", "SIRIUS", "Rhapsody", "Bluetooth", "iPod", "HD_RADIO");

    public static final int PRESET_CHANNELS = 40;

    public static class PlayInfoState {
        public String station; // NET_RADIO. Will also be used for TUNER where Radio_Text_A/B will be used instead.
        public String artist; // USB, iPOD, PC
        public String album; // USB, iPOD, PC
        public String song; // USB, iPOD, PC

        public String playbackMode = "Stop"; // All inputs

        public void invalidate() {
            this.playbackMode = "N/A";
            this.station = "N/A";
            this.artist = "N/A";
            this.album = "N/A";
            this.song = "N/A";
        }
    }

    public static class PlayControlState {
        public int presetChannel = 0; // Used by NET_RADIO, RADIO, HD_RADIO, iPOD, USB, PC
        public String presetChannelNames[] = new String[PRESET_CHANNELS];
        public boolean presetChannelNamesChanged = false;

        public void invalidate() {
            presetChannel = 0;
        }
    }

    public interface Listener {
        void playInfoUpdated(PlayInfoState msg);

        void playControlUpdated(PlayControlState msg);
    }

    private Listener observer;

    /**
     * Create a InputWithPlayControl object for altering menu positions and requesting current menu information as well
     * as controlling the playback and choosing a preset item.
     *
     * @param inputID The input ID like USB or NET_RADIO.
     * @param com The Yamaha communication object to send http requests.
     */
    public InputWithPlayControl(String inputID, HttpXMLSendReceive com, Listener observer) {
        this.inputID = inputID;
        this.comReference = new WeakReference<HttpXMLSendReceive>(com);
        this.observer = observer;
    }

    /**
     * Wraps the XML message with the inputID tags. Example with inputID=NET_RADIO:
     * <NETRADIO>message</NETRADIO>.
     *
     * @param message XML message
     * @return
     */
    protected String wrInput(String message) {
        return "<" + inputID + ">" + message + "</" + inputID + ">";
    }

    /**
     * Updates the playback information
     *
     * @throws Exception
     */
    public void updatePlaybackInformation() throws IOException, ParserConfigurationException, SAXException {
        if (observer == null) {
            return;
        }

        HttpXMLSendReceive com = comReference.get();
        String response = com.post(wrInput("<Play_Info>GetParam</Play_Info>"));
        Document doc = com.xml(response);
        if (doc.getFirstChild() == null) {
            throw new SAXException("<Play_Info>GetParam failed: " + response);
        }

        PlayInfoState msg = new PlayInfoState();

        Node playbackInfoNode = HttpXMLSendReceive.getNode(doc.getFirstChild(), "Play_Info/Playback_Info");
        msg.playbackMode = playbackInfoNode != null ? playbackInfoNode.getTextContent() : "";

        Node metaInfoNode = HttpXMLSendReceive.getNode(doc.getFirstChild(), "Play_Info/Meta_Info");
        Node sub;

        if (inputID.equals("TUNER")) {
            sub = HttpXMLSendReceive.getNode(metaInfoNode, "Radio_Text_A");
            msg.station = sub != null ? sub.getTextContent() : "";
        } else {
            sub = HttpXMLSendReceive.getNode(metaInfoNode, "Station");
            msg.station = sub != null ? sub.getTextContent() : "";
        }

        sub = HttpXMLSendReceive.getNode(metaInfoNode, "Artist");
        msg.artist = sub != null ? sub.getTextContent() : "";

        sub = HttpXMLSendReceive.getNode(metaInfoNode, "Album");
        msg.album = sub != null ? sub.getTextContent() : "";

        sub = HttpXMLSendReceive.getNode(metaInfoNode, "Song");
        msg.song = sub != null ? sub.getTextContent() : "";

        observer.playInfoUpdated(msg);
    }

    /**
     * Updates the preset information
     *
     * @throws Exception
     */
    public void updatePresetInformation() throws IOException, ParserConfigurationException, SAXException {
        if (observer == null) {
            return;
        }

        HttpXMLSendReceive com = comReference.get();
        String response = com.post(wrInput("<Play_Control>GetParam</Play_Control>"));
        Document doc = com.xml(response);
        if (doc.getFirstChild() == null) {
            throw new SAXException("<Play_Control>GetParam failed: " + response);
        }

        PlayControlState msg = new PlayControlState();

        Node playbackInfoNode = HttpXMLSendReceive.getNode(doc.getFirstChild(), "Play_Control/Preset/Preset_Sel");
        msg.presetChannel = playbackInfoNode != null ? Integer.valueOf(playbackInfoNode.getTextContent()) : -1;

        // Set preset channel names, obtained from this xpath:
        // NET_RADIO/Play_Control/Preset/Preset_Sel_Item/Item_1/Title
        playbackInfoNode = HttpXMLSendReceive.getNode(doc.getFirstChild(), "Play_Control/Preset/Preset_Sel_Item");
        if (playbackInfoNode != null) {
            for (int i = 1; i <= PRESET_CHANNELS; ++i) {
                Node itemNode = HttpXMLSendReceive.getNode(playbackInfoNode, "Item_" + String.valueOf(i) + "/Title");
                String v = itemNode != null ? itemNode.getTextContent() : "Item_" + String.valueOf(i);
                if (!v.equals(msg.presetChannelNames[i - 1])) {
                    msg.presetChannelNamesChanged = true;
                    msg.presetChannelNames[i - 1] = v;
                }
            }
        }

        observer.playControlUpdated(msg);
    }

    /**
     * Select a preset channel.
     *
     * @param number The preset position [1,40]
     * @throws Exception
     */
    public void selectItemByPresetNumber(int presetChannel)
            throws IOException, ParserConfigurationException, SAXException {
        comReference.get().postPut(wrInput(
                "<Play_Control><Preset><Preset_Sel>" + presetChannel + "</Preset_Sel></Preset></Play_Control>"));
        updatePresetInformation();
    }

    /**
     * Start the playback of the content which is usually selected by the means of the Navigation control class or
     * which has been stopped by stop().
     *
     * @throws Exception
     */
    public void play() throws IOException, ParserConfigurationException, SAXException {
        comReference.get().postPut(wrInput("<Play_Control><Playback>Play</Playback></Play_Control>"));
        updatePlaybackInformation();
    }

    /**
     * Stop the currently playing content. Use start() to start again.
     *
     * @throws Exception
     */
    public void stop() throws IOException, ParserConfigurationException, SAXException {
        comReference.get().postPut(wrInput("<Play_Control><Playback>Stop</Playback></Play_Control>"));
        updatePlaybackInformation();
    }

    /**
     * Pause the currently playing content. This is not available for streaming content like on NET_RADIO.
     *
     * @throws Exception
     */
    public void pause() throws IOException, ParserConfigurationException, SAXException {
        comReference.get().postPut(wrInput("<Play_Control><Playback>Pause</Playback></Play_Control>"));
        updatePlaybackInformation();
    }

    /**
     * Skip forward. This is not available for streaming content like on NET_RADIO.
     *
     * @throws Exception
     */
    public void skipFF() throws IOException, ParserConfigurationException, SAXException {
        comReference.get().postPut(wrInput("<Play_Control><Playback>Skip Fwd</Playback></Play_Control>"));
    }

    /**
     * Skip reverse. This is not available for streaming content like on NET_RADIO.
     *
     * @throws Exception
     */
    public void skipREV() throws IOException, ParserConfigurationException, SAXException {
        comReference.get().postPut(wrInput("<Play_Control><Playback>Skip Rev</Playback></Play_Control>"));
    }

    /**
     * Next track. This is not available for streaming content like on NET_RADIO.
     *
     * @throws Exception
     */
    public void nextTrack() throws IOException, ParserConfigurationException, SAXException {
        comReference.get().postPut(wrInput("<Play_Control><Playback>>>|</Playback></Play_Control>"));
        updatePlaybackInformation();
    }

    /**
     * Previous track. This is not available for streaming content like on NET_RADIO.
     *
     * @throws Exception
     */
    public void previousTrack() throws IOException, ParserConfigurationException, SAXException {
        comReference.get().postPut(wrInput("<Play_Control><Playback>|<<</Playback></Play_Control>"));
        updatePlaybackInformation();
    }

}
