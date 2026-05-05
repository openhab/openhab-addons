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

import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.ZONE_ID;
import static org.openhab.binding.homeconnectdirect.internal.common.utils.StringUtils.EMPTY_STRING;
import static org.openhab.binding.homeconnectdirect.internal.common.utils.StringUtils.HYPHEN;
import static org.openhab.binding.homeconnectdirect.internal.common.utils.StringUtils.convertToKebabCase;
import static org.openhab.binding.homeconnectdirect.internal.common.utils.StringUtils.isNotBlank;
import static org.openhab.binding.homeconnectdirect.internal.common.utils.StringUtils.toLowercase;
import static org.openhab.binding.homeconnectdirect.internal.servlet.ServletConstants.CONTENT_DISPOSITION_VALUE_TEMPLATE;
import static org.openhab.binding.homeconnectdirect.internal.servlet.ServletConstants.CONTENT_TYPE_ZIP;
import static org.openhab.binding.homeconnectdirect.internal.servlet.ServletConstants.HA_ID;
import static org.openhab.binding.homeconnectdirect.internal.servlet.ServletConstants.HEADER_CONTENT_DISPOSITION;
import static org.openhab.binding.homeconnectdirect.internal.servlet.ServletConstants.PROFILE_DOWNLOAD_FILENAME_TEMPLATE;
import static org.openhab.binding.homeconnectdirect.internal.servlet.ServletConstants.ZIP_FILE_PART;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import javax.servlet.ServletException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.homeconnectdirect.internal.service.profile.ApplianceProfileService;
import org.openhab.binding.homeconnectdirect.internal.service.profile.model.ApplianceProfile;
import org.openhab.binding.homeconnectdirect.internal.service.profile.model.TlsCredentials;
import org.openhab.binding.homeconnectdirect.internal.servlet.model.ApiCollectionResponse;
import org.openhab.binding.homeconnectdirect.internal.servlet.model.Error;
import org.openhab.binding.homeconnectdirect.internal.servlet.model.Profile;
import org.openhab.binding.homeconnectdirect.internal.servlet.routing.RequestHandlerContext;
import org.openhab.binding.homeconnectdirect.internal.servlet.routing.RequestHandlerException;

/**
 * Request handler for profile API endpoints.
 *
 * @author Jonas Brüstel - Initial contribution
 */
@NonNullByDefault
public class ProfileRequestHandler {

    private final ApplianceProfileService applianceProfileService;
    private final DateTimeFormatter fileNameDateFormatter;

    public ProfileRequestHandler(ApplianceProfileService applianceProfileService) {
        this.applianceProfileService = applianceProfileService;
        this.fileNameDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
    }

    public void getProfiles(RequestHandlerContext context) throws RequestHandlerException {
        var profiles = applianceProfileService.getProfiles().stream().map(this::mapToProfile)
                .sorted((p1, p2) -> p2.created().compareTo(p1.created())).toList();

        context.sendJson(new ApiCollectionResponse<>(profiles));
    }

    public void uploadProfile(RequestHandlerContext context) throws RequestHandlerException {
        var request = context.getRequest();

        try {
            var filePart = request.getPart(ZIP_FILE_PART);
            if (filePart == null || filePart.getSubmittedFileName() == null) {
                context.sendJson(new Error(HttpStatus.BAD_REQUEST_400, "No file submitted"),
                        HttpStatus.BAD_REQUEST_400);
                return;
            }

            var fileName = filePart.getSubmittedFileName();
            if (!fileName.endsWith(".zip")) {
                context.sendJson(new Error(HttpStatus.UNSUPPORTED_MEDIA_TYPE_415, "Only zip files are supported"),
                        HttpStatus.UNSUPPORTED_MEDIA_TYPE_415);
                return;
            }

            Optional<ApplianceProfile> profile;
            try (InputStream inputStream = filePart.getInputStream()) {
                profile = applianceProfileService.uploadProfileZip(inputStream);
            }

            if (profile.isPresent()) {
                context.sendNoContent();
            } else {
                context.sendJson(new Error(HttpStatus.INTERNAL_SERVER_ERROR_500, "Failed to process profile"),
                        HttpStatus.INTERNAL_SERVER_ERROR_500);
            }
        } catch (IOException | ServletException e) {
            throw new RequestHandlerException("Could not upload profile zip", e);
        }
    }

    public void downloadProfile(RequestHandlerContext context) throws RequestHandlerException {
        var applianceProfile = getProfileOrSendError(context);
        if (applianceProfile == null) {
            return;
        }

        var type = convertToKebabCase(toLowercase(applianceProfile.type()));
        var brand = toLowercase(applianceProfile.brand());
        var vib = toLowercase(applianceProfile.vib());
        var mac = isNotBlank(applianceProfile.mac()) ? toLowercase(applianceProfile.mac().replace(HYPHEN, EMPTY_STRING))
                : EMPTY_STRING;
        var filename = String.format(PROFILE_DOWNLOAD_FILENAME_TEMPLATE, type, brand, vib, mac,
                LocalDateTime.now(ZONE_ID).format(fileNameDateFormatter));

        var response = context.getResponse();
        response.setContentType(CONTENT_TYPE_ZIP);
        response.setHeader(HEADER_CONTENT_DISPOSITION, String.format(CONTENT_DISPOSITION_VALUE_TEMPLATE, filename));

        try {
            applianceProfileService.downloadProfileZip(applianceProfile.haId(), response.getOutputStream());
        } catch (IOException e) {
            throw new RequestHandlerException("Could not download profile zip", e);
        }
    }

    public void deleteProfile(RequestHandlerContext context) throws RequestHandlerException {
        var applianceProfile = getProfileOrSendError(context);
        if (applianceProfile == null) {
            return;
        }

        applianceProfileService.deleteProfile(applianceProfile.haId());
        context.sendNoContent();
    }

    private @Nullable ApplianceProfile getProfileOrSendError(RequestHandlerContext context)
            throws RequestHandlerException {
        var haId = context.getVariable(HA_ID);
        if (haId == null) {
            context.sendJson(new Error(HttpStatus.BAD_REQUEST_400, "Missing haId"), HttpStatus.BAD_REQUEST_400);
            return null;
        }

        var applianceProfile = applianceProfileService.getProfile(haId);
        if (applianceProfile == null) {
            context.sendJson(
                    new Error(HttpStatus.NOT_FOUND_404,
                            String.format("Profile with Home Appliance ID '%s' not found", haId)),
                    HttpStatus.NOT_FOUND_404);
            return null;
        }

        return applianceProfile;
    }

    private Profile mapToProfile(ApplianceProfile applianceProfile) {
        return new Profile(applianceProfile.haId(), applianceProfile.brand(), applianceProfile.type(),
                applianceProfile.vib(), applianceProfile.credentials() instanceof TlsCredentials ? "WSS" : "WS",
                applianceProfile.created());
    }
}
