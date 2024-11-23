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
package org.openhab.binding.yamahareceiver.internal.protocol.xml;

import static org.openhab.binding.yamahareceiver.internal.protocol.xml.XMLUtils.*;

import java.io.IOException;

import org.openhab.binding.yamahareceiver.internal.protocol.AbstractConnection;
import org.openhab.binding.yamahareceiver.internal.protocol.InputWithPresetControl;
import org.openhab.binding.yamahareceiver.internal.protocol.InputWithTunerBandControl;
import org.openhab.binding.yamahareceiver.internal.protocol.ReceivedMessageParseException;
import org.openhab.binding.yamahareceiver.internal.state.DabBandState;
import org.openhab.binding.yamahareceiver.internal.state.DabBandStateListener;
import org.openhab.binding.yamahareceiver.internal.state.DeviceInformationState;
import org.openhab.binding.yamahareceiver.internal.state.PlayInfoState;
import org.openhab.binding.yamahareceiver.internal.state.PlayInfoStateListener;
import org.openhab.binding.yamahareceiver.internal.state.PresetInfoState;
import org.openhab.binding.yamahareceiver.internal.state.PresetInfoStateListener;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

/**
 * This class implements the Yamaha Receiver protocol related to DAB tuners which allows to control band and preset.
 * This control is specific to dual band tuners only.
 *
 * Note that yamaha maintains separate presets for each band.
 *
 * The XML nodes {@code <DAB><Play_Control><Band>FM</Band></Play_Control></DAB>} are used.
 *
 * No state will be saved in here, but in {@link DabBandState}, {@link PresetInfoState} and {@link PlayInfoState}
 * instead.
 *
 * @author Tomasz Maruszak - Initial contribution, [yamaha] Tuner band selection and preset feature for dual band models
 *         (RX-S601D)
 */
public class InputWithTunerDABControlXML extends AbstractInputControlXML
        implements InputWithTunerBandControl, InputWithPresetControl {

    private static final String BAND_FM = "FM";
    private static final String BAND_DAB = "DAB";

    private final DabBandStateListener observerForBand;
    private final PresetInfoStateListener observerForPreset;
    private final PlayInfoStateListener observerForPlayInfo;

    protected CommandTemplate band = new CommandTemplate("<Play_Control><Band>%s</Band></Play_Control>",
            "Play_Info/Band");
    protected CommandTemplate preset = new CommandTemplate(
            "<Play_Control><%s><Preset><Preset_Sel>%d</Preset_Sel></Preset></%s></Play_Control>", "");

    /**
     * Need to remember last band state to drive the preset
     */
    private DabBandState bandState;

    /**
     * Create an InputWithPlayControl object for altering menu positions and requesting current menu information as well
     * as controlling the playback and choosing a preset item.
     *
     * @param inputID The input ID - TUNER is going to be used here.
     * @param con The Yamaha communication object to send http requests.
     */
    public InputWithTunerDABControlXML(String inputID, AbstractConnection con, DabBandStateListener observerForBand,
            PresetInfoStateListener observerForPreset, PlayInfoStateListener observerForPlayInfo,
            DeviceInformationState deviceInformationState) {
        super(LoggerFactory.getLogger(InputWithTunerDABControlXML.class), inputID, con, deviceInformationState);

        this.inputElement = "DAB";

        this.observerForBand = observerForBand;
        this.observerForPreset = observerForPreset;
        this.observerForPlayInfo = observerForPlayInfo;

        if (observerForBand == null && observerForPreset == null && observerForPlayInfo == null) {
            throw new IllegalArgumentException("At least one observer has to be provided");
        }
    }

    @Override
    public void update() throws IOException, ReceivedMessageParseException {
        Node responseNode = XMLProtocolService.getResponse(comReference.get(),
                wrInput("<Play_Info>GetParam</Play_Info>"), inputElement);

        // @formatter:off

        //Sample response:
        //<YAMAHA_AV rsp="GET" RC="0">
        //    <DAB>
        //        <Play_Info>
        //            <Feature_Availability>Ready</Feature_Availability>
        //            <FM>
        //                <Preset>
        //                    <Preset_Sel>1</Preset_Sel>
        //                </Preset>
        //                <Tuning>
        //                    <Freq>
        //                        <Val>9945</Val>
        //                        <Exp>2</Exp>
        //                        <Unit>MHz</Unit>
        //                    </Freq>
        //                </Tuning>
        //                <FM_Mode>Auto</FM_Mode>
        //                <Signal_Info>
        //                    <Tuned>Assert</Tuned>
        //                    <Stereo>Assert</Stereo>
        //                </Signal_Info>
        //                <Meta_Info>
        //                    <Program_Type>POP M</Program_Type>
        //                    <Program_Service>  22:59</Program_Service>
        //                    <Radio_Text>tel. 22 333 33 33   * Trojka *   e-mail: trojka@polskieradio.pl</Radio_Text>
        //                    <Clock_Time>22:59</Clock_Time>
        //                </Meta_Info>
        //            </FM>
        //            <DAB>
        //                <Status>Ready</Status>
        //                <Preset>
        //                    <Preset_Sel>No Preset</Preset_Sel>
        //                </Preset>
        //                <ID>2</ID>
        //                <Signal_Info>
        //                    <Freq>
        //                        <Val>218640</Val>
        //                        <Exp>3</Exp>
        //                        <Unit>MHz</Unit>
        //                    </Freq>
        //                    <Category>Primary</Category>
        //                    <Audio_Mode>Stereo</Audio_Mode>
        //                    <Bit_Rate>
        //                        <Val>128</Val>
        //                        <Exp>0</Exp>
        //                        <Unit>Kbps</Unit>
        //                    </Bit_Rate>
        //                    <Quality>82</Quality>
        //                    <Tune_Aid>45</Tune_Aid>
        //                    <Off_Air>Negate</Off_Air>
        //                    <DAB_PLUS>Assert</DAB_PLUS>
        //                </Signal_Info>
        //                <Meta_Info>
        //                    <Ch_Label>11B</Ch_Label>
        //                    <Service_Label>PR Czwórka</Service_Label>
        //                    <DLS>Kluboteka  Polskie Radio S.A.</DLS>
        //                    <Ensemble_Label>Polskie Radio</Ensemble_Label>
        //                    <Program_Type>Pop</Program_Type>
        //                    <Date_and_Time>12AUG&apos;17 23:47</Date_and_Time>
        //                </Meta_Info>
        //            </DAB>
        //            <Band>FM</Band>
        //        </Play_Info>
        //    </DAB>
        //</YAMAHA_AV>

        // @formatter:on

        DabBandState msgForBand = new DabBandState();
        PresetInfoState msgForPreset = new PresetInfoState();
        PlayInfoState msgForPlayInfo = new PlayInfoState();

        msgForBand.band = getNodeContentOrDefault(responseNode, "Play_Info/Band", msgForBand.band);
        logger.debug("Band set to {} for input {}", msgForBand.band, inputID);

        // store last state of band
        bandState = msgForBand;

        if (msgForBand.band.isEmpty()) {
            logger.warn("Band is unknown for input {}, therefore preset and playback information will not be available",
                    inputID);
        } else {
            Node playInfoNode = getNode(responseNode, "Play_Info/" + msgForBand.band);

            msgForPreset.presetChannel = getNodeContentOrDefault(playInfoNode, "Preset/Preset_Sel", -1);
            logger.debug("Preset set to {} for input {}", msgForPreset.presetChannel, inputID);

            Node metaInfoNode = getNode(playInfoNode, "Meta_Info");
            if (metaInfoNode != null) {
                msgForPlayInfo.album = getNodeContentOrDefault(metaInfoNode, "Program_Type", msgForPlayInfo.album);
                if (BAND_DAB.equals(msgForBand.band)) {
                    msgForPlayInfo.station = getNodeContentOrDefault(metaInfoNode, "Service_Label",
                            msgForPlayInfo.station);
                    msgForPlayInfo.artist = getNodeContentOrDefault(metaInfoNode, "Ensemble_Label",
                            msgForPlayInfo.artist);
                    msgForPlayInfo.song = getNodeContentOrDefault(metaInfoNode, "DLS", msgForPlayInfo.song);
                } else {
                    msgForPlayInfo.station = getNodeContentOrDefault(metaInfoNode, "Program_Service",
                            msgForPlayInfo.station);
                    msgForPlayInfo.artist = getNodeContentOrDefault(metaInfoNode, "Station", msgForPlayInfo.artist);
                    msgForPlayInfo.song = getNodeContentOrDefault(metaInfoNode, "Radio_Text", msgForPlayInfo.song);
                }
            }
        }

        // DAB does not provide channel names, the channel list will be empty
        msgForPreset.presetChannelNamesChanged = true;

        if (observerForBand != null) {
            observerForBand.dabBandUpdated(msgForBand);
        }
        if (observerForPreset != null) {
            observerForPreset.presetInfoUpdated(msgForPreset);
        }
        if (observerForPlayInfo != null) {
            observerForPlayInfo.playInfoUpdated(msgForPlayInfo);
        }
    }

    @Override
    public void selectBandByName(String band) throws IOException, ReceivedMessageParseException {
        // Example: <Play_Control><Band>FM</Band></Play_Control>
        String cmd = this.band.apply(band);
        comReference.get().send(wrInput(cmd));
        update();
    }

    @Override
    public void selectItemByPresetNumber(int presetChannel) throws IOException, ReceivedMessageParseException {
        if (bandState == null || bandState.band == null || bandState.band.isEmpty()) {
            logger.warn("Cannot change preset because the band is unknown for input {}", inputID);
            return;
        }

        // Example: <Play_Control><FM><Preset><Preset_Sel>2</Preset_Sel></Preset></FM></Play_Control>
        String cmd = this.preset.apply(bandState.band, presetChannel, bandState.band);
        comReference.get().send(wrInput(cmd));
        update();
    }
}
