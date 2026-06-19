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
package org.openhab.binding.homeconnectdirect.internal.servlet;

import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.BINDING_ID;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Servlet constants.
 *
 * @author Jonas Brüstel - Initial contribution
 */
@NonNullByDefault
public class ServletConstants {

    private ServletConstants() {
        // prevent instantiation
    }

    public static final String PATH_API = "/api";
    public static final String PATH_API_PROFILES = PATH_API + "/profiles";
    public static final String PATH_API_APPLIANCES = PATH_API + "/appliances";
    public static final String PATH_API_MESSAGES = PATH_API + "/messages";
    public static final String PATH_API_LOGS = PATH_API + "/logs";

    public static final String HA_ID = "haId";
    public static final String UID = "uid";
    public static final String LOG_FILE_ID = "fileId";
    public static final String ZIP_FILE_PART = "zipFile";
    public static final String PROXY_FILES_PART = "proxyFiles";

    public static final String GET = "GET";
    public static final String POST = "POST";
    public static final String DELETE = "DELETE";

    public static final String CONTENT_TYPE_JSON = "application/json";
    public static final String CONTENT_TYPE_ZIP = "application/zip";
    public static final String CONTENT_TYPE_FORM_DATA = "multipart/form-data";
    public static final String CONTENT_TYPE_HTML_UTF8 = "text/html; charset=UTF-8";
    public static final String CONTENT_TYPE_HTML = "text/html";
    public static final String HEADER_CONTENT_DISPOSITION = "Content-Disposition";
    public static final String HEADER_X_FRAME_OPTIONS = "X-Frame-Options";
    public static final String HEADER_PREFER = "Prefer";

    public static final String PROFILE_DOWNLOAD_FILENAME_TEMPLATE = BINDING_ID + "-%s-%s-%s-%s_%s.zip";
    public static final String LOG_DOWNLOAD_FILENAME_TEMPLATE = "log-" + BINDING_ID + "-%s-%s-%s_%s_%s.zip.log";
    public static final String CONTENT_DISPOSITION_VALUE_TEMPLATE = "attachment; filename=\"%s\"";
    public static final String UNAUTHORIZED = "Unauthorized";
    public static final String NOT_FOUND = "Not Found";
    public static final String SAME_ORIGIN = "SAMEORIGIN";
    public static final String PREFER_PERSIST = "handling=persist";
}
