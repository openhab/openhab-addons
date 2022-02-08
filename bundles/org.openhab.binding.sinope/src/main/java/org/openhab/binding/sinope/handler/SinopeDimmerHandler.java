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
package org.openhab.binding.sinope.handler;

import java.io.IOException;
import java.net.UnknownHostException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sinope.SinopeBindingConstants;
import org.openhab.binding.sinope.internal.config.SinopeConfig;
import org.openhab.binding.sinope.internal.core.SinopeDataReadRequest;
import org.openhab.binding.sinope.internal.core.SinopeDataWriteRequest;
import org.openhab.binding.sinope.internal.core.appdata.SinopeLightModeData;
import org.openhab.binding.sinope.internal.core.appdata.SinopeOutputIntensityData;
import org.openhab.binding.sinope.internal.core.base.SinopeDataAnswer;
import org.openhab.binding.sinope.internal.util.ByteUtil;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SinopeDimmerHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Christos Karras - Initial contribution
 */
@NonNullByDefault
public class SinopeDimmerHandler extends BaseThingHandler {

    private static final int DATA_ANSWER = 0x0A;

    private Logger logger = LoggerFactory.getLogger(SinopeDimmerHandler.class);

    private byte[] deviceId = new byte[0];

    public SinopeDimmerHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        try {
            if (SinopeBindingConstants.CHANNEL_DIMMER_OUTPUTINTENSITY.equals(channelUID.getId())
                    && command instanceof QuantityType) {
                setDimmerOutputIntensity(((QuantityType<?>) command).intValue());
            } else if (SinopeBindingConstants.CHANNEL_LIGHTMODE.equals(channelUID.getId())
                    && command instanceof DecimalType) {
                setLightMode(((DecimalType) command).intValue());
            }

        } catch (IOException e) {
            logger.debug("Cannot handle command for channel {} because of {}", channelUID.getId(),
                    e.getLocalizedMessage());
            this.getSinopeGatewayHandler().setCommunicationError(true);
        }
    }

    public void setDimmerOutputIntensity(int outputIntensity) throws IOException {
        this.getSinopeGatewayHandler().stopPoll(); // We are about to send something to gateway.
        try {
            if (this.getSinopeGatewayHandler().connectToBridge()) {
                logger.debug("Connected to bridge");

                SinopeDataWriteRequest req = new SinopeDataWriteRequest(this.getSinopeGatewayHandler().newSeq(),
                        deviceId, new SinopeOutputIntensityData());
                ((SinopeOutputIntensityData) req.getAppData()).setOutputIntensity(outputIntensity);

                SinopeDataAnswer answ = (SinopeDataAnswer) this.getSinopeGatewayHandler().execute(req);

                if (answ.getStatus() == DATA_ANSWER) {
                    int answOutputIntensity = (((SinopeOutputIntensityData) answ.getAppData())).getOutputIntensity();
                    updateDimmerOutputIntensity(outputIntensity);
                    logger.debug("Output intensity is now: {} %%", answOutputIntensity);

                } else {
                    logger.debug("Cannot set output intensity, status: {}", answ.getStatus());
                }
            } else {
                logger.debug("Could not connect to bridge to update Output Intensity");
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Cannot connect to bridge");
            }
        } finally {
            this.getSinopeGatewayHandler().schedulePoll();
        }
    }

    private void setLightMode(int mode) throws UnknownHostException, IOException {
        getSinopeGatewayHandler().stopPoll();
        try {
            if (getSinopeGatewayHandler().connectToBridge()) {
                logger.debug("Connected to bridge");

                SinopeDataWriteRequest req = new SinopeDataWriteRequest(getSinopeGatewayHandler().newSeq(), deviceId,
                        new SinopeLightModeData());
                ((SinopeLightModeData) req.getAppData()).setLightMode((byte) mode);

                SinopeDataAnswer answ = (SinopeDataAnswer) getSinopeGatewayHandler().execute(req);

                if (answ.getStatus() == DATA_ANSWER) {
                    int answLightMode = (((SinopeLightModeData) answ.getAppData())).getLightMode();
                    updateLightMode(answLightMode);
                    logger.debug("Light mode is now : {}", answLightMode);
                } else {
                    logger.debug("Cannot Set Light mode, status: {}", answ.getStatus());
                }
            } else {
                logger.debug("Could not connect to bridge to update Light Mode");
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Cannot connect to bridge");
            }
        } finally {
            getSinopeGatewayHandler().schedulePoll();
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        logger.debug("bridgeStatusChanged {}", bridgeStatusInfo);
        updateDeviceId();
    }

    @Override
    public void initialize() {
        logger.debug("initializeThing thing {}", getThing().getUID());
        updateDeviceId();
    }

    @Override
    protected void updateConfiguration(Configuration configuration) {
        super.updateConfiguration(configuration);
        updateDeviceId();
    }

    public void updateDimmerOutputIntensity(int outputIntensity) {
        updateState(SinopeBindingConstants.CHANNEL_DIMMER_OUTPUTINTENSITY, new PercentType(outputIntensity));
    }

    public void updateLightMode(int lightMode) {
        updateState(SinopeBindingConstants.CHANNEL_LIGHTMODE, new DecimalType(lightMode));
    }

    public void update() throws IOException {
        if (this.deviceId != null) {
            if (isLinked(SinopeBindingConstants.CHANNEL_DIMMER_OUTPUTINTENSITY)) {
                this.updateDimmerOutputIntensity(readOutputIntensity());
            }
            if (isLinked(SinopeBindingConstants.CHANNEL_LIGHTMODE)) {
                this.updateLightMode(readLightMode());
            }

        } else {
            logger.error("Device id is null for Thing UID: {}", getThing().getUID());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
        }
    }

    private int readOutputIntensity() throws UnknownHostException, IOException {
        SinopeDataReadRequest req = new SinopeDataReadRequest(this.getSinopeGatewayHandler().newSeq(), deviceId,
                new SinopeOutputIntensityData());
        logger.debug("Reading Output Intensity for device id: {}", ByteUtil.toString(deviceId));
        SinopeDataAnswer answ = (SinopeDataAnswer) this.getSinopeGatewayHandler().execute(req);
        int intensity = ((SinopeOutputIntensityData) answ.getAppData()).getOutputIntensity();
        logger.debug("Output intensity is : {} %", intensity);
        return intensity;
    }

    private int readLightMode() throws UnknownHostException, IOException {
        SinopeDataReadRequest req = new SinopeDataReadRequest(this.getSinopeGatewayHandler().newSeq(), deviceId,
                new SinopeLightModeData());
        logger.debug("Reading Light Mode for device id: {}", ByteUtil.toString(deviceId));
        SinopeDataAnswer answ = (SinopeDataAnswer) this.getSinopeGatewayHandler().execute(req);
        int lightMode = ((SinopeLightModeData) answ.getAppData()).getLightMode();
        logger.debug("Light mode is : {}", lightMode);
        return lightMode;
    }

    private void updateDeviceId() {
        String sDeviceId = (String) getConfig().get(SinopeBindingConstants.CONFIG_PROPERTY_DEVICE_ID);
        this.deviceId = SinopeConfig.convert(sDeviceId);
        if (this.deviceId == null) {
            logger.debug("Invalid Device id, cannot convert id: {}", sDeviceId);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Invalid Device id");
            return;
        }
        Bridge bridge = this.getBridge();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            return;
        }
        SinopeGatewayHandler handler = getSinopeGatewayHandler();
        if (handler != null) {
            handler.registerDimmerHandler(this);
        }
        updateStatus(ThingStatus.ONLINE);
    }

    private @Nullable SinopeGatewayHandler getSinopeGatewayHandler() {
        Bridge bridge = this.getBridge();
        if (bridge != null) {
            return (SinopeGatewayHandler) bridge.getHandler();
        } else {
            return null;
        }
    }
}
