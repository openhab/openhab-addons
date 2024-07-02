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
package org.openhab.io.openhabcloud.internal.actions;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.automation.Action;
import org.openhab.core.automation.handler.ModuleHandler;
import org.openhab.io.openhabcloud.internal.CloudService;

/**
 * This is a {@link ModuleHandler} implementation for {@link Action}s to hide a notification to a specific cloud user.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class HideBroadcastNotificationByTagActionHandler extends BaseHideNotificationActionHandler {

    public static final String TYPE_ID = "notification.HideBroadcastNotificationByTag";

    public HideBroadcastNotificationByTagActionHandler(Action module, CloudService cloudService) {
        super(module, cloudService);
    }

    @Override
    public @Nullable Map<String, Object> execute(Map<String, Object> context) {
        cloudService.hideBroadcastNotificationByTag(tag);
        return null;
    }
}
