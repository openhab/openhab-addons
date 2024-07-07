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

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.automation.Action;
import org.openhab.core.automation.handler.BaseActionModuleHandler;
import org.openhab.core.automation.handler.ModuleHandler;
import org.openhab.core.config.core.ConfigParser;
import org.openhab.io.openhabcloud.internal.CloudService;

/**
 * This is a base {@link ModuleHandler} implementation for {@link Action}s to send a notifications via openHAB Cloud.
 *
 * @author Christoph Weitkamp - Initial contribution
 * @author Dan Cunningham - Extended notification enhancements
 */
@NonNullByDefault
public abstract class BaseNotificationActionHandler extends BaseActionModuleHandler {

    public static final String PARAM_MESSAGE = "message";
    public static final String PARAM_ICON = "icon";
    public static final String PARAM_SEVERITY = "severity";
    public static final String PARAM_TAG = "tag";
    public static final String PARAM_TITLE = "title";
    public static final String PARAM_REFERENCE_ID = "referenceId";
    public static final String PARAM_ON_CLICK_ACTION = "onClickAction";
    public static final String PARAM_MEDIA_ATTACHMENT_URL = "mediaAttachmentUrl";
    public static final String PARAM_ACTION_BUTTON_1 = "actionButton1";
    public static final String PARAM_ACTION_BUTTON_2 = "actionButton2";
    public static final String PARAM_ACTION_BUTTON_3 = "actionButton3";

    protected final CloudService cloudService;

    protected final String message;
    protected final @Nullable String icon;
    protected final @Nullable String severity;
    protected final @Nullable String tag;
    protected final @Nullable String title;
    protected final @Nullable String referenceId;
    protected final @Nullable String onClickAction;
    protected final @Nullable String mediaAttachmentUrl;
    protected final @Nullable String actionButton1;
    protected final @Nullable String actionButton2;
    protected final @Nullable String actionButton3;

    public BaseNotificationActionHandler(Action module, CloudService cloudService) {
        super(module);
        this.cloudService = cloudService;

        this.message = Optional.ofNullable(stringConfig(PARAM_MESSAGE)).orElseThrow(() -> new IllegalArgumentException(
                String.format("Param '%s' should be of type String.", PARAM_MESSAGE)));
        this.icon = stringConfig(PARAM_ICON);
        this.severity = stringConfig(PARAM_SEVERITY);
        this.tag = stringConfig(PARAM_TAG);
        this.title = stringConfig(PARAM_TITLE);
        this.referenceId = stringConfig(PARAM_REFERENCE_ID);
        this.onClickAction = stringConfig(PARAM_ON_CLICK_ACTION);
        this.mediaAttachmentUrl = stringConfig(PARAM_MEDIA_ATTACHMENT_URL);
        this.actionButton1 = stringConfig(PARAM_ACTION_BUTTON_1);
        this.actionButton2 = stringConfig(PARAM_ACTION_BUTTON_2);
        this.actionButton3 = stringConfig(PARAM_ACTION_BUTTON_3);
    }

    protected @Nullable String stringConfig(String key) {
        return ConfigParser.valueAs(module.getConfiguration().get(key), String.class);
    }
}
