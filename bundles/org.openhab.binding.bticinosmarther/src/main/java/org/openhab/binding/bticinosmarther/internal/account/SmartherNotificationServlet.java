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
package org.openhab.binding.bticinosmarther.internal.account;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.bticinosmarther.internal.api.dto.Notification;
import org.openhab.binding.bticinosmarther.internal.api.exception.SmartherGatewayException;
import org.openhab.binding.bticinosmarther.internal.util.ModelUtil;
import org.openhab.binding.bticinosmarther.internal.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

/**
 * The {@code SmartherNotificationServlet} class acts as the registered endpoint to receive module status notifications
 * from the Legrand/Bticino C2C Webhook notification service.
 *
 * @author Fabio Possieri - Initial contribution
 */
@NonNullByDefault
public class SmartherNotificationServlet extends HttpServlet {

    private static final long serialVersionUID = -2474355132186048438L;

    private static final String CONTENT_TYPE = "application/json;charset=UTF-8";
    private static final String OK_RESULT_MSG = "{\"result\":0}";
    private static final String KO_RESULT_MSG = "{\"result\":1}";

    private final Logger logger = LoggerFactory.getLogger(SmartherNotificationServlet.class);

    private final SmartherAccountService accountService;

    /**
     * Constructs a {@code SmartherNotificationServlet} associated to the given {@link SmartherAccountService} service.
     *
     * @param accountService
     *            the account service to associate to the servlet
     */
    public SmartherNotificationServlet(SmartherAccountService accountService) {
        this.accountService = accountService;
    }

    @Override
    protected void doPost(@Nullable HttpServletRequest request, @Nullable HttpServletResponse response)
            throws ServletException, IOException {
        if (request != null && response != null) {
            logger.debug("Notification callback servlet received POST request {}", request.getRequestURI());

            // Handle the received data
            final String requestBody = StringUtil.readerToString(request.getReader());
            final String responseBody = dispatchNotifications(requestBody);

            // Build a http 200 (Success) response for the caller
            response.setContentType(CONTENT_TYPE);
            response.setStatus(HttpStatus.OK_200);
            response.getWriter().append(responseBody);
            response.getWriter().close();
        } else if (response != null) {
            // Build a http 400 (Bad Request) error response for the caller
            response.setContentType(CONTENT_TYPE);
            response.setStatus(HttpStatus.BAD_REQUEST_400);
            response.getWriter().close();
        } else {
            throw new ServletException("Notification callback with null request/response");
        }
    }

    /**
     * Dispatches all the notifications contained in the received payload to the proper notification handlers.
     * The response to the notification service is generated based on the different outcomes.
     *
     * @param payload
     *            the received servlet payload to process, may be {@code null}
     *
     * @return a string containing the response to the notification service
     */
    private String dispatchNotifications(@Nullable String payload) {
        try {
            logger.trace("C2C listener received payload: {}", payload);
            if (!StringUtil.isBlank(payload)) {
                List<Notification> notifications = ModelUtil.gsonInstance().fromJson(payload,
                        new TypeToken<List<Notification>>() {
                        }.getType());

                if (notifications != null) {
                    notifications.forEach(n -> handleSmartherNotification(n));
                }
            }
            return OK_RESULT_MSG;
        } catch (JsonSyntaxException e) {
            logger.warn("C2C payload parsing error: {} ", e.getMessage());
            return KO_RESULT_MSG;
        }
    }

    /**
     * Dispatches a single notification contained in the received payload to the proper notification handler.
     *
     * @param notification
     *            the notification to dispatch
     */
    private void handleSmartherNotification(Notification notification) {
        try {
            this.accountService.dispatchNotification(notification);
        } catch (SmartherGatewayException e) {
            logger.warn("C2C notification {}: not applied: {}", notification.getId(), e.getMessage());
        }
    }
}
