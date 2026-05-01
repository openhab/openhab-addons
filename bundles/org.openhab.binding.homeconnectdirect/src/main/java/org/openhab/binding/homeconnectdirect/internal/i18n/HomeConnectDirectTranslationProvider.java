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
package org.openhab.binding.homeconnectdirect.internal.i18n;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

/**
 * {@link HomeConnectDirectTranslationProvider} provides i18n message lookup.
 * 
 * @author Jonas Brüstel - Initial contribution
 */
@NonNullByDefault
public class HomeConnectDirectTranslationProvider {

    private final Bundle bundle;
    private final TranslationProvider translationProvider;
    private final LocaleProvider localeProvider;

    public HomeConnectDirectTranslationProvider(TranslationProvider translationProvider,
            LocaleProvider localeProvider) {
        this.bundle = FrameworkUtil.getBundle(this.getClass());
        this.translationProvider = translationProvider;
        this.localeProvider = localeProvider;
    }

    public String getText(String key, @Nullable Object... arguments) {
        var text = translationProvider.getText(bundle, key, null, localeProvider.getLocale(), arguments);
        return Objects.requireNonNullElse(text, key);
    }
}
