/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.netatmo.internal.providers;

import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.BINDING_ID;

import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.i18n.TranslationProvider;
import org.osgi.framework.Bundle;
import org.osgi.service.component.ComponentContext;

/**
 * The {@link BaseDsI18n} provides the internationalization service in form of the
 * {@link org.openhab.core.i18n.TranslationProvider} of the Netatmo -Bindings.
 * So this class can be implement e.g. by provider implementations like the
 * {@link org.openhab.core.thing.type.ChannelTypeProvider}.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 * @author Gaël L'hopital - adapted to Netatmo Binding
 */
@NonNullByDefault
public abstract class BaseDsI18n {

    private static final String LABEL_ID = "label";
    private static final String DESC_ID = "description";
    private static final String SEPERATOR = ".";

    private final TranslationProvider translationProvider;
    private @NonNullByDefault({}) Bundle bundle;

    public BaseDsI18n(TranslationProvider translationProvider) {
        this.translationProvider = translationProvider;
    }

    /**
     * Initializes the {@link BaseDsI18n}.
     *
     * @param componentContext
     */
    protected void activate(ComponentContext componentContext) {
        this.bundle = componentContext.getBundleContext().getBundle();
    }

    /**
     * Disposes the {@link BaseDsI18n}.
     *
     * @param componentContext
     */
    protected void deactivate(ComponentContext componentContext) {
        this.bundle = null;
    }

    /**
     * Returns the internationalized text in the language of the {@link Locale} of the given key. If the key an does not
     * exist at the internationalization of the {@link Locale} the {@link Locale#ENGLISH} will be used. If the key dose
     * not exists in {@link Locale#ENGLISH}, too, the key will be returned.
     *
     * @param key
     * @param locale
     * @return internationalized text
     */
    private String getText(String key, @Nullable Locale locale) {
        String result = translationProvider.getText(bundle, key,
                translationProvider.getText(bundle, key, key, Locale.ENGLISH), locale);
        return result != null ? result : key;
    }

    /**
     * Returns the internationalized label in the language of the {@link Locale} of the given key.
     *
     * @param key of internationalization label
     * @param locale of the wished language
     * @return internationalized label
     * @see #getText(String, Locale)
     */
    protected String getLabelText(String key, @Nullable Locale locale) {
        return getText(buildIdentifier(key, LABEL_ID), locale);
    }

    /**
     * Returns the internationalized description in the language of the {@link Locale} of the given key.
     *
     * @param key of internationalization description
     * @param locale of the wished language
     * @return internationalized description
     * @see #getText(String, Locale)
     */
    protected String getDescText(String key, @Nullable Locale locale) {
        return getText(buildIdentifier(key, DESC_ID), locale);
    }

    /**
     * Builds the key {@link String} through the given {@link Object}s.<br>
     * The key will be build as lower case {@link Object#toString()} + {@link #SEPERATOR} + {@link Object#toString()} +
     * ... , so the result {@link String} will be look like "object1_object2"
     *
     * @param parts to join
     * @return key
     */
    private static String buildIdentifier(String p1, String p2) {
        return String.join(SEPERATOR, "thing-type", BINDING_ID, p1, p2);
    }
}
