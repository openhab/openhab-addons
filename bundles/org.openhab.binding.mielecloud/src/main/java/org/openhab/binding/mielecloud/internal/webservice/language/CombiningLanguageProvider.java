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
package org.openhab.binding.mielecloud.internal.webservice.language;

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * {@link LanguageProvider} combining two {@link LanguageProvider}s, a prioritized and a fallback provider.
 *
 * @author Björn Lange - Initial contribution
 */
@NonNullByDefault
public class CombiningLanguageProvider implements LanguageProvider {
    private @Nullable LanguageProvider prioritizedLanguageProvider;
    private @Nullable LanguageProvider fallbackLanguageProvider;

    /**
     * Creates a new instance.
     *
     * @param prioritizedLanguageProvider Primary {@link LanguageProvider} to use. May be {@code null}, in that case the
     *            {@code fallbackLanguageProvider} will be used.
     * @param fallbackLanguageProvider {@link LanguageProvider} to fall back to if the
     *            {@code prioritizedLanguageProvider} is {@code null} or provides no language. May be
     *            {@code null}, in case the fallback is used and returns no language then no language will be returned.
     */
    public CombiningLanguageProvider(@Nullable LanguageProvider prioritizedLanguageProvider,
            @Nullable LanguageProvider fallbackLanguageProvider) {
        this.prioritizedLanguageProvider = prioritizedLanguageProvider;
        this.fallbackLanguageProvider = fallbackLanguageProvider;
    }

    public void setPrioritizedLanguageProvider(LanguageProvider prioritizedLanguageProvider) {
        this.prioritizedLanguageProvider = prioritizedLanguageProvider;
    }

    public void unsetPrioritizedLanguageProvider() {
        this.prioritizedLanguageProvider = null;
    }

    public void setFallbackLanguageProvider(LanguageProvider fallbackLanguageProvider) {
        this.fallbackLanguageProvider = fallbackLanguageProvider;
    }

    public void unsetFallbackLanguageProvider() {
        this.fallbackLanguageProvider = null;
    }

    @Override
    public Optional<String> getLanguage() {
        Optional<String> prioritizedLanguage = Optional.ofNullable(prioritizedLanguageProvider)
                .flatMap(LanguageProvider::getLanguage);
        if (prioritizedLanguage.isPresent()) {
            return prioritizedLanguage;
        } else {
            return Optional.ofNullable(fallbackLanguageProvider).flatMap(LanguageProvider::getLanguage);
        }
    }
}
