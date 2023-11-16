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
package org.openhab.binding.sonyprojector.internal.communication;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sonyprojector.internal.SonyProjectorException;
import org.openhab.binding.sonyprojector.internal.SonyProjectorModel;
import org.openhab.core.i18n.CommunicationException;
import org.openhab.core.i18n.ConnectionException;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.util.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for communicating with Sony Projectors
 *
 * @author Markus Wehrle - Initial contribution
 * @author Laurent Garnier - Refactoring to include new channels, consider serial connection and protocol depending on
 *         model
 */
@NonNullByDefault
public abstract class SonyProjectorConnector {

    private final Logger logger = LoggerFactory.getLogger(SonyProjectorConnector.class);

    private static final byte[] DUMMY_DATA = new byte[] { 0x00, 0x00 };
    private static final byte[] POWER_ON = new byte[] { 0x00, 0x01 };
    private static final byte[] POWER_OFF = new byte[] { 0x00, 0x00 };
    private static final byte[] OVERSCAN_ON = new byte[] { 0x00, 0x01 };
    private static final byte[] OVERSCAN_OFF = new byte[] { 0x00, 0x00 };
    private static final byte[] PICTURE_ON = new byte[] { 0x00, 0x01 };
    private static final byte[] PICTURE_OFF = new byte[] { 0x00, 0x00 };
    private static final byte[] XVCOLOR_ON = new byte[] { 0x00, 0x01 };
    private static final byte[] XVCOLOR_OFF = new byte[] { 0x00, 0x00 };

    private SonyProjectorModel model;

    /** The output stream */
    protected @Nullable OutputStream dataOut;

    /** The input stream */
    protected @Nullable InputStream dataIn;

    /** true if the connection is established, false if not */
    protected boolean connected;

    private boolean simu;

    /**
     * Constructor
     *
     * @param model the projector model in use
     * @param simu whether the communication is simulated or real
     */
    public SonyProjectorConnector(SonyProjectorModel model, boolean simu) {
        this.model = model;
        this.simu = simu;
    }

    /**
     * Set the projector model in use
     *
     * @param model the projector model in use
     */
    public void setModel(SonyProjectorModel model) {
        this.model = model;
    }

    /**
     * Request the projector to get the current power status
     *
     * @return the current power status
     *
     * @throws SonyProjectorException in case of any problem
     */
    public SonyProjectorStatusPower getStatusPower() throws SonyProjectorException {
        return SonyProjectorStatusPower.getFromDataCode(getSetting(SonyProjectorItem.STATUS_POWER));
    }

    /**
     * Power ON the projector
     *
     * @throws SonyProjectorException in case the projector is not ready for a power ON command or any other problem
     */
    public void powerOn() throws SonyProjectorException {
        SonyProjectorStatusPower status = null;
        try {
            status = getStatusPower();
        } catch (SonyProjectorException e) {
        }
        logger.debug("Current Power Status: {}", status == null ? "undefined" : status.toString());
        if (status != null && status != SonyProjectorStatusPower.STANDBY) {
            throw new SonyProjectorException("Projector not ready for command ON");
        } else if (model.isPowerCmdAvailable()) {
            logger.debug("Set Power ON using Power command");
            setSetting(SonyProjectorItem.POWER, POWER_ON);
        } else {
            logger.debug("Set Power ON using IR Power command");
            sendIR(SonyProjectorItem.IR_POWER_ON);
            if (status == null) {
                sendIR(SonyProjectorItem.IR_POWER_ON);
            }
        }
    }

    /**
     * Power OFF the projector
     *
     * @throws SonyProjectorException in case the projector is not ready for a power OFF command or any other problem
     */
    public void powerOff() throws SonyProjectorException {
        SonyProjectorStatusPower status = null;
        try {
            status = getStatusPower();
        } catch (SonyProjectorException e) {
        }
        logger.debug("Current Power Status: {}", status == null ? "undefined" : status.toString());
        if (status == null || status != SonyProjectorStatusPower.POWER_ON) {
            throw new SonyProjectorException("Projector not ready for command OFF");
        } else if (model.isPowerCmdAvailable()) {
            logger.debug("Set Power OFF using Power command");
            setSetting(SonyProjectorItem.POWER, POWER_OFF);
        } else {
            logger.debug("Set Power OFF using IR Power command");
            sendIR(SonyProjectorItem.IR_POWER_OFF);
        }
    }

    /**
     * Request the projector to get the current calibration preset
     *
     * @return the current calibration preset
     *
     * @throws SonyProjectorException in case of any problem
     */
    public String getCalibrationPreset() throws SonyProjectorException {
        return model.getCalibrPresetNameFromDataCode(getSetting(SonyProjectorItem.CALIBRATION_PRESET));
    }

    /**
     * Request the projector to change the calibration preset
     *
     * @param value the calibration preset to set
     *
     * @throws SonyProjectorException in case of any problem
     */
    public void setCalibrationPreset(String value) throws SonyProjectorException {
        setSetting(SonyProjectorItem.CALIBRATION_PRESET, model.getCalibrPresetDataCodeFromName(value));
    }

    /**
     * Request the projector to get the current video input
     *
     * @return the current video input
     *
     * @throws SonyProjectorException in case of any problem
     */
    public String getInput() throws SonyProjectorException {
        return model.getInputNameFromDataCode(getSetting(SonyProjectorItem.INPUT));
    }

    /**
     * Request the projector to change the video input
     *
     * @param value the video input to set
     *
     * @throws SonyProjectorException in case of any problem
     */
    public void setInput(String value) throws SonyProjectorException {
        setSetting(SonyProjectorItem.INPUT, model.getInputDataCodeFromName(value));
    }

    /**
     * Request the projector to get the current contrast setting
     *
     * @return the current contrast value
     *
     * @throws SonyProjectorException in case of any problem
     */
    public int getContrast() throws SonyProjectorException {
        return convertDataToInt(getSetting(SonyProjectorItem.CONTRAST));
    }

    /**
     * Request the projector to change the contrast setting
     *
     * @param value the contrast value to set
     *
     * @throws SonyProjectorException in case of any problem
     */
    public void setContrast(int value) throws SonyProjectorException {
        setSetting(SonyProjectorItem.CONTRAST, convertIntToData(value));
    }

    /**
     * Request the projector to get the current brightness setting
     *
     * @return the current brightness value
     *
     * @throws SonyProjectorException in case of any problem
     */
    public int getBrightness() throws SonyProjectorException {
        return convertDataToInt(getSetting(SonyProjectorItem.BRIGHTNESS));
    }

    /**
     * Request the projector to change the brightness setting
     *
     * @param value the brightness value to set
     *
     * @throws SonyProjectorException in case of any problem
     */
    public void setBrightness(int value) throws SonyProjectorException {
        setSetting(SonyProjectorItem.BRIGHTNESS, convertIntToData(value));
    }

    /**
     * Request the projector to get the current color setting
     *
     * @return the current color value
     *
     * @throws SonyProjectorException in case of any problem
     */
    public int getColor() throws SonyProjectorException {
        return convertDataToInt(getSetting(SonyProjectorItem.COLOR));
    }

    /**
     * Request the projector to change the color setting
     *
     * @param value the color value to set
     *
     * @throws SonyProjectorException in case of any problem
     */
    public void setColor(int value) throws SonyProjectorException {
        setSetting(SonyProjectorItem.COLOR, convertIntToData(value));
    }

    /**
     * Request the projector to get the current hue setting
     *
     * @return the current hue value
     *
     * @throws SonyProjectorException in case of any problem
     */
    public int getHue() throws SonyProjectorException {
        return convertDataToInt(getSetting(SonyProjectorItem.HUE));
    }

    /**
     * Request the projector to change the hue setting
     *
     * @param value the hue value to set
     *
     * @throws SonyProjectorException in case of any problem
     */
    public void setHue(int value) throws SonyProjectorException {
        setSetting(SonyProjectorItem.HUE, convertIntToData(value));
    }

    /**
     * Request the projector to get the current sharpness setting
     *
     * @return the current sharpness value
     *
     * @throws SonyProjectorException in case of any problem
     */
    public int getSharpness() throws SonyProjectorException {
        return convertDataToInt(getSetting(SonyProjectorItem.SHARPNESS));
    }

    /**
     * Request the projector to change the sharpness setting
     *
     * @param value the sharpness value to set
     *
     * @throws SonyProjectorException in case of any problem
     */
    public void setSharpness(int value) throws SonyProjectorException {
        setSetting(SonyProjectorItem.SHARPNESS, convertIntToData(value));
    }

    /**
     * Request the projector to get the current contrast enhancer mode
     *
     * @return the current contrast enhancer mode
     *
     * @throws SonyProjectorException in case of any problem
     */
    public String getContrastEnhancer() throws SonyProjectorException {
        return model.getContrastEnhancerNameFromDataCode(getSetting(SonyProjectorItem.CONTRAST_ENHANCER));
    }

    /**
     * Request the projector to change the contrast enhancer mode
     *
     * @param value the contrast enhancer mode to set
     *
     * @throws SonyProjectorException in case of any problem
     */
    public void setContrastEnhancer(String value) throws SonyProjectorException {
        setSetting(SonyProjectorItem.CONTRAST_ENHANCER, model.getContrastEnhancerDataCodeFromName(value));
    }

    /**
     * Request the projector to get the current film mode
     *
     * @return the current film mode
     *
     * @throws SonyProjectorException in case this setting is not available for the projector or any other problem
     */
    public String getFilmMode() throws SonyProjectorException {
        if (!model.isFilmModeAvailable()) {
            throw new SonyProjectorException("Unavailable item " + SonyProjectorItem.FILM_MODE.getName()
                    + " for projector model " + model.getName());
        }
        return model.getFilmModeNameFromDataCode(getSetting(SonyProjectorItem.FILM_MODE));
    }

    /**
     * Request the projector to change the film mode
     *
     * @param value the film mode to set
     *
     * @throws SonyProjectorException in case this setting is not available for the projector or any other problem
     */
    public void setFilmMode(String value) throws SonyProjectorException {
        if (!model.isFilmModeAvailable()) {
            throw new SonyProjectorException("Unavailable item " + SonyProjectorItem.FILM_MODE.getName()
                    + " for projector model " + model.getName());
        }
        setSetting(SonyProjectorItem.FILM_MODE, model.getFilmModeDataCodeFromName(value));
    }

    /**
     * Request the projector to get the lamp use time
     *
     * @return the lamp use time
     *
     * @throws SonyProjectorException in case of any problem
     */
    public int getLampUseTime() throws SonyProjectorException {
        return convertDataToInt(getSetting(SonyProjectorItem.LAMP_USE_TIME));
    }

    /**
     * Request the projector to get the current mode for the lamp control setting
     *
     * @return the current mode for the lamp control setting
     *
     * @throws SonyProjectorException in case this setting is not available for the projector or any other problem
     */
    public String getLampControl() throws SonyProjectorException {
        if (!model.isLampControlAvailable()) {
            throw new SonyProjectorException("Unavailable item " + SonyProjectorItem.LAMP_CONTROL.getName()
                    + " for projector model " + model.getName());
        }
        return SonyProjectorLampControl.getFromDataCode(getSetting(SonyProjectorItem.LAMP_CONTROL)).getName();
    }

    /**
     * Request the projector to change the mode for the lamp control setting
     *
     * @param value the mode to set for the lamp control setting
     *
     * @throws SonyProjectorException in case this setting is not available for the projector or any other problem
     */
    public void setLampControl(String value) throws SonyProjectorException {
        if (!model.isLampControlAvailable()) {
            throw new SonyProjectorException("Unavailable item " + SonyProjectorItem.LAMP_CONTROL.getName()
                    + " for projector model " + model.getName());
        }
        setSetting(SonyProjectorItem.LAMP_CONTROL, SonyProjectorLampControl.getFromName(value).getDataCode());
    }

    /**
     * Request the projector if the picture is muted or not
     *
     * @return OnOffType.ON if the picture is muted, OnOffType.OFF if not
     *
     * @throws SonyProjectorException in case of any problem
     */
    public OnOffType getPictureMuting() throws SonyProjectorException {
        return Arrays.equals(getSetting(SonyProjectorItem.PICTURE_MUTING), PICTURE_ON) ? OnOffType.ON : OnOffType.OFF;
    }

    /**
     * Request the projector to mute the picture
     *
     * @throws SonyProjectorException in case of any problem
     */
    public void mutePicture() throws SonyProjectorException {
        setSetting(SonyProjectorItem.PICTURE_MUTING, PICTURE_ON);
    }

    /**
     * Request the projector to unmute the picture
     *
     * @throws SonyProjectorException in case of any problem
     */
    public void unmutePicture() throws SonyProjectorException {
        setSetting(SonyProjectorItem.PICTURE_MUTING, PICTURE_OFF);
    }

    /**
     * Request the projector to get the current mode for the picture position setting
     *
     * @return the current mode for the picture position setting
     *
     * @throws SonyProjectorException in case this setting is not available for the projector or any other problem
     */
    public String getPicturePosition() throws SonyProjectorException {
        if (!model.isPicturePositionAvailable()) {
            throw new SonyProjectorException("Unavailable item " + SonyProjectorItem.PICTURE_POSITION.getName()
                    + " for projector model " + model.getName());
        }
        return model.getPicturePositionNameFromDataCode(getSetting(SonyProjectorItem.PICTURE_POSITION));
    }

    /**
     * Request the projector to change the mode for the picture position setting
     *
     * @param value the mode to set for the picture position setting
     *
     * @throws SonyProjectorException in case this setting is not available for the projector or any other problem
     */
    public void setPicturePosition(String value) throws SonyProjectorException {
        if (!model.isPicturePositionAvailable()) {
            throw new SonyProjectorException("Unavailable item " + SonyProjectorItem.PICTURE_POSITION.getName()
                    + " for projector model " + model.getName());
        }
        setSetting(SonyProjectorItem.PICTURE_POSITION, model.getPicturePositionCodeFromName(value));
    }

    /**
     * Request the projector if the overscan is enabled or not
     *
     * @return OnOffType.ON if the overscan is enabled, OnOffType.OFF if not
     *
     * @throws SonyProjectorException in case this setting is not available for the projector or any other problem
     */
    public OnOffType getOverscan() throws SonyProjectorException {
        if (!model.isOverscanAvailable()) {
            throw new SonyProjectorException("Unavailable item " + SonyProjectorItem.OVERSCAN.getName()
                    + " for projector model " + model.getName());
        }
        return Arrays.equals(getSetting(SonyProjectorItem.OVERSCAN), OVERSCAN_ON) ? OnOffType.ON : OnOffType.OFF;
    }

    /**
     * Request the projector to enable the overscan
     *
     * @throws SonyProjectorException in case this setting is not available for the projector or any other problem
     */
    public void enableOverscan() throws SonyProjectorException {
        if (!model.isOverscanAvailable()) {
            throw new SonyProjectorException("Unavailable item " + SonyProjectorItem.OVERSCAN.getName()
                    + " for projector model " + model.getName());
        }
        setSetting(SonyProjectorItem.OVERSCAN, OVERSCAN_ON);
    }

    /**
     * Request the projector to disable the overscan
     *
     * @throws SonyProjectorException in case this setting is not available for the projector or any other problem
     */
    public void disableOverscan() throws SonyProjectorException {
        if (!model.isOverscanAvailable()) {
            throw new SonyProjectorException("Unavailable item " + SonyProjectorItem.OVERSCAN.getName()
                    + " for projector model " + model.getName());
        }
        setSetting(SonyProjectorItem.OVERSCAN, OVERSCAN_OFF);
    }

    /**
     * Request the projector to get the current aspect ratio mode
     *
     * @return the current current aspect ratio mode
     *
     * @throws SonyProjectorException in case of any problem
     */
    public String getAspect() throws SonyProjectorException {
        return model.getAspectNameFromDataCode(getSetting(SonyProjectorItem.ASPECT));
    }

    /**
     * Request the projector to change the aspect ratio mode
     *
     * @param value the aspect ratio mode to set
     *
     * @throws SonyProjectorException in case of any problem
     */
    public void setAspect(String value) throws SonyProjectorException {
        setSetting(SonyProjectorItem.ASPECT, model.getAspectCodeFromName(value));
    }

    /**
     * Request the projector to get the current color temperature setting
     *
     * @return the current color temperature value
     *
     * @throws SonyProjectorException in case of any problem
     */
    public String getColorTemperature() throws SonyProjectorException {
        return model.getColorTempNameFromDataCode(getSetting(SonyProjectorItem.COLOR_TEMP));
    }

    /**
     * Request the projector to change the color temperature setting
     *
     * @param value the color temperature value to set
     *
     * @throws SonyProjectorException in case of any problem
     */
    public void setColorTemperature(String value) throws SonyProjectorException {
        setSetting(SonyProjectorItem.COLOR_TEMP, model.getColorTempCodeFromName(value));
    }

    /**
     * Request the projector to get the current iris mode
     *
     * @return the current iris mode
     *
     * @throws SonyProjectorException in case this setting is not available for the projector or any other problem
     */
    public String getIrisMode() throws SonyProjectorException {
        if (!model.isIrisModeAvailable()) {
            throw new SonyProjectorException("Unavailable item " + SonyProjectorItem.IRIS_MODE.getName()
                    + " for projector model " + model.getName());
        }
        return model.getIrisModeNameFromDataCode(getSetting(SonyProjectorItem.IRIS_MODE));
    }

    /**
     * Request the projector to change the iris mode
     *
     * @param value the iris mode to set
     *
     * @throws SonyProjectorException in case this setting is not available for the projector or any other problem
     */
    public void setIrisMode(String value) throws SonyProjectorException {
        if (!model.isIrisModeAvailable()) {
            throw new SonyProjectorException("Unavailable item " + SonyProjectorItem.IRIS_MODE.getName()
                    + " for projector model " + model.getName());
        }
        setSetting(SonyProjectorItem.IRIS_MODE, model.getIrisModeCodeFromName(value));
    }

    /**
     * Request the projector to get the current iris manual setting
     *
     * @return the current value for the iris manual setting
     *
     * @throws SonyProjectorException in case this setting is not available for the projector or any other problem
     */
    public int getIrisManual() throws SonyProjectorException {
        if (!model.isIrisManualAvailable()) {
            throw new SonyProjectorException("Unavailable item " + SonyProjectorItem.IRIS_MANUAL.getName()
                    + " for projector model " + model.getName());
        }
        return convertDataToInt(getSetting(SonyProjectorItem.IRIS_MANUAL));
    }

    /**
     * Request the projector to change the iris manual setting
     *
     * @param value the iris manual value to set
     *
     * @throws SonyProjectorException in case this setting is not available for the projector or any other problem
     */
    public void setIrisManual(int value) throws SonyProjectorException {
        if (!model.isIrisManualAvailable()) {
            throw new SonyProjectorException("Unavailable item " + SonyProjectorItem.IRIS_MANUAL.getName()
                    + " for projector model " + model.getName());
        }
        setSetting(SonyProjectorItem.IRIS_MANUAL, convertIntToData(value));
    }

    /**
     * Request the projector to get the current iris sensitivity
     *
     * @return the current iris sensitivity
     *
     * @throws SonyProjectorException in case this setting is not available for the projector or any other problem
     */
    public String getIrisSensitivity() throws SonyProjectorException {
        if (!model.isIrisSensitivityAvailable()) {
            throw new SonyProjectorException("Unavailable item " + SonyProjectorItem.IRIS_SENSITIVITY.getName()
                    + " for projector model " + model.getName());
        }
        return SonyProjectorIrisSensitivity.getFromDataCode(getSetting(SonyProjectorItem.IRIS_SENSITIVITY)).getName();
    }

    /**
     * Request the projector to change the iris sensitivity
     *
     * @param value the iris sensitivity to set
     *
     * @throws SonyProjectorException in case this setting is not available for the projector or any other problem
     */
    public void setIrisSensitivity(String value) throws SonyProjectorException {
        if (!model.isIrisSensitivityAvailable()) {
            throw new SonyProjectorException("Unavailable item " + SonyProjectorItem.IRIS_SENSITIVITY.getName()
                    + " for projector model " + model.getName());
        }
        setSetting(SonyProjectorItem.IRIS_SENSITIVITY, SonyProjectorIrisSensitivity.getFromName(value).getDataCode());
    }

    /**
     * Request the projector to get the current film projection mode
     *
     * @return the current film projection mode
     *
     * @throws SonyProjectorException in case this setting is not available for the projector or any other problem
     */
    public String getFilmProjection() throws SonyProjectorException {
        if (!model.isFilmProjectionAvailable()) {
            throw new SonyProjectorException("Unavailable item " + SonyProjectorItem.FILM_PROJECTION.getName()
                    + " for projector model " + model.getName());
        }
        return model.getFilmProjectionNameFromDataCode(getSetting(SonyProjectorItem.FILM_PROJECTION));
    }

    /**
     * Request the projector to change the film projection mode
     *
     * @param value the film projection mode to set
     *
     * @throws SonyProjectorException in case this setting is not available for the projector or any other problem
     */
    public void setFilmProjection(String value) throws SonyProjectorException {
        if (!model.isFilmProjectionAvailable()) {
            throw new SonyProjectorException("Unavailable item " + SonyProjectorItem.FILM_PROJECTION.getName()
                    + " for projector model " + model.getName());
        }
        setSetting(SonyProjectorItem.FILM_PROJECTION, model.getFilmProjectionCodeFromName(value));
    }

    /**
     * Request the projector to get the current motion enhancer mode
     *
     * @return the current motion enhancer mode
     *
     * @throws SonyProjectorException in case of any problem
     */
    public String getMotionEnhancer() throws SonyProjectorException {
        if (!model.isMotionEnhancerAvailable()) {
            throw new SonyProjectorException("Unavailable item " + SonyProjectorItem.MOTION_ENHANCER.getName()
                    + " for projector model " + model.getName());
        }
        return model.getMotionEnhancerNameFromDataCode(getSetting(SonyProjectorItem.MOTION_ENHANCER));
    }

    /**
     * Request the projector to change the motion enhancer mode
     *
     * @param value the motion enhancer mode to set
     *
     * @throws SonyProjectorException in case this setting is not available for the projector or any other problem
     */
    public void setMotionEnhancer(String value) throws SonyProjectorException {
        if (!model.isMotionEnhancerAvailable()) {
            throw new SonyProjectorException("Unavailable item " + SonyProjectorItem.MOTION_ENHANCER.getName()
                    + " for projector model " + model.getName());
        }
        setSetting(SonyProjectorItem.MOTION_ENHANCER, model.getMotionEnhancerCodeFromName(value));
    }

    /**
     * Request the projector to get the current gamma correction
     *
     * @return the current gamma correction
     *
     * @throws SonyProjectorException in case of any problem
     */
    public String getGammaCorrection() throws SonyProjectorException {
        return model.getGammaCorrectionNameFromDataCode(getSetting(SonyProjectorItem.GAMMA_CORRECTION));
    }

    /**
     * Request the projector to change the gamma correction
     *
     * @param value the gamma correction to set
     *
     * @throws SonyProjectorException in case of any problem
     */
    public void setGammaCorrection(String value) throws SonyProjectorException {
        setSetting(SonyProjectorItem.GAMMA_CORRECTION, model.getGammaCorrectionCodeFromName(value));
    }

    /**
     * Request the projector to get the current color space
     *
     * @return the current color space
     *
     * @throws SonyProjectorException in case of any problem
     */
    public String getColorSpace() throws SonyProjectorException {
        return model.getColorSpaceNameFromDataCode(getSetting(SonyProjectorItem.COLOR_SPACE));
    }

    /**
     * Request the projector to change the color space
     *
     * @param value the color space to set
     *
     * @throws SonyProjectorException in case of any problem
     */
    public void setColorSpace(String value) throws SonyProjectorException {
        setSetting(SonyProjectorItem.COLOR_SPACE, model.getColorSpaceCodeFromName(value));
    }

    /**
     * Request the projector to get the current noise reduction mode
     *
     * @return the current noise reduction mode
     *
     * @throws SonyProjectorException in case of any problem
     */
    public String getNr() throws SonyProjectorException {
        return model.getNrNameFromDataCode(getSetting(SonyProjectorItem.NR));
    }

    /**
     * Request the projector to change the noise reduction mode
     *
     * @param value the noise reduction mode to set
     *
     * @throws SonyProjectorException in case of any problem
     */
    public void setNr(String value) throws SonyProjectorException {
        setSetting(SonyProjectorItem.NR, model.getNrCodeFromName(value));
    }

    /**
     * Request the projector to get the current block noise reduction mode
     *
     * @return the current block noise reduction mode
     *
     * @throws SonyProjectorException in case this setting is not available for the projector or any other problem
     */
    public String getBlockNr() throws SonyProjectorException {
        if (!model.isBlockNrAvailable()) {
            throw new SonyProjectorException("Unavailable item " + SonyProjectorItem.BLOCK_NR.getName()
                    + " for projector model " + model.getName());
        }
        return SonyProjectorBlockNr.getFromDataCode(getSetting(SonyProjectorItem.BLOCK_NR)).getName();
    }

    /**
     * Request the projector to change the block noise reduction mode
     *
     * @param value the block noise reduction mode to set
     *
     * @throws SonyProjectorException in case this setting is not available for the projector or any other problem
     */
    public void setBlockNr(String value) throws SonyProjectorException {
        if (!model.isBlockNrAvailable()) {
            throw new SonyProjectorException("Unavailable item " + SonyProjectorItem.BLOCK_NR.getName()
                    + " for projector model " + model.getName());
        }
        setSetting(SonyProjectorItem.BLOCK_NR, SonyProjectorBlockNr.getFromName(value).getDataCode());
    }

    /**
     * Request the projector to get the current mosquito noise reduction mode
     *
     * @return the current mosquito noise reduction mode
     *
     * @throws SonyProjectorException in case this setting is not available for the projector or any other problem
     */
    public String getMosquitoNr() throws SonyProjectorException {
        if (!model.isMosquitoNrAvailable()) {
            throw new SonyProjectorException("Unavailable item " + SonyProjectorItem.MOSQUITO_NR.getName()
                    + " for projector model " + model.getName());
        }
        return SonyProjectorMosquitoNr.getFromDataCode(getSetting(SonyProjectorItem.MOSQUITO_NR)).getName();
    }

    /**
     * Request the projector to change the mosquito noise reduction mode
     *
     * @param value the mosquito noise reduction mode to set
     *
     * @throws SonyProjectorException in case this setting is not available for the projector or any other problem
     */
    public void setMosquitoNr(String value) throws SonyProjectorException {
        if (!model.isMosquitoNrAvailable()) {
            throw new SonyProjectorException("Unavailable item " + SonyProjectorItem.MOSQUITO_NR.getName()
                    + " for projector model " + model.getName());
        }
        setSetting(SonyProjectorItem.MOSQUITO_NR, SonyProjectorMosquitoNr.getFromName(value).getDataCode());
    }

    /**
     * Request the projector to get the current MPEG noise reduction mode
     *
     * @return the current MPEG noise reduction mode
     *
     * @throws SonyProjectorException in case this setting is not available for the projector or any other problem
     */
    public String getMpegNr() throws SonyProjectorException {
        if (!model.isMpegNrAvailable()) {
            throw new SonyProjectorException("Unavailable item " + SonyProjectorItem.MPEG_NR.getName()
                    + " for projector model " + model.getName());
        }
        return model.getMpegNrNameFromDataCode(getSetting(SonyProjectorItem.MPEG_NR));
    }

    /**
     * Request the projector to change the MPEG noise reduction mode
     *
     * @param value the MPEG noise reduction mode to set
     *
     * @throws SonyProjectorException in case this setting is not available for the projector or any other problem
     */
    public void setMpegNr(String value) throws SonyProjectorException {
        if (!model.isMpegNrAvailable()) {
            throw new SonyProjectorException("Unavailable item " + SonyProjectorItem.MPEG_NR.getName()
                    + " for projector model " + model.getName());
        }
        setSetting(SonyProjectorItem.MPEG_NR, model.getMpegNrCodeFromName(value));
    }

    /**
     * Request the projector to get the current value for xvColor
     *
     * @return the current value for xvColor
     *
     * @throws SonyProjectorException in case this setting is not available for the projector or any other problem
     */
    public OnOffType getXvColor() throws SonyProjectorException {
        if (!model.isXvColorAvailable()) {
            throw new SonyProjectorException("Unavailable item " + SonyProjectorItem.XVCOLOR.getName()
                    + " for projector model " + model.getName());
        }
        return Arrays.equals(getSetting(SonyProjectorItem.XVCOLOR), XVCOLOR_ON) ? OnOffType.ON : OnOffType.OFF;
    }

    /**
     * Request the projector to set xvColor to ON
     *
     * @throws SonyProjectorException in case this setting is not available for the projector or any other problem
     */
    public void enableXvColor() throws SonyProjectorException {
        if (!model.isXvColorAvailable()) {
            throw new SonyProjectorException("Unavailable item " + SonyProjectorItem.XVCOLOR.getName()
                    + " for projector model " + model.getName());
        }
        setSetting(SonyProjectorItem.XVCOLOR, XVCOLOR_ON);
    }

    /**
     * Request the projector to set xvColor to OFF
     *
     * @throws SonyProjectorException in case this setting is not available for the projector or any other problem
     */
    public void disableXvColor() throws SonyProjectorException {
        if (!model.isXvColorAvailable()) {
            throw new SonyProjectorException("Unavailable item " + SonyProjectorItem.XVCOLOR.getName()
                    + " for projector model " + model.getName());
        }
        setSetting(SonyProjectorItem.XVCOLOR, XVCOLOR_OFF);
    }

    /**
     * Request the projector to get the current value for a setting
     *
     * @param item the projector setting to get
     *
     * @return the current value for the setting
     *
     * @throws SonyProjectorException in case of any problem
     */
    protected byte[] getSetting(SonyProjectorItem item) throws SonyProjectorException {
        logger.debug("Get setting {}", item.getName());

        try {
            byte[] result = getResponseData(executeCommand(item, true, DUMMY_DATA));

            logger.debug("Get setting {} succeeded: result data: {}", item.getName(), HexUtils.bytesToHex(result));

            return result;
        } catch (CommunicationException e) {
            throw new SonyProjectorException("Get setting " + item.getName() + " failed", e);
        }
    }

    /**
     * Request the projector to set a new value for a setting
     *
     * @param item the projector setting to set
     * @param data the value to set for the setting
     *
     * @throws SonyProjectorException in case of any problem
     */
    private void setSetting(SonyProjectorItem item, byte[] data) throws SonyProjectorException {
        logger.debug("Set setting {} data {}", item.getName(), HexUtils.bytesToHex(data));

        try {
            executeCommand(item, false, data);
        } catch (CommunicationException e) {
            throw new SonyProjectorException("Set setting " + item.getName() + " failed", e);
        }

        logger.debug("Set setting {} succeeded", item.getName());
    }

    /**
     * Send an IR command to the projector
     *
     * @param item the IR information to send
     *
     * @throws SonyProjectorException in case of any problem
     */
    private synchronized void sendIR(SonyProjectorItem item) throws SonyProjectorException {
        logger.debug("Send IR {}", item.getName());

        try {
            boolean runningSession = connected;

            open();

            // Build the message and send it
            writeCommand(buildMessage(item, false, DUMMY_DATA));

            // Wait at least 45 ms
            Thread.sleep(45);

            // No response expected for SIRCS commands

            if (!runningSession) {
                close();
            }
        } catch (CommunicationException e) {
            throw new SonyProjectorException("Send IR " + item.getName() + " failed", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SonyProjectorException("Send IR " + item.getName() + " interrupted", e);
        }

        logger.debug("Send IR {} succeeded", item.getName());
    }

    /**
     * Connect to the projector, write a command, read the response and disconnect
     *
     * @param item the projector setting to get or set
     * @param getCommand true for a GET command or false for a SET command
     * @param data the value to be considered in case of a SET command
     *
     * @return the buffer containing the returned message
     *
     * @throws ConnectionException in case of any connection problem
     * @throws CommunicationException in case of any communication problem
     */
    private synchronized byte[] executeCommand(SonyProjectorItem item, boolean getCommand, byte[] data)
            throws ConnectionException, CommunicationException {
        boolean runningSession = connected;

        open();

        // Build the message and send it
        writeCommand(buildMessage(item, getCommand, data));

        // Read the response
        byte[] responseMessage = readResponse();

        if (!runningSession) {
            close();
        }

        // Validate the content of the response
        validateResponse(responseMessage, item);

        return responseMessage;
    }

    /**
     * Open the connection with the projector if not yet opened
     *
     * @throws ConnectionException in case of any problem
     */
    public abstract void open() throws ConnectionException;

    /**
     * Close the connection with the projector
     */
    public void close() {
        if (connected) {
            OutputStream dataOut = this.dataOut;
            if (dataOut != null) {
                try {
                    dataOut.close();
                } catch (IOException e) {
                }
                this.dataOut = null;
            }
            InputStream dataIn = this.dataIn;
            if (dataIn != null) {
                try {
                    dataIn.close();
                } catch (IOException e) {
                }
                this.dataIn = null;
            }
            connected = false;
        }
    }

    /**
     * Build the message buffer corresponding to the request of a particular information
     *
     * @param item the projector setting to get or set
     * @param getCommand true for a GET command or false for a SET command
     * @param data the value to be considered in case of a SET command
     *
     * @return the message buffer
     */
    protected abstract byte[] buildMessage(SonyProjectorItem item, boolean getCommand, byte[] data);

    /**
     * Reads some number of bytes from the input stream and stores them into the buffer array b. The number of bytes
     * actually read is returned as an integer.
     *
     * @param dataBuffer the buffer into which the data is read.
     * @return the total number of bytes read into the buffer, or -1 if there is no more data because the end of the
     *         stream has been reached.
     * @throws CommunicationException if the input stream is null, if the first byte cannot be read for any reason
     *             other than the end of the file, if the input stream has been closed, or if some other I/O error
     *             occurs.
     */
    protected int readInput(byte[] dataBuffer) throws CommunicationException {
        if (simu) {
            throw new CommunicationException("readInput failed: should not be called in simu mode");
        }
        InputStream dataIn = this.dataIn;
        if (dataIn == null) {
            throw new CommunicationException("readInput failed: input stream is null");
        }
        try {
            return dataIn.read(dataBuffer);
        } catch (IOException e) {
            logger.debug("readInput failed: {}", e.getMessage());
            throw new CommunicationException("readInput failed", e);
        }
    }

    /**
     * Write a command to the output stream
     *
     * @param message the buffer containing the message to be sent
     *
     * @throws CommunicationException in case of any communication problem
     */
    protected void writeCommand(byte[] message) throws CommunicationException {
        logger.debug("writeCommand: {}", HexUtils.bytesToHex(message));
        if (simu) {
            return;
        }
        OutputStream dataOut = this.dataOut;
        if (dataOut == null) {
            throw new CommunicationException("writeCommand failed: output stream is null");
        }
        try {
            dataOut.write(message);
            dataOut.flush();
        } catch (IOException e) {
            logger.debug("writeCommand failed: {}", e.getMessage());
            throw new CommunicationException("writeCommand failed", e);
        }
    }

    /**
     * Read the response from the input stream
     *
     * @return the buffer containing the returned message
     *
     * @throws CommunicationException in case of any communication problem
     */
    protected abstract byte[] readResponse() throws CommunicationException;

    /**
     * Validate the content of a returned message
     *
     * @param responseMessage the buffer containing the returned message
     * @param item the projector setting to get or set
     *
     * @throws CommunicationException if the message has unexpected content
     */
    protected abstract void validateResponse(byte[] responseMessage, SonyProjectorItem item)
            throws CommunicationException;

    /**
     * Extract the value from the returned message
     *
     * @param responseMessage the buffer containing the returned message
     *
     * @return the value of the projector setting that was requested
     */
    protected abstract byte[] getResponseData(byte[] responseMessage);

    private int convertDataToInt(byte[] data) {
        return ((data[0] & 0x000000FF) << 8) | (data[1] & 0x000000FF);
    }

    private byte[] convertIntToData(int value) {
        byte[] data = new byte[2];
        data[0] = (byte) ((value & 0x0000FF00) >> 8);
        data[1] = (byte) (value & 0x000000FF);
        return data;
    }
}
