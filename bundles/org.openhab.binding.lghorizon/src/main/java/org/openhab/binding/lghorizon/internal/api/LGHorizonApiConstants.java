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
package org.openhab.binding.lghorizon.internal.api;

import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * REST API constants for the LG Horizon service.
 *
 * @author Mark - Initial contribution
 */
@NonNullByDefault
public class LGHorizonApiConstants {

    public static final String AUTH_SERVICE_AUTHORIZATION_PATH = "/auth-service/v1/authorization";
    public static final String AUTH_SERVICE_AUTHORIZATION_REFRESH_PATH = "/auth-service/v1/authorization/refresh";
    public static final String V1_MQTT_TOKEN_PATH = "/v1/mqtt/token";
    public static final String CUSTOMER_PATH = "/v1/customer/%s?with=profiles%%2Cdevices";
    public static final String ENTITLEMENTS_PATH = "/v2/customers/%s/entitlements?enableDaypass=true";
    public static final String CHANNELS_PATH = "/v2/channels?cityId=%s&language=%s&productClass=Orion-DASH";
    public static final String EVENT_DETAIL_PATH = "/v2/replayEvent/%s?returnLinearContent=true&forceLinearResponse=true&language=%s";
    public static final String RECORDING_DETAIL_PATH = "/customers/%s/details/single/%s?profileId=%s&language=%s";
    public static final String VOD_DETAIL_PATH = "/v2/detailscreen/%s?language=%s&profileId=%s&cityId=%d";

    public static final String EN_CONFIG_SERVICE_CONF_WEB_BACKOFFICE_JSON = "/en/config-service/conf/web/backoffice.json";

    public static final String CONTENT_TYPE_HEADER = "content-type";
    public static final String APPLICATION_JSON = "application/json";
    public static final String IMAGE_JPEG = "image/jpeg";
    public static final String CHARSET_HEADER = "charset";
    public static final String UTF_8 = "utf-8";
    public static final String X_DEVICE_CODE_HEADER = "x-device-code";
    public static final String X_DEVICE_CODE_WEB = "web";
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER = "Bearer ";

    public static final int CREDENTIALS_ERROR = 97401;
    public static final int TOKEN_ERROR = 97402;

    public static final String PASSWORD_FIELD = "password";
    public static final String USERNAME_FIELD = "username";
    public static final String TOKEN_FIELD = "token";
    public static final String REFRESH_TOKEN_FIELD = "refreshToken";

    public static final String AUTHORIZATION_SERVICE_URL_FIELD = "authorizationService";
    public static final String PERSONALIZATION_SERVICE_URL_FIELD = "personalizationService";
    public static final String PURCHASE_SERVICE_URL_FIELD = "purchaseService";
    public static final String LINEAR_SERVICE_URL_FIELD = "linearService";
    public static final String VOD_SERVICE_URL_FIELD = "vodService";
    public static final String RECORDING_SERVICE_URL_FIELD = "recordingService";

    public static final String SOURCE_TYPE_LINEAR = "linear";
    public static final String SOURCE_TYPE_REVIEWBUFFER = "reviewbuffer";
    public static final String SOURCE_TYPE_REPLAY = "replay";
    public static final String SOURCE_TYPE_VOD = "vod";
    public static final String SOURCE_TYPE_NDVR = "ndvr";

    public static final String BOX_STATE_ONLINE_STANDBY = "ONLINE_STANDBY";
    public static final String BOX_STATE_ONLINE_RUNNING = "ONLINE_RUNNING";

    public static final int REQUEST_TIMEOUT = 15;
    public static final long TOKEN_REFRESH_MARGIN_SECONDS = TimeUnit.DAYS.toSeconds(1);

    public static final String DEFAULT_LANGUAGE = "en";

    private LGHorizonApiConstants() {
        // private constructor to prevent instantiation
    }
}
