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
package org.openhab.binding.digitalstrom.internal.providers;

import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openhab.core.i18n.TranslationProvider;
import org.osgi.framework.Bundle;
import org.osgi.service.component.ComponentContext;

/**
 * The {@link BaseDsI18n} provides the internationalization service in form of the
 * {@link org.openhab.core.i18n.TranslationProvider} of the
 * digitalSTROM-Bindings. So this class can be implement e.g. by provider implementations like the
 * {@link org.openhab.core.thing.type.ChannelTypeProvider}.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 */
public abstract class BaseDsI18n {

    public static final String LABEL_ID = "label";
    public static final String DESC_ID = "desc";
    public static final String SEPERATOR = "_";

    private TranslationProvider translationProvider;
    private Bundle bundle;

    /**
     * Initializes the {@link BaseDsI18n}.
     *
     * @param componentContext
     */
    protected void activate(ComponentContext componentContext) {
        this.bundle = componentContext.getBundleContext().getBundle();
        init();
    }

    /**
     * Will be call after the {@link BaseDsI18n} is initialized and can be overridden by subclasses to handle some
     * initial jobs.
     */
    protected void init() {
        // Can be overridden by subclasses
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
     * Sets the {@link TranslationProvider} at the {@link BaseDsI18n}.
     *
     * @param translationProvider
     */

    protected void setTranslationProvider(TranslationProvider translationProvider) {
        this.translationProvider = translationProvider;
    }

    /**
     * Unsets the {@link TranslationProvider} at the {@link BaseDsI18n}.
     *
     * @param translationProvider
     */
    protected void unsetTranslationProvider(TranslationProvider translationProvider) {
        this.translationProvider = null;
    }

    /**
     * Returns the internationalized text in the language of the {@link Locale} of the given key. If the key does not
     * exist at the internationalization of the {@link Locale} the {@link Locale#ENGLISH} will be used. If the key does
     * not exists in {@link Locale#ENGLISH}, too, the key will be returned.
     *
     * @param key
     * @param locale
     * @return internationalized text
     */
    protected String getText(String key, Locale locale) {
        return translationProvider != null
                ? translationProvider.getText(bundle, key,
                        translationProvider.getText(bundle, key, key, Locale.ENGLISH), locale)
                : key;
    }

    /**
     * Returns the internationalized label in the language of the {@link Locale} of the given key.
     *
     * @param key of internationalization label
     * @param locale of the wished language
     * @return internationalized label
     * @see #getText(String, Locale)
     */
    protected String getLabelText(String key, Locale locale) {
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
    protected String getDescText(String key, Locale locale) {
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
    public static String buildIdentifier(Object... parts) {
        return Stream.of(parts) //
                .map(Object::toString) //
                .map(String::toLowerCase) //
                .collect(Collectors.joining(SEPERATOR));
    }
}
