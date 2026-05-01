/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.homeconnectdirect.internal.servlet.handler.api;

import static org.openhab.binding.homeconnectdirect.internal.common.utils.StringUtils.EMPTY_STRING;
import static org.openhab.binding.homeconnectdirect.internal.servlet.ServletConstants.UID;
import static org.openhab.binding.homeconnectdirect.internal.servlet.ServletUtils.mapInteger;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.homeconnectdirect.internal.handler.BaseHomeConnectDirectHandler;
import org.openhab.binding.homeconnectdirect.internal.service.description.model.DeviceDescriptionType;
import org.openhab.binding.homeconnectdirect.internal.servlet.model.ApiCollectionResponse;
import org.openhab.binding.homeconnectdirect.internal.servlet.model.Appliance;
import org.openhab.binding.homeconnectdirect.internal.servlet.model.Error;
import org.openhab.binding.homeconnectdirect.internal.servlet.routing.RequestHandlerContext;
import org.openhab.binding.homeconnectdirect.internal.servlet.routing.RequestHandlerException;
import org.openhab.core.thing.Thing;

/**
 * Request handler for appliance API endpoints.
 *
 * @author Jonas Brüstel - Initial contribution
 */
@NonNullByDefault
public class ApplianceRequestHandler {

    public void getAppliances(RequestHandlerContext context) throws RequestHandlerException {
        context.sendJson(
                new ApiCollectionResponse<>(context.getApplianceThings().stream().map(this::mapToAppliance).toList()));
    }

    public void getDeviceDescription(RequestHandlerContext context) throws RequestHandlerException {
        var thing = getThingOrSendError(context);
        if (thing == null) {
            return;
        }

        var uid = mapInteger(context.getQueryParameter("uid"));
        var parentUid = mapInteger(context.getQueryParameter("parentUid"));
        var typeParameter = context.getQueryParameter("type");
        var deviceDescriptionType = mapDeviceDescriptionType(typeParameter);

        if (thing.getHandler() instanceof BaseHomeConnectDirectHandler baseHomeConnectDirectHandler) {
            var deviceDescriptionService = baseHomeConnectDirectHandler.getDeviceDescriptionService();
            if (deviceDescriptionService != null) {
                if (uid != null && deviceDescriptionType != null) {
                    var descriptionObject = deviceDescriptionService.getDeviceDescriptionObject(uid, parentUid,
                            deviceDescriptionType);
                    if (descriptionObject != null) {
                        context.sendJson(descriptionObject);
                    } else {
                        context.sendJson(
                                new Error(HttpStatus.NOT_FOUND_404,
                                        String.format("Device description with uid '%s' not found", uid)),
                                HttpStatus.NOT_FOUND_404);
                    }
                } else {
                    context.sendJson(deviceDescriptionService.getDeviceDescription());
                }
            } else {
                context.sendJson(new Error(HttpStatus.BAD_REQUEST_400, "Unknown type parameter"),
                        HttpStatus.BAD_REQUEST_400);
            }
        }
    }

    public void createYamlCode(RequestHandlerContext context) throws RequestHandlerException {
        var thing = getThingOrSendError(context);
        if (thing == null) {
            return;
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("version", 1);

        Map<String, Object> things = new LinkedHashMap<>();
        Map<String, Object> thingMap = new LinkedHashMap<>();

        var label = thing.getLabel();
        thingMap.put("label", label != null ? label : thing.getUID().toString());

        var config = thing.getConfiguration().getProperties();
        if (!config.isEmpty()) {
            thingMap.put("config", config);
        }

        var channels = thing.getChannels();
        if (!channels.isEmpty()) {
            Map<String, Object> channelsMap = new LinkedHashMap<>();
            for (var channel : channels) {
                var channelTypeUID = channel.getChannelTypeUID();
                if (channelTypeUID != null) {
                    Map<String, Object> channelMap = new LinkedHashMap<>();
                    String type = channelTypeUID.getId();
                    channelMap.put("type", type);

                    if ("number".equals(type) || "string".equals(type) || "switch".equals(type)
                            || "trigger".equals(type) || "enum-switch".equals(type)) {
                        var channelLabel = channel.getLabel();
                        if (channelLabel != null) {
                            channelMap.put("label", channelLabel);
                        }
                        var channelDescription = channel.getDescription();
                        if (channelDescription != null) {
                            channelMap.put("description", channelDescription);
                        }
                    }

                    var channelConfig = channel.getConfiguration().getProperties();
                    if (!channelConfig.isEmpty()) {
                        channelMap.put("config", channelConfig);
                    }
                    channelsMap.put(channel.getUID().getId(), channelMap);
                }
            }
            thingMap.put("channels", channelsMap);
        }

        things.put(thing.getUID().toString(), thingMap);
        result.put("things", things);

        context.sendJson(result);
    }

    public void createDslCode(RequestHandlerContext context) throws RequestHandlerException {
        var thing = getThingOrSendError(context);
        if (thing == null) {
            return;
        }

        StringBuilder dsl = new StringBuilder();
        dsl.append("Thing ");
        dsl.append(thing.getUID());
        dsl.append(" \"").append(thing.getLabel() != null ? thing.getLabel() : thing.getUID().toString()).append("\"");

        var config = thing.getConfiguration().getProperties();
        if (!config.isEmpty()) {
            dsl.append(" [");
            List<String> configEntries = new ArrayList<>();
            config.forEach((key, value) -> configEntries.add(formatConfigValue(key, value)));
            dsl.append(String.join(", ", configEntries));
            dsl.append("]");
        }

        var channels = thing.getChannels();
        if (!channels.isEmpty()) {
            dsl.append(" {\n  Channels:\n");
            for (var channel : channels) {
                var channelTypeUID = channel.getChannelTypeUID();
                if (channelTypeUID != null) {
                    var type = channelTypeUID.getId();
                    if ("number".equals(type) || "string".equals(type) || "switch".equals(type)
                            || "trigger".equals(type) || "enum-switch".equals(type)) {
                        dsl.append("    Type ").append(type).append(" : ").append(channel.getUID().getId());

                        if (channel.getLabel() != null) {
                            dsl.append(" \"").append(channel.getLabel()).append("\"");
                        }

                        var channelConfig = channel.getConfiguration().getProperties();
                        if (!channelConfig.isEmpty()) {
                            dsl.append(" [");
                            List<String> channelConfigEntries = new ArrayList<>();
                            channelConfig
                                    .forEach((key, value) -> channelConfigEntries.add(formatConfigValue(key, value)));
                            dsl.append(String.join(", ", channelConfigEntries));
                            dsl.append("]");
                        }
                        dsl.append("\n");
                    }
                }
            }
            dsl.append("}");
        }

        context.sendJson(new DslCode(dsl.toString()));
    }

    private @Nullable Thing getThingOrSendError(RequestHandlerContext context) throws RequestHandlerException {
        var uid = context.getVariable(UID);
        if (uid == null) {
            context.sendJson(new Error(HttpStatus.BAD_REQUEST_400, "Missing uid"), HttpStatus.BAD_REQUEST_400);
            return null;
        }

        var thing = context.getApplianceThing(uid);
        if (thing != null && thing.getHandler() instanceof BaseHomeConnectDirectHandler handler) {
            return handler.getThing();
        } else {
            context.sendJson(
                    new Error(HttpStatus.NOT_FOUND_404, String.format("Appliance with thing ID '%s' not found", uid)),
                    HttpStatus.NOT_FOUND_404);
            return null;
        }
    }

    private Appliance mapToAppliance(Thing thing) {
        var configuration = thing.getConfiguration();
        var label = thing.getLabel();
        var uid = thing.getUID().toString();
        var haId = configuration.get("haId");
        var address = configuration.get("address");
        int messageCount = 0;

        if (thing.getHandler() instanceof BaseHomeConnectDirectHandler handler) {
            messageCount = handler.getApplianceMessages().size();
        }

        return new Appliance(uid, haId != null ? haId.toString() : EMPTY_STRING,
                label != null ? label : thing.getUID().toString(), address != null ? address.toString() : EMPTY_STRING,
                thing.getStatus().toString(), messageCount);
    }

    private String formatConfigValue(String key, Object value) {
        if (value instanceof Number || value instanceof Boolean) {
            return key + "=" + value;
        }
        return key + "=\"" + value + "\"";
    }

    private @Nullable DeviceDescriptionType mapDeviceDescriptionType(@Nullable String typeString) {
        if (typeString == null) {
            return null;
        }

        try {
            return DeviceDescriptionType.valueOf(typeString);
        } catch (IllegalStateException e) {
            return null;
        }
    }

    private record DslCode(String code) {
    }
}
