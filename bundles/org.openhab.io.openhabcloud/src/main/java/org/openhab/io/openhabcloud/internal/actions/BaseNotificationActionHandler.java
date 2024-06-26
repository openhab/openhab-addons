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
 * @author Dan Cunningham - Extended Notification Enhancements
 */
@NonNullByDefault
public abstract class BaseNotificationActionHandler extends BaseActionModuleHandler {

    public static final String PARAM_MESSAGE = "message";
    public static final String PARAM_ICON = "icon";
    public static final String PARAM_SEVERITY = "severity";
    public static final String PARAM_ON_CLICK_ACTION = "onClickAction";
    public static final String PARAM_MEDIA_ATTACHMENT_URL = "mediaAttachmentUrl";
    public static final String PARAM_ACTION_BUTTON_1 = "actionButton1";
    public static final String PARAM_ACTION_BUTTON_2 = "actionButton2";
    public static final String PARAM_ACTION_BUTTON_3 = "actionButton3";

    protected final CloudService cloudService;

    protected final String message;
    protected final @Nullable String icon;
    protected final @Nullable String severity;
    protected final @Nullable String onClickAction;
    protected final @Nullable String mediaAttachmentUrl;
    protected final @Nullable String actionButton1;
    protected final @Nullable String actionButton2;
    protected final @Nullable String actionButton3;

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

        Object onClickActionParam = module.getConfiguration().get(PARAM_ON_CLICK_ACTION);
        this.onClickAction = onClickActionParam instanceof String ? onClickActionParam.toString() : null;

        Object mediaAttachmentUrlParam = module.getConfiguration().get(PARAM_MEDIA_ATTACHMENT_URL);
        this.mediaAttachmentUrl = mediaAttachmentUrlParam instanceof String ? mediaAttachmentUrlParam.toString() : null;

        Object actionButton1Param = module.getConfiguration().get(PARAM_ACTION_BUTTON_1);
        this.actionButton1 = actionButton1Param instanceof String ? actionButton1Param.toString() : null;

        Object actionButton2Param = module.getConfiguration().get(PARAM_ACTION_BUTTON_2);
        this.actionButton2 = actionButton2Param instanceof String ? actionButton2Param.toString() : null;

        Object actionButton3Param = module.getConfiguration().get(PARAM_ACTION_BUTTON_3);
        this.actionButton3 = actionButton3Param instanceof String ? actionButton3Param.toString() : null;
    }
}
