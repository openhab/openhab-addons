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
import org.openhab.core.config.core.ConfigParser;
import org.openhab.io.openhabcloud.internal.CloudService;

/**
 * This is a base {@link ModuleHandler} implementation for {@link Action}s to hide notifications via openHAB Cloud.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public abstract class BaseHideNotificationActionHandler extends BaseActionModuleHandler {

    public static final String PARAM_TAG = "tag";
    public static final String PARAM_REFERENCE_ID = "referenceId";

    protected final CloudService cloudService;
    protected final @Nullable String tag;
    protected final @Nullable String referenceId;

    public BaseHideNotificationActionHandler(Action module, CloudService cloudService) {
        super(module);
        this.cloudService = cloudService;
        this.tag = stringConfig(PARAM_TAG);
        this.referenceId = stringConfig(PARAM_REFERENCE_ID);
    }

    protected @Nullable String stringConfig(String key) {
        return ConfigParser.valueAs(module.getConfiguration().get(key), String.class);
    }
}
