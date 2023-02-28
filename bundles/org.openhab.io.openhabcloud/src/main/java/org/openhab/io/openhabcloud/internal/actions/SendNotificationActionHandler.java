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
package org.openhab.io.openhabcloud.internal.actions;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.automation.Action;
import org.openhab.core.automation.handler.ModuleHandler;
import org.openhab.io.openhabcloud.internal.CloudService;

/**
 * This is a {@link ModuleHandler} implementation for {@link Action}s to send a notification to a specific cloud user.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class SendNotificationActionHandler extends BaseNotificationActionHandler {

    public static final String TYPE_ID = "notification.SendNotification";
    public static final String EXTENDED_TYPE_ID = "notification.SendExtendedNotification";
    public static final String PARAM_USER = "userId";

    private final String userId;

    public SendNotificationActionHandler(Action module, CloudService cloudService) {
        super(module, cloudService);

        Object userIdParam = module.getConfiguration().get(PARAM_USER);
        if (userIdParam instanceof String) {
            this.userId = userIdParam.toString();
        } else {
            throw new IllegalArgumentException(String.format("Param '%s' should be of type String.", PARAM_USER));
        }
    }

    @Override
    public @Nullable Map<String, Object> execute(Map<String, Object> context) {
        cloudService.sendNotification(userId, message, icon, severity);
        return null;
    }
}
