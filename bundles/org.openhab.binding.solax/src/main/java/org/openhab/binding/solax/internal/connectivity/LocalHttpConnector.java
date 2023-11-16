/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.solax.internal.connectivity;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.core.io.net.http.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link LocalHttpConnector} class uses HttpUtil to retrieve the raw JSON data from Inverter's Wi-Fi module.
 *
 * @author Konstantin Polihronov - Initial contribution
 */
@NonNullByDefault
public class LocalHttpConnector implements SolaxConnector {

    private static final int HTTP_REQUEST_TIME_OUT = 5000;

    private static final String CONTENT_TYPE = "text/html; charset=utf-8";

    private final Logger logger = LoggerFactory.getLogger(LocalHttpConnector.class);

    private static final String OPT_TYPE = "optType";
    private static final String READ_REALTIME_DATA = "ReadRealTimeData";

    private static final String PASSWORD = "pwd";
    // The serial number of the Wifi dongle is the password for the connection (default)
    private String passwordValue;
    private String uri;

    public LocalHttpConnector(String passwordValue, String host) {
        this.passwordValue = passwordValue;
        this.uri = "http://" + host;
    }

    @Override
    public @Nullable String retrieveData() throws IOException {
        String requestBody = createRequestBody();
        logger.trace("Uri: {}, Request body: {}", uri, requestBody);
        String result = HttpUtil.executeUrl(HttpMethod.POST.name(), uri,
                new ByteArrayInputStream(requestBody.getBytes(StandardCharsets.UTF_8)), CONTENT_TYPE,
                HTTP_REQUEST_TIME_OUT);
        logger.trace("Retrieved content = {}", result);
        return result;
    }

    private String createRequestBody() {
        StringBuilder sb = new StringBuilder();

        sb.append(OPT_TYPE).append("=").append(READ_REALTIME_DATA);
        sb.append("&");
        sb.append(PASSWORD).append("=").append(passwordValue);

        return sb.toString();
    }
}
