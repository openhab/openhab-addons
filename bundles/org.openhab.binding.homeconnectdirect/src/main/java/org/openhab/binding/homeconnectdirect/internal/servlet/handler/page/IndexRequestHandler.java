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
package org.openhab.binding.homeconnectdirect.internal.servlet.handler.page;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.SERVLET_ASSETS_PATH;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.SERVLET_BASE_PATH;
import static org.openhab.binding.homeconnectdirect.internal.common.utils.ConfigurationUtils.getDeviceId;
import static org.openhab.binding.homeconnectdirect.internal.servlet.ServletConstants.PATH_API_APPLIANCES;
import static org.openhab.binding.homeconnectdirect.internal.servlet.ServletConstants.PATH_API_LOGS;
import static org.openhab.binding.homeconnectdirect.internal.servlet.ServletConstants.PATH_API_MESSAGES;
import static org.openhab.binding.homeconnectdirect.internal.servlet.ServletConstants.PATH_API_PROFILES;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.homeconnectdirect.internal.servlet.ServletUtils;
import org.openhab.binding.homeconnectdirect.internal.servlet.routing.RequestHandlerContext;
import org.openhab.binding.homeconnectdirect.internal.servlet.routing.RequestHandlerException;

/**
 * Request handler for index page.
 *
 * @author Jonas Brüstel - Initial contribution
 */
@NonNullByDefault
public class IndexRequestHandler {

    private static final String TEMPLATE = "/templates/index.html";

    public void indexPage(RequestHandlerContext ctx) throws RequestHandlerException {
        try (InputStream is = getClass().getResourceAsStream(TEMPLATE)) {
            if (is == null) {
                throw new RequestHandlerException("Template not found: " + TEMPLATE);
            }

            String template = new String(is.readAllBytes(), UTF_8);
            String content = ServletUtils.replacePlaceholders(template, key -> switch (key) {
                case "loginEnabled" -> Boolean.toString(ctx.getConfiguration().loginEnabled);
                case "servletPath" -> SERVLET_BASE_PATH;
                case "assetPath" -> SERVLET_ASSETS_PATH;
                case "bindingDeviceId" -> getDeviceId();
                case "apiAppliancesPath" -> PATH_API_APPLIANCES;
                case "apiProfilesPath" -> PATH_API_PROFILES;
                case "apiMessagesPath" -> PATH_API_MESSAGES;
                case "apiLogsPath" -> PATH_API_LOGS;
                case "messageQueueSize" -> String.valueOf(ctx.getConfiguration().messageQueueSize);
                default -> null;
            });
            ctx.sendHtml(content);
        } catch (IOException e) {
            throw new RequestHandlerException("Error reading template " + TEMPLATE, e);
        }
    }
}
