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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.automation.Action;
import org.openhab.core.automation.handler.BaseActionModuleHandler;
import org.openhab.core.automation.handler.ModuleHandler;
import org.openhab.io.openhabcloud.internal.CloudService;

/**
 * This is a base {@link ModuleHandler} implementation for {@link Action}s to send a notifications via openHAB Cloud.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public abstract class BaseNotificationActionHandler extends BaseActionModuleHandler {

    public static final String PARAM_MESSAGE = "message";
    public static final String PARAM_ICON = "icon";
    public static final String PARAM_SEVERITY = "severity";

    protected final CloudService cloudService;

    protected final String message;
    protected final @Nullable String icon;
    protected final @Nullable String severity;

    public BaseNotificationActionHandler(Action module, CloudService cloudService) {
        super(module);
        this.cloudService = cloudService;

        Object messageParam = module.getConfiguration().get(PARAM_MESSAGE);
        if (messageParam instanceof String) {
            this.message = messageParam.toString();
        } else {
            throw new IllegalArgumentException(String.format("Param '%s' should be of type String.", PARAM_MESSAGE));
        }

        Object iconParam = module.getConfiguration().get(PARAM_ICON);
        this.icon = iconParam instanceof String ? iconParam.toString() : null;

        Object severityParam = module.getConfiguration().get(PARAM_SEVERITY);
        this.severity = severityParam instanceof String ? severityParam.toString() : null;
    }
}
