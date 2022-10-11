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

import java.text.MessageFormat;
import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

/**
 * This class provides translations. It is a helper class for i18n / localization efforts.
 *
 * @implNote It is implemented as a static singleton, enforced by the single-element enum pattern.
 * @apiNote @set() must be called to provide tanslation service, otherwise all functions will return untranslated text.
 *          Thread safety is ensured.
 * @author Holger Friedrich - Initial contribution
 *
 */
@NonNullByDefault
public enum KNXTranslationProvider {
    I18N;

    private @Nullable LocaleProvider localeProvider;
    private @Nullable TranslationProvider translationProvider;
    private Bundle bundle;

    private KNXTranslationProvider() {
        localeProvider = null;
        translationProvider = null;
        bundle = FrameworkUtil.getBundle(this.getClass());
    }

    /**
     * get translated text
     *
     * @param text text to be translated, may contain placeholders \{n\} for the n-th optional argument of this function
     * @param arguments any optional arguments, will be inserted
     * @return translated text with subsitutions if translationprovide is set and provides a translation, otherwise
     *         returns original text with substitutions
     */
    public String get(final String text, @Nullable Object @Nullable... arguments) {
        // ensure thread safety: calls to set(..) should not lead to race condition
        final TranslationProvider translationProvider = this.translationProvider;
        final LocaleProvider localeProvider = this.localeProvider;
        if (translationProvider != null) {
            // localeProvider might be null, but if not, getLocale will return NonNull Locale
            // locale cannot be cached, as getLocale() will return different result once locale is changed by user
            final Locale locale = (localeProvider != null) ? localeProvider.getLocale() : Locale.getDefault();
            final String res = translationProvider.getText(bundle, text, text, locale, arguments);
            if (res != null) {
                return res;
            }
        }
        // translating not possible, we still have the original text without any subsititutions
        if (arguments == null || arguments.length == 0) {
            return text;
        }
        // else execute pattern substitution in untranslated text
        return MessageFormat.format(text, arguments);
    }

    /**
     * get exception in user readable (and possibly localized) form
     *
     * @param e any exception
     * @return localized message in form <description (translated)> (<class name>, <e.getLocalizedMessage (not
     *         translated)>), empty string for null. May possibly change in further releases.
     */
    public String getLocalizedException(final Throwable e) {
        StringBuffer res = new StringBuffer();
        final String exName = e.getClass().getSimpleName();
        final String key = "exception." + exName;
        final String translatedDescription = KNXTranslationProvider.I18N.get(key);
        Boolean foundTranslation = !key.equals(translatedDescription);
        // detailed message cannot be translated, e.getLocalizedMessage will likely return English
        String detail = e.getLocalizedMessage();
        if (detail == null) {
            detail = "";
        }

        if (foundTranslation) {
            res.append(translatedDescription);
            res.append(" (");
            res.append(exName);
            if (!detail.isBlank()) {
                res.append(", ");
                res.append(detail);
            }
            res.append(")");
        } else {
            res.append(exName);
            if (!detail.isBlank()) {
                res.append(", ");
                res.append(detail);
            }
        }
        return res.toString();
    }

    /**
     * Set translation providers. To be called to make any translation work.
     *
     * @param localeProvider openHAB locale provider, can be generated via \@Activate / \@Reference LocaleProvider in
     *            handler factory
     * @param translationProvider openHAB locale provider, can be generated via \@Activate / \@Reference
     *            TranslationProvider in handler factory
     */
    public void setProvider(@Nullable LocaleProvider localeProvider,
            @Nullable TranslationProvider translationProvider) {
        this.localeProvider = localeProvider;
        this.translationProvider = translationProvider;
    }
}
