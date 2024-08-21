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
package org.openhab.binding.mielecloud.internal.webservice.api.json;

import java.util.Objects;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * Immutable POJO representing an error message. Queried from the Miele REST API.
 *
 * @author Björn Lange - Initial contribution
 */
@NonNullByDefault
public class ErrorMessage {
    @Nullable
    private String message;

    /**
     * Creates a new {@link ErrorMessage} from the given Json text.
     *
     * @param json The Json text.
     * @return The created {@link ErrorMessage}.
     * @throws MieleSyntaxException if parsing the data from {@code json} fails.
     */
    public static ErrorMessage fromJson(String json) {
        try {
            ErrorMessage errorMessage = new Gson().fromJson(json, ErrorMessage.class);
            if (errorMessage == null) {
                throw new MieleSyntaxException("Failed to parse Json.");
            }
            return errorMessage;
        } catch (JsonSyntaxException e) {
            throw new MieleSyntaxException("Failed to parse Json.", e);
        }
    }

    public Optional<String> getMessage() {
        return Optional.ofNullable(message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(message);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ErrorMessage other = (ErrorMessage) obj;
        return Objects.equals(message, other.message);
    }
}
