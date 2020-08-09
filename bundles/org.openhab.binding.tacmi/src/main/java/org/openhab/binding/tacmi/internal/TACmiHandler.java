/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.tacmi.internal;

import static org.openhab.binding.tacmi.internal.TACmiBindingConstants.*;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.ConfigConstants;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.tacmi.internal.message.AnalogMessage;
import org.openhab.binding.tacmi.internal.message.AnalogValue;
import org.openhab.binding.tacmi.internal.message.DigitalMessage;
import org.openhab.binding.tacmi.internal.message.Message;
import org.openhab.binding.tacmi.internal.message.MessageType;
import org.openhab.binding.tacmi.internal.podData.PodData;
import org.openhab.binding.tacmi.internal.podData.PodIdentifier;
import org.openhab.binding.tacmi.internal.stateCache.StateCacheUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link TACmiHandler} is responsible for handling commands, which are sent
 * to one of the channels.
 *
 * @author Timo Wendt - Initial contribution
 * @author Christian Niessner - Ported to OpenHAB2
 */
@NonNullByDefault
public class TACmiHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(TACmiHandler.class);

    private final String STATE_CACHE_BASE = ConfigConstants.getUserDataFolder() + File.separator
            + TACmiBindingConstants.BINDING_ID + File.separator;

    private final Map<@Nullable PodIdentifier, @Nullable PodData> podDatas = new HashMap<>();
    private final Map<@Nullable ChannelUID, @Nullable TACmiChannelConfiguration> channelConfigByUID = new HashMap<>();

    private @Nullable TACmiCoEBridgeHandler bridge;
    private long lastMessageRecvTS; // last received message timestamp
    private boolean online; // online status shadow

    // state persistence (required as multiple states are sent at once so we need all
    // current states after startup)
    private @Nullable StateCacheUtils stateCacheUtils;

    /**
     * the C.M.I.'s address
     */
    private @Nullable InetAddress cmiAddress;

    /**
     * the CoE CAN-Node we representing
     */
    private int node;

    public TACmiHandler(final Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING);
        this.online = false;

        scheduler.execute(this::initializeDetached);
    }

    private void initializeDetached() {
        final TACmiConfiguration config = getConfigAs(TACmiConfiguration.class);

        if (config.host == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No host configured!");
            return;
        }
        try {
            cmiAddress = InetAddress.getByName(config.host);
        } catch (final UnknownHostException e1) {
            // message logged by framework via updateStatus
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Failed to get IP of CMI for '" + config.host + "'");
            return;
        }

        this.node = config.node;

        // initialize lookup maps...
        this.channelConfigByUID.clear();
        for (final Channel chann : getThing().getChannels()) {
            final ChannelTypeUID ct = chann.getChannelTypeUID();
            final boolean analog = CHANNEL_TYPE_COE_ANALOG_IN_UID.equals(ct)
                    || CHANNEL_TYPE_COE_ANALOG_OUT_UID.equals(ct);
            final boolean outgoing = CHANNEL_TYPE_COE_ANALOG_OUT_UID.equals(ct)
                    || CHANNEL_TYPE_COE_DIGITAL_OUT_UID.equals(ct);
            // for the analog out channel we have the measurement type. for the input
            // channel we take it from the C.M.I.
            final Class<? extends TACmiChannelConfiguration> ccClass = CHANNEL_TYPE_COE_ANALOG_OUT_UID.equals(ct)
                    ? TACmiChannelConfigurationAnalog.class
                    : TACmiChannelConfiguration.class;
            final TACmiChannelConfiguration cc = chann.getConfiguration().as(ccClass);
            this.channelConfigByUID.put(chann.getUID(), cc);
            final MessageType messageType = analog ? MessageType.ANALOG : MessageType.DIGITAL;
            final byte podId = this.getPodId(messageType, cc.output);
            final PodIdentifier pi = new PodIdentifier(messageType, podId, outgoing);
            // initialize podData
            getPodData(pi);
        }

        // this automatically restores persisted states...
        this.stateCacheUtils = new StateCacheUtils(
                new File(STATE_CACHE_BASE + getThing().getUID().getAsString().replace(':', '_') + ".json"),
                this.podDatas.values(), config.persistInterval);

        final Bridge br = getBridge();
        final TACmiCoEBridgeHandler bridge = br == null ? null : (TACmiCoEBridgeHandler) br.getHandler();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED, "No Bridge configured!");
            return;
        }
        bridge.registerCMI(this);
        this.bridge = bridge;

        // we set it to offline - will be set to online as soon as we start receiving
        // data...
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING, "Waiting for data from the C.M.I.");
    }

    private PodData getPodData(final PodIdentifier pi) {
        PodData pd = this.podDatas.get(pi);
        if (pd == null) {
            pd = new PodData(pi.podId, pi.messageType);
            if (pi.outgoing) {
                pd.message = pd.messageType == MessageType.ANALOG ? new AnalogMessage((byte) this.node, pi.podId)
                        : new DigitalMessage((byte) this.node, pi.podId);
            }
            this.podDatas.put(pi, pd);
        }
        return pd;
    }

    private byte getPodId(final MessageType messageType, final int output) {
        assert output >= 1 && output <= 32; // range 1-32
        // pod ID's: 0 & 9 for digital states, 1-8 for analog values
        if (messageType == MessageType.ANALOG) {
            return (byte) (((output - 1) / 4) + 1);
        }
        return (byte) (((output - 1) / 16) == 0 ? 0 : 9);
    }

    @Override
    public void handleCommand(final ChannelUID channelUID, final Command command) {
        final TACmiChannelConfiguration channelConfig = this.channelConfigByUID.get(channelUID);
        if (channelConfig == null) {
            logger.warn("Recived unhandled command '{}' on Channel {} ", command, channelUID);
            return;
        }
        final Channel channel = thing.getChannel(channelUID);
        if (channel == null) {
            return;
        }

        if (command instanceof RefreshType) {
            // we try to find the last known state from cache and return it.
            MessageType mt;
            if ((TACmiBindingConstants.CHANNEL_TYPE_COE_DIGITAL_IN_UID.equals(channel.getChannelTypeUID()))) {
                mt = MessageType.DIGITAL;
            } else if ((TACmiBindingConstants.CHANNEL_TYPE_COE_ANALOG_IN_UID.equals(channel.getChannelTypeUID()))) {
                mt = MessageType.ANALOG;
            } else {
                logger.warn("Recived unhandled command '{}' on Channel {} ", command, channelUID);
                return;
            }
            final byte podId = getPodId(mt, channelConfig.output);
            PodData pd = getPodData(new PodIdentifier(mt, podId, true));
            @Nullable
            Message message = pd.message;
            if (message == null) {
                // no data received yet from the C.M.I. and persistence might be disabled..
                return;
            }
            if (mt == MessageType.ANALOG) {
                final AnalogValue value = ((AnalogMessage) message).getAnalogValue(channelConfig.output);
                updateState(channel.getUID(), new DecimalType(value.value));
            } else {
                final boolean state = ((DigitalMessage) message).getPortState(channelConfig.output);
                updateState(channel.getUID(), state ? OnOffType.ON : OnOffType.OFF);
            }
            return;
        }
        MessageType mt;
        if ((TACmiBindingConstants.CHANNEL_TYPE_COE_DIGITAL_OUT_UID.equals(channel.getChannelTypeUID()))) {
            mt = MessageType.DIGITAL;
        } else if ((TACmiBindingConstants.CHANNEL_TYPE_COE_ANALOG_OUT_UID.equals(channel.getChannelTypeUID()))) {
            mt = MessageType.ANALOG;
        } else {
            logger.warn("Recived unhandled command '{}' on Channel {} ", command, channelUID);
            return;
        }

        final byte podId = getPodId(mt, channelConfig.output);
        PodData pd = getPodData(new PodIdentifier(mt, podId, true));
        @Nullable
        Message message = pd.message;
        if (message == null) {
            logger.error("Internal error - BUG - no outgoing message for command '{}' on Channel {} ", command,
                    channelUID);
            return;
        }
        boolean modified;
        switch (mt) {
            case DIGITAL:
                final boolean state = OnOffType.ON.equals(command) ? true : false;
                modified = ((DigitalMessage) message).setPortState((channelConfig.output - 1) % 16, state);
                break;
            case ANALOG:
                final TACmiMeasureType measureType = TACmiMeasureType
                        .values()[((TACmiChannelConfigurationAnalog) channelConfig).type];
                final DecimalType dt = (DecimalType) command;
                final double val = dt.doubleValue() * measureType.getOffset();
                modified = message.setValue((channelConfig.output - 1) % 4, (short) val, measureType.ordinal());
                break;
            default:
                logger.warn("Recived unhandled command '{}' on Channel {} ", command, channelUID);
                return;
        }
        if (modified) {
            pd.dirty = true; // flag as dirty
            try {
                @Nullable
                final TACmiCoEBridgeHandler br = this.bridge;
                @Nullable
                final InetAddress cmia = this.cmiAddress;
                if (br != null && cmia != null) {
                    br.sendData(message.getRaw(), cmia);
                    pd.lastSent = System.currentTimeMillis();
                }
                // we also update the local state after we successfully sent out the command
                // there is no feedback from the C.M.I. so we only could assume the message has been received when we
                // were able to send it...
                updateState(channel.getUID(), (State) command);
            } catch (final IOException e) {
                logger.warn("Error sending message: {}: {}", e.getClass().getName(), e.getMessage());
            }
        }
    }

    @Override
    public void dispose() {
        final TACmiCoEBridgeHandler br = this.bridge;
        if (br != null) {
            br.unregisterCMI(this);
        }
        @Nullable
        final StateCacheUtils scu = this.stateCacheUtils;
        if (scu != null) {
            scu.persistStates(podDatas.values(), true);
        }
        super.dispose();
    }

    public boolean isFor(final InetAddress remoteAddress, final int node) {
        @Nullable
        final InetAddress cmia = this.cmiAddress;
        if (cmia == null) {
            return false;
        }
        return this.node == node && cmia.equals(remoteAddress);
    }

    public void handleCoE(final Message message) {
        final ChannelTypeUID channelType = message.getType() == MessageType.DIGITAL
                ? TACmiBindingConstants.CHANNEL_TYPE_COE_DIGITAL_IN_UID
                : TACmiBindingConstants.CHANNEL_TYPE_COE_ANALOG_IN_UID;
        if (!this.online) {
            updateStatus(ThingStatus.ONLINE);
            this.online = true;
        }
        this.lastMessageRecvTS = System.currentTimeMillis();
        for (final Channel channel : thing.getChannels()) {
            if (!(channelType.equals(channel.getChannelTypeUID()))) {
                continue;
            }
            final int output = ((Number) channel.getConfiguration().get(TACmiBindingConstants.CHANNEL_CONFIG_OUTPUT))
                    .intValue();
            if (!message.hasPortnumber(output)) {
                continue;
            }

            if (message.getType() == MessageType.ANALOG) {
                final AnalogValue value = ((AnalogMessage) message).getAnalogValue(output);
                updateState(channel.getUID(), new DecimalType(value.value));
            } else {
                final boolean state = ((DigitalMessage) message).getPortState(output);
                updateState(channel.getUID(), state ? OnOffType.ON : OnOffType.OFF);
            }
        }
    }

    public void checkForTimeout() {
        final long refTs = System.currentTimeMillis();
        if (online && refTs - this.lastMessageRecvTS > 900000) {
            // 900 sec no data - set thing to offline..
            this.online = false;
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "No update from C.M.I. for 15 min");
        }
        for (final PodData pd : this.podDatas.values()) {
            if (pd == null) {
                continue;
            }
            @Nullable
            Message message = pd.message;
            if (message != null && refTs - pd.lastSent > 300000) {
                // reset every 300 secs...
                try {
                    @Nullable
                    final TACmiCoEBridgeHandler br = this.bridge;
                    @Nullable
                    final InetAddress cmia = this.cmiAddress;
                    if (br != null && cmia != null) {
                        br.sendData(message.getRaw(), cmia);
                        pd.lastSent = System.currentTimeMillis();
                    }
                } catch (final IOException e) {
                    logger.warn("Error sending message to C.M.I.: {}: {}", e.getClass().getName(), e.getMessage());
                }
            }
        }
        final StateCacheUtils scu = this.stateCacheUtils;
        if (scu != null) {
            scu.persistStates(podDatas.values(), false);
        }
    }
}
