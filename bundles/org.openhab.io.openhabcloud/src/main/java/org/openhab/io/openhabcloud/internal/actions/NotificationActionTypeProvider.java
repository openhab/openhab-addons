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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.automation.Visibility;
import org.openhab.core.automation.type.ActionType;
import org.openhab.core.automation.type.ModuleType;
import org.openhab.core.automation.type.ModuleTypeProvider;
import org.openhab.core.common.registry.ProviderChangeListener;
import org.openhab.core.config.core.ConfigDescriptionParameter;
import org.openhab.core.config.core.ConfigDescriptionParameter.Type;
import org.openhab.core.config.core.ConfigDescriptionParameterBuilder;
import org.osgi.service.component.annotations.Component;

/**
 * This class provides a {@link ModuleTypeProvider} implementation to provide actions to send notifications via
 * openHAB Cloud.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
@Component(service = ModuleTypeProvider.class)
public class NotificationActionTypeProvider implements ModuleTypeProvider {

    private static final ModuleType SEND_NOTIFICATION_ACTION = new ActionType(SendNotificationActionHandler.TYPE_ID,
            getSendNotificationConfig(false, null), "send a notification",
            "Sends a notification to a specific cloud user.", null, Visibility.VISIBLE, null, null);
    private static final ModuleType SEND_EXTENDED_NOTIFICATION_ACTION = new ActionType(
            SendNotificationActionHandler.EXTENDED_TYPE_ID, getSendNotificationConfig(true, null),
            "send a notification with icon and severity",
            "Sends a notification to a specific cloud user. Optionally add an icon or the severity.", null,
            Visibility.VISIBLE, null, null);
    private static final ModuleType SEND_BROADCAST_NOTIFICATION_ACTION = new ActionType(
            SendBroadcastNotificationActionHandler.TYPE_ID, getNotificationConfig(false, null),
            "broadcast a notification", "Sends a notification to all devices of all users.", null, Visibility.VISIBLE,
            null, null);
    private static final ModuleType SEND_EXRENDED_BROADCAST_NOTIFICATION_ACTION = new ActionType(
            SendBroadcastNotificationActionHandler.EXTENDED_TYPE_ID, getNotificationConfig(true, null),
            "broadcast a notification with icon and severity",
            "Sends a notification to all devices of all users. Optionally add an icon or the severity.", null,
            Visibility.VISIBLE, null, null);
    private static final ModuleType SEND_LOG_NOTIFICATION_ACTION = new ActionType(
            SendLogNotificationActionHandler.TYPE_ID, getNotificationConfig(false, null), "send a log message",
            "Sends a log notification to the openHAB Cloud instance. Notifications are NOT sent to any registered devices.",
            null, Visibility.VISIBLE, null, null);
    private static final ModuleType SEND_EXTENDED_LOG_NOTIFICATION_ACTION = new ActionType(
            SendLogNotificationActionHandler.EXTENDED_TYPE_ID, getNotificationConfig(true, null),
            "send a log message with icon and severity",
            "Sends a log notification to the openHAB Cloud instance. Optionally add an icon or the severity. Notifications are NOT sent to any registered devices.",
            null, Visibility.VISIBLE, null, null);
    private static final List<ModuleType> MODULE_TYPES = List.of(SEND_NOTIFICATION_ACTION,
            SEND_EXTENDED_NOTIFICATION_ACTION, SEND_BROADCAST_NOTIFICATION_ACTION,
            SEND_EXRENDED_BROADCAST_NOTIFICATION_ACTION, SEND_LOG_NOTIFICATION_ACTION,
            SEND_EXTENDED_LOG_NOTIFICATION_ACTION);

    @SuppressWarnings("unchecked")
    @Override
    public @Nullable ModuleType getModuleType(String UID, @Nullable Locale locale) {
        switch (UID) {
            case SendNotificationActionHandler.TYPE_ID:
                return SEND_NOTIFICATION_ACTION;
            case SendNotificationActionHandler.EXTENDED_TYPE_ID:
                return SEND_EXTENDED_NOTIFICATION_ACTION;
            case SendBroadcastNotificationActionHandler.TYPE_ID:
                return SEND_BROADCAST_NOTIFICATION_ACTION;
            case SendBroadcastNotificationActionHandler.EXTENDED_TYPE_ID:
                return SEND_EXRENDED_BROADCAST_NOTIFICATION_ACTION;
            case SendLogNotificationActionHandler.TYPE_ID:
                return SEND_LOG_NOTIFICATION_ACTION;
            case SendLogNotificationActionHandler.EXTENDED_TYPE_ID:
                return SEND_EXTENDED_LOG_NOTIFICATION_ACTION;
            default:
                return null;
        }
    }

    @Override
    public Collection<ModuleType> getAll() {
        return MODULE_TYPES;
    }

    @Override
    public Collection<ModuleType> getModuleTypes(@Nullable Locale locale) {
        return MODULE_TYPES;
    }

    private static List<ConfigDescriptionParameter> getSendNotificationConfig(boolean isExtended,
            @Nullable Locale locale) {
        List<ConfigDescriptionParameter> params = new ArrayList<>();
        params.add(ConfigDescriptionParameterBuilder.create(SendNotificationActionHandler.PARAM_USER, Type.TEXT)
                .withRequired(true).withLabel("User Id").withDescription("The cloud user id of the recipient.")
                .build());
        params.addAll(getNotificationConfig(isExtended, locale));
        return params;
    }

    private static List<ConfigDescriptionParameter> getNotificationConfig(boolean isExtended, @Nullable Locale locale) {
        List<ConfigDescriptionParameter> params = new ArrayList<>();
        params.add(getMessageConfigParameter(locale));
        if (isExtended) {
            params.add(getIconConfigParameter(locale));
            params.add(getSeverityConfigParameter(locale));
        }
        return params;
    }

    private static ConfigDescriptionParameter getMessageConfigParameter(@Nullable Locale locale) {
        return ConfigDescriptionParameterBuilder.create(BaseNotificationActionHandler.PARAM_MESSAGE, Type.TEXT)
                .withRequired(true).withLabel("Message").withDescription("The body of the notification.").build();
    }

    private static ConfigDescriptionParameter getIconConfigParameter(@Nullable Locale locale) {
        return ConfigDescriptionParameterBuilder.create(BaseNotificationActionHandler.PARAM_ICON, Type.TEXT)
                .withLabel("Icon").withDescription("The icon of the notification.").build();
    }

    private static ConfigDescriptionParameter getSeverityConfigParameter(@Nullable Locale locale) {
        return ConfigDescriptionParameterBuilder.create(BaseNotificationActionHandler.PARAM_SEVERITY, Type.TEXT)
                .withLabel("Severity").withDescription("The severity of the notification.").build();
    }

    @Override
    public void addProviderChangeListener(ProviderChangeListener<ModuleType> listener) {
        // does nothing because this provider does not change
    }

    @Override
    public void removeProviderChangeListener(ProviderChangeListener<ModuleType> listener) {
        // does nothing because this provider does not change
    }
}
