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
package org.openhab.binding.tado.tests;

import java.text.MessageFormat;
import java.util.Locale;

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

    public MockedTranslationProvider() {
    }

    @Override
    @Nullable
    public String getText(@Nullable Bundle bundle, @Nullable String key, @Nullable String defaultText,
            @Nullable Locale locale) {
        return key;
    }

    @Override
    @Nullable
    public String getText(@Nullable Bundle bundle, @Nullable String key, @Nullable String defaultText,
            @Nullable Locale locale, @Nullable Object @Nullable... arguments) {
        return MessageFormat.format(key, arguments);
    }
}
