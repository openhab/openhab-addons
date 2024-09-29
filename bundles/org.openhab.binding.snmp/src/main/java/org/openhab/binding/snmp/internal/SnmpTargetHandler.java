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
package org.openhab.binding.snmp.internal;

import static org.openhab.binding.snmp.internal.SnmpBindingConstants.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.snmp.internal.config.SnmpChannelConfiguration;
import org.openhab.binding.snmp.internal.config.SnmpInternalChannelConfiguration;
import org.openhab.binding.snmp.internal.config.SnmpTargetConfiguration;
import org.openhab.binding.snmp.internal.types.SnmpChannelMode;
import org.openhab.binding.snmp.internal.types.SnmpDatatype;
import org.openhab.binding.snmp.internal.types.SnmpProtocolVersion;
import org.openhab.binding.snmp.internal.types.SnmpSecurityModel;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.util.ThingHandlerHelper;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.openhab.core.types.util.UnitUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.AbstractTarget;
import org.snmp4j.CommandResponder;
import org.snmp4j.CommandResponderEvent;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.PDUv1;
import org.snmp4j.ScopedPDU;
import org.snmp4j.Snmp;
import org.snmp4j.UserTarget;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.event.ResponseListener;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.UsmUser;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.Counter64;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.IpAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.Opaque;
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
    private static final Pattern HEX_STRING_VALIDITY = Pattern.compile("([A-Fa-f0-9]{2}[ :-]?)+");
    private static final Pattern HEX_STRING_EXTRACTOR = Pattern.compile("[^A-Fa-f0-9]");

    private final Logger logger = LoggerFactory.getLogger(SnmpTargetHandler.class);

    private @NonNullByDefault({}) SnmpTargetConfiguration config;
    private final SnmpService snmpService;
    private @Nullable ScheduledFuture<?> refresh;
    private int timeoutCounter = 0;

    private @NonNullByDefault({}) AbstractTarget<UdpAddress> target;
    private @NonNullByDefault({}) String targetAddressString;

    private @NonNullByDefault({}) Set<SnmpInternalChannelConfiguration> readChannelSet;
    private @NonNullByDefault({}) Set<SnmpInternalChannelConfiguration> writeChannelSet;
    private @NonNullByDefault({}) Set<SnmpInternalChannelConfiguration> trapChannelSet;

    // SNMP v3
    private @Nullable UsmUser usmUser;
    private @Nullable OctetString engineId;

    public SnmpTargetHandler(Thing thing, SnmpService snmpService) {
        super(thing);
        this.snmpService = snmpService;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (target.getAddress() == null && !renewTargetAddress()) {
            logger.info("failed to renew target address, can't process '{}' to '{}'.", command, channelUID);
            return;
        }

        try {
            if (command instanceof RefreshType) {
                SnmpInternalChannelConfiguration channel = readChannelSet.stream()
                        .filter(c -> channelUID.equals(c.channelUID)).findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("no readable channel found"));
                PDU pdu = getPDU();
                pdu.setType(PDU.GET);
                pdu.add(new VariableBinding(channel.oid));
                snmpService.send(pdu, target, null, this);
            } else if (command instanceof DecimalType || command instanceof QuantityType
                    || command instanceof StringType || command instanceof OnOffType) {
                SnmpInternalChannelConfiguration channel = writeChannelSet.stream()
                        .filter(config -> channelUID.equals(config.channelUID)).findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("no writable channel found"));
                Variable variable;
                if (command instanceof OnOffType) {
                    variable = OnOffType.ON.equals(command) ? channel.onValue : channel.offValue;
                    if (variable == null) {
                        logger.debug("skipping {} to {}: no value defined", command, channelUID);
                        return;
                    }
                } else {
                    Command rawValue = command;
                    if (command instanceof QuantityType<?> quantityCommand) {
                        Unit<?> channelUnit = channel.unit;
                        if (channelUnit == null) {
                            rawValue = new DecimalType(quantityCommand.toBigDecimal());
                        } else {
                            QuantityType<?> convertedValue = quantityCommand.toUnit(channelUnit);
                            if (convertedValue == null) {
                                logger.warn("Cannot convert '{}' to configured unit '{}'", command, channelUnit);
                                return;
                            }
                            rawValue = new DecimalType(convertedValue.toBigDecimal());
                        }
                    }
                    variable = convertDatatype(rawValue, channel.datatype);
                }
                PDU pdu = getPDU();
                pdu.setType(PDU.SET);
                pdu.add(new VariableBinding(channel.oid, variable));
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

        if (thing.getThingTypeUID().equals(THING_TYPE_TARGET3)) {
            // override default for target3 things
            config.protocol = SnmpProtocolVersion.v3;
        }

        try {
            if (config.protocol.toInteger() == SnmpConstants.version1
                    || config.protocol.toInteger() == SnmpConstants.version2c) {
                CommunityTarget<UdpAddress> target = new CommunityTarget<>();
                target.setCommunity(new OctetString(config.community));
                this.target = target;
            } else if (config.protocol.toInteger() == SnmpConstants.version3) {
                String userName = config.user;
                if (userName == null) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "user not set");
                    return;
                }
                String authPassphrase = config.authPassphrase;
                if (config.securityModel != SnmpSecurityModel.NO_AUTH_NO_PRIV
                        && (authPassphrase == null || authPassphrase.isBlank())) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "Authentication passphrase not configured");
                    return;
                }
                String privPassphrase = config.privPassphrase;
                if (config.securityModel == SnmpSecurityModel.AUTH_PRIV
                        && (privPassphrase == null || privPassphrase.isBlank())) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "Privacy passphrase not configured");
                    return;
                }
                usmUser = new UsmUser(new OctetString(userName),
                        config.securityModel != SnmpSecurityModel.NO_AUTH_NO_PRIV ? config.authProtocol.getOid() : null,
                        config.securityModel != SnmpSecurityModel.NO_AUTH_NO_PRIV && authPassphrase != null
                                ? new OctetString(authPassphrase)
                                : null,
                        config.securityModel == SnmpSecurityModel.AUTH_PRIV ? config.privProtocol.getOid() : null,
                        config.securityModel == SnmpSecurityModel.AUTH_PRIV && privPassphrase != null
                                ? new OctetString(privPassphrase)
                                : null);

                UserTarget<UdpAddress> target = new UserTarget<>();
                target.setSecurityName(new OctetString(config.user));
                target.setSecurityLevel(config.securityModel.getSecurityLevel());
                this.target = target;
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "SNMP version not supported");
                return;
            }

            snmpService.addCommandResponder(this);

            target.setRetries(config.retries);
            target.setTimeout(config.timeout);
            target.setVersion(config.protocol.toInteger());
            target.setAddress(null);

            timeoutCounter = 0;
        } catch (IllegalArgumentException e) {
            // some methods of SNMP4J throw an unchecked IllegalArgumentException if they receive invalid values
            String message = "Exception during initialization: " + e.getMessage();
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, message);
            return;
        }

        updateStatus(ThingStatus.UNKNOWN);
        refresh = scheduler.scheduleWithFixedDelay(this::refresh, 0, config.refresh, TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        final ScheduledFuture<?> r = refresh;
        if (r != null && !r.isCancelled()) {
            r.cancel(true);
        }
        snmpService.removeCommandResponder(this);

        UsmUser user = usmUser;
        OctetString engineId = this.engineId;
        Address address = target.getAddress();
        if (user != null && engineId != null && address != null) {
            snmpService.removeUser(address, user, engineId);
        }

        usmUser = null;
        this.engineId = null;
    }

    @Override
    public void onResponse(@Nullable ResponseEvent event) {
        if (event == null) {
            return;
        }

        if (event.getSource() instanceof Snmp) {
            // Always cancel async request when response has been received
            // otherwise a memory leak is created! Not canceling a request
            // immediately can be useful when sending a request to a broadcast
            // address (Comment is taken from the SNMP4J API doc).
            ((Snmp) event.getSource()).cancel(event.getRequest(), this);
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
        if (ThingHandlerHelper.isHandlerInitialized(this)) {
            updateStatus(ThingStatus.ONLINE);
        }
        logger.trace("{} received {}", thing.getUID(), response);

        response.getVariableBindings().forEach(variable -> {
            if (variable != null) {
                updateChannels(variable.getOid(), variable.getVariable(), readChannelSet);
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

        if ((pdu.getType() == PDU.V1TRAP) && config.community.equals(community) && (pdu instanceof PDUv1 pduv1)) {
            logger.trace("{} received trap is PDUv1.", thing.getUID());
            OID oidEnterprise = pduv1.getEnterprise();
            int trapValue = pduv1.getGenericTrap();
            if (trapValue == PDUv1.ENTERPRISE_SPECIFIC) {
                trapValue = pduv1.getSpecificTrap();
            }
            updateChannels(oidEnterprise, new UnsignedInteger32(trapValue), trapChannelSet);
        }
        if ((pdu.getType() == PDU.TRAP || pdu.getType() == PDU.V1TRAP) && config.community.equals(community)
                && targetAddressString.equals(address)) {
            pdu.getVariableBindings().forEach(variable -> {
                if (variable != null) {
                    updateChannels(variable.getOid(), variable.getVariable(), trapChannelSet);
                }
            });
        }
    }

    private @Nullable SnmpInternalChannelConfiguration getChannelConfigFromChannel(Channel channel) {
        SnmpChannelConfiguration config = channel.getConfiguration().as(SnmpChannelConfiguration.class);

        String oid = config.oid;
        if (oid == null) {
            logger.warn("oid must not be null");
            return null;
        }

        SnmpDatatype datatype = config.datatype; // maybe null, override later
        Variable onValue = null;
        Variable offValue = null;
        Unit<?> unit = null;
        State exceptionValue = UnDefType.UNDEF;

        if (CHANNEL_TYPE_UID_NUMBER.equals(channel.getChannelTypeUID())) {
            if (datatype == null) {
                datatype = SnmpDatatype.INT32;
            } else if (datatype == SnmpDatatype.IPADDRESS || datatype == SnmpDatatype.STRING) {
                return null;
            }
            String configExceptionValue = config.exceptionValue;
            if (configExceptionValue != null) {
                exceptionValue = DecimalType.valueOf(configExceptionValue);
            }
            String configUnit = config.unit;
            if (configUnit != null) {
                unit = UnitUtils.parseUnit(configUnit);
                if (unit == null) {
                    logger.warn("Failed to parse unit from '{}'for channel '{}'", configUnit, channel.getUID());
                }
            }
        } else if (CHANNEL_TYPE_UID_STRING.equals(channel.getChannelTypeUID())) {
            if (datatype == null) {
                datatype = SnmpDatatype.STRING;
            } else if (datatype != SnmpDatatype.IPADDRESS && datatype != SnmpDatatype.STRING
                    && datatype != SnmpDatatype.HEXSTRING) {
                return null;
            }
            String configExceptionValue = config.exceptionValue;
            if (configExceptionValue != null) {
                exceptionValue = StringType.valueOf(configExceptionValue);
            }
        } else if (CHANNEL_TYPE_UID_SWITCH.equals(channel.getChannelTypeUID())) {
            if (datatype == null) {
                datatype = SnmpDatatype.UINT32;
            }
            try {
                final String configOnValue = config.onvalue;
                if (configOnValue != null) {
                    onValue = convertDatatype(new StringType(configOnValue), datatype);
                }
                final String configOffValue = config.offvalue;
                if (configOffValue != null) {
                    offValue = convertDatatype(new StringType(configOffValue), datatype);
                }
            } catch (IllegalArgumentException e) {
                logger.warn("illegal value configuration for channel {}", channel.getUID());
                return null;
            }
            String configExceptionValue = config.exceptionValue;
            if (configExceptionValue != null) {
                exceptionValue = OnOffType.from(configExceptionValue);
            }
        } else {
            logger.warn("unknown channel type found for channel {}", channel.getUID());
            return null;
        }
        return new SnmpInternalChannelConfiguration(channel.getUID(), new OID(oid), config.mode, datatype, onValue,
                offValue, exceptionValue, unit, config.doNotLogException);
    }

    private void generateChannelConfigs() {
        Set<SnmpInternalChannelConfiguration> channelConfigs = thing.getChannels().stream()
                .map(this::getChannelConfigFromChannel).filter(Objects::nonNull).map(Objects::requireNonNull)
                .collect(Collectors.toUnmodifiableSet());
        this.readChannelSet = channelConfigs.stream()
                .filter(c -> c.mode == SnmpChannelMode.READ || c.mode == SnmpChannelMode.READ_WRITE)
                .collect(Collectors.toSet());
        this.writeChannelSet = channelConfigs.stream()
                .filter(c -> c.mode == SnmpChannelMode.WRITE || c.mode == SnmpChannelMode.READ_WRITE)
                .collect(Collectors.toSet());
        this.trapChannelSet = channelConfigs.stream().filter(c -> c.mode == SnmpChannelMode.TRAP)
                .collect(Collectors.toSet());
    }

    private void updateChannels(OID oid, Variable value, Set<SnmpInternalChannelConfiguration> channelConfigs) {
        Set<SnmpInternalChannelConfiguration> updateChannelConfigs = channelConfigs.stream()
                .filter(c -> c.oid.equals(oid)).collect(Collectors.toSet());
        if (!updateChannelConfigs.isEmpty()) {
            updateChannelConfigs.forEach(channelConfig -> {
                ChannelUID channelUID = channelConfig.channelUID;
                final Channel channel = thing.getChannel(channelUID);
                State state;
                if (channel == null) {
                    logger.warn("channel uid {} in channel config set but channel not found", channelUID);
                    return;
                }
                if (value.isException()) {
                    if (!channelConfig.doNotLogException) {
                        logger.info("SNMP Exception: request {} returned '{}'", oid, value);
                    }
                    state = channelConfig.exceptionValue;
                } else if (CHANNEL_TYPE_UID_NUMBER.equals(channel.getChannelTypeUID())) {
                    try {
                        if (channelConfig.datatype == SnmpDatatype.FLOAT) {
                            if (value instanceof Opaque opaque) {
                                byte[] octets = opaque.toByteArray();
                                if (octets.length < 3) {
                                    // two bytes identifier and one byte length should always be present
                                    throw new UnsupportedOperationException("Not enough octets");
                                }
                                if (octets.length != (3 + octets[2])) {
                                    // octet 3 contains the lengths of the value
                                    throw new UnsupportedOperationException("Not enough octets");
                                }
                                if (octets[0] == (byte) 0x9f && octets[1] == 0x78 && octets[2] == 0x04) {
                                    // floating point value
                                    Unit<?> channelUnit = channelConfig.unit;
                                    float floatValue = Float.intBitsToFloat(
                                            octets[3] << 24 | octets[4] << 16 | octets[5] << 8 | octets[6]);
                                    state = channelUnit == null ? new DecimalType(floatValue)
                                            : new QuantityType<>(floatValue, channelUnit);
                                } else {
                                    throw new UnsupportedOperationException("Unknown opaque datatype" + value);
                                }
                            } else {
                                Unit<?> channelUnit = channelConfig.unit;
                                state = channelUnit == null ? new DecimalType(value.toString())
                                        : new QuantityType<>(value + channelUnit.getSymbol());
                            }
                        } else {
                            Unit<?> channelUnit = channelConfig.unit;
                            state = channelUnit == null ? new DecimalType(value.toLong())
                                    : new QuantityType<>(value.toLong(), channelUnit);
                        }
                    } catch (UnsupportedOperationException e) {
                        logger.warn("could not convert {} to number for channel {}", value, channelUID);
                        return;
                    }
                } else if (CHANNEL_TYPE_UID_STRING.equals(channel.getChannelTypeUID())) {
                    if (channelConfig.datatype == SnmpDatatype.HEXSTRING) {
                        String rawString = ((OctetString) value).toHexString(' ');
                        state = new StringType(rawString.toLowerCase());
                    } else {
                        state = new StringType(value.toString());
                    }
                } else if (CHANNEL_TYPE_UID_SWITCH.equals(channel.getChannelTypeUID())) {
                    if (value.equals(channelConfig.onValue)) {
                        state = OnOffType.ON;
                    } else if (value.equals(channelConfig.offValue)) {
                        state = OnOffType.OFF;
                    } else {
                        logger.debug("channel {} received unmapped value {} ", channelUID, value);
                        return;
                    }
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
        switch (datatype) {
            case INT32 -> {
                if (command instanceof DecimalType decimalCommand) {
                    return new Integer32(decimalCommand.intValue());
                } else if (command instanceof StringType stringCommand) {
                    return new Integer32((new DecimalType(stringCommand.toString())).intValue());
                }
            }
            case UINT32 -> {
                if (command instanceof DecimalType decimalCommand) {
                    return new UnsignedInteger32(decimalCommand.intValue());
                } else if (command instanceof StringType stringCommand) {
                    return new UnsignedInteger32((new DecimalType(stringCommand.toString())).intValue());
                }
            }
            case COUNTER64 -> {
                if (command instanceof DecimalType decimalCommand) {
                    return new Counter64(decimalCommand.longValue());
                } else if (command instanceof StringType stringCommand) {
                    return new Counter64((new DecimalType(stringCommand.toString())).longValue());
                }
            }
            case FLOAT, STRING -> {
                if (command instanceof DecimalType decimalCommand) {
                    return new OctetString(decimalCommand.toString());
                } else if (command instanceof StringType stringCommand) {
                    return new OctetString(stringCommand.toString());
                }
            }
            case HEXSTRING -> {
                if (command instanceof StringType stringCommand) {
                    String commandString = stringCommand.toString().toLowerCase();
                    Matcher commandMatcher = HEX_STRING_VALIDITY.matcher(commandString);
                    if (commandMatcher.matches()) {
                        commandString = HEX_STRING_EXTRACTOR.matcher(commandString).replaceAll("");
                        return OctetString.fromHexStringPairs(commandString);
                    }
                }
            }
            case IPADDRESS -> {
                if (command instanceof StringType stringCommand) {
                    return new IpAddress(stringCommand.toString());
                }
            }
            default -> {
            }
        }
        throw new IllegalArgumentException("illegal conversion of " + command + " to " + datatype);
    }

    private boolean renewTargetAddress() {
        try {
            target.setAddress(new UdpAddress(InetAddress.getByName(config.hostname), config.port));
            targetAddressString = target.getAddress().getInetAddress().getHostAddress();
            logger.trace("Determined {} as address for {} (thing {})", target.getAddress(), config.hostname,
                    this.thing.getUID());
            UsmUser user = usmUser;
            if (user != null && target instanceof UserTarget<UdpAddress> userTarget) {
                byte[] engineIdBytes = snmpService.getEngineId(target.getAddress());
                if (engineIdBytes != null) {
                    OctetString engineIdOctets = new OctetString(engineIdBytes);
                    this.engineId = engineIdOctets;
                    logger.trace("Determined {} as engineId for {}", engineIdOctets, target.getAddress());
                    snmpService.addUser(user, engineIdOctets);
                    userTarget.setAuthoritativeEngineID(engineIdBytes);
                } else {
                    target.setAddress(null);
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "Cannot determine engineId");
                    return false;
                }
            }
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
        PDU pdu = getPDU();
        pdu.setType(PDU.GET);
        readChannelSet.stream().map(c -> new VariableBinding(c.oid)).forEach(pdu::add);
        if (!pdu.getVariableBindings().isEmpty()) {
            try {
                snmpService.send(pdu, target, null, this);
            } catch (IOException e) {
                logger.info("Could not send PDU", e);
            }
        }
    }

    private PDU getPDU() {
        if (config.protocol == SnmpProtocolVersion.v3 || config.protocol == SnmpProtocolVersion.V3) {
            return new ScopedPDU();
        } else {
            return new PDU();
        }
    }
}
