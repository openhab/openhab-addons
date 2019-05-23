/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.snmp.internal;

import static org.openhab.binding.snmp.internal.SnmpBindingConstants.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.snmp.internal.config.SnmpChannelConfiguration;
import org.openhab.binding.snmp.internal.config.SnmpTargetConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.AbstractTarget;
import org.snmp4j.CommandResponder;
import org.snmp4j.CommandResponderEvent;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.event.ResponseListener;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Counter64;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.IpAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.UnsignedInteger32;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;

/**
 * The {@link SnmpTargetHandler} is responsible for handling commands, which are
 * sent to one of the channels or update remote channels
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class SnmpTargetHandler extends BaseThingHandler implements ResponseListener, CommandResponder {
    private final Logger logger = LoggerFactory.getLogger(SnmpTargetHandler.class);

    private @NonNullByDefault({}) SnmpTargetConfiguration config;
    private final SnmpService snmpService;
    private @NonNullByDefault({}) ScheduledFuture<?> refresh;
    private int timeoutCounter = 0;

    private @NonNullByDefault({}) AbstractTarget target;
    private @NonNullByDefault({}) String targetAddressString;

    private @NonNullByDefault({}) Set<SnmpChannelConfiguration> readChannelSet;
    private @NonNullByDefault({}) Set<SnmpChannelConfiguration> writeChannelSet;
    private @NonNullByDefault({}) Set<SnmpChannelConfiguration> trapChannelSet;

    public SnmpTargetHandler(Thing thing, SnmpService snmpService) {
        super(thing);
        this.snmpService = snmpService;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        try {
            if (command instanceof RefreshType) {
                SnmpChannelConfiguration channel = readChannelSet.stream().filter(c -> channelUID.equals(c._channelUID))
                        .findFirst().orElseThrow(() -> new IllegalArgumentException("no writable channel found"));
                PDU pdu = new PDU(PDU.GET, Collections.singletonList(new VariableBinding(channel._oid)));
                snmpService.send(pdu, target, null, this);
            } else if (command instanceof DecimalType || command instanceof StringType) {
                SnmpChannelConfiguration channel = writeChannelSet.stream()
                        .filter(config -> channelUID.equals(config._channelUID)).findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("no writable channel found"));
                PDU pdu = new PDU(PDU.SET, Collections
                        .singletonList(new VariableBinding(channel._oid, convertDatatype(command, channel.datatype))));
                snmpService.send(pdu, target, null, this);
            }
        } catch (IllegalArgumentException e) {
            logger.warn("can't process command {} to {}: {}", command, channelUID, e.getMessage());
        } catch (IOException e) {
            logger.warn("Could not send PDU while processing command {} to {}", command, channelUID);
        }
    }

    @Override
    public void initialize() {
        config = getConfigAs(SnmpTargetConfiguration.class);

        generateChannelConfigs();

        if (config.protocol == SnmpConstants.version1 || config.protocol == SnmpConstants.version2c) {
            CommunityTarget target = new CommunityTarget();
            target.setCommunity(new OctetString(config.community));
            target.setRetries(config.retries);
            target.setTimeout(config.timeout);
            target.setVersion(config.protocol);
            target.setAddress(null);
            this.target = target;
            snmpService.addCommandResponder(this);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "SNMP version not supported");
            return;
        }

        timeoutCounter = 0;

        updateStatus(ThingStatus.UNKNOWN);
        refresh = scheduler.scheduleWithFixedDelay(this::refresh, 0, config.refresh, TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        if (!refresh.isCancelled()) {
            refresh.cancel(true);
        }
        snmpService.removeCommandResponder(this);
    }

    @Override
    public void onResponse(@Nullable ResponseEvent event) {
        if (event == null) {
            return;
        }
        PDU response = event.getResponse();
        if (response == null) {
            Exception e = event.getError();
            if (e == null) { // no response, no error -> request timed out
                timeoutCounter++;
                if (timeoutCounter > config.retries) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "request timed out");
                    target.setAddress(null);
                }
                return;
            }
            logger.warn("{} requested {} and got error: {}", thing.getUID(), event.getRequest(), e.getMessage());
            return;
        }
        timeoutCounter = 0;
        logger.trace("{} received {}", thing.getUID(), response);

        response.getVariableBindings().forEach(variable -> {
            OID oid = variable.getOid();
            Variable value = variable.getVariable();
            if (value.isException()) {
                logger.warn("Error: request {}:{} returned '{}'", event.getPeerAddress(), oid, variable.toString());
                return;
            } else {
                updateChannels(oid, value, readChannelSet);
            }
        });
    }

    @Override
    public void processPdu(@Nullable CommandResponderEvent event) {
        if (event == null) {
            return;
        }
        logger.trace("{} received trap {}", thing.getUID(), event);

        final PDU pdu = event.getPDU();
        final String address = ((UdpAddress) event.getPeerAddress()).getInetAddress().getHostAddress();
        final String community = new String(event.getSecurityName());

        if ((pdu.getType() == PDU.TRAP || pdu.getType() == PDU.V1TRAP) && config.community.equals(community)
                && targetAddressString.equals(address)) {
            pdu.getVariableBindings().forEach(variable -> {
                OID oid = variable.getOid();
                Variable value = variable.getVariable();
                updateChannels(oid, value, trapChannelSet);
            });
        }
    }

    private void generateChannelConfigs() {
        Set<SnmpChannelConfiguration> channelConfigs = Collections
                .unmodifiableSet(thing.getChannels().stream().map(channel -> {
                    SnmpChannelConfiguration config = channel.getConfiguration().as(SnmpChannelConfiguration.class);
                    config._channelUID = channel.getUID();
                    config._oid = new OID(config.oid);
                    if (CHANNEL_TYPE_UID_NUMBER.equals(channel.getChannelTypeUID())) {
                        if (config.datatype == null) {
                            config.datatype = SnmpDatatype.INT32;
                        } else if (config.datatype == SnmpDatatype.IPADDRESS
                                || config.datatype == SnmpDatatype.STRING) {
                            return null;
                        }
                    } else if (CHANNEL_TYPE_UID_STRING.equals(channel.getChannelTypeUID())) {
                        if (config.datatype == null) {
                            config.datatype = SnmpDatatype.STRING;
                        } else if (config.datatype != SnmpDatatype.IPADDRESS
                                && config.datatype != SnmpDatatype.STRING) {
                            return null;
                        }
                    } else {
                        logger.warn("unknown channel type found for channel {}", channel.getUID());
                        return null;
                    }
                    return config;
                }).filter(Objects::nonNull).collect(Collectors.toSet()));
        this.readChannelSet = channelConfigs.stream()
                .filter(c -> c.mode == SnmpChannelMode.READ || c.mode == SnmpChannelMode.READ_WRITE)
                .collect(Collectors.toSet());
        this.writeChannelSet = channelConfigs.stream()
                .filter(c -> c.mode == SnmpChannelMode.WRITE || c.mode == SnmpChannelMode.READ_WRITE)
                .collect(Collectors.toSet());
        this.trapChannelSet = channelConfigs.stream().filter(c -> c.mode == SnmpChannelMode.TRAP)
                .collect(Collectors.toSet());
    }

    private void updateChannels(OID oid, Variable value, Set<SnmpChannelConfiguration> channelConfigs) {
        Set<SnmpChannelConfiguration> updateChannelConfigs = channelConfigs.stream().filter(c -> c._oid.equals(oid))
                .collect(Collectors.toSet());
        if (!updateChannelConfigs.isEmpty()) {
            updateChannelConfigs.forEach(channelConfig -> {
                ChannelUID channelUID = channelConfig._channelUID;
                final Channel channel = thing.getChannel(channelUID);
                State state;
                if (channel == null) {
                    logger.warn("channel uid {} in channel config set but channel not found", channelUID);
                    return;
                }
                if (CHANNEL_TYPE_UID_NUMBER.equals(channel.getChannelTypeUID())) {
                    try {
                        state = new DecimalType(value.toLong());
                    } catch (UnsupportedOperationException e) {
                        logger.warn("could not convert {} to number for channel {}", value, channelUID);
                        return;
                    }
                } else if (CHANNEL_TYPE_UID_STRING.equals(channel.getChannelTypeUID())) {
                    state = new StringType(value.toString());
                } else {
                    logger.warn("channel {} has unknown ChannelTypeUID", channelUID);
                    return;
                }
                updateState(channelUID, state);
            });
        } else {
            logger.debug("received value {} for unknown OID {}, skipping", value, oid);
        }
    }

    private Variable convertDatatype(Command command, SnmpDatatype datatype) {
        try {
            switch (datatype) {
                case INT32:
                    return new Integer32(((DecimalType) command).intValue());
                case UINT32:
                    return new UnsignedInteger32(((DecimalType) command).intValue());
                case COUNTER64:
                    return new Counter64(((DecimalType) command).longValue());
                case STRING:
                    return new OctetString(((StringType) command).toString());
                case IPADDRESS:
                    return new IpAddress(((StringType) command).toString());
                default:
                    throw new IllegalArgumentException(
                            "illegal conversion of " + command.toString() + " to " + datatype.toString());
            }
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(
                    "illegal conversion of " + command.toString() + " to " + datatype.toString());
        }
    }

    private boolean renewTargetAddress() {
        try {
            target.setAddress(new UdpAddress(InetAddress.getByName(config.hostname), config.port));
            targetAddressString = ((UdpAddress) target.getAddress()).getInetAddress().getHostAddress();
            updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
            return true;
        } catch (UnknownHostException e) {
            target.setAddress(null);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Cannot resolve target host");
            return false;
        }
    }

    private void refresh() {
        if (target.getAddress() == null) {
            if (!renewTargetAddress()) {
                logger.info("failed to renew target address, waiting for next refresh cycle");
                return;
            }
        }
        PDU pdu = new PDU(PDU.GET,
                readChannelSet.stream().map(c -> new VariableBinding(c._oid)).collect(Collectors.toList()));
        if (!pdu.getVariableBindings().isEmpty()) {
            try {
                snmpService.send(pdu, target, null, this);
            } catch (IOException e) {
                logger.info("Could not send PDU: {}", e);
            }
        }
    }

}
