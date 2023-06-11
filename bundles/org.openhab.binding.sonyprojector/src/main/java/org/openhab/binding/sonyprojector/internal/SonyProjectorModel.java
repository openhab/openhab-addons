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
package org.openhab.binding.sonyprojector.internal;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.sonyprojector.internal.communication.SonyProjectorAspect;
import org.openhab.binding.sonyprojector.internal.communication.SonyProjectorBlockNr;
import org.openhab.binding.sonyprojector.internal.communication.SonyProjectorCalibrationPreset;
import org.openhab.binding.sonyprojector.internal.communication.SonyProjectorColorSpace;
import org.openhab.binding.sonyprojector.internal.communication.SonyProjectorColorTemp;
import org.openhab.binding.sonyprojector.internal.communication.SonyProjectorContrastEnhancer;
import org.openhab.binding.sonyprojector.internal.communication.SonyProjectorFilmMode;
import org.openhab.binding.sonyprojector.internal.communication.SonyProjectorFilmProjection;
import org.openhab.binding.sonyprojector.internal.communication.SonyProjectorGammaCorrection;
import org.openhab.binding.sonyprojector.internal.communication.SonyProjectorInput;
import org.openhab.binding.sonyprojector.internal.communication.SonyProjectorIrisMode;
import org.openhab.binding.sonyprojector.internal.communication.SonyProjectorIrisSensitivity;
import org.openhab.binding.sonyprojector.internal.communication.SonyProjectorLampControl;
import org.openhab.binding.sonyprojector.internal.communication.SonyProjectorMosquitoNr;
import org.openhab.binding.sonyprojector.internal.communication.SonyProjectorMotionEnhancer;
import org.openhab.binding.sonyprojector.internal.communication.SonyProjectorMpegNr;
import org.openhab.binding.sonyprojector.internal.communication.SonyProjectorNr;
import org.openhab.binding.sonyprojector.internal.communication.SonyProjectorPicturePosition;
import org.openhab.core.types.StateOption;

/**
 * Represents the different supported projector models
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public enum SonyProjectorModel {

    // HW models

    HW10("VPL-HW10", false, 2, 2, 2, 1, true, 0, true, 2, 3, 3, true, true, 2, 2, true, true, 0, true, 3, 0, 0),
    HW15("VPL-HW15", false, 2, 2, 2, 1, true, 0, true, 2, 3, 3, true, true, 3, 2, true, true, 0, true, 3, 0, 0),
    HW20("VPL-HW20", false, 2, 2, 2, 1, true, 0, true, 2, 3, 3, true, true, 3, 2, true, true, 0, true, 3, 0, 0),
    HW30ES("VPL-HW30ES", false, 4, 3, 3, 2, true, 0, true, 2, 4, 3, true, true, 4, 2, true, true, 0, true, 4, 0, 0),
    HW35ES("VPL-HW35ES", false, 1, 3, 1, 2, true, 0, true, 5, 6, 0, false, false, 6, 2, false, false, 2, true, 6, 3, 2),
    HW40ES("VPL-HW40ES", false, 1, 3, 1, 2, true, 0, true, 5, 6, 0, false, false, 6, 2, false, false, 2, true, 6, 3, 2),
    HW45ES("VPL-HW45ES", false, 1, 1, 1, 1, true, 0, false, 6, 6, 0, false, false, 6, 2, false, false, 2, true, 6, 0,
            1),
    HW50ES("VPL-HW50ES", false, 1, 3, 1, 2, true, 0, true, 5, 7, 5, true, false, 1, 2, false, false, 2, true, 6, 3, 2),
    HW55ES("VPL-HW55ES", false, 1, 3, 1, 2, true, 0, true, 5, 7, 5, true, false, 1, 2, false, false, 2, true, 6, 3, 2),
    HW58ES("VPL-HW58ES", false, 1, 3, 1, 2, true, 0, true, 5, 6, 0, false, false, 6, 2, false, false, 2, true, 6, 3, 2),
    HW60("VPL-HW60", true, 1, 1, 1, 1, true, 0, false, 6, 1, 1, true, false, 1, 1, false, false, 1, true, 1, 0, 1),
    HW65("VPL-HW65", true, 1, 1, 1, 1, true, 0, false, 6, 1, 1, true, false, 1, 1, false, false, 1, true, 1, 0, 1),
    HW68("VPL-HW68", true, 1, 1, 1, 1, true, 0, false, 6, 1, 1, true, false, 1, 1, false, false, 1, true, 1, 0, 1),

    // VW models

    // VW10HT("VPL-VW10HT", false, 2, 1, 1, 1, true, 0, true, 1, 1, 1, true, false, 2, 2, true, true, 0, true, 3, 0, 0),
    // VW11HT("VPL-VW11HT", false, 2, 1, 1, 1, true, 0, true, 1, 1, 1, true, false, 2, 2, true, true, 0, true, 3, 0, 0),
    // VW12HT("VPL-VW12HT", false, 2, 1, 1, 1, true, 0, true, 1, 1, 1, true, false, 2, 2, true, true, 0, true, 3, 0, 0),

    VW40("VPL-VW40", false, 2, 2, 2, 0, true, 0, true, 2, 2, 2, true, true, 2, 2, false, false, 0, false, 3, 0, 0),
    VW50("VPL-VW50", false, 2, 2, 2, 0, true, 0, true, 2, 2, 2, true, true, 2, 2, false, false, 0, false, 3, 0, 0),
    VW60("VPL-VW60", false, 2, 2, 2, 0, true, 0, true, 3, 2, 2, true, true, 2, 2, false, false, 0, false, 3, 0, 0),
    VW70("VPL-VW70", false, 2, 2, 2, 1, true, 0, true, 3, 3, 3, true, true, 3, 2, true, true, 0, true, 3, 0, 0),
    VW80("VPL-VW80", false, 2, 2, 2, 2, true, 0, true, 3, 3, 3, true, true, 3, 2, true, true, 0, true, 3, 1, 2),
    VW85("VPL-VW85", false, 3, 2, 3, 2, true, 0, true, 3, 4, 3, true, true, 5, 2, true, true, 0, true, 4, 1, 2),
    VW90("VPL-VW90ES", false, 3, 5, 3, 2, true, 0, true, 3, 4, 3, true, true, 5, 2, true, true, 0, true, 4, 2, 2),
    VW95("VPL-VW95ES", false, 4, 3, 3, 2, true, 2, true, 3, 4, 3, true, true, 5, 2, true, true, 0, true, 4, 2, 2),

    VW100("VPL-VW100", false, 2, 4, 2, 3, false, 0, true, 4, 2, 4, false, false, 2, 2, false, false, 0, false, 3, 0, 0),

    VW200("VPL-VW200", false, 2, 2, 2, 2, false, 0, true, 3, 2, 3, true, true, 2, 2, false, false, 0, true, 3, 1, 2),
    VW260("VPL-VW260ES", true, 1, 1, 1, 1, true, 0, false, 1, 1, 0, false, false, 1, 1, false, false, 1, true, 2, 0, 1),
    VW270("VPL-VW270ES", true, 1, 1, 1, 1, true, 0, false, 1, 1, 0, false, false, 1, 1, false, false, 1, true, 2, 0, 3),
    VW285("VPL-VW285ES", true, 1, 1, 1, 1, true, 0, false, 1, 1, 0, false, false, 1, 1, false, false, 1, true, 2, 0, 1),
    VW295("VPL-VW295ES", true, 1, 1, 1, 1, true, 0, false, 1, 1, 0, false, false, 1, 1, false, false, 1, true, 2, 0, 3),

    VW300("VPL-VW300ES", true, 1, 1, 1, 1, true, 0, false, 1, 1, 0, false, false, 1, 1, false, false, 1, true, 1, 0, 1),
    VW315("VPL-VW315", true, 1, 1, 1, 1, true, 0, false, 1, 1, 0, false, false, 1, 1, false, false, 1, true, 1, 0, 1),
    VW320("VPL-VW320", true, 1, 1, 1, 1, true, 0, false, 1, 1, 0, false, false, 1, 1, false, false, 1, true, 1, 0, 1),
    VW328("VPL-VW328", true, 1, 1, 1, 1, true, 0, false, 1, 1, 0, false, false, 1, 1, false, false, 1, true, 1, 0, 1),
    VW350("VPL-VW350ES", true, 1, 1, 1, 1, true, 0, false, 1, 1, 0, false, false, 1, 1, false, false, 1, true, 1, 0, 1),
    // VW360("VPL-VW360ES", false, 2, 4, 2, 3, true, 0, true, 1, 1, 1, true, false, 1, 2, false, false, 1, true, 1, 0,
    // 1),
    VW365("VPL-VW365", true, 1, 1, 1, 1, true, 0, false, 1, 1, 0, false, false, 1, 1, false, false, 1, true, 1, 0, 1),
    VW385("VPL-VW385ES", true, 1, 1, 1, 1, true, 1, false, 1, 1, 1, true, false, 1, 1, false, false, 1, true, 2, 0, 1),

    VW500("VPL-VW500ES", true, 1, 1, 1, 1, true, 1, false, 1, 1, 1, true, false, 1, 1, false, false, 1, true, 1, 0, 1),
    VW515("VPL-VW515", true, 1, 1, 1, 1, true, 1, false, 1, 1, 1, true, false, 1, 1, false, false, 1, true, 2, 0, 1),
    VW520("VPL-VW520", true, 1, 1, 1, 1, true, 1, false, 1, 1, 1, true, false, 1, 1, false, false, 1, true, 2, 0, 1),
    VW528("VPL-VW528", true, 1, 1, 1, 1, true, 1, false, 1, 1, 1, true, false, 1, 1, false, false, 1, true, 2, 0, 1),
    VW550("VPL-VW550ES", true, 1, 1, 1, 1, true, 1, false, 1, 1, 1, true, false, 1, 1, false, false, 1, true, 1, 0, 1),
    VW570("VPL-VW570ES", true, 1, 1, 1, 1, true, 1, false, 1, 1, 1, true, false, 1, 1, false, false, 1, true, 2, 0, 1),

    VW600("VPL-VW600ES", true, 1, 1, 1, 1, true, 1, false, 1, 1, 1, true, false, 1, 1, false, false, 1, true, 1, 0, 1),
    VW665("VPL-VW665", true, 1, 1, 1, 1, true, 1, false, 1, 1, 1, true, false, 1, 1, false, false, 1, true, 2, 0, 1),
    VW675("VPL-VW675ES", true, 1, 1, 1, 1, true, 1, false, 1, 1, 1, true, false, 1, 1, false, false, 1, true, 2, 0, 1),
    // VW685("VPL-VW685ES", true, 1, 1, 1, 1, true, 1, false, 1, 1, 1, true, false, 1, 1, false, false, 1, true, 2, 0,
    // 1),
    VW695("VPL-VW695ES", true, 1, 1, 1, 1, true, 1, false, 1, 1, 1, true, false, 1, 1, false, false, 1, true, 2, 0, 1),

    VW760("VPL-VW760ES", true, 1, 1, 1, 1, false, 1, false, 1, 1, 1, false, false, 1, 1, false, false, 1, true, 2, 0,
            1),

    VW870("VPL-VW870ES", true, 1, 1, 1, 1, false, 1, false, 1, 1, 1, false, false, 1, 1, false, false, 1, true, 2, 0,
            1),
    VW885("VPL-VW885ES", true, 1, 1, 1, 1, false, 1, false, 1, 1, 1, false, false, 1, 1, false, false, 1, true, 2, 0,
            1),

    VW995("VPL-VW995ES", true, 1, 1, 1, 1, false, 1, false, 1, 1, 1, false, false, 1, 1, false, false, 1, true, 2, 0,
            1),

    VW1000ES("VPL-VW1000ES", false, 5, 3, 1, 2, true, 1, true, 1, 5, 5, true, false, 1, 2, false, false, 2, true, 5, 3,
            2),
    VW1100ES("VPL-VW1100ES", false, 5, 3, 1, 2, true, 1, true, 1, 5, 5, true, false, 1, 2, false, false, 2, true, 5, 3,
            2);

    // VW5000ES("VPL-VW5000ES", false, 5, 3, 1, 2, true, 0, true, 1, 5, 5, true, false, 1, 2, false, false, 2, true, 5,
    // 3, 2);

    private String name;
    private boolean powerCmdAvailable;
    private int calibrPresetsCategory;
    private int inputCategory;
    private int contrastEnhancerCategory;
    private int filmModeCategory;
    private boolean lampControlAvailable;
    private int picturePositionCategory;
    private boolean overscanAvailable;
    private int aspectCategory;
    private int colorTempCategory;
    private int irisModeCategory;
    private boolean irisManualAvailable;
    private boolean irisSensitivityAvailable;
    private int gammaCorrectionCategory;
    private int nrCategory;
    private boolean blockNrAvailable;
    private boolean mosquitoNrAvailable;
    private int mpegNrCategory;
    private boolean xvColorAvailable;
    private int colorSpaceCategory;
    private int filmProjectionCategory;
    private int motionEnhancerCategory;

    /**
     * Constructor
     *
     * @param name the model name
     * @param powerCmdAvailable true if the POWER command is available
     * @param calibrPresetsCategory the category from {@link SonyProjectorCalibrationPreset}
     * @param inputCategory the category from {@link SonyProjectorInput}
     * @param contrastEnhancerCategory the category from {@link SonyProjectorContrastEnhancer}
     * @param filmModeCategory the category from {@link SonyProjectorFilmMode}
     * @param lampControlAvailable true if the lamp control setting is available
     * @param picturePositionCategory the category from {@link SonyProjectorPicturePosition}
     * @param overscanAvailable true if the overscan setting is available
     * @param aspectCategory the category from {@link SonyProjectorAspect}
     * @param colorTempCategory the category from {@link SonyProjectorColorTemp}
     * @param irisModeCategory the category from {@link SonyProjectorIrisMode}
     * @param irisManualAvailable true if the iris manual setting is available
     * @param irisSensitivityAvailable true if the iris sensitivity setting is available
     * @param gammaCorrectionCategory the category from {@link SonyProjectorGammaCorrection}
     * @param nrCategory the category from {@link SonyProjectorNr}
     * @param blockNrAvailable true if the block noise reduction setting is available
     * @param mosquitoNrAvailable true if the mosquito noise reduction setting is available
     * @param mpegNrCategory the category from {@link SonyProjectorMpegNr}
     * @param xvColorAvailable true if the xvColor setting is available
     * @param colorSpaceCategory the category from {@link SonyProjectorColorSpace}
     * @param filmProjectionCategory the category from {@link SonyProjectorFilmProjection}
     * @param motionEnhancerCategory the category from {@link SonyProjectorMotionEnhancer}
     */
    private SonyProjectorModel(String name, boolean powerCmdAvailable, int calibrPresetsCategory, int inputCategory,
            int contrastEnhancerCategory, int filmModeCategory, boolean lampControlAvailable,
            int picturePositionCategory, boolean overscanAvailable, int aspectCategory, int colorTempCategory,
            int irisModeCategory, boolean irisManualAvailable, boolean irisSensitivityAvailable,
            int gammaCorrectionCategory, int nrCategory, boolean blockNrAvailable, boolean mosquitoNrAvailable,
            int mpegNrCategory, boolean xvColorAvailable, int colorSpaceCategory, int filmProjectionCategory,
            int motionEnhancerCategory) {
        this.name = name;
        this.powerCmdAvailable = powerCmdAvailable;
        this.calibrPresetsCategory = calibrPresetsCategory;
        this.inputCategory = inputCategory;
        this.contrastEnhancerCategory = contrastEnhancerCategory;
        this.filmModeCategory = filmModeCategory;
        this.lampControlAvailable = lampControlAvailable;
        this.picturePositionCategory = picturePositionCategory;
        this.overscanAvailable = overscanAvailable;
        this.aspectCategory = aspectCategory;
        this.colorTempCategory = colorTempCategory;
        this.irisModeCategory = irisModeCategory;
        this.irisManualAvailable = irisManualAvailable;
        this.irisSensitivityAvailable = irisSensitivityAvailable;
        this.gammaCorrectionCategory = gammaCorrectionCategory;
        this.nrCategory = nrCategory;
        this.blockNrAvailable = blockNrAvailable;
        this.mosquitoNrAvailable = mosquitoNrAvailable;
        this.mpegNrCategory = mpegNrCategory;
        this.xvColorAvailable = xvColorAvailable;
        this.colorSpaceCategory = colorSpaceCategory;
        this.filmProjectionCategory = filmProjectionCategory;
        this.motionEnhancerCategory = motionEnhancerCategory;
    }

    /**
     * Get the model name
     *
     * @return the model name
     */
    public String getName() {
        return name;
    }

    /**
     * Inform whether the POWER command is available
     *
     * @return true if the POWER command is available
     */
    public boolean isPowerCmdAvailable() {
        return powerCmdAvailable;
    }

    /**
     * Get the list of {@link StateOption} associated to the available calibration presets
     *
     * @return the list of {@link StateOption} associated to the available calibration presets
     */
    public List<StateOption> getCalibrPresetStateOptions() {
        return SonyProjectorCalibrationPreset.getStateOptions(calibrPresetsCategory);
    }

    /**
     * Get the calibration preset associated to a name
     *
     * @param name the name used to identify the calibration preset
     *
     * @return the calibration preset associated to the searched name
     *
     * @throws SonyProjectorException - If no calibration preset is associated to the searched name
     */
    public byte[] getCalibrPresetDataCodeFromName(String name) throws SonyProjectorException {
        return SonyProjectorCalibrationPreset.getFromName(calibrPresetsCategory, name).getDataCode();
    }

    /**
     * Get the calibration preset associated to a data code
     *
     * @param dataCode the data code used to identify the calibration preset
     *
     * @return the calibration preset associated to the searched data code
     *
     * @throws SonyProjectorException - If no calibration preset is associated to the searched data code
     */
    public String getCalibrPresetNameFromDataCode(byte[] dataCode) throws SonyProjectorException {
        return SonyProjectorCalibrationPreset.getFromDataCode(calibrPresetsCategory, dataCode).getName();
    }

    /**
     * Get the list of {@link StateOption} associated to the available video inputs
     *
     * @return the list of {@link StateOption} associated to the available video inputs
     */
    public List<StateOption> getInputStateOptions() {
        return SonyProjectorInput.getStateOptions(inputCategory);
    }

    /**
     * Get the video input associated to a name
     *
     * @param name the name used to identify the video input
     *
     * @return the video input associated to the searched name
     *
     * @throws SonyProjectorException - If no video input is associated to the searched name
     */
    public byte[] getInputDataCodeFromName(String name) throws SonyProjectorException {
        return SonyProjectorInput.getFromName(inputCategory, name).getDataCode();
    }

    /**
     * Get the video input associated to a data code
     *
     * @param dataCode the data code used to identify the video input
     *
     * @return the video input associated to the searched data code
     *
     * @throws SonyProjectorException - If no video input is associated to the searched data code
     */
    public String getInputNameFromDataCode(byte[] dataCode) throws SonyProjectorException {
        return SonyProjectorInput.getFromDataCode(inputCategory, dataCode).getName();
    }

    /**
     * Get the list of {@link StateOption} associated to the available contrast enhancer modes
     *
     * @return the list of {@link StateOption} associated to the available contrast enhancer modes
     */
    public List<StateOption> getContrastEnhancerStateOptions() {
        return SonyProjectorContrastEnhancer.getStateOptions(contrastEnhancerCategory);
    }

    /**
     * Get the contrast enhancer mode associated to a name
     *
     * @param name the name used to identify the contrast enhancer mode
     *
     * @return the contrast enhancer mode associated to the searched name
     *
     * @throws SonyProjectorException - If no contrast enhancer mode is associated to the searched name
     */
    public byte[] getContrastEnhancerDataCodeFromName(String name) throws SonyProjectorException {
        return SonyProjectorContrastEnhancer.getFromName(contrastEnhancerCategory, name).getDataCode();
    }

    /**
     * Get the contrast enhancer mode associated to a data code
     *
     * @param dataCode the data code used to identify the contrast enhancer mode
     *
     * @return the contrast enhancer mode associated to the searched data code
     *
     * @throws SonyProjectorException - If no contrast enhancer mode is associated to the searched data code
     */
    public String getContrastEnhancerNameFromDataCode(byte[] dataCode) throws SonyProjectorException {
        return SonyProjectorContrastEnhancer.getFromDataCode(contrastEnhancerCategory, dataCode).getName();
    }

    /**
     * Get the list of {@link StateOption} associated to the available film modes
     *
     * @return the list of {@link StateOption} associated to the available film modes
     */
    public List<StateOption> getFilmModeStateOptions() {
        return SonyProjectorFilmMode.getStateOptions(filmModeCategory);
    }

    /**
     * Inform whether the film mode setting is available
     *
     * @return true if the film mode setting is available
     */
    public boolean isFilmModeAvailable() {
        return filmModeCategory > 0;
    }

    /**
     * Get the film mode associated to a name
     *
     * @param name the name used to identify the film mode
     *
     * @return the film mode associated to the searched name
     *
     * @throws SonyProjectorException - If no film mode is associated to the searched name
     */
    public byte[] getFilmModeDataCodeFromName(String name) throws SonyProjectorException {
        return SonyProjectorFilmMode.getFromName(filmModeCategory, name).getDataCode();
    }

    /**
     * Get the film mode associated to a data code
     *
     * @param dataCode the data code used to identify the film mode
     *
     * @return the film mode associated to the searched data code
     *
     * @throws SonyProjectorException - If no film mode is associated to the searched data code
     */
    public String getFilmModeNameFromDataCode(byte[] dataCode) throws SonyProjectorException {
        return SonyProjectorFilmMode.getFromDataCode(filmModeCategory, dataCode).getName();
    }

    /**
     * Get the list of {@link StateOption} associated to the available lamp control modes
     *
     * @return the list of {@link StateOption} associated to the available lamp control modes
     */
    public List<StateOption> getLampControlStateOptions() {
        return lampControlAvailable ? SonyProjectorLampControl.getStateOptions() : new ArrayList<>();
    }

    /**
     * Inform whether the lamp control setting is available
     *
     * @return true if the lamp control setting is available
     */
    public boolean isLampControlAvailable() {
        return lampControlAvailable;
    }

    /**
     * Get the list of {@link StateOption} associated to the available picture positions
     *
     * @return the list of {@link StateOption} associated to the available picture positions
     */
    public List<StateOption> getPicturePositionStateOptions() {
        return SonyProjectorPicturePosition.getStateOptions(picturePositionCategory);
    }

    /**
     * Inform whether the picture position setting is available
     *
     * @return true if the picture position setting is available
     */
    public boolean isPicturePositionAvailable() {
        return picturePositionCategory > 0;
    }

    /**
     * Get the picture position associated to a name
     *
     * @param name the name used to identify the picture position
     *
     * @return the picture position associated to the searched name
     *
     * @throws SonyProjectorException - If no picture position is associated to the searched name
     */
    public byte[] getPicturePositionCodeFromName(String name) throws SonyProjectorException {
        return SonyProjectorPicturePosition.getFromName(picturePositionCategory, name).getDataCode();
    }

    /**
     * Get the picture position associated to a data code
     *
     * @param dataCode the data code used to identify the picture position
     *
     * @return the picture position associated to the searched data code
     *
     * @throws SonyProjectorException - If no picture position is associated to the searched data code
     */
    public String getPicturePositionNameFromDataCode(byte[] dataCode) throws SonyProjectorException {
        return SonyProjectorPicturePosition.getFromDataCode(picturePositionCategory, dataCode).getName();
    }

    /**
     * Get the list of {@link StateOption} associated to the available aspect modes
     *
     * @return the list of {@link StateOption} associated to the available aspect modes
     */
    public List<StateOption> getAspectStateOptions() {
        return SonyProjectorAspect.getStateOptions(aspectCategory);
    }

    /**
     * Get the aspect mode associated to a name
     *
     * @param name the name used to identify the aspect mode
     *
     * @return the aspect mode associated to the searched name
     *
     * @throws SonyProjectorException - If no aspect mode is associated to the searched name
     */
    public byte[] getAspectCodeFromName(String name) throws SonyProjectorException {
        return SonyProjectorAspect.getFromName(aspectCategory, name).getDataCode();
    }

    /**
     * Get the aspect mode associated to a data code
     *
     * @param dataCode the data code used to identify the aspect mode
     *
     * @return the aspect mode associated to the searched data code
     *
     * @throws SonyProjectorException - If no aspect mode is associated to the searched data code
     */
    public String getAspectNameFromDataCode(byte[] dataCode) throws SonyProjectorException {
        return SonyProjectorAspect.getFromDataCode(aspectCategory, dataCode).getName();
    }

    /**
     * Get the list of {@link StateOption} associated to the available color temperatures
     *
     * @return the list of {@link StateOption} associated to the available color temperatures
     */
    public List<StateOption> getColorTempStateOptions() {
        return SonyProjectorColorTemp.getStateOptions(colorTempCategory);
    }

    /**
     * Get the color temperature associated to a name
     *
     * @param name the name used to identify the color temperature
     *
     * @return the color temperature associated to the searched name
     *
     * @throws SonyProjectorException - If no color temperature is associated to the searched name
     */
    public byte[] getColorTempCodeFromName(String name) throws SonyProjectorException {
        return SonyProjectorColorTemp.getFromName(colorTempCategory, name).getDataCode();
    }

    /**
     * Get the color temperature associated to a data code
     *
     * @param dataCode the data code used to identify the color temperature
     *
     * @return the color temperature associated to the searched data code
     *
     * @throws SonyProjectorException - If no color temperature is associated to the searched data code
     */
    public String getColorTempNameFromDataCode(byte[] dataCode) throws SonyProjectorException {
        return SonyProjectorColorTemp.getFromDataCode(colorTempCategory, dataCode).getName();
    }

    /**
     * Get the list of {@link StateOption} associated to the available iris modes
     *
     * @return the list of {@link StateOption} associated to the available iris modes
     */
    public List<StateOption> getIrisModeStateOptions() {
        return SonyProjectorIrisMode.getStateOptions(irisModeCategory);
    }

    /**
     * Inform whether the iris mode setting is available
     *
     * @return true if the iris mode setting is available
     */
    public boolean isIrisModeAvailable() {
        return irisModeCategory > 0;
    }

    /**
     * Get the iris mode associated to a name
     *
     * @param name the name used to identify the iris mode
     *
     * @return the iris mode associated to the searched name
     *
     * @throws SonyProjectorException - If no iris mode is associated to the searched name
     */
    public byte[] getIrisModeCodeFromName(String name) throws SonyProjectorException {
        return SonyProjectorIrisMode.getFromName(irisModeCategory, name).getDataCode();
    }

    /**
     * Get the iris mode associated to a data code
     *
     * @param dataCode the data code used to identify the iris mode
     *
     * @return the iris mode associated to the searched data code
     *
     * @throws SonyProjectorException - If no iris mode is associated to the searched data code
     */
    public String getIrisModeNameFromDataCode(byte[] dataCode) throws SonyProjectorException {
        return SonyProjectorIrisMode.getFromDataCode(irisModeCategory, dataCode).getName();
    }

    /**
     * Inform whether the overscan setting is available
     *
     * @return true if the overscan setting is available
     */
    public boolean isOverscanAvailable() {
        return overscanAvailable;
    }

    /**
     * Inform whether the iris manual setting is available
     *
     * @return true if the iris manual setting is available
     */
    public boolean isIrisManualAvailable() {
        return irisManualAvailable;
    }

    /**
     * Get the list of {@link StateOption} associated to the available iris sensitivities
     *
     * @return the list of {@link StateOption} associated to the available iris sensitivities
     */
    public List<StateOption> getIrisSensitivityStateOptions() {
        return irisSensitivityAvailable ? SonyProjectorIrisSensitivity.getStateOptions() : new ArrayList<>();
    }

    /**
     * Inform whether the iris sensitivity setting is available
     *
     * @return true if the iris sensitivity setting is available
     */
    public boolean isIrisSensitivityAvailable() {
        return irisSensitivityAvailable;
    }

    /**
     * Get the list of {@link StateOption} associated to the available gamma corrections
     *
     * @return the list of {@link StateOption} associated to the available gamma corrections
     */
    public List<StateOption> getGammaCorrectionStateOptions() {
        return SonyProjectorGammaCorrection.getStateOptions(gammaCorrectionCategory);
    }

    /**
     * Get the gamma correction associated to a name
     *
     * @param name the name used to identify the gamma correction
     *
     * @return the gamma correction associated to the searched name
     *
     * @throws SonyProjectorException - If no gamma correction is associated to the searched name
     */
    public byte[] getGammaCorrectionCodeFromName(String name) throws SonyProjectorException {
        return SonyProjectorGammaCorrection.getFromName(gammaCorrectionCategory, name).getDataCode();
    }

    /**
     * Get the gamma correction associated to a data code
     *
     * @param dataCode the data code used to identify the gamma correction
     *
     * @return the gamma correction associated to the searched data code
     *
     * @throws SonyProjectorException - If no gamma correction is associated to the searched data code
     */
    public String getGammaCorrectionNameFromDataCode(byte[] dataCode) throws SonyProjectorException {
        return SonyProjectorGammaCorrection.getFromDataCode(gammaCorrectionCategory, dataCode).getName();
    }

    /**
     * Get the list of {@link StateOption} associated to the available nose reduction modes
     *
     * @return the list of {@link StateOption} associated to the available nose reduction modes
     */
    public List<StateOption> getNrStateOptions() {
        return SonyProjectorNr.getStateOptions(nrCategory);
    }

    /**
     * Get the noise reduction mode associated to a name
     *
     * @param name the name used to identify the noise reduction mode
     *
     * @return the noise reduction mode associated to the searched name
     *
     * @throws SonyProjectorException - If no noise reduction mode is associated to the searched name
     */
    public byte[] getNrCodeFromName(String name) throws SonyProjectorException {
        return SonyProjectorNr.getFromName(nrCategory, name).getDataCode();
    }

    /**
     * Get the noise reduction mode associated to a data code
     *
     * @param dataCode the data code used to identify the noise reduction mode
     *
     * @return the noise reduction mode associated to the searched data code
     *
     * @throws SonyProjectorException - If no noise reduction mode is associated to the searched data code
     */
    public String getNrNameFromDataCode(byte[] dataCode) throws SonyProjectorException {
        return SonyProjectorNr.getFromDataCode(nrCategory, dataCode).getName();
    }

    /**
     * Get the list of {@link StateOption} associated to the available block nose reduction modes
     *
     * @return the list of {@link StateOption} associated to the available block nose reduction modes
     */
    public List<StateOption> getBlockNrStateOptions() {
        return blockNrAvailable ? SonyProjectorBlockNr.getStateOptions() : new ArrayList<>();
    }

    /**
     * Inform whether the block noise reduction setting is available
     *
     * @return true if the block noise reduction setting is available
     */
    public boolean isBlockNrAvailable() {
        return blockNrAvailable;
    }

    /**
     * Get the list of {@link StateOption} associated to the available mosquito nose reduction modes
     *
     * @return the list of {@link StateOption} associated to the available mosquito nose reduction modes
     */
    public List<StateOption> getMosquitoNrStateOptions() {
        return mosquitoNrAvailable ? SonyProjectorMosquitoNr.getStateOptions() : new ArrayList<>();
    }

    /**
     * Inform whether the mosquito noise reduction setting is available
     *
     * @return true if the mosquito noise reduction setting is available
     */
    public boolean isMosquitoNrAvailable() {
        return mosquitoNrAvailable;
    }

    /**
     * Get the list of {@link StateOption} associated to the available MPEG nose reduction modes
     *
     * @return the list of {@link StateOption} associated to the available MPEG nose reduction modes
     */
    public List<StateOption> getMpegNrStateOptions() {
        return SonyProjectorMpegNr.getStateOptions(mpegNrCategory);
    }

    /**
     * Inform whether the MPEG noise reduction setting is available
     *
     * @return true if the MPEG noise reduction setting is available
     */
    public boolean isMpegNrAvailable() {
        return mpegNrCategory > 0;
    }

    /**
     * Get the MPEG noise reduction mode associated to a name
     *
     * @param name the name used to identify the MPEG noise reduction mode
     *
     * @return the MPEG noise reduction mode associated to the searched name
     *
     * @throws SonyProjectorException - If no MPEG noise reduction mode is associated to the searched name
     */
    public byte[] getMpegNrCodeFromName(String name) throws SonyProjectorException {
        return SonyProjectorMpegNr.getFromName(mpegNrCategory, name).getDataCode();
    }

    /**
     * Get the MPEG noise reduction mode associated to a data code
     *
     * @param dataCode the data code used to identify the MPEG noise reduction mode
     *
     * @return the MPEG noise reduction mode associated to the searched data code
     *
     * @throws SonyProjectorException - If no MPEG noise reduction mode is associated to the searched data code
     */
    public String getMpegNrNameFromDataCode(byte[] dataCode) throws SonyProjectorException {
        return SonyProjectorMpegNr.getFromDataCode(mpegNrCategory, dataCode).getName();
    }

    /**
     * Inform whether the xvColor setting is available
     *
     * @return true if the xvColor setting is available
     */
    public boolean isXvColorAvailable() {
        return xvColorAvailable;
    }

    /**
     * Get the list of {@link StateOption} associated to the available color spaces
     *
     * @return the list of {@link StateOption} associated to the available color spaces
     */
    public List<StateOption> getColorSpaceStateOptions() {
        return SonyProjectorColorSpace.getStateOptions(colorSpaceCategory);
    }

    /**
     * Get the color space associated to a name
     *
     * @param name the name used to identify the color space
     *
     * @return the color space associated to the searched name
     *
     * @throws SonyProjectorException - If no color space is associated to the searched name
     */
    public byte[] getColorSpaceCodeFromName(String name) throws SonyProjectorException {
        return SonyProjectorColorSpace.getFromName(colorSpaceCategory, name).getDataCode();
    }

    /**
     * Get the color space associated to a data code
     *
     * @param dataCode the data code used to identify the color space
     *
     * @return the color space associated to the searched data code
     *
     * @throws SonyProjectorException - If no color space is associated to the searched data code
     */
    public String getColorSpaceNameFromDataCode(byte[] dataCode) throws SonyProjectorException {
        return SonyProjectorColorSpace.getFromDataCode(colorSpaceCategory, dataCode).getName();
    }

    /**
     * Get the list of {@link StateOption} associated to the available film projection modes
     *
     * @return the list of {@link StateOption} associated to the available film projection modes
     */
    public List<StateOption> getFilmProjectionStateOptions() {
        return SonyProjectorFilmProjection.getStateOptions(filmProjectionCategory);
    }

    /**
     * Inform whether the film projection setting is available
     *
     * @return true if the film projection setting is available
     */
    public boolean isFilmProjectionAvailable() {
        return filmProjectionCategory > 0;
    }

    /**
     * Get the film projection mode associated to a name
     *
     * @param name the name used to identify the film projection mode
     *
     * @return the film projection mode associated to the searched name
     *
     * @throws SonyProjectorException - If no film projection mode is associated to the searched name
     */
    public byte[] getFilmProjectionCodeFromName(String name) throws SonyProjectorException {
        return SonyProjectorFilmProjection.getFromName(filmProjectionCategory, name).getDataCode();
    }

    /**
     * Get the film projection mode associated to a data code
     *
     * @param dataCode the data code used to identify the film projection mode
     *
     * @return the film projection mode associated to the searched data code
     *
     * @throws SonyProjectorException - If no film projection mode is associated to the searched data code
     */
    public String getFilmProjectionNameFromDataCode(byte[] dataCode) throws SonyProjectorException {
        return SonyProjectorFilmProjection.getFromDataCode(filmProjectionCategory, dataCode).getName();
    }

    /**
     * Get the list of {@link StateOption} associated to the available motion enhancer modes
     *
     * @return the list of {@link StateOption} associated to the available motion enhancer modes
     */
    public List<StateOption> getMotionEnhancerStateOptions() {
        return SonyProjectorMotionEnhancer.getStateOptions(motionEnhancerCategory);
    }

    /**
     * Inform whether the motion enhancer setting is available
     *
     * @return true if the motion enhancer setting is available
     */
    public boolean isMotionEnhancerAvailable() {
        return motionEnhancerCategory > 0;
    }

    /**
     * Get the motion enhancer mode associated to a name
     *
     * @param name the name used to identify the motion enhancer mode
     *
     * @return the motion enhancer mode associated to the searched name
     *
     * @throws SonyProjectorException - If no motion enhancer mode is associated to the searched name
     */
    public byte[] getMotionEnhancerCodeFromName(String name) throws SonyProjectorException {
        return SonyProjectorMotionEnhancer.getFromName(motionEnhancerCategory, name).getDataCode();
    }

    /**
     * Get the motion enhancer mode associated to a data code
     *
     * @param dataCode the data code used to identify the motion enhancer mode
     *
     * @return the motion enhancer mode associated to the searched data code
     *
     * @throws SonyProjectorException - If no motion enhancer mode is associated to the searched data code
     */
    public String getMotionEnhancerNameFromDataCode(byte[] dataCode) throws SonyProjectorException {
        return SonyProjectorMotionEnhancer.getFromDataCode(motionEnhancerCategory, dataCode).getName();
    }

    /**
     * Get the projector model mode associated to a name
     *
     * @param name the name used to identify the projector model
     * @param strict true for a strict matching with the searched name
     *
     * @return the projector model associated to the searched name
     *
     * @throws SonyProjectorException - If no projector model is associated to the searched name
     */
    public static SonyProjectorModel getFromName(String name, boolean strict) throws SonyProjectorException {
        String otherName = lessStrictName(name);
        for (SonyProjectorModel value : SonyProjectorModel.values()) {
            if (strict && value.getName().equals(name)) {
                return value;
            } else if (!strict && lessStrictName(value.getName()).equals(otherName)) {
                return value;
            }
        }
        throw new SonyProjectorException("Invalid model name: " + name);
    }

    private static String lessStrictName(String name) {
        // Ignore the ending letters
        String newName = name;
        byte[] data = name.getBytes();
        // Search the first number, starting at right
        int last = data.length - 1;
        for (int i = last; i >= 0; i--) {
            if (((char) data[i]) >= '0' && ((char) data[i]) <= '9') {
                last = i;
                break;
            }
        }
        byte[] newData = Arrays.copyOf(data, last + 1);
        newName = new String(newData, StandardCharsets.UTF_8);
        return newName;
    }
}
