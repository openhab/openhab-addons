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
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.ZONE_ID;
import static org.openhab.binding.homeconnectdirect.internal.service.websocket.model.Resource.RO_ALL_DESCRIPTION_CHANGES;
import static org.openhab.binding.homeconnectdirect.internal.service.websocket.model.Resource.RO_ALL_MANDATORY_VALUES;
import static org.openhab.binding.homeconnectdirect.internal.service.websocket.model.Resource.RO_DESCRIPTION_CHANGE;
import static org.openhab.binding.homeconnectdirect.internal.service.websocket.model.Resource.RO_VALUES;
import static org.openhab.binding.homeconnectdirect.internal.servlet.ServletConstants.CONTENT_DISPOSITION_VALUE_TEMPLATE;
import static org.openhab.binding.homeconnectdirect.internal.servlet.ServletConstants.CONTENT_TYPE_ZIP;
import static org.openhab.binding.homeconnectdirect.internal.servlet.ServletConstants.HEADER_CONTENT_DISPOSITION;
import static org.openhab.binding.homeconnectdirect.internal.servlet.ServletConstants.LOG_FILE_ID;
import static org.openhab.binding.homeconnectdirect.internal.servlet.ServletConstants.PROXY_FILES_PART;
import static org.openhab.binding.homeconnectdirect.internal.servlet.ServletConstants.ZIP_FILE_PART;
import static org.openhab.binding.homeconnectdirect.internal.servlet.ServletUtils.filterOutMessage;
import static org.openhab.binding.homeconnectdirect.internal.servlet.ServletUtils.mapInteger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.Part;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.homeconnectdirect.internal.handler.model.ApplianceMessage;
import org.openhab.binding.homeconnectdirect.internal.handler.model.MessageType;
import org.openhab.binding.homeconnectdirect.internal.handler.model.Value;
import org.openhab.binding.homeconnectdirect.internal.service.description.DeviceDescriptionService;
import org.openhab.binding.homeconnectdirect.internal.service.description.DeviceDescriptionUtils;
import org.openhab.binding.homeconnectdirect.internal.service.description.model.DeviceDescriptionType;
import org.openhab.binding.homeconnectdirect.internal.service.description.model.change.DeviceDescriptionChange;
import org.openhab.binding.homeconnectdirect.internal.service.feature.FeatureMappingService;
import org.openhab.binding.homeconnectdirect.internal.service.feature.model.FeatureMapping;
import org.openhab.binding.homeconnectdirect.internal.service.websocket.model.Action;
import org.openhab.binding.homeconnectdirect.internal.service.websocket.model.Resource;
import org.openhab.binding.homeconnectdirect.internal.service.websocket.model.data.DescriptionChangeData;
import org.openhab.binding.homeconnectdirect.internal.service.websocket.model.data.ValueData;
import org.openhab.binding.homeconnectdirect.internal.servlet.model.ApiCollectionResponse;
import org.openhab.binding.homeconnectdirect.internal.servlet.model.Error;
import org.openhab.binding.homeconnectdirect.internal.servlet.model.LogFile;
import org.openhab.binding.homeconnectdirect.internal.servlet.model.MessageFilter;
import org.openhab.binding.homeconnectdirect.internal.servlet.routing.RequestHandlerContext;
import org.openhab.binding.homeconnectdirect.internal.servlet.routing.RequestHandlerException;
import org.openhab.core.cache.ExpiringCacheMap;
import org.openhab.core.util.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

/**
 * Request handler for log API endpoints.
 *
 * @author Jonas Brüstel - Initial contribution
 */
@NonNullByDefault
public class LogRequestHandler {

    private static final int DEFAULT_CACHE_TTL_IN_MIN = 10;

    private final Logger logger;
    private final ExpiringCacheMap<String, List<ApplianceMessage>> logMessagesCache;
    private final ExpiringCacheMap<String, FeatureMapping> featureMappingCache;
    private final ExpiringCacheMap<String, DeviceDescriptionService> initialDeviceDescriptionServiceCache;

    public LogRequestHandler() {
        this.logger = LoggerFactory.getLogger(LogRequestHandler.class);
        this.logMessagesCache = new ExpiringCacheMap<>(Duration.ofMinutes(DEFAULT_CACHE_TTL_IN_MIN));
        this.featureMappingCache = new ExpiringCacheMap<>(Duration.ofMinutes(DEFAULT_CACHE_TTL_IN_MIN));
        this.initialDeviceDescriptionServiceCache = new ExpiringCacheMap<>(
                Duration.ofMinutes(DEFAULT_CACHE_TTL_IN_MIN));

        createLogsDirectory();
    }

    public void getLogs(RequestHandlerContext context) throws RequestHandlerException {
        context.sendJson(new ApiCollectionResponse<>(getLogFiles()));
    }

    public void getDeviceDescription(RequestHandlerContext context) throws RequestHandlerException {
        var logFile = getLogFileOrSendError(context);
        if (logFile == null) {
            return;
        }
        var featureMapping = getLogFileFeatureMapping(logFile);
        if (featureMapping == null) {
            context.sendJson(new Error(HttpStatus.INTERNAL_SERVER_ERROR_500, "Missing feature mapping XML!"),
                    HttpStatus.INTERNAL_SERVER_ERROR_500);
            return;
        }
        var deviceDescriptionService = getLogFileInitialDeviceDescription(logFile, featureMapping);
        if (deviceDescriptionService == null) {
            context.sendJson(new Error(HttpStatus.INTERNAL_SERVER_ERROR_500, "Missing device description XML!"),
                    HttpStatus.INTERNAL_SERVER_ERROR_500);
            return;
        }

        // apply changes to device description
        getLogFileMessages(logFile, context.getGson()).stream()
                .filter(applianceMessage -> MessageType.INCOMING.equals(applianceMessage.type()))
                .filter(applianceMessage -> RO_DESCRIPTION_CHANGE.equals(applianceMessage.resource())
                        || RO_ALL_DESCRIPTION_CHANGES.equals(applianceMessage.resource()))
                .sorted(Comparator.comparing(ApplianceMessage::dateTime))
                .map(applianceMessage -> applianceMessage.getRawPayloadAsList(DescriptionChangeData.class))
                .filter(Objects::nonNull).forEach(deviceDescriptionService::applyDescriptionChanges);

        var uid = mapInteger(context.getQueryParameter("uid"));
        var parentUid = mapInteger(context.getQueryParameter("parentUid"));
        var typeParameter = context.getQueryParameter("type");
        var deviceDescriptionType = mapDeviceDescriptionType(typeParameter);

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
    }

    public void uploadLog(RequestHandlerContext context) throws RequestHandlerException {
        var request = context.getRequest();

        try {
            var filePart = request.getPart(ZIP_FILE_PART);
            if (filePart != null && filePart.getSubmittedFileName() != null) {
                handleZipUpload(context, filePart);
            } else {
                var parts = request.getParts().stream().filter(p -> PROXY_FILES_PART.equals(p.getName())).toList();
                if (parts.size() == 3) {
                    handleProxyUpload(context, parts);
                } else {
                    context.sendJson(new Error(HttpStatus.BAD_REQUEST_400, "No valid file(s) submitted"),
                            HttpStatus.BAD_REQUEST_400);
                }
            }
        } catch (IOException | ServletException e) {
            throw new RequestHandlerException("Could not upload log", e);
        }
    }

    private void handleZipUpload(RequestHandlerContext context, Part filePart) throws RequestHandlerException {
        var fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
        if (!fileName.endsWith(".zip") && !fileName.endsWith(".zip.log")) {
            context.sendJson(
                    new Error(HttpStatus.UNSUPPORTED_MEDIA_TYPE_415, "Only zip (or zip.log) files are supported"),
                    HttpStatus.UNSUPPORTED_MEDIA_TYPE_415);
            return;
        }

        Path tempZipPath = null;
        try {
            tempZipPath = Files.createTempFile("hcd-upload-", ".zip");

            try (InputStream inputStream = filePart.getInputStream();
                    ZipInputStream zis = new ZipInputStream(inputStream);
                    OutputStream outputStream = Files.newOutputStream(tempZipPath);
                    ZipOutputStream zos = new ZipOutputStream(outputStream)) {
                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    ZipEntry newEntry = new ZipEntry(entry.getName());
                    zos.putNextEntry(newEntry);

                    if (entry.getName().endsWith(".json")) {
                        processMessagesJson(zis, zos, context.getGson());
                    } else {
                        zis.transferTo(zos);
                    }
                    zos.closeEntry();
                }
            }

            finalizeUpload(context, tempZipPath, fileName);
        } catch (IOException e) {
            context.sendJson(new Error(HttpStatus.BAD_REQUEST_400, "Invalid json file. Could not read file."),
                    HttpStatus.BAD_REQUEST_400);
        } finally {
            try {
                if (tempZipPath != null) {
                    Files.deleteIfExists(tempZipPath);
                }
            } catch (IOException e) {
                logger.warn("Could not delete temp file {}", tempZipPath, e);
            }
        }
    }

    private void handleProxyUpload(RequestHandlerContext context, List<Part> parts) throws RequestHandlerException {
        Part jsonPart = null;
        Part deviceDescriptionPart = null;
        Part featureMappingPart = null;

        for (Part part : parts) {
            String name = part.getSubmittedFileName();
            if (name != null) {
                if (name.endsWith(".json")) {
                    jsonPart = part;
                } else if (name.endsWith("_DeviceDescription.xml")) {
                    deviceDescriptionPart = part;
                } else if (name.endsWith("_FeatureMapping.xml")) {
                    featureMappingPart = part;
                }
            }
        }

        if (jsonPart == null || deviceDescriptionPart == null || featureMappingPart == null) {
            context.sendJson(new Error(HttpStatus.BAD_REQUEST_400, "Missing required files (*.json, *.xml)"),
                    HttpStatus.BAD_REQUEST_400);
            return;
        }

        String fileName = Paths.get(jsonPart.getSubmittedFileName()).getFileName().toString();
        String zipFileName = fileName.substring(0, fileName.lastIndexOf('.')) + ".zip";
        Path tempZipPath = null;
        Path tempDeviceDescriptionPath = null;
        Path tempFeatureMappingPath = null;

        try {
            // Create temp files for XMLs to parse them
            tempDeviceDescriptionPath = Files.createTempFile("hcd-dd-", ".xml");
            tempFeatureMappingPath = Files.createTempFile("hcd-fm-", ".xml");
            try (InputStream is = deviceDescriptionPart.getInputStream()) {
                Files.copy(is, tempDeviceDescriptionPath, StandardCopyOption.REPLACE_EXISTING);
            }
            try (InputStream is = featureMappingPart.getInputStream()) {
                Files.copy(is, tempFeatureMappingPath, StandardCopyOption.REPLACE_EXISTING);
            }
            var featureMapping = new FeatureMappingService(tempFeatureMappingPath).getFeatureMapping();
            var deviceDescriptionService = new DeviceDescriptionService("unknown", tempDeviceDescriptionPath,
                    featureMapping);

            tempZipPath = Files.createTempFile("hcd-upload-proxy-", ".zip");

            try (OutputStream outputStream = Files.newOutputStream(tempZipPath);
                    ZipOutputStream zos = new ZipOutputStream(outputStream)) {
                // Process JSON
                ZipEntry jsonEntry = new ZipEntry(jsonPart.getSubmittedFileName());
                zos.putNextEntry(jsonEntry);
                try (InputStream is = jsonPart.getInputStream()) {
                    processProxyMessagesJson(is, zos, context.getGson(), deviceDescriptionService, featureMapping);
                }
                zos.closeEntry();

                // Add XMLs to Zip
                ZipEntry deviceDescriptionEntry = new ZipEntry(deviceDescriptionPart.getSubmittedFileName());
                zos.putNextEntry(deviceDescriptionEntry);
                Files.copy(tempDeviceDescriptionPath, zos);
                zos.closeEntry();

                ZipEntry featureMappingEntry = new ZipEntry(featureMappingPart.getSubmittedFileName());
                zos.putNextEntry(featureMappingEntry);
                Files.copy(tempFeatureMappingPath, zos);
                zos.closeEntry();
            }

            finalizeUpload(context, tempZipPath, zipFileName);
        } catch (IOException e) {
            context.sendJson(new Error(HttpStatus.BAD_REQUEST_400, "No valid HA proxy files submitted"),
                    HttpStatus.BAD_REQUEST_400);
        } finally {
            try {
                if (tempZipPath != null) {
                    Files.deleteIfExists(tempZipPath);
                }
                if (tempDeviceDescriptionPath != null) {
                    Files.deleteIfExists(tempDeviceDescriptionPath);
                }
                if (tempFeatureMappingPath != null) {
                    Files.deleteIfExists(tempFeatureMappingPath);
                }
            } catch (IOException e) {
                logger.warn("Could not delete temp xml files", e);
            }
        }
    }

    private void finalizeUpload(RequestHandlerContext context, Path tempZipPath, String fileName)
            throws RequestHandlerException, IOException {
        var targetPath = Paths.get(BINDING_LOGS_PATH, fileName);
        Files.move(tempZipPath, targetPath, StandardCopyOption.REPLACE_EXISTING);

        if (!isValidLogFile(targetPath)) {
            try {
                Files.deleteIfExists(targetPath);
            } catch (IOException e) {
                logger.warn("Could not delete invalid log file {}", targetPath, e);
            }

            context.sendJson(
                    new Error(HttpStatus.BAD_REQUEST_400,
                            "Invalid log file. Must contain .json, *_DeviceDescription.xml and *_FeatureMapping.xml"),
                    HttpStatus.BAD_REQUEST_400);
            return;
        }

        context.sendNoContent();
    }

    private void processMessagesJson(InputStream inputStream, ZipOutputStream zos, Gson gson) throws IOException {
        var json = new String(inputStream.readAllBytes(), UTF_8);
        var isValid = true;

        try {
            ApplianceMessage[] messages = gson.fromJson(json, ApplianceMessage[].class);
            if (messages == null) {
                isValid = false;
            } else {
                for (ApplianceMessage message : messages) {
                    if (message.type() == null || message.dateTime() == null || message.resource() == null
                            || message.action() == null) {
                        isValid = false;
                    }
                }
            }
        } catch (JsonSyntaxException e) {
            isValid = false;
        }

        if (isValid) {
            OutputStreamWriter writer = new OutputStreamWriter(zos, UTF_8);
            writer.write(json);
            writer.flush();
        } else {
            throw new IOException("Invalid json log file");
        }
    }

    private void processProxyMessagesJson(InputStream is, OutputStream os, Gson gson,
            DeviceDescriptionService deviceDescriptionService, FeatureMapping featureMapping) throws IOException {
        var json = new String(is.readAllBytes(), UTF_8);
        JsonElement root;

        try {
            root = gson.fromJson(json, JsonElement.class);
            if (root == null) {
                throw new IOException("Invalid json log file");
            }
        } catch (JsonSyntaxException e) {
            throw new IOException("Invalid JSON in messages json file", e);
        }

        var isValid = true;
        if (root.isJsonArray()) {
            JsonArray array = root.getAsJsonArray();
            JsonArray newArray = new JsonArray();
            List<ProxyLogEntry> entries = new ArrayList<>();

            for (JsonElement element : array) {
                if (element.isJsonObject()) {
                    try {
                        var proxyLogEntry = gson.fromJson(element, ProxyLogEntry.class);
                        if (proxyLogEntry != null) {
                            entries.add(proxyLogEntry);
                        } else {
                            isValid = false;
                        }
                    } catch (JsonSyntaxException e) {
                        logger.warn("Failed to parse proxy log entry", e);
                        isValid = false;
                    }
                } else {
                    isValid = false;
                }
            }

            if (isValid) {
                try {
                    entries.sort(
                            Comparator.comparing(e -> OffsetDateTime.parse(Objects.requireNonNull(e.timestamp()))));

                    List<ApplianceMessage> convertedMessages = new ArrayList<>();
                    for (ProxyLogEntry entry : entries) {
                        convertedMessages
                                .add(convertProxyMessage(entry, gson, deviceDescriptionService, featureMapping));
                    }

                    convertedMessages.sort(Comparator.comparing(ApplianceMessage::dateTime).reversed());
                    convertedMessages.forEach(msg -> newArray.add(gson.toJsonTree(msg)));

                    root = newArray;
                } catch (Exception e) {
                    logger.warn("Failed to convert legacy message", e);
                    isValid = false;
                }
            }
        } else {
            isValid = false;
        }

        if (!isValid) {
            throw new IOException("Invalid json log file");
        }

        OutputStreamWriter writer = new OutputStreamWriter(os, UTF_8);
        gson.toJson(root, writer);
        writer.flush();
    }

    private ApplianceMessage convertProxyMessage(ProxyLogEntry logEntry, Gson gson,
            DeviceDescriptionService deviceDescriptionService, FeatureMapping featureMapping) throws IOException {
        var msgObj = logEntry.message();
        var logEntrySender = logEntry.sender();
        var logEntryTimestamp = logEntry.timestamp();
        var logEntrySessionId = logEntry.sessionId();

        // check
        if (msgObj == null || !msgObj.has("sID") || !msgObj.has("msgID") || !msgObj.has("version")
                || !msgObj.has("resource") || !msgObj.has("action") || logEntrySender == null
                || logEntryTimestamp == null || logEntrySessionId == null) {
            throw new IOException("Invalid log entry message");
        }

        try {
            var dateTime = OffsetDateTime.parse(logEntryTimestamp);
            var sessionId = Long.parseLong(logEntrySessionId, 16);

            var type = logEntrySender == ProxySender.HOME_APPLIANCE ? MessageType.INCOMING : MessageType.OUTGOING;

            var id = msgObj.get("sID").getAsLong();
            var messageId = msgObj.get("msgID").getAsLong();
            var version = msgObj.get("version").getAsInt();
            var code = msgObj.has("code") ? msgObj.get("code").getAsInt() : null;

            var resource = Objects.requireNonNull(gson.fromJson(msgObj.get("resource"), Resource.class));
            var action = Objects.requireNonNull(gson.fromJson(msgObj.get("action"), Action.class));

            JsonArray dataList = null;
            if (msgObj.has("data")) {
                dataList = gson.fromJson(msgObj.get("data"), new TypeToken<JsonArray>() {
                }.getType());
            }

            // handle device description change messages
            List<DeviceDescriptionChange> deviceDescriptionChanges = null;
            if (dataList != null && MessageType.INCOMING.equals(type)
                    && (RO_DESCRIPTION_CHANGE.equals(resource) || RO_ALL_DESCRIPTION_CHANGES.equals(resource))) {
                List<DescriptionChangeData> descriptionChangeData = gson.fromJson(dataList,
                        TypeToken.getParameterized(List.class, DescriptionChangeData.class).getType());
                deviceDescriptionChanges = deviceDescriptionService.applyDescriptionChanges(descriptionChangeData);
            }

            // handle value messages
            List<Value> values = null;
            if (dataList != null && MessageType.INCOMING.equals(type)
                    && (RO_VALUES.equals(resource) || RO_ALL_MANDATORY_VALUES.equals(resource))) {
                List<ValueData> valueDataList = gson.fromJson(dataList,
                        TypeToken.getParameterized(List.class, ValueData.class).getType());
                values = DeviceDescriptionUtils.mapValues(deviceDescriptionService, featureMapping, resource,
                        valueDataList, null);
            }

            return new ApplianceMessage(dateTime, id, type, resource, version, sessionId, messageId, action, code,
                    dataList, values, deviceDescriptionChanges);
        } catch (JsonSyntaxException | NumberFormatException | IllegalStateException | DateTimeParseException e) {
            throw new IOException("Invalid Json format");
        }
    }

    private boolean isValidLogFile(Path path) {
        boolean hasJson = false;
        boolean hasDeviceDescription = false;
        boolean hasFeatureMapping = false;

        try (ZipFile zipFile = new ZipFile(path.toFile())) {
            var entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                var entry = entries.nextElement();
                if (entry == null || entry.isDirectory()) {
                    continue;
                }
                String name = entry.getName();
                if (name.endsWith(".json")) {
                    hasJson = true;
                } else if (name.endsWith("_DeviceDescription.xml")) {
                    hasDeviceDescription = true;
                } else if (name.endsWith("_FeatureMapping.xml")) {
                    hasFeatureMapping = true;
                }
            }
        } catch (IOException e) {
            return false;
        }

        return hasJson && hasDeviceDescription && hasFeatureMapping;
    }

    public void downloadLog(RequestHandlerContext context) throws RequestHandlerException {
        var logFile = getLogFileOrSendError(context);
        if (logFile == null) {
            return;
        }

        var response = context.getResponse();
        response.setContentType(CONTENT_TYPE_ZIP);
        response.setHeader(HEADER_CONTENT_DISPOSITION,
                String.format(CONTENT_DISPOSITION_VALUE_TEMPLATE, logFile.fileName()));

        try {
            Files.copy(Paths.get(BINDING_LOGS_PATH + File.separator + logFile.fileName()), response.getOutputStream());
        } catch (IOException e) {
            throw new RequestHandlerException("Could not read log file", e);
        }
    }

    public void deleteLog(RequestHandlerContext context) throws RequestHandlerException {
        var logFile = getLogFileOrSendError(context);
        if (logFile == null) {
            return;
        }

        try {
            Files.deleteIfExists(Paths.get(BINDING_LOGS_PATH + File.separator + logFile.fileName()));
            context.sendNoContent();
        } catch (IOException e) {
            throw new RequestHandlerException("Could not read log file", e);
        }
    }

    public void getLogMessages(RequestHandlerContext context) throws RequestHandlerException {
        var logFile = getLogFileOrSendError(context);
        if (logFile == null) {
            return;
        }

        MessageFilter filter = null;
        var filterParameter = context.getQueryParameter("filter");
        if (filterParameter != null) {
            try {
                filter = context.getGson().fromJson(filterParameter, MessageFilter.class);
            } catch (JsonSyntaxException e) {
                logger.debug("Could not parse filter from request! error={}", e.getMessage());
            }
        }

        context.sendJson(new ApiCollectionResponse<>(
                getFilteredLogFileMessages(getLogFileMessages(logFile, context.getGson()), filter)));
    }

    public void getLogMessageResources(RequestHandlerContext context) throws RequestHandlerException {
        var logFile = getLogFileOrSendError(context);
        if (logFile == null) {
            return;
        }

        context.sendJson(new ApiCollectionResponse<>(getLogFileMessages(logFile, context.getGson()).stream()
                .map(ApplianceMessage::resource).distinct().toList()));
    }

    public void getLogMessageValueKeys(RequestHandlerContext context) throws RequestHandlerException {
        var logFile = getLogFileOrSendError(context);
        if (logFile == null) {
            return;
        }

        var keys = getLogFileMessages(logFile, context.getGson()).stream().flatMap(applianceMessage -> {
            var values = applianceMessage.values();
            if (values == null) {
                return Stream.empty();
            } else {
                return values.stream().map(Value::key);
            }
        }).distinct().toList();
        context.sendJson(new ApiCollectionResponse<>(keys));
    }

    public void getLogMessageDescriptionChangeKeys(RequestHandlerContext context) throws RequestHandlerException {
        var logFile = getLogFileOrSendError(context);
        if (logFile == null) {
            return;
        }

        var keys = getLogFileMessages(logFile, context.getGson()).stream().flatMap(applianceMessage -> {
            var changes = applianceMessage.descriptionChanges();
            if (changes == null) {
                return Stream.empty();
            } else {
                return changes.stream().map(DeviceDescriptionChange::key);
            }
        }).distinct().toList();
        context.sendJson(new ApiCollectionResponse<>(keys));
    }

    private @Nullable LogFile getLogFileOrSendError(RequestHandlerContext context) throws RequestHandlerException {
        var fileId = context.getVariable(LOG_FILE_ID);
        if (fileId == null) {
            context.sendJson(new Error(HttpStatus.BAD_REQUEST_400, "Missing fileId"), HttpStatus.BAD_REQUEST_400);
            return null;
        }

        var logFile = getLogFiles().stream().filter(f -> f.id().equals(fileId)).findFirst().orElse(null);
        if (logFile == null) {
            context.sendJson(
                    new Error(HttpStatus.NOT_FOUND_404, String.format("Log file with ID '%s' not found", fileId)),
                    HttpStatus.NOT_FOUND_404);
        }

        return logFile;
    }

    private List<LogFile> getLogFiles() {
        var result = new ArrayList<LogFile>();
        Path directory = Paths.get(BINDING_LOGS_PATH);

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
            for (Path path : stream) {
                var id = calculateFileId(path);
                var fileName = path.getFileName().toString();
                var fileSize = Files.size(path);
                var createdAt = Files.getLastModifiedTime(path).toInstant().atZone(ZONE_ID).toOffsetDateTime();

                result.add(new LogFile(id, fileName, fileSize, createdAt));
            }
        } catch (IOException | NoSuchAlgorithmException e) {
            logger.error("Could not read logs directory: {} error: {}", BINDING_LOGS_PATH, e.getMessage());
        }

        result.sort(Comparator.comparing(LogFile::createdAt).reversed());

        return result;
    }

    private String calculateFileId(Path path) throws IOException, NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");

        // include filename in hash
        md.update(path.getFileName().toString().getBytes(UTF_8));

        try (InputStream is = Files.newInputStream(path); DigestInputStream dis = new DigestInputStream(is, md)) {
            dis.transferTo(OutputStream.nullOutputStream());
        }
        return HexUtils.bytesToHex(md.digest());
    }

    private List<ApplianceMessage> getLogFileMessages(LogFile logFile, Gson gson) {
        var result = new ArrayList<ApplianceMessage>();
        var filePath = Path.of(BINDING_LOGS_PATH, logFile.fileName());
        if (!Files.exists(filePath)) {
            return result;
        }

        // check cache
        var cachedResult = logMessagesCache.get(logFile.id());
        if (cachedResult != null) {
            return cachedResult;
        }

        // get messages from zip file
        try (ZipFile zipFile = new ZipFile(filePath.toFile())) {
            var entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                var entry = entries.nextElement();
                if (entry != null && entry.getName().endsWith(".json")) {
                    try (InputStream is = zipFile.getInputStream(entry);
                            InputStreamReader reader = new InputStreamReader(is, UTF_8)) {
                        ApplianceMessage[] messagesArray = gson.fromJson(reader, ApplianceMessage[].class);
                        result.addAll(List.of(messagesArray));
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Could not read log file '{}'. error: {}", logFile.fileName(), e.getMessage());
        }
        result.sort(Comparator.comparing(ApplianceMessage::dateTime).reversed());

        logMessagesCache.put(logFile.id(), () -> result);
        return result;
    }

    private List<ApplianceMessage> getFilteredLogFileMessages(List<ApplianceMessage> messages,
            @Nullable MessageFilter filter) {
        return messages.stream().filter(applianceMessage -> !filterOutMessage(applianceMessage, filter)).toList();
    }

    private @Nullable FeatureMapping getLogFileFeatureMapping(LogFile logFile) {
        var filePath = Path.of(BINDING_LOGS_PATH, logFile.fileName());
        if (!Files.exists(filePath)) {
            return null;
        }

        // check cache
        var cachedResult = featureMappingCache.get(logFile.id());
        if (cachedResult != null) {
            return cachedResult;
        }

        // get featureMapping from zip file
        FeatureMapping result = null;
        try (ZipFile zipFile = new ZipFile(filePath.toFile())) {
            var entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                var entry = entries.nextElement();
                if (entry != null && entry.getName().endsWith("_FeatureMapping.xml")) {
                    try (InputStream is = zipFile.getInputStream(entry)) {
                        result = new FeatureMappingService(is).getFeatureMapping();
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Could not read feature mapping from log file '{}'. error: {}", logFile.fileName(),
                    e.getMessage());
        }

        FeatureMapping finalResult = result;
        featureMappingCache.put(logFile.id(), () -> finalResult);
        return result;
    }

    private @Nullable DeviceDescriptionService getLogFileInitialDeviceDescription(LogFile logFile,
            FeatureMapping featureMapping) {
        var filePath = Path.of(BINDING_LOGS_PATH, logFile.fileName());
        if (!Files.exists(filePath)) {
            return null;
        }

        // check cache
        var cachedResult = initialDeviceDescriptionServiceCache.get(logFile.id());
        if (cachedResult != null) {
            return cachedResult;
        }

        // get device description file from zip file
        DeviceDescriptionService result = null;
        try (ZipFile zipFile = new ZipFile(filePath.toFile())) {
            var entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                var entry = entries.nextElement();
                if (entry != null && entry.getName().endsWith("_DeviceDescription.xml")) {
                    try (InputStream is = zipFile.getInputStream(entry)) {
                        result = new DeviceDescriptionService("unknown", is, featureMapping);
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Could not read device description from log file '{}'. error: {}", logFile.fileName(),
                    e.getMessage());
        }

        DeviceDescriptionService finalResult = result;
        initialDeviceDescriptionServiceCache.put(logFile.id(), () -> finalResult);
        return result;
    }

    private void createLogsDirectory() {
        try {
            Files.createDirectories(Paths.get(BINDING_LOGS_PATH));
        } catch (IOException e) {
            logger.error("Could not create logs directory! directory={}", BINDING_LOGS_PATH, e);
        }
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

    private record ProxyLogEntry(@Nullable String timestamp, @Nullable String sessionId, @Nullable ProxySender sender,
            @Nullable JsonObject message) {
    }

    private enum ProxySender {
        APP,
        HOME_APPLIANCE
    }
}
