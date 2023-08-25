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

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.argoclima.internal.ArgoClimaTranslationProvider;

/**
 * The class {@code ArgoConfigurationException} is thrown in case of any configuration-related issue (ex. invalid value
 * format)
 *
 * @author Mateusz Bronk - Initial contribution
 */
@NonNullByDefault
public class ArgoConfigurationException extends ArgoLocalizedException {

    private static final long serialVersionUID = 174501670495658964L;
    public final @Nullable String rawValue;

    private ArgoConfigurationException(String paramName, @Nullable String paramValue, String defaultMessage,
            String localizedMessageKey, @Nullable ArgoClimaTranslationProvider i18nProvider,
            Object @Nullable... messageFormatArguments) {
        super(defaultMessage, localizedMessageKey, i18nProvider, messageFormatArguments);
        this.rawValue = paramValue;
    }

    private ArgoConfigurationException(String paramName, @Nullable String paramValue, String defaultMessage,
            String localizedMessageKey, @Nullable ArgoClimaTranslationProvider i18nProvider, Throwable cause,
            Object @Nullable... messageFormatArguments) {
        super(defaultMessage, localizedMessageKey, i18nProvider, cause, messageFormatArguments);
        this.rawValue = paramValue;
    }

    /**
     * Named c-tor: for all kinds of invalid params (caused by underlying exception)
     *
     * @param <T> Type of param
     * @param paramName Config key of param
     * @param paramValue Config value
     * @param i18nProvider Framework's translation provider
     * @param cause Inner cause
     * @return new {@code ArgoConfigurationException}
     */
    public static <@NonNull T> ArgoConfigurationException forInvalidParamValue(String paramName, T paramValue,
            @Nullable ArgoClimaTranslationProvider i18nProvider, Throwable cause) {
        return new ArgoConfigurationException(paramName, paramValue.toString(), "Invalid \"{0}\" value: {1}",
                "thing-status.argoclima.configuration.invalid-format", i18nProvider, cause, paramName, paramValue);
    }

    /**
     * Named c-tor: for empty required params
     *
     * @param paramName Config key of param
     * @param i18nProvider Framework's translation provider
     * @return new {@code ArgoConfigurationException}
     */
    public static ArgoConfigurationException forEmptyRequiredParam(String paramName,
            @Nullable ArgoClimaTranslationProvider i18nProvider) {
        return new ArgoConfigurationException(paramName, "", "\"{0}\" is empty",
                "thing-status.argoclima.configuration.empty-value", i18nProvider, paramName);
    }

    /**
     * Named c-tor: For numeric params which are out of range
     *
     * @param <T> Type of param (numeric)
     * @param paramName Config key of param
     * @param paramValue Config value
     * @param i18nProvider Framework's translation provider
     * @param rangeBegin Min value (inclusive)
     * @param rangeEnd Max value (inclusive)
     * @return new {@code ArgoConfigurationException}
     */
    public static <@NonNull T extends Number> ArgoConfigurationException forParamOutOfRange(String paramName,
            T paramValue, @Nullable ArgoClimaTranslationProvider i18nProvider, T rangeBegin, T rangeEnd) {
        return new ArgoConfigurationException(paramName, paramValue.toString(),
                "\"{0}\" must be in range [{1,number,#}..{2,number,#}]",
                "thing-status.argoclima.configuration.value-not-in-range", i18nProvider, paramName, rangeBegin,
                rangeEnd);
    }

    /**
     * Named c-tor: For numeric params which are below minimum value
     *
     * @param <T> Type of param (numeric)
     * @param paramName Config key of param
     * @param paramValue Config value
     * @param i18nProvider Framework's translation provider
     * @param minValue Min value (inclusive)
     * @return new {@code ArgoConfigurationException}
     */
    public static <@NonNull T extends Number> ArgoConfigurationException forParamBelowMin(String paramName,
            T paramValue, @Nullable ArgoClimaTranslationProvider i18nProvider, T minValue) {
        return new ArgoConfigurationException(paramName, paramValue.toString(), "\"{0}\" must be >= {1,number,#}",
                "thing-status.argoclima.configuration.value-below-min", i18nProvider, paramName, minValue);
    }

    /**
     * Named c-tor: For interdependent parameters that caused conflict
     *
     * @param <T1> Type of 1st param
     * @param <T2> Value of 1st param
     * @param paramName Config key of 1st param
     * @param paramValue Config value of 1st param
     * @param conflictingParamName Config key of 2nd param
     * @param conflictingParamValue Config value of 2nd param
     * @param i18nProvider Framework's translation provider
     * @return new {@code ArgoConfigurationException}
     */
    public static <@NonNull T1, @NonNull T2> ArgoConfigurationException forConflictingParams(String paramName,
            T1 paramValue, String conflictingParamName, T2 conflictingParamValue,
            @Nullable ArgoClimaTranslationProvider i18nProvider) {
        return new ArgoConfigurationException(paramName, paramValue.toString(),
                "Cannot set \"{0}\" to {1}, when \"{2}\" is {3}",
                "thing-status.argoclima.configuration.invalid-combination", i18nProvider, paramName, paramValue,
                conflictingParamName, conflictingParamValue);
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
