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
package org.openhab.binding.hdpowerview.providers;

import static java.util.Map.entry;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.i18n.TranslationProvider;
import org.osgi.framework.Bundle;

/**
 * Translation provider for unit tests.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class MockedTranslationProvider implements TranslationProvider {

    private static final Map<String, String> TEXTS = Map.ofEntries(
            entry("dynamic-channel.scene-activate.description", "Activates the scene ''{0}''"),
            entry("dynamic-channel.scene-group-activate.description", "Activates the scene group ''{0}''"),
            entry("dynamic-channel.automation-enabled.description", "Enables/disables the automation ''{0}''"),
            entry("dynamic-channel.automation-enabled.label", "{0}, {1}, {2}"),
            entry("dynamic-channel.automation.hour", "{0}hr"), entry("dynamic-channel.automation.minute", "{0}m"),
            entry("dynamic-channel.automation.hour-minute", "{0}hr {1}m"),
            entry("dynamic-channel.automation.at-sunrise", "At sunrise"),
            entry("dynamic-channel.automation.before-sunrise", "{0} before sunrise"),
            entry("dynamic-channel.automation.after-sunrise", "{0} after sunrise"),
            entry("dynamic-channel.automation.at-sunset", "At sunset"),
            entry("dynamic-channel.automation.before-sunset", "{0} before sunset"),
            entry("dynamic-channel.automation.after-sunset", "{0} after sunset"),
            entry("dynamic-channel.automation.weekdays", "Weekdays"),
            entry("dynamic-channel.automation.weekends", "Weekends"),
            entry("dynamic-channel.automation.all-days", "All days"));

    public MockedTranslationProvider() {
    }

    @Nullable
    public String getText(@Nullable Bundle bundle, @Nullable String key, @Nullable String defaultText,
            @Nullable Locale locale) {
        return "";
    }

    @Nullable
    public String getText(@Nullable Bundle bundle, @Nullable String key, @Nullable String defaultText,
            @Nullable Locale locale, @Nullable Object @Nullable... arguments) {
        String text = TEXTS.get(key);
        return MessageFormat.format(text != null ? text : key, arguments);
    }
}
