/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.yamahareceiver.internal.protocol.xml;

import java.io.IOException;
import java.lang.ref.WeakReference;

import org.apache.commons.lang.StringUtils;
import org.openhab.binding.yamahareceiver.YamahaReceiverBindingConstants;
import org.openhab.binding.yamahareceiver.internal.protocol.AbstractConnection;
import org.openhab.binding.yamahareceiver.internal.protocol.InputWithPlayControl;
import org.openhab.binding.yamahareceiver.internal.protocol.ReceivedMessageParseException;
import org.openhab.binding.yamahareceiver.internal.state.PlayInfoState;
import org.openhab.binding.yamahareceiver.internal.state.PlayInfoStateListener;
import org.openhab.binding.yamahareceiver.internal.state.PresetInfoState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

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
 * No state will be saved in here, but in {@link PlayInfoState} and
 * {@link PresetInfoState} instead.
 *
 * @author David Graeff
 * @author Tomasz Maruszak - Spotify support, refactoring
 */
public class InputWithPlayControlXML implements InputWithPlayControl {

    public static final int PRESET_CHANNELS = 40;

    private final Logger logger = LoggerFactory.getLogger(InputWithPlayControlXML.class);

    protected final WeakReference<AbstractConnection> comReference;

    protected final String inputID;

    private final PlayInfoStateListener observer;

    /**
     * Create a InputWithPlayControl object for altering menu positions and requesting current menu information as well
     * as controlling the playback and choosing a preset item.
     *
     * @param inputID The input ID like USB or NET_RADIO.
     * @param com The Yamaha communication object to send http requests.
     */
    public InputWithPlayControlXML(String inputID, AbstractConnection com, PlayInfoStateListener observer) {
        this.inputID = inputID;
        this.comReference = new WeakReference<>(com);
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
    @Override
    public void update() throws IOException, ReceivedMessageParseException {
        if (observer == null) {
            return;
        }

        AbstractConnection com = comReference.get();
        String response = com.sendReceive(wrInput("<Play_Info>GetParam</Play_Info>"));
        Document doc = XMLUtils.xml(response);
        if (doc.getFirstChild() == null) {
            throw new ReceivedMessageParseException("<Play_Info>GetParam failed: " + response);
        }

        PlayInfoState msg = new PlayInfoState();

        Node playInfoNode = XMLUtils.getNode(doc.getFirstChild(), "Play_Info");

        msg.playbackMode = XMLUtils.getNodeContentOrDefault(playInfoNode, "Playback_Info", msg.playbackMode);

        Node metaInfoNode = XMLUtils.getNode(playInfoNode, "Meta_Info");
        if (metaInfoNode != null) {
            String stationElement = YamahaReceiverBindingConstants.INPUT_TUNER.equals(inputID) ? "Radio_Text_A" : "Station";
            msg.station = XMLUtils.getNodeContentOrDefault(metaInfoNode, stationElement, msg.station);

            msg.artist = XMLUtils.getNodeContentOrDefault(metaInfoNode, "Artist", msg.artist);
            msg.album = XMLUtils.getNodeContentOrDefault(metaInfoNode, "Album", msg.album);

            String songElement = YamahaReceiverBindingConstants.INPUT_SPOTIFY.equals(inputID) ? "Track" : "Song";
            msg.song = XMLUtils.getNodeContentOrDefault(metaInfoNode, songElement, msg.song);
        }

        if (YamahaReceiverBindingConstants.INPUT_SPOTIFY.equals(inputID)) {
            //<YAMAHA_AV rsp="GET" RC="0">
            //    <Spotify>
            //        <Play_Info>
            //            <Feature_Availability>Ready</Feature_Availability>
            //            <Playback_Info>Play</Playback_Info>
            //            <Meta_Info>
            //                <Artist>Way Out West</Artist>
            //                <Album>Tuesday Maybe</Album>
            //                <Track>Tuesday Maybe</Track>
            //            </Meta_Info>
            //            <Album_ART>
            //                <URL>/YamahaRemoteControl/AlbumART/AlbumART3929.jpg</URL>
            //                <ID>39290</ID>
            //                <Format>JPEG</Format>
            //            </Album_ART>
            //            <Input_Logo>
            //                <URL_S>/YamahaRemoteControl/Logos/logo005.png</URL_S>
            //                <URL_M></URL_M>
            //                <URL_L></URL_L>
            //            </Input_Logo>
            //        </Play_Info>
            //    </Spotify>
            //</YAMAHA_AV>

            // Spotify input supports song cover image
            String songImageUrl = XMLUtils.getNodeContentOrDefault(playInfoNode, "Album_ART/URL", "");
            if (StringUtils.isNotEmpty(songImageUrl)) {
                msg.songImageUrl = String.format("http://%s%s", com.getHost(), songImageUrl);
            }
        }

        logger.trace("Playback: {}, Station: {}, Artist: {}, Album: {}, Song: {}, SongImageUrl: {}",
                msg.playbackMode, msg.station, msg.artist, msg.album, msg.song, msg.songImageUrl);

        observer.playInfoUpdated(msg);
    }

    /**
     * Start the playback of the content which is usually selected by the means of the Navigation control class or
     * which has been stopped by stop().
     *
     * @throws Exception
     */
    @Override
    public void play() throws IOException, ReceivedMessageParseException {
        sendPlaybackCommand("Play");
    }

    /**
     * Stop the currently playing content. Use start() to start again.
     *
     * @throws Exception
     */
    @Override
    public void stop() throws IOException, ReceivedMessageParseException {
        sendPlaybackCommand("Stop");
    }

    /**
     * Pause the currently playing content. This is not available for streaming content like on NET_RADIO.
     *
     * @throws Exception
     */
    @Override
    public void pause() throws IOException, ReceivedMessageParseException {
        sendPlaybackCommand("Pause");
    }

    /**
     * Skip forward. This is not available for streaming content like on NET_RADIO.
     *
     * @throws Exception
     */
    @Override
    public void skipFF() throws IOException, ReceivedMessageParseException {
        if (YamahaReceiverBindingConstants.INPUT_SPOTIFY.equals(inputID)) {
            logger.warn("Command skip forward is not supported for input {}", inputID);
            return;
        }
        sendPlaybackCommand("Skip Fwd");
    }

    /**
     * Skip reverse. This is not available for streaming content like on NET_RADIO.
     *
     * @throws Exception
     */
    @Override
    public void skipREV() throws IOException, ReceivedMessageParseException {
        if (YamahaReceiverBindingConstants.INPUT_SPOTIFY.equals(inputID)) {
            logger.warn("Command skip reverse is not supported for input {}", inputID);
            return;
        }
        sendPlaybackCommand("Skip Rev");
    }

    /**
     * Next track. This is not available for streaming content like on NET_RADIO.
     *
     * @throws Exception
     */
    @Override
    public void nextTrack() throws IOException, ReceivedMessageParseException {
        String cmd = YamahaReceiverBindingConstants.INPUT_SPOTIFY.equals(inputID) ? "Skip Fwd" : ">>|";
        sendPlaybackCommand(cmd);
    }

    /**
     * Previous track. This is not available for streaming content like on NET_RADIO.
     *
     * @throws Exception
     */
    @Override
    public void previousTrack() throws IOException, ReceivedMessageParseException {
        String cmd = YamahaReceiverBindingConstants.INPUT_SPOTIFY.equals(inputID) ? "Skip Rev" : "|<<";
        sendPlaybackCommand(cmd);
    }

    /**
     * Sends a playback command to the AVR. After command is invoked, the state is also being refreshed.
     * @param command - the protocol level command name
     * @throws IOException
     * @throws ReceivedMessageParseException
     */
    private void sendPlaybackCommand(String command) throws IOException, ReceivedMessageParseException {
        comReference.get().send(wrInput("<Play_Control><Playback>" + command + "</Playback></Play_Control>"));
        update();
    }

}
