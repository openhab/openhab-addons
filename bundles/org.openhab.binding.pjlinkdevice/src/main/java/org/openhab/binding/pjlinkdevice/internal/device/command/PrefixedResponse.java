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
package org.openhab.binding.pjlinkdevice.internal.device.command;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Base class for most responses that can be retrieved from the device.
 *
 * A prefix has to be passed in the constructor for which is checked.
 *
 * Subclasses have to implement parseResponseWithoutPrefix, which allows parsing without having to remove the prefix
 * first.
 *
 * @author Nils Schnabel - Initial contribution
 */
@NonNullByDefault
public abstract class PrefixedResponse<ResponseType> implements Response<ResponseType> {
    private String prefix;
    private @Nullable Set<ErrorCode> specifiedErrors;
    private ResponseType result;

    public PrefixedResponse(String prefix, String response) throws ResponseException {
        this(prefix, null, response);
    }

    public PrefixedResponse(String prefix, @Nullable Set<ErrorCode> specifiedErrors, String response)
            throws ResponseException {
        this.prefix = prefix;
        this.specifiedErrors = specifiedErrors;
        this.result = parse(response);
    }

    public ResponseType getResult() {
        return this.result;
    }

    @Override
    public ResponseType parse(String response) throws ResponseException {
        String fullPrefix = "%1" + this.prefix;
        if (!response.toUpperCase().startsWith(fullPrefix)) {
            throw new ResponseException(
                    MessageFormat.format("Expected prefix ''{0}'' ({1}), instead got ''{2}'' ({3})", fullPrefix,
                            Arrays.toString(fullPrefix.getBytes()), response, Arrays.toString(response.getBytes())));
        }
        String result = response.substring(fullPrefix.length());
        ErrorCode.checkForErrorStatus(result, this.specifiedErrors);
        return parseResponseWithoutPrefix(result);
    }

    protected abstract ResponseType parseResponseWithoutPrefix(String responseWithoutPrefix) throws ResponseException;
}
