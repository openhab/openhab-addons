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

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.BINDING_LOGS_PATH;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.BINDING_PROFILES_PATH;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.ZONE_ID;
import static org.openhab.binding.homeconnectdirect.internal.common.utils.StringUtils.convertToKebabCase;
import static org.openhab.binding.homeconnectdirect.internal.common.utils.StringUtils.toLowercase;
import static org.openhab.binding.homeconnectdirect.internal.servlet.ServletConstants.CONTENT_DISPOSITION_VALUE_TEMPLATE;
import static org.openhab.binding.homeconnectdirect.internal.servlet.ServletConstants.CONTENT_TYPE_ZIP;
import static org.openhab.binding.homeconnectdirect.internal.servlet.ServletConstants.HEADER_CONTENT_DISPOSITION;
import static org.openhab.binding.homeconnectdirect.internal.servlet.ServletConstants.HEADER_PREFER;
import static org.openhab.binding.homeconnectdirect.internal.servlet.ServletConstants.LOG_DOWNLOAD_FILENAME_TEMPLATE;
import static org.openhab.binding.homeconnectdirect.internal.servlet.ServletConstants.PREFER_PERSIST;
import static org.openhab.binding.homeconnectdirect.internal.servlet.ServletConstants.UID;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.homeconnectdirect.internal.handler.BaseHomeConnectDirectHandler;
import org.openhab.binding.homeconnectdirect.internal.handler.model.ApplianceMessage;
import org.openhab.binding.homeconnectdirect.internal.handler.model.SendMessageRequest;
import org.openhab.binding.homeconnectdirect.internal.handler.model.Value;
import org.openhab.binding.homeconnectdirect.internal.service.description.model.change.DeviceDescriptionChange;
import org.openhab.binding.homeconnectdirect.internal.service.profile.ApplianceProfileService;
import org.openhab.binding.homeconnectdirect.internal.service.websocket.model.Resource;
import org.openhab.binding.homeconnectdirect.internal.servlet.model.ApiCollectionResponse;
import org.openhab.binding.homeconnectdirect.internal.servlet.model.Error;
import org.openhab.binding.homeconnectdirect.internal.servlet.routing.RequestHandlerContext;
import org.openhab.binding.homeconnectdirect.internal.servlet.routing.RequestHandlerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Request handler for message API endpoints.
 *
 * @author Jonas Brüstel - Initial contribution
 */
@NonNullByDefault
public class MessageRequestHandler {

    private static final String MESSAGES_JSON = "messages.json";

    private final ApplianceProfileService applianceProfileService;
    private final DateTimeFormatter fileNameDateFormatter;
    private final Logger logger;

    public MessageRequestHandler(ApplianceProfileService applianceProfileService) {
        this.applianceProfileService = applianceProfileService;
        this.fileNameDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
        logger = LoggerFactory.getLogger(MessageRequestHandler.class);
    }

    public void saveOrDownloadMessages(RequestHandlerContext context) throws RequestHandlerException {
        var response = context.getResponse();
        var thingHandler = getHandlerOrSendError(context);
        if (thingHandler == null) {
            return;
        }

        var preferHeader = context.getRequest().getHeader(HEADER_PREFER);
        var haId = context.getHaIdFromThing(thingHandler.getThing());
        if (haId == null) {
            throw new RequestHandlerException("Missing haId from thing");
        }

        var applianceProfile = applianceProfileService.getProfile(haId);
        if (applianceProfile == null) {
            context.sendJson(new Error(HttpStatus.BAD_REQUEST_400,
                    String.format("Could not create message log zip for appliance with uid '%s'. Profile not found. ",
                            thingHandler.getThing().getUID())),
                    HttpStatus.BAD_REQUEST_400);
            return;
        }

        var type = convertToKebabCase(toLowercase(applianceProfile.type()));
        var brand = toLowercase(applianceProfile.brand());
        var vib = toLowercase(applianceProfile.vib());
        var filename = String.format(LOG_DOWNLOAD_FILENAME_TEMPLATE, type, brand, vib,
                LocalDateTime.now(ZONE_ID).format(fileNameDateFormatter),
                org.openhab.core.util.StringUtils.getRandomHex(4));

        try {
            OutputStream outputStream;
            // persist log file on server
            if (PREFER_PERSIST.equalsIgnoreCase(preferHeader)) {
                outputStream = Files.newOutputStream(Paths.get(BINDING_LOGS_PATH + File.separator + filename));
                context.sendCreated();
            }
            // direct download
            else {
                response.setContentType(CONTENT_TYPE_ZIP);
                response.setHeader(HEADER_CONTENT_DISPOSITION,
                        String.format(CONTENT_DISPOSITION_VALUE_TEMPLATE, filename));
                outputStream = response.getOutputStream();
            }

            try (ZipOutputStream zos = new ZipOutputStream(outputStream, UTF_8);
                    OutputStreamWriter writer = new OutputStreamWriter(zos, UTF_8)) {
                // home appliance messages
                ZipEntry zipEntry = new ZipEntry(MESSAGES_JSON);
                zos.putNextEntry(zipEntry);
                writer.write(context.getGson().toJson(thingHandler.getApplianceMessages()));
                writer.flush();
                zos.closeEntry();

                // add original description and feature mapping XML files
                for (Path path : List.of(Paths.get(BINDING_PROFILES_PATH, applianceProfile.deviceDescriptionFileName()),
                        Paths.get(BINDING_PROFILES_PATH, applianceProfile.featureMappingFileName()))) {
                    if (Files.exists(path)) {
                        ZipEntry fileEntry = new ZipEntry(path.getFileName().toString());
                        zos.putNextEntry(fileEntry);
                        Files.copy(path, zos);
                        zos.closeEntry();
                    } else {
                        logger.warn("Profile file '{}' does not exist!", path);
                    }
                }
            }
        } catch (IOException e) {
            throw new RequestHandlerException(
                    String.format("Error while creating message log zip for appliance with ID '%s'. error: %s", haId,
                            e.getMessage()),
                    e);
        }
    }

    public void sendMessage(RequestHandlerContext context) throws RequestHandlerException {
        var thingHandler = getHandlerOrSendError(context);
        if (thingHandler == null) {
            return;
        }

        var request = context.getRequestObject(SendMessageRequest.class);
        if (request == null) {
            context.sendJson(new Error(HttpStatus.BAD_REQUEST_400, "Invalid request body"), HttpStatus.BAD_REQUEST_400);
            return;
        }
        thingHandler.send(request);

        context.sendNoContent();
    }

    public void getMessageResources(RequestHandlerContext context) throws RequestHandlerException {
        var thingHandler = getHandlerOrSendError(context);
        if (thingHandler == null) {
            return;
        }

        Set<Resource> resources = new HashSet<>();
        // Add static resources from Resource class
        for (Field field : Resource.class.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) && field.getType().equals(Resource.class)) {
                try {
                    var staticResource = (Resource) field.get(null);
                    if (staticResource != null) {
                        resources.add(staticResource);
                    }
                } catch (IllegalAccessException e) {
                    logger.warn("Could not access static resource field", e);
                }
            }
        }
        // Add already used resources
        resources.addAll(thingHandler.getApplianceMessages().stream().map(ApplianceMessage::resource).toList());

        var sortedResources = resources.stream()
                .sorted(Comparator.comparing(Resource::service).thenComparing(Resource::endpoint)).toList();

        context.sendJson(new ApiCollectionResponse<>(sortedResources));
    }

    public void getMessageValueKeys(RequestHandlerContext context) throws RequestHandlerException {
        var thingHandler = getHandlerOrSendError(context);
        if (thingHandler == null) {
            return;
        }

        var keys = thingHandler.getApplianceMessages().stream().flatMap(applianceMessage -> {
            var values = applianceMessage.values();
            if (values != null) {
                return values.stream().map(Value::key);
            } else {
                return Stream.empty();
            }
        }).distinct().toList();

        context.sendJson(new ApiCollectionResponse<>(keys));
    }

    public void getMessageDescriptionChangeKeys(RequestHandlerContext context) throws RequestHandlerException {
        var thingHandler = getHandlerOrSendError(context);
        if (thingHandler == null) {
            return;
        }

        var keys = thingHandler.getApplianceMessages().stream().flatMap(applianceMessage -> {
            var changes = applianceMessage.descriptionChanges();
            if (changes != null) {
                return changes.stream().map(DeviceDescriptionChange::key);
            } else {
                return Stream.empty();
            }
        }).distinct().toList();

        context.sendJson(new ApiCollectionResponse<>(keys));
    }

    private @Nullable BaseHomeConnectDirectHandler getHandlerOrSendError(RequestHandlerContext context)
            throws RequestHandlerException {
        var uid = context.getVariable(UID);
        if (uid == null) {
            context.sendJson(new Error(HttpStatus.BAD_REQUEST_400, "Missing uid"), HttpStatus.BAD_REQUEST_400);
            return null;
        }

        var thing = context.getApplianceThing(uid);
        if (thing != null && thing.getHandler() instanceof BaseHomeConnectDirectHandler handler) {
            return handler;
        } else {
            context.sendJson(
                    new Error(HttpStatus.NOT_FOUND_404, String.format("Appliance with thing ID '%s' not found", uid)),
                    HttpStatus.NOT_FOUND_404);
            return null;
        }
    }
}
