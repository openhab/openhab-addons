/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.smarther.internal.account;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.smarther.internal.api.exception.SmartherAuthorizationException;
import org.openhab.binding.smarther.internal.api.exception.SmartherGatewayException;
import org.openhab.binding.smarther.internal.api.exception.SmartherNotificationException;
import org.openhab.binding.smarther.internal.api.model.ModelUtil;
import org.openhab.binding.smarther.internal.api.model.Notification;
import org.openhab.binding.smarther.internal.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.reflect.TypeToken;

/**
 * The {@link SmartherNotificationServlet} manages the notifications from BTicino/Legrand C2C notification service.
 *
 * @author Fabio Possieri - Initial contribution
 */
@NonNullByDefault
public class SmartherNotificationServlet extends HttpServlet {

    private static final long serialVersionUID = -2474355132186048438L;

    private static final String CONTENT_TYPE = "application/json;charset=UTF-8";
    private static final String OK_RESULT_MSG = "{\"result\":0}";

    private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final SmartherAccountService accountService;

    public SmartherNotificationServlet(SmartherAccountService accountService) {
        this.accountService = accountService;
    }

    @Override
    protected void doPost(@Nullable HttpServletRequest req, @Nullable HttpServletResponse resp)
            throws ServletException, IOException {
        if (req == null || resp == null) {
            throw new SmartherNotificationException("Notification callback with null request/response");
        }

        logger.debug("Notification callback servlet received POST request {}", req.getRequestURI());

        // Handle the received data
        final String requestBody = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        final String responseBody = handleSmartherNotifications(requestBody);

        // Build response for the caller
        resp.setContentType(CONTENT_TYPE);
        resp.getWriter().append(responseBody);
        resp.setStatus(HttpStatus.OK_200);
        resp.getWriter().close();
    }

    /**
     * Handles a notification payload received from BTicino/Legrand C2C notification service. If that is the case,
     * BTicino/Legrand C2C notification service will pass a list of notifications via the url and these are processed.
     * In case of an error, this is logged and the notifications are not passed on to the handler. Based on all these
     * different outcomes, the response is generated to inform the C2C service.
     *
     * @param payload the body part of the POST request this servlet is processing
     */
    private String handleSmartherNotifications(@Nullable String payload) {
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
    }

    /**
     * Handles a single notification received from BTicino/Legrand C2C notification service.
     *
     * @param notification the received notification
     */
    private void handleSmartherNotification(Notification notification) {
        if (!notification.hasData()) {
            logger.warn("C2C notification {}: no valid data received", notification.getId());
        } else if (!notification.getData().hasChronothermostat()) {
            logger.warn("C2C notification {}: no chronothermostat data received", notification.getId());
        } else if (!notification.getData().toChronothermostat().hasSender()) {
            logger.warn("C2C notification {}: no sender reference received", notification.getId());
        } else {
            try {
                accountService.handleNotification(notification);
            } catch (SmartherAuthorizationException | SmartherGatewayException e) {
                logger.warn("C2C notification {}: not applied: {}", notification.getId(), e.getMessage());
            }
        }
    }

}
