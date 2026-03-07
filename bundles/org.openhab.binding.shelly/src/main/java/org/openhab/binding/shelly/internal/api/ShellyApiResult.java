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
package org.openhab.binding.shelly.internal.api;

import static org.eclipse.jetty.http.HttpStatus.*;
import static org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.*;

import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;

/**
 * The {@link ShellyApiResult} wraps up the API result and provides some more information like url, http code, received
 * response etc.
 *
 * @author Markus Michels - Initial contribution
 * @author Ravi Nadahar - Created builder and made class immutable
 */
@NonNullByDefault
public class ShellyApiResult {

    private static final String LOW_SHELLY_APIERR_TIMEOUT = SHELLY_APIERR_TIMEOUT.toLowerCase(Locale.ROOT);

    public final String url;
    public final String method;
    public final String response;
    public final int httpCode;
    public final String httpReason;
    public final String authChallenge;

    public ShellyApiResult(@Nullable String method, @Nullable String url, @Nullable String response, int httpCode,
            @Nullable String httpReason, @Nullable String authChallenge) {
        this.method = method == null ? "" : method;
        this.url = url == null ? "" : url;
        this.response = response == null ? "" : response;
        this.httpCode = httpCode;
        this.httpReason = httpReason == null ? "" : httpReason;
        this.authChallenge = authChallenge == null ? "" : authChallenge;
    }

    public String getUrl() {
        return !url.isEmpty() ? method + " " + url : "";
    }

    public String getHttpResponse() {
        return response;
    }

    @Override
    public String toString() {
        return getUrl() + " > " + getHttpResponse();
    }

    public boolean isHttpOk() {
        return httpCode == OK_200;
    }

    public boolean isNotFound() {
        return httpCode == NOT_FOUND_404;
    }

    public boolean isHttpAccessUnauthorized() {
        return (httpCode == UNAUTHORIZED_401 || response.contains(SHELLY_APIERR_UNAUTHORIZED));
    }

    public boolean isHttpTimeout() {
        return httpCode == -1 || response.toLowerCase(Locale.ROOT).contains(LOW_SHELLY_APIERR_TIMEOUT);
    }

    public boolean isHttpServerError() {
        return httpCode == INTERNAL_SERVER_ERROR_500;
    }

    public boolean isNotCalibrtated() {
        return response.contains(SHELLY_APIERR_NOT_CALIBRATED);
    }

    public ShellyApiResultBuilder modify() {
        return new ShellyApiResultBuilder(this);
    }

    public static ShellyApiResultBuilder builder() {
        return new ShellyApiResultBuilder();
    }

    public static ShellyApiResultBuilder builder(@Nullable String method, @Nullable String url) {
        return new ShellyApiResultBuilder(method, url);
    }

    public static ShellyApiResultBuilder builder(@Nullable ContentResponse contentResponse) {
        return new ShellyApiResultBuilder(contentResponse);
    }

    public static class ShellyApiResultBuilder {

        private @Nullable String url;
        private @Nullable String method;
        private @Nullable String response;
        private int httpCode = -1;
        private @Nullable String httpReason;
        private @Nullable String authChallenge;

        public ShellyApiResultBuilder() {
        }

        public ShellyApiResultBuilder(@Nullable String method, @Nullable String url) {
            this.method = method;
            this.url = url;
        }

        public ShellyApiResultBuilder(@Nullable ContentResponse contentResponse) {
            if (contentResponse != null) {
                String r = contentResponse.getContentAsString();
                response = r != null ? r : "";
                httpCode = contentResponse.getStatus();
                httpReason = contentResponse.getReason();

                Request request = contentResponse.getRequest();
                url = request.getURI().toString();
                method = request.getMethod();
            }
        }

        public ShellyApiResultBuilder(ShellyApiResult existingResult) {
            this.url = existingResult.url;
            this.method = existingResult.method;
            this.response = existingResult.response;
            this.httpCode = existingResult.httpCode;
            this.httpReason = existingResult.httpReason;
            this.authChallenge = existingResult.authChallenge;
        }

        public @Nullable String url() {
            return url;
        }

        public ShellyApiResultBuilder url(@Nullable String url) {
            this.url = url;
            return this;
        }

        public @Nullable String method() {
            return method;
        }

        public ShellyApiResultBuilder method(@Nullable String method) {
            this.method = method;
            return this;
        }

        public @Nullable String response() {
            return response;
        }

        public ShellyApiResultBuilder response(@Nullable String response) {
            this.response = response;
            return this;
        }

        public int httpCode() {
            return httpCode;
        }

        public ShellyApiResultBuilder httpCode(int httpCode) {
            this.httpCode = httpCode;
            return this;
        }

        public @Nullable String httpReason() {
            return httpReason;
        }

        public ShellyApiResultBuilder httpReason(@Nullable String httpReason) {
            this.httpReason = httpReason;
            return this;
        }

        public @Nullable String authChallenge() {
            return authChallenge;
        }

        public ShellyApiResultBuilder authChallenge(@Nullable String authChallenge) {
            this.authChallenge = authChallenge;
            return this;
        }

        public ShellyApiResult build() {
            return new ShellyApiResult(method, url, response, httpCode, httpReason, authChallenge);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(getClass().getSimpleName()).append(" [");
            if (url != null) {
                sb.append("url=").append(url).append(", ");
            }
            if (method != null) {
                sb.append("method=").append(method).append(", ");
            }
            if (response != null) {
                sb.append("response=").append(response).append(", ");
            }
            if (httpReason != null) {
                sb.append("httpReason=").append(httpReason).append(", ");
            }
            if (authChallenge != null) {
                sb.append("authChallenge=").append(authChallenge).append(", ");
            }
            sb.append("httpCode=").append(httpCode).append(']');
            return sb.toString();
        }
    }
}
