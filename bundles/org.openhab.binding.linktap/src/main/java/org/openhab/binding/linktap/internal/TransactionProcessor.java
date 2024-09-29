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
package org.openhab.binding.linktap.internal;

import static org.openhab.binding.linktap.internal.LinkTapBindingConstants.BRIDGE_PROP_UTC_OFFSET;
import static org.openhab.binding.linktap.internal.LinkTapBindingConstants.GSON;
import static org.openhab.binding.linktap.protocol.frames.GatewayDeviceResponse.*;
import static org.openhab.binding.linktap.protocol.http.TransientCommunicationIssueException.TransientExecptionDefinitions.*;

import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.UUID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.linktap.protocol.frames.DeviceCmdReq;
import org.openhab.binding.linktap.protocol.frames.GatewayDeviceResponse;
import org.openhab.binding.linktap.protocol.frames.HandshakeResp;
import org.openhab.binding.linktap.protocol.frames.TLGatewayFrame;
import org.openhab.binding.linktap.protocol.frames.WaterMeterStatus;
import org.openhab.binding.linktap.protocol.http.CommandNotSupportedException;
import org.openhab.binding.linktap.protocol.http.DeviceIdException;
import org.openhab.binding.linktap.protocol.http.GatewayIdException;
import org.openhab.binding.linktap.protocol.http.InvalidParameterException;
import org.openhab.binding.linktap.protocol.http.NotTapLinkGatewayException;
import org.openhab.binding.linktap.protocol.http.TransientCommunicationIssueException;
import org.openhab.binding.linktap.protocol.http.WebServerApi;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.thing.ThingStatus;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link TransactionProcessor} is a transaction processor, that each Gateway has an instance of.
 * It is responsible for handling received frames from the Gateway.
 *
 * @author David Goodyear - Initial contribution
 */
@NonNullByDefault
public final class TransactionProcessor {

    // The Gateway pushes messages to us, the majority expect a response and are documented as
    // GW->Broker->App. These are sent via a HTTP request to the WebSerlet listening for the payloads.
    // Then we can also send data to the Gateway, these all also typically get a response, and are documented as
    // App->Broker->GW. These are sent via a POST request, to the relevant Gateway.
    // As the Gateway is an embedded device,

    private static final WebServerApi API = WebServerApi.getInstance();
    private static final int MAX_COMMAND_RETRIES = 3;
    private static final TransactionProcessor INSTANCE = new TransactionProcessor();

    private final Logger logger = LoggerFactory.getLogger(TransactionProcessor.class);

    private @Nullable TranslationProvider translationProvider;
    private @Nullable LocaleProvider localeProvider;
    private @Nullable Bundle bundle;

    public void setTranslationProviderInfo(TranslationProvider translationProvider, LocaleProvider localeProvider,
            Bundle bundle) {
        this.bundle = bundle;
        this.localeProvider = localeProvider;
        this.translationProvider = translationProvider;
    }

    public String getLocalizedText(String key, @Nullable Object @Nullable... arguments) {
        TranslationProvider translationProv = translationProvider;
        LocaleProvider localeProv = localeProvider;
        if (translationProv == null || localeProv == null) {
            return key;
        }
        String result = translationProv.getText(bundle, key, key, localeProv.getLocale(), arguments);
        return Objects.nonNull(result) ? result : key;
    }

    private TransactionProcessor() {
    }

    public static TransactionProcessor getInstance() {
        return INSTANCE;
    }

    public String processGwRequest(final String sourceHost, int command, final String payload) {
        final UUID uid = UUID.randomUUID();
        logger.debug("{} = GW -> APP Request {} -> Payload {}", uid, sourceHost, payload);
        String response = "";
        try {
            processGw(sourceHost, command, payload);
        } catch (CommandNotSupportedException cnse) {
            logger.warn("{}", getLocalizedText("bug-report.gw-unsupported-command", command));
        }
        logger.debug("{} = GW -> APP Response {} -> Payload {}", uid, payload, response);
        return response;
    }

    public String processGw(final String sourceHost, int command, final String payload)
            throws CommandNotSupportedException {
        final GatewayDeviceResponse frame = GSON.fromJson(payload, GatewayDeviceResponse.class);
        if (frame == null) {
            return "";
        }

        final String fromGatewayId = frame.gatewayId;
        final LinkTapBridgeHandler bridge = LinkTapBridgeHandler.GW_ID_LOOKUP.getItem(fromGatewayId);
        if (bridge != null) {
            logger.trace("Found bridge with ID: {} -> {}", fromGatewayId, bridge.getThing().getLabel());
        } else {
            logger.trace("Bridge not found with ID: {}", fromGatewayId);
        }

        // Only the water timer status payload arrives without a command, if there is a command id
        // then we use the one from the frame instead.
        command = CMD_UPDATE_WATER_TIMER_STATUS;
        if (frame.command != DEFAULT_INT) {
            command = frame.command;
        }

        final ResultStatus resultStatus = frame.getRes();
        if (resultStatus == ResultStatus.RET_CMD_NOT_SUPPORTED) {
            throw new CommandNotSupportedException(resultStatus);
        }

        String response = "";
        switch (command) {
            case CMD_UPDATE_WATER_TIMER_STATUS:
                WaterMeterStatus meterStatus = GSON.fromJson(payload, WaterMeterStatus.class);

                if (meterStatus != null) {
                    final String devId = meterStatus.deviceStatuses.get(0).deviceId;
                    final LinkTapHandler device = LinkTapBridgeHandler.DEV_ID_LOOKUP.getItem(devId);

                    if (device != null) {
                        logger.trace("Found device with ID: {} -> {}", devId, device.getThing().getLabel());
                        device.processDeviceCommand(command, payload);
                    } else {
                        logger.debug("No device with id {} found to process command {}",
                                meterStatus.deviceStatuses.get(0).deviceId, command);
                    }
                }
                break;
            case CMD_HANDSHAKE:
            case CMD_DATETIME_SYNC:
                response = generateTimeDateResponse(bridge, frame.gatewayId, command);
                if (bridge != null) {
                    bridge.processGatewayCommand(CMD_HANDSHAKE, payload);
                }
                break;
            case CMD_NOTIFICATION_WATERING_SKIPPED: {
                // This does not work - device id is within devStat!
                final DeviceCmdReq devFrame = GSON.fromJson(payload, DeviceCmdReq.class);
                if (devFrame != null) {
                    final LinkTapHandler device = LinkTapBridgeHandler.DEV_ID_LOOKUP.getItem(devFrame.deviceId);

                    if (device != null) {
                        logger.trace("Found device with ID: {} -> {}", devFrame.deviceId, device.getThing().getLabel());
                        device.processDeviceCommand(command, payload);
                    } else {
                        logger.debug("No device with id {} found to process command {}", devFrame.deviceId, command);
                    }
                }
                break;
            }
            case CMD_RAINFALL_DATA: {
                final DeviceCmdReq devFrame = GSON.fromJson(payload, DeviceCmdReq.class);
                if (devFrame != null) {
                    final LinkTapHandler device = LinkTapBridgeHandler.DEV_ID_LOOKUP.getItem(devFrame.deviceId);

                    if (device != null) {
                        logger.trace("Found device with ID: {} -> {}", devFrame.deviceId, device.getThing().getLabel());
                        device.processDeviceCommand(command, payload);
                    } else {
                        logger.trace("No device modelled to process meter status command");
                    }
                }
                break;
            }
            default:
                logger.warn("{}", getLocalizedText("warning.unexpected-response-frame", command, payload));
        }
        return response;
    }

    public String sendRequest(final LinkTapBridgeHandler handler, final TLGatewayFrame request)
            throws GatewayIdException, DeviceIdException, CommandNotSupportedException, InvalidParameterException {
        if (handler.getThing().getStatus().equals(ThingStatus.OFFLINE)) {
            logger.trace("Gateway offline");
            return "";
        }
        int triesLeft = MAX_COMMAND_RETRIES;
        int retry = 0;
        while (triesLeft > 0) {
            try {
                return sendSingleRequest(handler, request);
            } catch (TransientCommunicationIssueException tcie) {
                --triesLeft;
                try {
                    Thread.sleep(1000L * retry);
                } catch (InterruptedException ie) {
                    return "";
                }
                ++retry;
            }
        }
        return "";
    }

    public String sendSingleRequest(final LinkTapBridgeHandler handler, final TLGatewayFrame request)
            throws GatewayIdException, DeviceIdException, CommandNotSupportedException, InvalidParameterException,
            TransientCommunicationIssueException {
        // We need the hostname from the handler of the bridge

        // Responses can be one of the following types
        try {
            UUID uid = UUID.randomUUID();
            final String targetHost = handler.getHostname();
            final String payloadJson = GSON.toJson(request);
            logger.debug("{} = APP -> GW Request {} -> Payload {}", uid, targetHost, payloadJson);

            String response = API.sendRequest(targetHost, GSON.toJson(request));
            logger.debug("{} = APP -> GW Response {} -> Payload {}", uid, targetHost, response.trim());
            GatewayDeviceResponse gatewayFrame = GSON.fromJson(response, GatewayDeviceResponse.class);

            if (gatewayFrame == null) {
                throw new TransientCommunicationIssueException(COMMUNICATIONS_LOST);
            }

            if (!(request.command == CMD_UPDATE_WATER_TIMER_STATUS && gatewayFrame.command == -1)
                    && request.command != gatewayFrame.command) {
                logger.warn("{}",
                        getLocalizedText("warning.incorrect-cmd-resp", request.command, gatewayFrame.command));
                throw new TransientCommunicationIssueException(COMMUNICATIONS_LOST);
            }

            final ResultStatus rs = gatewayFrame.getRes();

            switch (gatewayFrame.command) {
                case CMD_ADD_END_DEVICE: // 1
                case CMD_REMOVE_END_DEVICE: // 2
                case CMD_UPDATE_WATER_TIMER_STATUS: // 3
                case CMD_SETUP_WATER_PLAN: // 4
                case CMD_REMOVE_WATER_PLAN: // 5
                case CMD_IMMEDIATE_WATER_START: // 6
                case CMD_IMMEDIATE_WATER_STOP: // 7
                case CMD_RAINFALL_DATA: // 8
                case CMD_ALERT_ENABLEMENT: // 10
                case CMD_ALERT_DISMISS: // 11
                case CMD_LOCKOUT_STATE: // 12
                case CMD_DATETIME_READ: // 14
                case CMD_WIRELESS_CHECK: // 15
                case CMD_GET_CONFIGURATION: // 16
                case CMD_SET_CONFIGURATION: // 17
                case CMD_PAUSE_WATER_PLAN: // 18
                    switch (rs) {
                        case RET_SUCCESS:
                            logger.trace("Request successfully processed");
                            return response;
                        case RET_MESSAGE_FORMAT_ERR:
                        case RET_BAD_PARAMETER:
                            logger.trace("Request issued incorrectly - format or parameter error");
                            throw new InvalidParameterException(rs);
                        case RET_CMD_NOT_SUPPORTED:
                            logger.trace("Command not supported by device");
                            throw new CommandNotSupportedException(rs);
                        case RET_DEVICE_ID_ERROR:
                        case RET_DEVICE_NOT_FOUND:
                            logger.trace("Device configuration error - check DEVICE ID in metadata");
                            throw new DeviceIdException(rs);
                        case RET_GATEWAY_ID_NOT_MATCHED:
                            logger.trace("Gateway configuration error - check GATEWAY ID in metadata");
                            throw new GatewayIdException(rs);
                        case RET_GATEWAY_BUSY:
                        case RET_GW_INTERNAL_ERR:
                            logger.trace("The request can be re-tried");
                            break;
                        case RET_CONFLICT_WATER_PLAN:
                            logger.trace("Gateway rejected command due to water plan conflict");
                            break;
                        case INVALID:
                        default:
                            logger.warn("{}", getLocalizedText("warning.unexpected-cmd-result"));
                    }
                    break;
                case DEFAULT_INT:
                    if (request.command == CMD_UPDATE_WATER_TIMER_STATUS) {
                        return response;
                    }
                default:
                    logger.warn("{}", getLocalizedText("warning.unexpected-response-frame", gatewayFrame.command,
                            GSON.toJson(request)));
                    return "";
            }

            return response;
        } catch (NotTapLinkGatewayException e) {
            logger.warn("{}", getLocalizedText("warning.non-gw"));
        } catch (UnknownHostException e) {
            throw new TransientCommunicationIssueException(HOST_NOT_RESOLVED);
        }
        return "";
    }

    private String generateTimeDateResponse(final @Nullable LinkTapBridgeHandler bridge, final String gwId,
            final int commandId) {
        final LocalDateTime currentTime = LocalDateTime.now();
        int wday = currentTime.getDayOfWeek().getValue();
        String dateStr = currentTime.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String timeStr = currentTime.format(DateTimeFormatter.ofPattern("HHmmss"));

        // Presume we are running in local time, unless we have the bridge modelled with the relevant UTC data
        if (bridge != null) {
            final String utcOffset = bridge.getThing().getProperties().get(BRIDGE_PROP_UTC_OFFSET);
            if (utcOffset != null && !utcOffset.isEmpty()) {
                OffsetDateTime odt = currentTime.atOffset(ZoneOffset.UTC);
                odt = odt.plusSeconds(Long.parseLong(utcOffset));
                wday = odt.getDayOfWeek().getValue();
                dateStr = odt.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                timeStr = odt.format(DateTimeFormatter.ofPattern("HHmmss"));
            }
        }

        final HandshakeResp respPayload = new HandshakeResp();
        respPayload.command = commandId;
        respPayload.gatewayId = gwId;
        respPayload.wday = wday;
        respPayload.date = dateStr;
        respPayload.time = timeStr;
        return GSON.toJson(respPayload);
    }
}
