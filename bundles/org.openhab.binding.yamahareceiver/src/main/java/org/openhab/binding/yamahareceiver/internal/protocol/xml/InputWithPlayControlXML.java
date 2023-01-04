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
package org.openhab.binding.yamahareceiver.internal.protocol.xml;

import static org.openhab.binding.yamahareceiver.internal.YamahaReceiverBindingConstants.Inputs.INPUT_SPOTIFY;
import static org.openhab.binding.yamahareceiver.internal.protocol.xml.XMLConstants.Commands.PLAYBACK_STATUS_CMD;
import static org.openhab.binding.yamahareceiver.internal.protocol.xml.XMLProtocolService.getResponse;
import static org.openhab.binding.yamahareceiver.internal.protocol.xml.XMLUtils.*;

import java.io.IOException;

import org.openhab.binding.yamahareceiver.internal.config.YamahaBridgeConfig;
import org.openhab.binding.yamahareceiver.internal.protocol.AbstractConnection;
import org.openhab.binding.yamahareceiver.internal.protocol.InputWithPlayControl;
import org.openhab.binding.yamahareceiver.internal.protocol.ReceivedMessageParseException;
import org.openhab.binding.yamahareceiver.internal.state.DeviceInformationState;
import org.openhab.binding.yamahareceiver.internal.state.PlayInfoState;
import org.openhab.binding.yamahareceiver.internal.state.PlayInfoStateListener;
import org.openhab.binding.yamahareceiver.internal.state.PresetInfoState;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

/**
 * This class implements the Yamaha Receiver protocol related to navigation functionally. USB, NET_RADIO, IPOD and
 * other inputs are using the same way of playback control.
 * <p>
 * The XML nodes <Play_Info> and <Play_Control> are used.
 * <p>
 * Example:
 * <p>
 * InputWithPlayControl menu = new InputWithPlayControl("NET_RADIO", comObject);
 * menu.goToPath(menuDir);
 * menu.selectItem(stationName);
 * <p>
 * No state will be saved in here, but in {@link PlayInfoState} and
 * {@link PresetInfoState} instead.
 *
 * @author David Graeff
 * @author Tomasz Maruszak - Spotify support, refactoring
 */
public class InputWithPlayControlXML extends AbstractInputControlXML implements InputWithPlayControl {

    private final PlayInfoStateListener observer;
    private final YamahaBridgeConfig bridgeConfig;

    protected CommandTemplate playCmd = new CommandTemplate("<Play_Control><Playback>%s</Playback></Play_Control>",
            "Play_Info/Playback_Info");
    protected CommandTemplate skipCmd = new CommandTemplate("<Play_Control><Playback>%s</Playback></Play_Control>");
    protected String skipForwardValue = "Skip Fwd";
    protected String skipBackwardValue = "Skip Rev";

    /**
     * Create an InputWithPlayControl object for altering menu positions and requesting current menu information as well
     * as controlling the playback and choosing a preset item.
     *
     * @param inputID The input ID like USB or NET_RADIO.
     * @param com The Yamaha communication object to send http requests.
     */
    public InputWithPlayControlXML(String inputID, AbstractConnection com, PlayInfoStateListener observer,
            YamahaBridgeConfig bridgeConfig, DeviceInformationState deviceInformationState) {
        super(LoggerFactory.getLogger(InputWithPlayControlXML.class), inputID, com, deviceInformationState);

        this.observer = observer;
        this.bridgeConfig = bridgeConfig;

        this.applyModelVariations();
    }

    /**
     * Apply command changes to ensure compatibility with all supported models
     */
    protected void applyModelVariations() {
        if (inputFeatureDescriptor != null) {
            // For RX-V3900
            if (inputFeatureDescriptor.hasCommandEnding("Play_Control,Play")) {
                playCmd = new CommandTemplate("<Play_Control><Play>%s</Play></Play_Control>", "Play_Info/Status");
                logger.debug("Input {} - adjusting command to: {}", inputElement, playCmd);
            }
            // For RX-V3900
            if (inputFeatureDescriptor.hasCommandEnding("Play_Control,Skip")) {
                // For RX-V3900 the command value is also different
                skipForwardValue = "Fwd";
                skipBackwardValue = "Rev";

                skipCmd = new CommandTemplate("<Play_Control><Skip>%s</Skip></Play_Control>");
                logger.debug("Input {} - adjusting command to: {}", inputElement, skipCmd);
            }
        }
    }

    /**
     * Start the playback of the content which is usually selected by the means of the Navigation control class or
     * which has been stopped by stop().
     *
     * @throws Exception
     */
    @Override
    public void play() throws IOException, ReceivedMessageParseException {
        sendCommand(playCmd.apply("Play"));
    }

    /**
     * Stop the currently playing content. Use start() to start again.
     *
     * @throws Exception
     */
    @Override
    public void stop() throws IOException, ReceivedMessageParseException {
        sendCommand(playCmd.apply("Stop"));
    }

    /**
     * Pause the currently playing content. This is not available for streaming content like on NET_RADIO.
     *
     * @throws Exception
     */
    @Override
    public void pause() throws IOException, ReceivedMessageParseException {
        sendCommand(playCmd.apply("Pause"));
    }

    /**
     * Skip forward. This is not available for streaming content like on NET_RADIO.
     *
     * @throws Exception
     */
    @Override
    public void skipFF() throws IOException, ReceivedMessageParseException {
        if (INPUT_SPOTIFY.equals(inputID)) {
            logger.warn("Command skip forward is not supported for input {}", inputID);
            return;
        }
        sendCommand(skipCmd.apply(">>|"));
    }

    /**
     * Skip reverse. This is not available for streaming content like on NET_RADIO.
     *
     * @throws Exception
     */
    @Override
    public void skipREV() throws IOException, ReceivedMessageParseException {
        if (INPUT_SPOTIFY.equals(inputID)) {
            logger.warn("Command skip reverse is not supported for input {}", inputID);
            return;
        }
        sendCommand(skipCmd.apply("|<<"));
    }

    /**
     * Next track. This is not available for streaming content like on NET_RADIO.
     *
     * @throws Exception
     */
    @Override
    public void nextTrack() throws IOException, ReceivedMessageParseException {
        sendCommand(skipCmd.apply(skipForwardValue));
    }

    /**
     * Previous track. This is not available for streaming content like on NET_RADIO.
     *
     * @throws Exception
     */
    @Override
    public void previousTrack() throws IOException, ReceivedMessageParseException {
        sendCommand(skipCmd.apply(skipBackwardValue));
    }

    /**
     * Sends a playback command to the AVR. After command is invoked, the state is also being refreshed.
     *
     * @param command - the protocol level command name
     * @throws IOException
     * @throws ReceivedMessageParseException
     */
    private void sendCommand(String command) throws IOException, ReceivedMessageParseException {
        comReference.get().send(wrInput(command));
        update();
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

        // <YAMAHA_AV rsp="GET" RC="0">
        // <Spotify>
        // <Play_Info>
        // <Feature_Availability>Ready</Feature_Availability>
        // <Playback_Info>Play</Playback_Info>
        // <Meta_Info>
        // <Artist>Way Out West</Artist>
        // <Album>Tuesday Maybe</Album>
        // <Track>Tuesday Maybe</Track>
        // </Meta_Info>
        // <Album_ART>
        // <URL>/YamahaRemoteControl/AlbumART/AlbumART3929.jpg</URL>
        // <ID>39290</ID>
        // <Format>JPEG</Format>
        // </Album_ART>
        // <Input_Logo>
        // <URL_S>/YamahaRemoteControl/Logos/logo005.png</URL_S>
        // <URL_M></URL_M>
        // <URL_L></URL_L>
        // </Input_Logo>
        // </Play_Info>
        // </Spotify>
        // </YAMAHA_AV>

        AbstractConnection con = comReference.get();
        Node node = getResponse(con, wrInput(PLAYBACK_STATUS_CMD), inputElement);

        PlayInfoState msg = new PlayInfoState();

        msg.playbackMode = getNodeContentOrDefault(node, playCmd.getPath(), msg.playbackMode);

        // elements for these are named differently per model and per input, so we try to match any known element
        msg.station = getAnyNodeContentOrDefault(node, msg.station, "Play_Info/Meta_Info/Radio_Text_A",
                "Play_Info/Meta_Info/Station", "Play_Info/RDS/Program_Service");
        msg.artist = getAnyNodeContentOrDefault(node, msg.artist, "Play_Info/Meta_Info/Artist",
                "Play_Info/Title/Artist", "Play_Info/RDS/Radio_Text_A");
        msg.album = getAnyNodeContentOrDefault(node, msg.album, "Play_Info/Meta_Info/Album", "Play_Info/Title/Album",
                "Play_Info/RDS/Program_Type");
        msg.song = getAnyNodeContentOrDefault(node, msg.song, "Play_Info/Meta_Info/Track", "Play_Info/Meta_Info/Song",
                "Play_Info/Title/Song", "Play_Info/RDS/Radio_Text_B");

        // Spotify and NET RADIO input supports song cover image (at least on RX-S601D)
        String songImageUrl = getNodeContentOrEmpty(node, "Play_Info/Album_ART/URL");
        msg.songImageUrl = !songImageUrl.isEmpty() ? String.format("http://%s%s", con.getHost(), songImageUrl)
                : bridgeConfig.getAlbumUrl();

        logger.trace("Playback: {}, Station: {}, Artist: {}, Album: {}, Song: {}, SongImageUrl: {}", msg.playbackMode,
                msg.station, msg.artist, msg.album, msg.song, msg.songImageUrl);

        observer.playInfoUpdated(msg);
    }
}
