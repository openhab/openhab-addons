/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.yamahareceiver.internal.protocol.xml;

import java.io.IOException;
import java.lang.ref.WeakReference;

import org.openhab.binding.yamahareceiver.YamahaReceiverBindingConstants;
import org.openhab.binding.yamahareceiver.internal.protocol.AbstractConnection;
import org.openhab.binding.yamahareceiver.internal.protocol.InputWithPlayControl;
import org.openhab.binding.yamahareceiver.internal.protocol.ReceivedMessageParseException;
import org.openhab.binding.yamahareceiver.internal.state.PlayInfoState;
import org.openhab.binding.yamahareceiver.internal.state.PlayInfoStateListener;
import org.openhab.binding.yamahareceiver.internal.state.PresetInfoState;
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
 */
public class InputWithPlayControlXML implements InputWithPlayControl {
    protected final WeakReference<AbstractConnection> comReference;

    protected final String inputID;

    public static final int PRESET_CHANNELS = 40;

    private PlayInfoStateListener observer;

    /**
     * Create a InputWithPlayControl object for altering menu positions and requesting current menu information as well
     * as controlling the playback and choosing a preset item.
     *
     * @param inputID The input ID like USB or NET_RADIO.
     * @param com The Yamaha communication object to send http requests.
     */
    public InputWithPlayControlXML(String inputID, AbstractConnection com, PlayInfoStateListener observer) {
        this.inputID = inputID;
        this.comReference = new WeakReference<AbstractConnection>(com);
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

        Node playbackInfoNode = XMLUtils.getNode(doc.getFirstChild(), "Play_Info/Playback_Info");
        msg.playbackMode = playbackInfoNode != null ? playbackInfoNode.getTextContent() : "";

        Node metaInfoNode = XMLUtils.getNode(doc.getFirstChild(), "Play_Info/Meta_Info");
        Node sub;

        if (inputID.equals("TUNER")) {
            sub = XMLUtils.getNode(metaInfoNode, "Radio_Text_A");
            msg.station = sub != null ? sub.getTextContent() : "";
        } else {
            sub = XMLUtils.getNode(metaInfoNode, "Station");
            msg.station = sub != null ? sub.getTextContent() : "";
        }

        sub = XMLUtils.getNode(metaInfoNode, "Artist");
        msg.artist = sub != null ? sub.getTextContent() : "";

        sub = XMLUtils.getNode(metaInfoNode, "Album");
        msg.album = sub != null ? sub.getTextContent() : "";

        sub = XMLUtils.getNode(metaInfoNode, "Song");
        msg.song = sub != null ? sub.getTextContent() : "";

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
        comReference.get().send(wrInput("<Play_Control><Playback>Play</Playback></Play_Control>"));
        update();
    }

    /**
     * Stop the currently playing content. Use start() to start again.
     *
     * @throws Exception
     */
    @Override
    public void stop() throws IOException, ReceivedMessageParseException {
        comReference.get().send(wrInput("<Play_Control><Playback>Stop</Playback></Play_Control>"));
        update();
    }

    /**
     * Pause the currently playing content. This is not available for streaming content like on NET_RADIO.
     *
     * @throws Exception
     */
    @Override
    public void pause() throws IOException, ReceivedMessageParseException {
        comReference.get().send(wrInput("<Play_Control><Playback>Pause</Playback></Play_Control>"));
        update();
    }

    /**
     * Skip forward. This is not available for streaming content like on NET_RADIO.
     *
     * @throws Exception
     */
    @Override
    public void skipFF() throws IOException, ReceivedMessageParseException {
        comReference.get().send(wrInput("<Play_Control><Playback>Skip Fwd</Playback></Play_Control>"));
    }

    /**
     * Skip reverse. This is not available for streaming content like on NET_RADIO.
     *
     * @throws Exception
     */
    @Override
    public void skipREV() throws IOException, ReceivedMessageParseException {
        comReference.get().send(wrInput("<Play_Control><Playback>Skip Rev</Playback></Play_Control>"));
    }

    /**
     * Next track. This is not available for streaming content like on NET_RADIO.
     *
     * @throws Exception
     */
    @Override
    public void nextTrack() throws IOException, ReceivedMessageParseException {
        comReference.get().send(wrInput("<Play_Control><Playback>>>|</Playback></Play_Control>"));
        update();
    }

    /**
     * Previous track. This is not available for streaming content like on NET_RADIO.
     *
     * @throws Exception
     */
    @Override
    public void previousTrack() throws IOException, ReceivedMessageParseException {
        comReference.get().send(wrInput("<Play_Control><Playback>|<<</Playback></Play_Control>"));
        update();
    }

}
