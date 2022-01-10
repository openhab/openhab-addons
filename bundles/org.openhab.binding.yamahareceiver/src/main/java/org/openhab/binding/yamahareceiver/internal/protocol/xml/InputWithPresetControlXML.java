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

import static org.openhab.binding.yamahareceiver.internal.protocol.xml.XMLConstants.GET_PARAM;
import static org.openhab.binding.yamahareceiver.internal.protocol.xml.XMLProtocolService.getResponse;
import static org.openhab.binding.yamahareceiver.internal.protocol.xml.XMLUtils.*;

import java.io.IOException;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.openhab.binding.yamahareceiver.internal.protocol.AbstractConnection;
import org.openhab.binding.yamahareceiver.internal.protocol.InputWithPresetControl;
import org.openhab.binding.yamahareceiver.internal.protocol.ReceivedMessageParseException;
import org.openhab.binding.yamahareceiver.internal.state.DeviceInformationState;
import org.openhab.binding.yamahareceiver.internal.state.PlayInfoState;
import org.openhab.binding.yamahareceiver.internal.state.PresetInfoState;
import org.openhab.binding.yamahareceiver.internal.state.PresetInfoStateListener;
import org.slf4j.LoggerFactory;
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
 * @author Tomasz Maruszak - Compatibility fixes
 */
public class InputWithPresetControlXML extends AbstractInputControlXML implements InputWithPresetControl {

    protected CommandTemplate preset = new CommandTemplate(
            "<Play_Control><Preset><Preset_Sel>%s</Preset_Sel></Preset></Play_Control>",
            "Play_Control/Preset/Preset_Sel");

    private final PresetInfoStateListener observer;

    /**
     * Create a InputWithPlayControl object for altering menu positions and requesting current menu information as well
     * as controlling the playback and choosing a preset item.
     *
     * @param inputID The input ID like USB or NET_RADIO.
     * @param con The Yamaha communication object to send http requests.
     */
    public InputWithPresetControlXML(String inputID, AbstractConnection con, PresetInfoStateListener observer,
            DeviceInformationState deviceInformationState) {
        super(LoggerFactory.getLogger(InputWithPresetControlXML.class), inputID, con, deviceInformationState);

        this.observer = observer;

        this.applyModelVariations();
    }

    /**
     * Apply command changes to ensure compatibility with all supported models
     */
    protected void applyModelVariations() {
        if (deviceDescriptor == null) {
            logger.trace("Descriptor not available");
            return;
        }

        // add compatibility adjustments here (if any)
    }

    /**
     * Updates the preset information
     *
     * @throws Exception
     */
    @Override
    public void update() throws IOException, ReceivedMessageParseException {
        if (observer == null) {
            return;
        }

        AbstractConnection con = comReference.get();
        Node response = getResponse(con,
                wrInput("<Play_Control><Preset><Preset_Sel_Item>GetParam</Preset_Sel_Item></Preset></Play_Control>"),
                inputElement);

        PresetInfoState msg = new PresetInfoState();

        // Set preset channel names, obtained from this xpath:
        // NET_RADIO/Play_Control/Preset/Preset_Sel_Item/Item_1/Title
        Node presetNode = getNode(response, "Play_Control/Preset/Preset_Sel_Item");
        if (presetNode != null) {
            for (int i = 1; i <= PRESET_CHANNELS; i++) {
                Node itemNode = getNode(presetNode, "Item_" + i);
                if (itemNode == null) {
                    break;
                }

                String title = getNodeContentOrDefault(itemNode, "Title", "Item_" + i);
                String value = getNodeContentOrDefault(itemNode, "Param", String.valueOf(i));

                // For RX-V3900 when a preset slot is not used, this is how it looks
                if (title.isEmpty() && "Not Used".equalsIgnoreCase(value)) {
                    continue;
                }

                int presetChannel = convertToPresetNumber(value);
                PresetInfoState.Preset preset = new PresetInfoState.Preset(title, presetChannel);
                msg.presetChannelNames.add(preset);
            }
        }
        msg.presetChannelNamesChanged = true;

        String presetValue = getNodeContentOrEmpty(response, preset.getPath());

        // fall back to second method of obtaining current preset (works for Tuner on RX-V3900)
        if (presetValue.isEmpty()) {
            try {
                Node presetResponse = getResponse(con, wrInput(preset.apply(GET_PARAM)), inputElement);
                presetValue = getNodeContentOrEmpty(presetResponse, preset.getPath());
            } catch (IOException | ReceivedMessageParseException e) {
                // this is on purpose, in case the AVR does not support this request and responds with error or nonsense
            }
        }

        // For Tuner input on RX-V3900 this is not a number (e.g. "A1" or "B1").
        msg.presetChannel = convertToPresetNumber(presetValue);

        observer.presetInfoUpdated(msg);
    }

    private int convertToPresetNumber(String presetValue) {
        if (!presetValue.isEmpty()) {
            if (StringUtils.isNumeric(presetValue)) {
                return Integer.parseInt(presetValue);
            } else {
                // special handling for RX-V3900, where 'A1' becomes 101 and 'B2' becomes 202 preset
                if (presetValue.length() >= 2) {
                    Character presetAlpha = presetValue.charAt(0);
                    if (Character.isLetter(presetAlpha) && Character.isUpperCase(presetAlpha)
                            && Character.isDigit(presetValue.charAt(1))) {
                        int presetNumber = Integer.parseInt(presetValue.substring(1));
                        return (ArrayUtils.indexOf(LETTERS, presetAlpha) + 1) * 100 + presetNumber;
                    }
                }
            }
        }
        return -1;
    }

    /**
     * Select a preset channel.
     *
     * @param presetChannel The preset position [1,40]
     * @throws Exception
     */
    @Override
    public void selectItemByPresetNumber(int presetChannel) throws IOException, ReceivedMessageParseException {
        String presetValue;

        // special handling for RX-V3900, where 'A1' becomes 101 and 'B2' becomes 202 preset
        if (presetChannel > 100) {
            int presetNumber = presetChannel % 100;
            char presetAlpha = LETTERS[presetChannel / 100 - 1];
            presetValue = Character.toString(presetAlpha) + presetNumber;
        } else {
            presetValue = Integer.toString(presetChannel);
        }

        String cmd = wrInput(preset.apply(presetValue));
        comReference.get().send(cmd);
        update();
    }

    private static final Character[] LETTERS = new Character[] { 'A', 'B', 'C', 'D' };
}
