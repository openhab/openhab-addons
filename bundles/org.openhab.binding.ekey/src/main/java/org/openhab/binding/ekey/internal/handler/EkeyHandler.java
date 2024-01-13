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
package org.openhab.binding.ekey.internal.handler;

import static org.openhab.binding.ekey.internal.EkeyBindingConstants.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ekey.internal.EkeyConfiguration;
import org.openhab.binding.ekey.internal.EkeyDynamicStateDescriptionProvider;
import org.openhab.binding.ekey.internal.api.EkeyPacketListener;
import org.openhab.binding.ekey.internal.api.EkeyUdpPacketReceiver;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelKind;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.StateOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EkeyHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Hans-Jörg Merk - Initial contribution
 * @author Robert Delbrück - Add natIp
 */
@NonNullByDefault
public class EkeyHandler extends BaseThingHandler implements EkeyPacketListener {

    private final Logger logger = LoggerFactory.getLogger(EkeyHandler.class);

    private final EkeyDynamicStateDescriptionProvider ekeyStateDescriptionProvider;

    private EkeyConfiguration config = new EkeyConfiguration();
    private @Nullable EkeyUdpPacketReceiver receiver;

    public EkeyHandler(Thing thing, EkeyDynamicStateDescriptionProvider ekeyStateDescriptionProvider) {
        super(thing);
        this.ekeyStateDescriptionProvider = ekeyStateDescriptionProvider;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // The binding does not handle any command
    }

    @Override
    public void initialize() {
        logger.debug("ekey handler initializing");
        config = getConfigAs(EkeyConfiguration.class);

        if (!config.ipAddress.isEmpty() && config.port != 0) {
            updateStatus(ThingStatus.UNKNOWN);

            scheduler.submit(() -> {
                populateChannels(config.protocol);
                String readerThreadName = "OH-binding-" + getThing().getUID().getAsString();

                EkeyUdpPacketReceiver localReceiver = receiver = new EkeyUdpPacketReceiver(
                        Optional.ofNullable(config.natIp).orElse(config.ipAddress), config.port, readerThreadName);
                localReceiver.addEkeyPacketListener(this);
                try {
                    localReceiver.openConnection();
                } catch (IOException e) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Cannot open connection)");
                }
                updateStatus(ThingStatus.ONLINE);
            });
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No IP address specified)");
        }
    }

    @Override
    public void dispose() {
        EkeyUdpPacketReceiver localReceiver = this.receiver;

        if (localReceiver != null) {
            localReceiver.closeConnection();
            localReceiver.removeEkeyPacketListener(this);
        }
        super.dispose();
    }

    @Override
    public void messageReceived(byte[] message) {
        logger.debug("messageReceived() : {}", message);
        config = getConfigAs(EkeyConfiguration.class);
        String delimiter = config.delimiter;

        switch (config.protocol) {
            case "RARE":
                parseRare(message, delimiter);
                break;
            case "MULTI":
                parseMulti(message, delimiter);
                break;
            case "HOME":
                parseHome(message, delimiter);
                break;
        }
    }

    @Override
    public void connectionStatusChanged(ThingStatus status, byte @Nullable [] message) {
        if (message != null) {
            this.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, null);
        }
        this.updateStatus(status);
    }

    public void parseRare(byte[] message, String delimiter) {
        logger.debug("parse RARE packet");
        if (message.length >= 72) {
            byte[] newMessage = Arrays.copyOf(message, 72);
            String messageString = new String(newMessage);
            long action = getIntValueFrom(newMessage, 4, 4);
            long terminalid = getIntValueFrom(newMessage, 8, 4);
            String terminalSerial = getStringValueFrom(newMessage, 12, 14);
            long relayid = getIntValueFrom(newMessage, 26, 1);
            long userid = getIntValueFrom(newMessage, 28, 4);
            long fingerid = getIntValueFrom(newMessage, 32, 4);
            int serial = reconstructFsSerial(terminalid);
            if (logger.isTraceEnabled()) {
                logger.trace("messageString received() : {}", messageString);
                logger.trace("AKTION           : {}", action);
                logger.trace("TERMINAL SERIAL  : {}", terminalSerial);
                logger.trace("RESERVED         : {}", getStringValueFrom(newMessage, 27, 1));
                logger.trace("RELAY ID         : {}", relayid);
                logger.trace("USER ID          : {}", userid);
                logger.trace("FINGER ID        : {}", fingerid);
                logger.trace("EVENT            : {}", getStringValueFrom(newMessage, 36, 16));
                logger.trace("FS SERIAL        : {}", serial);
            }
            updateState(CHANNEL_TYPE_ACTION, new DecimalType(action));
            if (!terminalSerial.isEmpty()) {
                updateState(CHANNEL_TYPE_TERMID, DecimalType.valueOf(terminalSerial));
            } else {
                updateState(CHANNEL_TYPE_TERMID, DecimalType.valueOf("-1"));
            }
            updateState(CHANNEL_TYPE_RESERVED, StringType.valueOf(getStringValueFrom(newMessage, 27, 1)));
            updateState(CHANNEL_TYPE_RELAYID, new DecimalType(relayid));
            updateState(CHANNEL_TYPE_USERID, new DecimalType(userid));
            updateState(CHANNEL_TYPE_FINGERID, new DecimalType(fingerid));
            updateState(CHANNEL_TYPE_EVENT, StringType.valueOf(getStringValueFrom(newMessage, 36, 16)));
            updateState(CHANNEL_TYPE_FSSERIAL, new DecimalType(serial));

        }
    }

    public void parseMulti(byte[] message, String delimiter) {
        logger.debug("parse MULTI packet");
        if (message.length >= 46) {
            byte[] newMessage = Arrays.copyOf(message, 46);
            String messageString = new String(newMessage);
            String[] array = messageString.split(delimiter);
            if (logger.isTraceEnabled()) {
                logger.trace("messageString received() : {}", messageString);
                logger.trace("USER ID     : {}", array[1]);
                logger.trace("USER ID     : {}", array[1]);
                logger.trace("USER NAME   : {}", array[2]);
                logger.trace("USER STATUS : {}", array[3]);
                logger.trace("FINGER ID   : {}", array[4]);
                logger.trace("KEY ID      : {}", array[5]);
                logger.trace("SERIENNR FS : {}", array[6]);
                logger.trace("NAME FS     : {}", new String(array[7]).replace("-", ""));
                logger.trace("AKTION      : {}", array[8]);
                logger.trace("INPUT ID    : {}", array[9]);

            }
            if (!"-".equals(array[1])) {
                updateState(CHANNEL_TYPE_USERID, DecimalType.valueOf((array[1])));
            } else {
                updateState(CHANNEL_TYPE_USERID, DecimalType.valueOf("-1"));
            }
            String userName = (array[2]).toString();
            if (!userName.isEmpty()) {
                userName = userName.replace("-", "");
                userName = userName.replace(" ", "");
                updateState(CHANNEL_TYPE_USERNAME, StringType.valueOf(userName));
            }
            if (!"-".equals(array[3])) {
                updateState(CHANNEL_TYPE_USERSTATUS, DecimalType.valueOf((array[3])));
            } else {
                updateState(CHANNEL_TYPE_USERSTATUS, DecimalType.valueOf("-1"));
            }
            if (!"-".equals(array[4])) {
                updateState(CHANNEL_TYPE_FINGERID, DecimalType.valueOf((array[4])));
            } else {
                updateState(CHANNEL_TYPE_FINGERID, DecimalType.valueOf("-1"));
            }
            if (!"-".equals(array[5])) {
                updateState(CHANNEL_TYPE_KEYID, DecimalType.valueOf((array[5])));
            } else {
                updateState(CHANNEL_TYPE_KEYID, DecimalType.valueOf("-1"));
            }
            updateState(CHANNEL_TYPE_FSSERIAL, DecimalType.valueOf((array[6])));
            updateState(CHANNEL_TYPE_FSNAME, new StringType(new String(array[7]).replace("-", "")));
            updateState(CHANNEL_TYPE_ACTION, DecimalType.valueOf((array[8])));
            if (!"-".equals(array[9])) {
                updateState(CHANNEL_TYPE_INPUTID, DecimalType.valueOf((array[9])));
            } else {
                updateState(CHANNEL_TYPE_INPUTID, DecimalType.valueOf("-1"));
            }
        } else {
            logger.trace("received packet is to short : {}", message);
        }
    }

    public void parseHome(byte[] message, String delimiter) {
        logger.debug("parse HOME packet");
        if (message.length >= 27) {
            byte[] newMessage = Arrays.copyOf(message, 27);
            String messageString = new String(newMessage);
            String[] array = messageString.split(delimiter);
            if (logger.isTraceEnabled()) {
                logger.trace("messageString received() : {}", messageString);
                logger.trace("USER ID     : {}", array[1]);
                logger.trace("FINGER ID   : {}", array[2]);
                logger.trace("SERIENNR FS : {}", array[3]);
                logger.trace("AKTION      : {}", array[4]);
                logger.trace("RELAY ID    : {}", array[5]);
            }
            if (!"-".equals(array[1])) {
                updateState(CHANNEL_TYPE_USERID, DecimalType.valueOf((array[1])));
            } else {
                updateState(CHANNEL_TYPE_USERID, DecimalType.valueOf("-1"));
            }
            if (!"-".equals(array[2])) {
                updateState(CHANNEL_TYPE_FINGERID, DecimalType.valueOf((array[2])));
            } else {
                updateState(CHANNEL_TYPE_FINGERID, DecimalType.valueOf("-1"));
            }
            updateState(CHANNEL_TYPE_FSSERIAL, DecimalType.valueOf((array[3])));
            updateState(CHANNEL_TYPE_ACTION, DecimalType.valueOf((array[4])));
            if (!"-".equals(array[5])) {
                State relayId = DecimalType.valueOf((array[5]));
                updateState(CHANNEL_TYPE_RELAYID, relayId);
            } else {
                updateState(CHANNEL_TYPE_RELAYID, DecimalType.valueOf("-1"));
            }
        } else {
            logger.trace("received packet is to short : {}", message);
        }
    }

    public void addChannel(String channelId, String itemType, @Nullable final Collection<String> options) {
        if (thing.getChannel(channelId) == null) {
            logger.debug("Channel '{}' for UID to be added", channelId);
            ThingBuilder thingBuilder = editThing();
            final ChannelTypeUID channelTypeUID = new ChannelTypeUID(BINDING_ID, channelId);
            Channel channel = ChannelBuilder.create(new ChannelUID(getThing().getUID(), channelId), itemType)
                    .withType(channelTypeUID).withKind(ChannelKind.STATE).build();
            thingBuilder.withChannel(channel);
            updateThing(thingBuilder.build());
        }
        if (options != null) {
            final List<StateOption> stateOptions = options.stream()
                    .map(e -> new StateOption(e, e.substring(0, 1) + e.substring(1).toLowerCase()))
                    .collect(Collectors.toList());
            logger.debug("StateOptions : '{}'", stateOptions);
            ekeyStateDescriptionProvider.setStateOptions(new ChannelUID(getThing().getUID(), channelId), stateOptions);
        }
    }

    public void populateChannels(String protocol) {
        String channelId = "";
        String itemType = "Number";

        switch (protocol) {
            case "HOME":
                channelId = CHANNEL_TYPE_RELAYID;
                addChannel(channelId, itemType, null);
                break;
            case "MULTI":
                channelId = CHANNEL_TYPE_USERNAME;
                addChannel(channelId, "String", null);
                channelId = CHANNEL_TYPE_USERSTATUS;
                addChannel(channelId, itemType, null);
                channelId = CHANNEL_TYPE_KEYID;
                addChannel(channelId, itemType, null);
                channelId = CHANNEL_TYPE_FSNAME;
                addChannel(channelId, "String", null);
                channelId = CHANNEL_TYPE_INPUTID;
                addChannel(channelId, itemType, null);
                break;
            case "RARE":
                channelId = CHANNEL_TYPE_TERMID;
                addChannel(channelId, itemType, null);
                channelId = CHANNEL_TYPE_RELAYID;
                addChannel(channelId, itemType, null);
                channelId = CHANNEL_TYPE_RESERVED;
                addChannel(channelId, "String", null);
                channelId = CHANNEL_TYPE_EVENT;
                addChannel(channelId, "String", null);
                break;
        }
    }

    private long getIntValueFrom(byte[] bytes, int start, int length) {
        if (start + length > bytes.length) {
            return -1;
        }
        long value = 0;
        int bits = 0;
        for (int i = start; i < start + length; i++) {
            value |= (bytes[i] & 0xFF) << bits;
            bits += 8;
        }
        return value;
    }

    private String getStringValueFrom(byte[] bytes, int start, int length) {
        if (start + length > bytes.length) {
            logger.debug("Could not get String from bytes");
            return "";
        }
        StringBuffer value = new StringBuffer();
        for (int i = start + length - 1; i >= start; i--) {
            if (bytes[i] > (byte) ' ' && bytes[i] < (byte) '~') {
                value.append((char) bytes[i]);
            }
        }
        return value.toString();
    }

    private int reconstructFsSerial(long termID) {
        long s = termID;
        s ^= 0x70000000;
        int ssss = (int) (s & 0xFFFF);
        s >>= 16;
        int yy = (int) s % 53;
        int ww = (int) s / 53;
        yy *= 1000000;
        ww *= 10000;
        return ww + yy + ssss;
    }
}
