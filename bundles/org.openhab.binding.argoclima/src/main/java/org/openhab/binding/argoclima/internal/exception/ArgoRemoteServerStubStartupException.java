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
 * The class {@code ArgoRemoteServerStubStartupException} is thrown in case of any issues when starting the stub Argo
 * server (for intercepting mode)
 *
 * @see org.openhab.binding.argoclima.internal.device.passthrough.RemoteArgoApiServerStub
 * @author Mateusz Bronk - Initial contribution
 */
@NonNullByDefault
public class ArgoRemoteServerStubStartupException extends ArgoLocalizedException {

    private static final long serialVersionUID = 3798832375487523670L;

    public ArgoRemoteServerStubStartupException(String defaultMessage, String localizedMessageKey,
            ArgoClimaTranslationProvider i18nProvider, @Nullable Throwable cause,
            Object @Nullable... messageFormatArguments) {
        super(defaultMessage, localizedMessageKey, i18nProvider, cause, messageFormatArguments);
    }

    public ArgoRemoteServerStubStartupException(String defaultMessage, String localizedMessageKey,
            ArgoClimaTranslationProvider i18nProvider, @Nullable Throwable cause) {
        super(defaultMessage, localizedMessageKey, i18nProvider, cause);
    }

    public ArgoRemoteServerStubStartupException(String defaultMessage, String localizedMessageKey,
            ArgoClimaTranslationProvider i18nProvider, Object @Nullable... messageFormatArguments) {
        super(defaultMessage, localizedMessageKey, i18nProvider, messageFormatArguments);
    }

    public ArgoRemoteServerStubStartupException(String defaultMessage, String localizedMessageKey,
            ArgoClimaTranslationProvider i18nProvider) {
        super(defaultMessage, localizedMessageKey, i18nProvider);
    }
}
