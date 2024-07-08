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
import java.util.Optional;

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
public class HideNotificationByReferenceIdActionHandler extends BaseHideNotificationActionHandler {

    public static final String TYPE_ID = "notification.HideNotificationByReferenceId";

    public static final String PARAM_USER = "userId";

    private final String userId;

    public HideNotificationByReferenceIdActionHandler(Action module, CloudService cloudService) {
        super(module, cloudService);

        this.userId = Optional.ofNullable(stringConfig(PARAM_USER)).orElseThrow(
                () -> new IllegalArgumentException(String.format("Param '%s' should be of type String.", PARAM_USER)));
    }

    @Override
    public @Nullable Map<String, Object> execute(Map<String, Object> context) {
        cloudService.hideNotificationByReferenceId(userId, referenceId);
        return null;
    }
}
