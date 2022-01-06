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
package org.openhab.binding.enocean.internal.eep;

import static org.openhab.binding.enocean.internal.EnOceanBindingConstants.*;
import static org.openhab.binding.enocean.internal.messages.ESP3Packet.*;

import java.util.Arrays;
import java.util.function.Function;

import org.openhab.binding.enocean.internal.Helper;
import org.openhab.binding.enocean.internal.messages.ERP1Message;
import org.openhab.binding.enocean.internal.messages.ERP1Message.RORG;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Daniel Weber - Initial contribution
 */
public abstract class EEP {

    protected RORG newRORG = RORG.Unknown;
    protected byte[] bytes;
    protected byte[] optionalData;
    protected byte[] senderId;
    protected byte status;

    protected byte[] destinationId;

    protected boolean suppressRepeating;

    protected Logger logger = LoggerFactory.getLogger(EEP.class);

    private EEPType eepType = null;
    protected ERP1Message packet = null;

    public EEP() {
        // ctor for sending

        status = 0x00;
        senderId = null;
        bytes = null;
    }

    public EEP(ERP1Message packet) {
        // ctor for receiving

        // Todo validation??
        this.packet = packet;
        setData(packet.getPayload(ESP3_RORG_LENGTH, getDataLength()));
        setSenderId(packet.getPayload(ESP3_RORG_LENGTH + getDataLength(), ESP3_SENDERID_LENGTH));
        setStatus(packet.getPayload(ESP3_RORG_LENGTH + getDataLength() + ESP3_SENDERID_LENGTH, 1)[0]);
        setOptionalData(packet.getOptionalPayload());
    }

    public EEP convertFromCommand(String channelId, String channelTypeId, Command command,
            Function<String, State> getCurrentStateFunc, Configuration config) {
        if (!getEEPType().isChannelSupported(channelId, channelTypeId)) {
            throw new IllegalArgumentException(String.format("Command %s of channel %s(%s) is not supported",
                    command.toString(), channelId, channelTypeId));
        }

        if (channelTypeId.equals(CHANNEL_TEACHINCMD) && command == OnOffType.ON) {
            teachInQueryImpl(config);
        } else {
            convertFromCommandImpl(channelId, channelTypeId, command, getCurrentStateFunc, config);
        }
        return this;
    }

    public State convertToState(String channelId, String channelTypeId, Configuration config,
            Function<String, State> getCurrentStateFunc) {
        if (!getEEPType().isChannelSupported(channelId, channelTypeId)) {
            throw new IllegalArgumentException(
                    String.format("Channel %s(%s) is not supported", channelId, channelTypeId));
        }

        switch (channelTypeId) {
            case CHANNEL_RSSI:
                if (this.optionalData == null || this.optionalData.length < 6) {
                    return UnDefType.UNDEF;
                }

                return new DecimalType((this.optionalData[5] & 0xFF) * -1);
            case CHANNEL_REPEATCOUNT:
                if (this.optionalData == null || this.optionalData.length < 6) {
                    return UnDefType.UNDEF;
                }

                return new DecimalType(this.status & 0b1111);
            case CHANNEL_LASTRECEIVED:
                return new DateTimeType();
        }

        return convertToStateImpl(channelId, channelTypeId, getCurrentStateFunc, config);
    }

    public String convertToEvent(String channelId, String channelTypeId, String lastEvent, Configuration config) {
        if (!getEEPType().isChannelSupported(channelId, channelTypeId)) {
            throw new IllegalArgumentException(
                    String.format("Channel %s(%s) is not supported", channelId, channelTypeId));
        }

        return convertToEventImpl(channelId, channelTypeId, lastEvent, config);
    }

    public EEP setRORG(RORG newRORG) {
        this.newRORG = newRORG;
        return this;
    }

    public EEP setData(byte... bytes) {
        if (!validateData(bytes)) {
            throw new IllegalArgumentException();
        }

        this.bytes = Arrays.copyOf(bytes, bytes.length);
        return this;
    }

    public boolean hasData() {
        return (this.bytes != null) && (this.bytes.length > 0);
    }

    public EEP setOptionalData(byte... bytes) {
        if (bytes != null) {
            this.optionalData = Arrays.copyOf(bytes, bytes.length);
        }

        return this;
    }

    public EEP setSenderId(byte[] senderId) {
        if (senderId == null || senderId.length != ESP3_SENDERID_LENGTH) {
            throw new IllegalArgumentException();
        }

        this.senderId = Arrays.copyOf(senderId, senderId.length);
        return this;
    }

    public EEP setStatus(byte status) {
        this.status = status;
        return this;
    }

    public EEP setSuppressRepeating(boolean suppressRepeating) {
        this.suppressRepeating = suppressRepeating;
        return this;
    }

    public final ERP1Message getERP1Message() {
        if (isValid()) {
            int optionalDataLength = 0;
            if (optionalData != null) {
                optionalDataLength = optionalData.length;
            }

            byte[] payLoad = new byte[ESP3_RORG_LENGTH + getDataLength() + ESP3_SENDERID_LENGTH + ESP3_STATUS_LENGTH
                    + optionalDataLength];
            Arrays.fill(payLoad, ZERO);

            byte rorgValue = getEEPType().getRORG().getValue();
            if (newRORG != RORG.Unknown) {
                rorgValue = newRORG.getValue();
            }

            // set repeater count to max if suppressRepeating
            payLoad = Helper.concatAll(new byte[] { rorgValue }, bytes, senderId,
                    new byte[] { (byte) (status | (suppressRepeating ? 0b1111 : 0)) }, optionalData);

            return new ERP1Message(payLoad.length - optionalDataLength, optionalDataLength, payLoad);
        } else {
            logger.warn("ERP1Message for EEP {} is not valid!", this.getClass().getName());
        }

        return null;
    }

    protected boolean validateData(byte[] bytes) {
        return bytes != null && bytes.length == getDataLength();
    }

    public boolean isValid() {
        return validateData(bytes) && senderId != null && senderId.length == ESP3_SENDERID_LENGTH;
    }

    protected EEPType getEEPType() {
        if (eepType == null) {
            eepType = EEPType.getType(this.getClass());
        }

        return eepType;
    }

    protected int getDataLength() {
        if (getEEPType() != null) {
            return getEEPType().getRORG().getDataLength();
        }

        return 0;
    }

    protected void convertFromCommandImpl(String channelId, String channelTypeId, Command command,
            Function<String, State> getCurrentStateFunc, Configuration config) {
        logger.warn("No implementation for sending data from channel {}/{} for this EEP!", channelId, channelTypeId);
    }

    protected State convertToStateImpl(String channelId, String channelTypeId,
            Function<String, State> getCurrentStateFunc, Configuration config) {
        return UnDefType.UNDEF;
    }

    protected String convertToEventImpl(String channelId, String channelTypeId, String lastEvent,
            Configuration config) {
        return null;
    }

    protected void teachInQueryImpl(Configuration config) {
        logger.warn("No implementation for sending a response for this teach in!");
    }

    protected boolean getBit(int byteData, int bit) {
        int mask = (1 << bit);
        return (byteData & mask) != 0;
    }

    public ThingTypeUID getThingTypeUID() {
        return getEEPType().getThingTypeUID();
    }

    public byte[] getSenderId() {
        return senderId;
    }

    public void addConfigPropertiesTo(DiscoveryResultBuilder discoveredThingResultBuilder) {
        discoveredThingResultBuilder.withProperty(PARAMETER_RECEIVINGEEPID, getEEPType().getId());
    }

    public EEP setDestinationId(byte[] destinationId) {
        if (destinationId != null) {
            this.destinationId = Arrays.copyOf(destinationId, destinationId.length);
            setOptionalData(Helper.concatAll(new byte[] { 0x01 }, destinationId, new byte[] { (byte) 0xff, 0x00 }));
        }
        return this;
    }
}
