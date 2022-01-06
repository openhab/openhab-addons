/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.automation.Action;
import org.openhab.core.automation.Module;
import org.openhab.core.automation.handler.BaseModuleHandlerFactory;
import org.openhab.core.automation.handler.ModuleHandler;
import org.openhab.core.automation.handler.ModuleHandlerFactory;
import org.openhab.io.openhabcloud.internal.CloudService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

/**
 * This class provides a {@link ModuleHandlerFactory} implementation to provide actions to send notifications via
 * openHAB Cloud.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
@Component(service = ModuleHandlerFactory.class)
public class NotificationModuleHandlerFactory extends BaseModuleHandlerFactory {

    private static final Collection<String> TYPES = List.of(SendNotificationActionHandler.TYPE_ID,
            SendNotificationActionHandler.EXTENDED_TYPE_ID, SendBroadcastNotificationActionHandler.TYPE_ID,
            SendBroadcastNotificationActionHandler.EXTENDED_TYPE_ID, SendLogNotificationActionHandler.TYPE_ID,
            SendLogNotificationActionHandler.EXTENDED_TYPE_ID);
    private final CloudService cloudService;

    @Activate
    public NotificationModuleHandlerFactory(final @Reference CloudService cloudService) {
        this.cloudService = cloudService;
    }

    @Override
    @Deactivate
    protected void deactivate() {
        super.deactivate();
    }

    @Override
    public Collection<String> getTypes() {
        return TYPES;
    }

    @Override
    protected @Nullable ModuleHandler internalCreate(Module module, String ruleUID) {
        if (module instanceof Action) {
            switch (module.getTypeUID()) {
                case SendNotificationActionHandler.TYPE_ID:
                case SendNotificationActionHandler.EXTENDED_TYPE_ID:
                    return new SendNotificationActionHandler((Action) module, cloudService);
                case SendBroadcastNotificationActionHandler.TYPE_ID:
                case SendBroadcastNotificationActionHandler.EXTENDED_TYPE_ID:
                    return new SendBroadcastNotificationActionHandler((Action) module, cloudService);
                case SendLogNotificationActionHandler.TYPE_ID:
                case SendLogNotificationActionHandler.EXTENDED_TYPE_ID:
                    return new SendLogNotificationActionHandler((Action) module, cloudService);
                default:
                    break;
            }
        }
        return null;
    }
}
