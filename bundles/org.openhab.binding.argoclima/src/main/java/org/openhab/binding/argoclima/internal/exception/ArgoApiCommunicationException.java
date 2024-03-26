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
package org.openhab.binding.argoclima.internal.exception;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.argoclima.internal.ArgoClimaTranslationProvider;

/**
 * The class {@code ArgoApiCommunicationException} is thrown in case of any issues with communication with the Argo HVAC
 * device (incl. indirect communication, via sniffing)
 *
 * @author Mateusz Bronk - Initial contribution
 */
@NonNullByDefault
public class ArgoApiCommunicationException extends ArgoLocalizedException {

    private static final long serialVersionUID = -6618438267962155601L;

    public ArgoApiCommunicationException(String defaultMessage, String localizedMessageKey,
            ArgoClimaTranslationProvider i18nProvider, @Nullable Throwable cause,
            Object @Nullable... messageFormatArguments) {
        super(defaultMessage, localizedMessageKey, i18nProvider, cause, messageFormatArguments);
    }

    public ArgoApiCommunicationException(String defaultMessage, String localizedMessageKey,
            ArgoClimaTranslationProvider i18nProvider, @Nullable Throwable cause) {
        super(defaultMessage, localizedMessageKey, i18nProvider, cause);
    }

    public ArgoApiCommunicationException(String defaultMessage, String localizedMessageKey,
            ArgoClimaTranslationProvider i18nProvider, Object @Nullable... messageFormatArguments) {
        super(defaultMessage, localizedMessageKey, i18nProvider, messageFormatArguments);
    }

    public ArgoApiCommunicationException(String defaultMessage, String localizedMessageKey,
            ArgoClimaTranslationProvider i18nProvider) {
        super(defaultMessage, localizedMessageKey, i18nProvider);
    }

    /**
     * {@inheritDoc}
     *
     * @implNote We want cause of all these exceptions included in the message by default
     */
    @Override
    public @Nullable String getMessage() {
        return super.getMessage(true);
    }

    /**
     * {@inheritDoc}
     *
     * @implNote We want cause of all these exceptions included in the message by default
     */
    @Override
    public @Nullable String getLocalizedMessage() {
        return super.getLocalizedMessage(true);
    }
}
