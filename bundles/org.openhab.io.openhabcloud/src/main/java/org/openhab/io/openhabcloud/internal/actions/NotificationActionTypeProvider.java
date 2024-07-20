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
 * @author Dan Cunningham - Extended notification enhancements
 */
@NonNullByDefault
@Component(service = ModuleTypeProvider.class)
public class NotificationActionTypeProvider implements ModuleTypeProvider {

    private static final ModuleType SEND_NOTIFICATION_ACTION = new ActionType(SendNotificationActionHandler.TYPE_ID,
            getNotificationConfig(ConfigType.NOT_EXTENDED, true, null), "send a notification",
            "Sends a notification to a specific cloud user.", null, Visibility.VISIBLE, null, null);

    private static final ModuleType SEND_EXTENDED_NOTIFICATION_ACTION = new ActionType(
            SendNotificationActionHandler.EXTENDED_TYPE_ID, getNotificationConfig(ConfigType.EXTENDED, true, null),
            "send a notification with icon and severity (tag)",
            "Sends a notification to a specific cloud user. Optionally add an icon or the severity (tag).", null,
            Visibility.VISIBLE, null, null);

    private static final ModuleType SEND_EXTENDED2_NOTIFICATION_ACTION = new ActionType(
            SendNotificationActionHandler.EXTENDED2_TYPE_ID, getNotificationConfig(ConfigType.EXTENDED2, true, null),
            "send a notification with icon, tag, title, reference id, click action, media attachment and action buttons",
            "Sends a notification to a specific cloud user. Optionally add an icon, tag, title, reference id, on click action, media to attach, and up to 3 action buttons with a format of \"Title=Action\".",
            null, Visibility.VISIBLE, null, null);

    private static final ModuleType SEND_BROADCAST_NOTIFICATION_ACTION = new ActionType(
            SendBroadcastNotificationActionHandler.TYPE_ID, getNotificationConfig(ConfigType.NOT_EXTENDED, true, null),
            "broadcast a notification", "Sends a notification to all devices of all users.", null, Visibility.VISIBLE,
            null, null);

    private static final ModuleType SEND_EXTENDED_BROADCAST_NOTIFICATION_ACTION = new ActionType(
            SendBroadcastNotificationActionHandler.EXTENDED_TYPE_ID,
            getNotificationConfig(ConfigType.EXTENDED, false, null),
            "broadcast a notification with icon and severity (tag)",
            "Sends a notification to all devices of all users. Optionally add an icon or the severity (tag).", null,
            Visibility.VISIBLE, null, null);

    private static final ModuleType SEND_EXTENDED2_BROADCAST_NOTIFICATION_ACTION = new ActionType(
            SendBroadcastNotificationActionHandler.EXTENDED2_TYPE_ID,
            getNotificationConfig(ConfigType.EXTENDED2, false, null),
            "broadcast a notification with with icon, tag, title, reference id, on click action, media attachment and action buttons",
            "Sends a notification to all devices of all users. Optionally add an icon, tag, title, reference id, click action, media to attach, and up to 3 action buttons with a format of \"Title=Action\".",
            null, Visibility.VISIBLE, null, null);

    private static final ModuleType SEND_LOG_NOTIFICATION_ACTION = new ActionType(
            SendLogNotificationActionHandler.TYPE_ID, getNotificationConfig(ConfigType.NOT_EXTENDED, false, null),
            "send a log message",
            "Sends a log notification to the openHAB Cloud instance. Notifications are NOT sent to any registered devices.",
            null, Visibility.VISIBLE, null, null);

    private static final ModuleType SEND_EXTENDED_LOG_NOTIFICATION_ACTION = new ActionType(
            SendLogNotificationActionHandler.EXTENDED_TYPE_ID, getNotificationConfig(ConfigType.EXTENDED, false, null),
            "send a log message with icon and severity (tag)",
            "Sends a log notification to the openHAB Cloud instance. Optionally add an icon or the severity (tag). Notifications are NOT sent to any registered devices.",
            null, Visibility.VISIBLE, null, null);

    private static final ModuleType HIDE_NOTIFICATION_BY_REFERENCE_ID_ACTION = new ActionType(
            HideNotificationByReferenceIdActionHandler.TYPE_ID,
            getNotificationConfig(ConfigType.HIDE_BY_REF, true, null),
            "hide notifications by reference id for all devices of a specific user",
            "Hide notifications by reference id for all devices of a specific user.", null, Visibility.VISIBLE, null,
            null);

    private static final ModuleType HIDE_BROADCAST_NOTIFICATION_BY_REFERENCE_ID_ACTION = new ActionType(
            HideBroadcastNotificationByReferenceIdActionHandler.TYPE_ID,
            getNotificationConfig(ConfigType.HIDE_BY_REF, false, null),
            "hide notifications by reference id for all users and devices",
            "Hide notifications by reference id for all users and devices.", null, Visibility.VISIBLE, null, null);

    private static final ModuleType HIDE_NOTIFICATION_BY_TAG_ACTION = new ActionType(
            HideNotificationByTagActionHandler.TYPE_ID, getNotificationConfig(ConfigType.HIDE_BY_TAG, true, null),
            "hide notifications by tag for all devices of a specific user",
            "Hide notifications by tag id for all devices of a specific user.", null, Visibility.VISIBLE, null, null);

    private static final ModuleType HIDE_BROADCAST_NOTIFICATION_BY_TAG_ACTION = new ActionType(
            HideBroadcastNotificationByTagActionHandler.TYPE_ID,
            getNotificationConfig(ConfigType.HIDE_BY_TAG, false, null),
            "hide notifications by tag id for all users and devices",
            "Hide notifications by tag id for all users and devices.", null, Visibility.VISIBLE, null, null);

    private static final List<ModuleType> MODULE_TYPES = List.of(SEND_NOTIFICATION_ACTION,
            SEND_EXTENDED_NOTIFICATION_ACTION, SEND_EXTENDED2_NOTIFICATION_ACTION, SEND_BROADCAST_NOTIFICATION_ACTION,
            SEND_EXTENDED_BROADCAST_NOTIFICATION_ACTION, SEND_EXTENDED2_BROADCAST_NOTIFICATION_ACTION,
            SEND_LOG_NOTIFICATION_ACTION, SEND_EXTENDED_LOG_NOTIFICATION_ACTION,
            HIDE_BROADCAST_NOTIFICATION_BY_REFERENCE_ID_ACTION, HIDE_BROADCAST_NOTIFICATION_BY_TAG_ACTION,
            HIDE_NOTIFICATION_BY_REFERENCE_ID_ACTION, HIDE_NOTIFICATION_BY_TAG_ACTION);

    @SuppressWarnings("unchecked")
    @Override
    public @Nullable ModuleType getModuleType(String UID, @Nullable Locale locale) {
        switch (UID) {
            case SendNotificationActionHandler.TYPE_ID:
                return SEND_NOTIFICATION_ACTION;
            case SendNotificationActionHandler.EXTENDED_TYPE_ID:
                return SEND_EXTENDED_NOTIFICATION_ACTION;
            case SendNotificationActionHandler.EXTENDED2_TYPE_ID:
                return SEND_EXTENDED2_NOTIFICATION_ACTION;
            case SendBroadcastNotificationActionHandler.TYPE_ID:
                return SEND_BROADCAST_NOTIFICATION_ACTION;
            case SendBroadcastNotificationActionHandler.EXTENDED_TYPE_ID:
                return SEND_EXTENDED_BROADCAST_NOTIFICATION_ACTION;
            case SendBroadcastNotificationActionHandler.EXTENDED2_TYPE_ID:
                return SEND_EXTENDED2_BROADCAST_NOTIFICATION_ACTION;
            case SendLogNotificationActionHandler.TYPE_ID:
                return SEND_LOG_NOTIFICATION_ACTION;
            case SendLogNotificationActionHandler.EXTENDED_TYPE_ID:
                return SEND_EXTENDED_LOG_NOTIFICATION_ACTION;
            case HideNotificationByTagActionHandler.TYPE_ID:
                return HIDE_NOTIFICATION_BY_TAG_ACTION;
            case HideBroadcastNotificationByTagActionHandler.TYPE_ID:
                return HIDE_BROADCAST_NOTIFICATION_BY_TAG_ACTION;
            case HideNotificationByReferenceIdActionHandler.TYPE_ID:
                return HIDE_NOTIFICATION_BY_REFERENCE_ID_ACTION;
            case HideBroadcastNotificationByReferenceIdActionHandler.TYPE_ID:
                return HIDE_BROADCAST_NOTIFICATION_BY_REFERENCE_ID_ACTION;
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

    private static List<ConfigDescriptionParameter> getNotificationConfig(ConfigType type, boolean userRequired,
            @Nullable Locale locale) {
        List<ConfigDescriptionParameter> params = new ArrayList<>();

        if (userRequired) {
            params.add(ConfigDescriptionParameterBuilder.create(SendNotificationActionHandler.PARAM_USER, Type.TEXT)
                    .withRequired(true).withLabel("User Id").withDescription("The cloud user id of the recipient.")
                    .build());
        }
        switch (type) {
            case EXTENDED:
                params.add(getMessageConfigParameter(locale));
                params.add(getIconConfigParameter(locale));
                params.add(getSeverityConfigParameter(locale));
                break;
            case EXTENDED2:
                params.add(getMessageConfigParameter(locale));
                params.add(getIconConfigParameter(locale));
                params.add(getTagConfigParameter(locale));
                params.add(getTitleConfigParameter(locale));
                params.add(getReferenceIdConfigParameter(locale));
                params.add(getonClickActionConfigParameter(locale));
                params.add(getMediaAttachmentUrlConfigParameter(locale));
                params.add(getActionButton1ConfigParameter(locale));
                params.add(getActionButton2ConfigParameter(locale));
                params.add(getActionButton3ConfigParameter(locale));
                break;
            case HIDE_BY_TAG:
                params.add(getTagConfigParameter(locale));
                break;
            case HIDE_BY_REF:
                params.add(getReferenceIdConfigParameter(locale));
                break;
            default:
                break;
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
                .withLabel("Severity (Tag)").withDescription("The severity/tag of the notification.").build();
    }

    private static ConfigDescriptionParameter getTagConfigParameter(@Nullable Locale locale) {
        return ConfigDescriptionParameterBuilder.create(BaseNotificationActionHandler.PARAM_TAG, Type.TEXT)
                .withLabel("Tag").withDescription("The tag of the notification.").build();
    }

    private static ConfigDescriptionParameter getTitleConfigParameter(@Nullable Locale locale) {
        return ConfigDescriptionParameterBuilder.create(BaseNotificationActionHandler.PARAM_TITLE, Type.TEXT)
                .withLabel("Title").withDescription("The title of the notification.").build();
    }

    private static ConfigDescriptionParameter getReferenceIdConfigParameter(@Nullable Locale locale) {
        return ConfigDescriptionParameterBuilder.create(BaseNotificationActionHandler.PARAM_REFERENCE_ID, Type.TEXT)
                .withLabel("Reference Id").withDescription("A reference Id for the notification.").build();
    }

    private static ConfigDescriptionParameter getonClickActionConfigParameter(@Nullable Locale locale) {
        return ConfigDescriptionParameterBuilder.create(BaseNotificationActionHandler.PARAM_ON_CLICK_ACTION, Type.TEXT)
                .withLabel("On Click Action").withDescription("The action to perform when clicked.").build();
    }

    private static ConfigDescriptionParameter getMediaAttachmentUrlConfigParameter(@Nullable Locale locale) {
        return ConfigDescriptionParameterBuilder
                .create(BaseNotificationActionHandler.PARAM_MEDIA_ATTACHMENT_URL, Type.TEXT)
                .withLabel("Media Attachment URL").withDescription("The media to attach to a notification.").build();
    }

    private static ConfigDescriptionParameter getActionButton1ConfigParameter(@Nullable Locale locale) {
        return ConfigDescriptionParameterBuilder.create(BaseNotificationActionHandler.PARAM_ACTION_BUTTON_1, Type.TEXT)
                .withLabel("Action Button 1").withDescription("An action button in the format \"Title=Action\".")
                .build();
    }

    private static ConfigDescriptionParameter getActionButton2ConfigParameter(@Nullable Locale locale) {
        return ConfigDescriptionParameterBuilder.create(BaseNotificationActionHandler.PARAM_ACTION_BUTTON_2, Type.TEXT)
                .withLabel("Action Button 2").withDescription("An action button in the format \"Title=Action\".")
                .build();
    }

    private static ConfigDescriptionParameter getActionButton3ConfigParameter(@Nullable Locale locale) {
        return ConfigDescriptionParameterBuilder.create(BaseNotificationActionHandler.PARAM_ACTION_BUTTON_3, Type.TEXT)
                .withLabel("Action Button 3").withDescription("An action button in the format \"Title=Action\".")
                .build();
    }

    @Override
    public void addProviderChangeListener(ProviderChangeListener<ModuleType> listener) {
        // does nothing because this provider does not change
    }

    @Override
    public void removeProviderChangeListener(ProviderChangeListener<ModuleType> listener) {
        // does nothing because this provider does not change
    }

    private enum ConfigType {
        NOT_EXTENDED,
        EXTENDED,
        EXTENDED2,
        HIDE_BY_TAG,
        HIDE_BY_REF;
    }
}
