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

import org.openhab.binding.yamahareceiver.internal.protocol.AbstractConnection;
import org.openhab.binding.yamahareceiver.internal.protocol.InputWithPresetControl;
import org.openhab.binding.yamahareceiver.internal.protocol.ReceivedMessageParseException;
import org.openhab.binding.yamahareceiver.internal.state.PlayInfoState;
import org.openhab.binding.yamahareceiver.internal.state.PresetInfoState;
import org.openhab.binding.yamahareceiver.internal.state.PresetInfoStateListener;
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
public class InputWithPresetControlXML implements InputWithPresetControl {
    protected final WeakReference<AbstractConnection> comReference;

    protected final String inputID;

    public static final int PRESET_CHANNELS = 40;

    private PresetInfoStateListener observer;

    /**
     * Create a InputWithPlayControl object for altering menu positions and requesting current menu information as well
     * as controlling the playback and choosing a preset item.
     *
     * @param inputID The input ID like USB or NET_RADIO.
     * @param com The Yamaha communication object to send http requests.
     */
    public InputWithPresetControlXML(String inputID, AbstractConnection com, PresetInfoStateListener observer) {
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
     * Updates the preset information
     *
     * @throws Exception
     */
    public void update() throws IOException, ReceivedMessageParseException {
        if (observer == null) {
            return;
        }

        AbstractConnection com = comReference.get();
        String response = com.sendReceive(wrInput("<Play_Control>GetParam</Play_Control>"));
        Document doc = XMLUtils.xml(response);
        if (doc.getFirstChild() == null) {
            throw new ReceivedMessageParseException("<Play_Control>GetParam failed: " + response);
        }

        PresetInfoState msg = new PresetInfoState();

        Node playbackInfoNode = XMLUtils.getNode(doc.getFirstChild(), "Play_Control/Preset/Preset_Sel");
        msg.presetChannel = playbackInfoNode != null ? Integer.valueOf(playbackInfoNode.getTextContent()) : -1;

        // Set preset channel names, obtained from this xpath:
        // NET_RADIO/Play_Control/Preset/Preset_Sel_Item/Item_1/Title
        playbackInfoNode = XMLUtils.getNode(doc.getFirstChild(), "Play_Control/Preset/Preset_Sel_Item");
        if (playbackInfoNode != null) {
            for (int i = 1; i <= PRESET_CHANNELS; ++i) {
                Node itemNode = XMLUtils.getNode(playbackInfoNode, "Item_" + String.valueOf(i) + "/Title");
                String v = itemNode != null ? itemNode.getTextContent() : "Item_" + String.valueOf(i);
                if (!v.equals(msg.presetChannelNames[i - 1])) {
                    msg.presetChannelNamesChanged = true;
                    msg.presetChannelNames[i - 1] = v;
                }
            }
        }

        observer.presetInfoUpdated(msg);
    }

    /**
     * Select a preset channel.
     *
     * @param number The preset position [1,40]
     * @throws Exception
     */
    public void selectItemByPresetNumber(int presetChannel) throws IOException, ReceivedMessageParseException {
        comReference.get().send(wrInput(
                "<Play_Control><Preset><Preset_Sel>" + presetChannel + "</Preset_Sel></Preset></Play_Control>"));
        update();
    }
}
