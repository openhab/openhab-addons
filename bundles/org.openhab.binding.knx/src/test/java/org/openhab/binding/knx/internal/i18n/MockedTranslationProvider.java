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
package org.openhab.binding.knx.internal.i18n;

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
 * @author Holger Friedrich - Imported from hdpowerview, adapted
 */
@NonNullByDefault
public class MockedTranslationProvider implements TranslationProvider {

    private static final Map<String, String> TEXTS = Map.ofEntries(entry("binding.knx.name", "KNX Binding"),
            entry("error.knx-unknown-ip-connection-type", "Unknown IP connection type: {0}."),
            entry("exception.KNXException", "Translated KNX Exception"));

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
