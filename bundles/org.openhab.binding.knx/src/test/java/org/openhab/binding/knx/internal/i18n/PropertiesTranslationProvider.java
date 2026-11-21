/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Properties;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.i18n.TranslationProvider;
import org.osgi.framework.Bundle;

/**
 * Translation provider for unit tests that answers from the add-on's own English texts, so a test also shows that
 * the key exists.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public class PropertiesTranslationProvider implements TranslationProvider {
    private final Properties texts = new Properties();

    @SuppressWarnings("null")
    public PropertiesTranslationProvider() {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("OH-INF/i18n/knx.properties")) {
            if (in == null) {
                throw new IllegalStateException("knx.properties not found");
            }
            texts.load(new InputStreamReader(in, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public @Nullable String getText(@Nullable Bundle bundle, @Nullable String key, @Nullable String defaultText,
            @Nullable Locale locale) {
        String text = key == null ? null : texts.getProperty(key);
        return text != null ? text : defaultText;
    }

    @Override
    public @Nullable String getText(@Nullable Bundle bundle, @Nullable String key, @Nullable String defaultText,
            @Nullable Locale locale, @Nullable Object @Nullable... arguments) {
        String text = getText(bundle, key, defaultText, locale);
        return text == null ? null : MessageFormat.format(text, arguments);
    }
}
