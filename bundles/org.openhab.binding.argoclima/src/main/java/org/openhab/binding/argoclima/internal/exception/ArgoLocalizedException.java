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

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.argoclima.internal.ArgoClimaTranslationProvider;

/**
 * Base for localized exceptions (their messages are used for thing status)
 *
 * @author Mateusz Bronk - Initial contribution
 */
@NonNullByDefault
public class ArgoLocalizedException extends Exception {

    private static final long serialVersionUID = 8729362177716420196L;

    protected final @Nullable ArgoClimaTranslationProvider i18nProvider;
    private final String localizedMessageKey;
    private final List<@Nullable Object> localizedMessageParams; // using list for null annotations to be
                                                                 // happy

    protected ArgoLocalizedException(String defaultMessage, String localizedMessageKey,
            @Nullable ArgoClimaTranslationProvider i18nProvider, @Nullable Throwable cause,
            Object @Nullable... messageFormatArguments) {
        super(MessageFormat.format(defaultMessage, messageFormatArguments), cause);
        this.localizedMessageKey = localizedMessageKey;
        this.localizedMessageParams = Arrays.asList(messageFormatArguments);
        this.i18nProvider = i18nProvider;
    }

    protected ArgoLocalizedException(String defaultMessage, String localizedMessageKey,
            @Nullable ArgoClimaTranslationProvider i18nProvider, @Nullable Throwable cause) {
        super(defaultMessage, cause);
        this.localizedMessageKey = localizedMessageKey;
        this.localizedMessageParams = List.<@Nullable Object> of();
        this.i18nProvider = i18nProvider;
    }

    protected ArgoLocalizedException(String defaultMessage, String localizedMessageKey,
            @Nullable ArgoClimaTranslationProvider i18nProvider, Object @Nullable... messageFormatArguments) {
        this(defaultMessage, localizedMessageKey, i18nProvider, (Throwable) null, messageFormatArguments);
    }

    protected ArgoLocalizedException(String defaultMessage, String localizedMessageKey,
            @Nullable ArgoClimaTranslationProvider i18nProvider) {
        this(defaultMessage, localizedMessageKey, i18nProvider, (Throwable) null);
    }

    @Override
    public @Nullable String getLocalizedMessage() {
        return this.getLocalizedMessage(false);
    }

    @Override
    public @Nullable String getMessage() {
        return this.getMessage(false);
    }

    /**
     * Similar to {@link #getLocalizedMessage()}, but additionally can embed cause's message
     *
     * @param includeCause Whether to embed cause message
     * @return Localized exception message
     */
    public final String getLocalizedMessage(boolean includeCause) {
        if (i18nProvider == null) {
            return getMessage(includeCause); // fallback
        }
        var i18nProvider = Objects.requireNonNull(this.i18nProvider);

        @Nullable
        String localizedMessage;
        if (!localizedMessageParams.isEmpty()) {
            localizedMessage = i18nProvider.getText(localizedMessageKey, null, localizedMessageParams.toArray());
        } else {
            localizedMessage = i18nProvider.getText(localizedMessageKey, null);
        }

        if (localizedMessage == null || localizedMessage.isBlank()) {
            // default to EN-US message (fallback to class name on failure)
            localizedMessage = Objects.requireNonNullElse(this.getMessage(), this.getClass().getSimpleName());

        }
        String localizedMessageNonNull = Objects.requireNonNull(localizedMessage); // This is 100% redundant, but
                                                                                   // Eclipse wasn't able to correctly
                                                                                   // interpret Optional.ofNullable()
                                                                                   // inside a map... so doing if-based
                                                                                   // logic, lists vs. arrays and this
                                                                                   // instead - avoids suppression :)

        if (this.getCause() != null) {
            var causeMessage = Objects.requireNonNull(this.getCause()).getLocalizedMessage();
            if (causeMessage != null && !(localizedMessageNonNull.endsWith(causeMessage))) {
                // Sometimes the cause is already embedded in the message at throw site. If it isn't though... let's add
                localizedMessageNonNull += ". " + i18nProvider
                        .getText("thing-status.cause.argoclima.exception.caused-by", "Caused by: {0}", causeMessage);
            }
        }
        return localizedMessageNonNull;
    }

    /**
     * Similar to {@link #getMessage()}, but additionally can embed cause's message
     *
     * @implNote Guaranteed non-null. Will default to class name in case message was null
     *
     * @param includeCause Whether to embed cause message
     * @return EN-US exception message
     */
    public final String getMessage(boolean includeCause) {
        @Nullable
        String message = super.getMessage();
        if (message != null && this.getCause() != null) {
            var causeMessage = Objects.requireNonNull(this.getCause()).getLocalizedMessage();
            if (causeMessage != null && !(message.endsWith(causeMessage))) {
                // Sometimes the cause is already embedded in the message at throw site. If it isn't though... let's add
                // it
                message += MessageFormat.format(". Caused by: {0}", causeMessage);
            }
        }
        return Objects.requireNonNullElse(message, this.getClass().getSimpleName());
    }
}
