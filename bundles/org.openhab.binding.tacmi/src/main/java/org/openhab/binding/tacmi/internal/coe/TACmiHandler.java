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
package org.openhab.binding.tacmi.internal.coe;

import static org.openhab.binding.tacmi.internal.TACmiBindingConstants.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tacmi.internal.TACmiBindingConstants;
import org.openhab.binding.tacmi.internal.TACmiMeasureType;
import org.openhab.binding.tacmi.internal.message.AnalogMessage;
import org.openhab.binding.tacmi.internal.message.AnalogValue;
import org.openhab.binding.tacmi.internal.message.DigitalMessage;
import org.openhab.binding.tacmi.internal.message.Message;
import org.openhab.binding.tacmi.internal.message.MessageType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
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

    private final Map<PodIdentifier, PodData> podDatas = new HashMap<>();
    private final Map<ChannelUID, TACmiChannelConfiguration> channelConfigByUID = new HashMap<>();

    private @Nullable TACmiCoEBridgeHandler bridge;
    private long lastMessageRecvTS; // last received message timestamp

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
        updateStatus(ThingStatus.UNKNOWN);

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
        this.podDatas.clear();
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
                    : TACmiChannelConfigurationDigital.class;
            final TACmiChannelConfiguration channelConfig = chann.getConfiguration().as(ccClass);
            this.channelConfigByUID.put(chann.getUID(), channelConfig);
            final MessageType messageType = analog ? MessageType.ANALOG : MessageType.DIGITAL;
            final byte podId = this.getPodId(messageType, channelConfig.output);
            final PodIdentifier pi = new PodIdentifier(messageType, podId, outgoing);
            // initialize podData
            PodData pd = this.getPodData(pi);
            if (outgoing) {
                int outputIdx = getOutputIndex(channelConfig.output, analog);
                PodDataOutgoing podDataOutgoing = (PodDataOutgoing) pd;
                // we have to track value state for all outgoing channels to ensure we have valid values for all
                // channels in use before we send a message to the C.M.I. otherwise it could trigger some strange things
                // on TA side...
                boolean set = false;
                if (analog) {
                    TACmiChannelConfigurationAnalog ca = (TACmiChannelConfigurationAnalog) channelConfig;
                    Double initialValue = ca.initialValue;
                    if (initialValue != null) {
                        final TACmiMeasureType measureType = TACmiMeasureType.values()[ca.type];
                        final double val = initialValue.doubleValue() * measureType.getOffset();
                        @Nullable
                        Message message = pd.message;
                        if (message != null) {
                            // shouldn't happen, just in case...
                            message.setValue(outputIdx, (short) val, measureType.ordinal());
                            set = true;
                        }
                    }
                } else {
                    // digital...
                    TACmiChannelConfigurationDigital ca = (TACmiChannelConfigurationDigital) channelConfig;
                    Boolean initialValue = ca.initialValue;
                    if (initialValue != null) {
                        @Nullable
                        DigitalMessage message = (DigitalMessage) pd.message;
                        if (message != null) {
                            // shouldn't happen, just in case...
                            message.setPortState(outputIdx, initialValue);
                            set = true;
                        }
                    }
                }
                podDataOutgoing.channeUIDs[outputIdx] = chann.getUID();
                podDataOutgoing.initialized[outputIdx] = set;
            }
        }

        final Bridge br = getBridge();
        final TACmiCoEBridgeHandler bridge = br == null ? null : (TACmiCoEBridgeHandler) br.getHandler();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "No Bridge configured!");
            return;
        }
        bridge.registerCMI(this);
        this.bridge = bridge;

        // we set it to UNKNOWN. Will be set to ONLIN~E as soon as we start receiving
        // data or to OFFLINE when no data is received within 900 seconds.
        updateStatus(ThingStatus.UNKNOWN);
    }

    private PodData getPodData(final PodIdentifier pi) {
        PodData pd = this.podDatas.get(pi);
        if (pd == null) {
            if (pi.outgoing) {
                pd = new PodDataOutgoing(pi, (byte) this.node);
            } else {
                pd = new PodData(pi, (byte) this.node);
            }
            this.podDatas.put(pi, pd);
        }
        return pd;
    }

    private byte getPodId(final MessageType messageType, final int output) {
        assert output >= 1 && output <= 32; // range 1-32
        // pod ID's: 0 & 9 for digital states, 1-8 for analog values
        boolean analog = messageType == MessageType.ANALOG;
        int outputIdx = getOutputIndex(output, analog);
        if (messageType == MessageType.ANALOG) {
            return (byte) (outputIdx + 1);
        }
        return (byte) (outputIdx == 0 ? 0 : 9);
    }

    /**
     * calculates output index position within the POD.
     * TA output index starts with 1, our arrays starts at 0. We also have to keep the pod size in mind...
     *
     * @param output
     * @param analog
     * @return
     */
    private int getOutputIndex(int output, boolean analog) {
        int outputIdx = output - 1;
        if (analog) {
            outputIdx %= 4;
        } else {
            outputIdx %= 16;
        }
        return outputIdx;
    }

    @Override
    public void handleCommand(final ChannelUID channelUID, final Command command) {
        final TACmiChannelConfiguration channelConfig = this.channelConfigByUID.get(channelUID);
        if (channelConfig == null) {
            logger.debug("Recived unhandled command '{}' for unknown Channel {} ", command, channelUID);
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
                logger.debug("Recived unhandled command '{}' on unknown Channel type {} ", command, channelUID);
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
                updateState(channel.getUID(), OnOffType.from(state));
            }
            return;
        }
        boolean analog;
        MessageType mt;
        if ((TACmiBindingConstants.CHANNEL_TYPE_COE_DIGITAL_OUT_UID.equals(channel.getChannelTypeUID()))) {
            mt = MessageType.DIGITAL;
            analog = false;
        } else if ((TACmiBindingConstants.CHANNEL_TYPE_COE_ANALOG_OUT_UID.equals(channel.getChannelTypeUID()))) {
            mt = MessageType.ANALOG;
            analog = true;
        } else {
            logger.debug("Recived unhandled command '{}' on Channel {} ", command, channelUID);
            return;
        }

        final byte podId = getPodId(mt, channelConfig.output);
        PodDataOutgoing podDataOutgoing = (PodDataOutgoing) getPodData(new PodIdentifier(mt, podId, true));
        @Nullable
        Message message = podDataOutgoing.message;
        if (message == null) {
            logger.error("Internal error - BUG - no outgoing message for command '{}' on Channel {} ", command,
                    channelUID);
            return;
        }
        int outputIdx = getOutputIndex(channelConfig.output, analog);
        boolean modified;
        if (analog) {
            final TACmiMeasureType measureType = TACmiMeasureType
                    .values()[((TACmiChannelConfigurationAnalog) channelConfig).type];
            final Number dt = (Number) command;
            final double val = dt.doubleValue() * measureType.getOffset();
            modified = message.setValue(outputIdx, (short) val, measureType.ordinal());
        } else {
            final boolean state = OnOffType.ON.equals(command) ? true : false;
            modified = ((DigitalMessage) message).setPortState(outputIdx, state);
        }
        podDataOutgoing.initialized[outputIdx] = true;
        if (modified) {
            try {
                @Nullable
                final TACmiCoEBridgeHandler br = this.bridge;
                @Nullable
                final InetAddress cmia = this.cmiAddress;
                if (br != null && cmia != null && podDataOutgoing.isAllValuesInitialized()) {
                    br.sendData(message.getRaw(), cmia);
                    podDataOutgoing.lastSent = System.currentTimeMillis();
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
        if (getThing().getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatus.ONLINE);
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
                State newState;
                switch (value.measureType) {
                    case TEMPERATURE:
                        newState = new QuantityType<>(value.value, SIUnits.CELSIUS);
                        break;
                    case KILOWATT:
                        // TA uses kW, in OH we use W
                        newState = new QuantityType<>(value.value * 1000, Units.WATT);
                        break;
                    case KILOWATTHOURS:
                        newState = new QuantityType<>(value.value, Units.KILOWATT_HOUR);
                        break;
                    case MEGAWATTHOURS:
                        newState = new QuantityType<>(value.value, Units.MEGAWATT_HOUR);
                        break;
                    case SECONDS:
                        newState = new QuantityType<>(value.value, Units.SECOND);
                        break;
                    default:
                        newState = new DecimalType(value.value);
                        break;
                }
                updateState(channel.getUID(), newState);
            } else {
                final boolean state = ((DigitalMessage) message).getPortState(output);
                updateState(channel.getUID(), OnOffType.from(state));
            }
        }
    }

    public void checkForTimeout() {
        final long refTs = System.currentTimeMillis();
        if (refTs - this.lastMessageRecvTS > 900000 && getThing().getStatus() != ThingStatus.OFFLINE) {
            // no data received for 900 seconds - set thing status to offline..
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "No update from C.M.I. for 15 min");
        }
        for (final PodData pd : this.podDatas.values()) {
            if (!(pd instanceof PodDataOutgoing)) {
                continue;
            }
            PodDataOutgoing podDataOutgoing = (PodDataOutgoing) pd;
            @Nullable
            Message message = pd.message;
            if (message != null && refTs - podDataOutgoing.lastSent > 300000) {
                // re-send every 300 seconds...
                @Nullable
                final InetAddress cmia = this.cmiAddress;
                if (podDataOutgoing.isAllValuesInitialized()) {
                    try {
                        @Nullable
                        final TACmiCoEBridgeHandler br = this.bridge;
                        if (br != null && cmia != null) {
                            br.sendData(message.getRaw(), cmia);
                            podDataOutgoing.lastSent = System.currentTimeMillis();
                        }
                    } catch (final IOException e) {
                        logger.warn("Error sending message to C.M.I.: {}: {}", e.getClass().getName(), e.getMessage());
                    }
                } else {
                    // pod is not entirely initialized - log warn for user but also set lastSent to prevent flooding of
                    // logs...
                    if (cmia != null) {
                        logger.warn("Sending data to {} {}.{} is blocked as we don't have valid values for channels {}",
                                cmia.getHostAddress(), this.node, podDataOutgoing.podId,
                                podDataOutgoing.getUninitializedChannelNames());
                    }
                    podDataOutgoing.lastSent = System.currentTimeMillis();
                }
            }
        }
    }
}
