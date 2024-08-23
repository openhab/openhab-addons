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
package org.openhab.binding.mielecloud.internal.webservice.language;

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.i18n.LocaleProvider;

/**
 * Language provider relying on the openHAB runtime to provide a locale which is converted to a language.
 *
 * @author Björn Lange - Initial Contribution
 */
@NonNullByDefault
public class OpenHabLanguageProvider implements LanguageProvider {
    private final LocaleProvider localeProvider;

    public OpenHabLanguageProvider(LocaleProvider localeProvider) {
        this.localeProvider = localeProvider;
    }

    @Override
    public Optional<String> getLanguage() {
        return Optional.of(localeProvider.getLocale().getLanguage());
    }
}
