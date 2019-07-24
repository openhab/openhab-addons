/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Nils Schnabel - Initial contribution
 */
@NonNullByDefault
public abstract class PrefixedResponse<ResponseType> implements Response<ResponseType> {
    protected String prefix;
    @Nullable
    protected Set<ErrorCode> specifiedErrors;
    ResponseType result;

    public PrefixedResponse(String prefix, String response) throws ResponseException {
      this(prefix, null, response);
    }

    public PrefixedResponse(String prefix, @Nullable Set<ErrorCode> specifiedErrors, String response) throws ResponseException {
        this.prefix = prefix;
        this.specifiedErrors = specifiedErrors;
        this.result = this.parse(response);
    }

    public ResponseType getResult() {
        return this.result;
    }

    @Override
    public ResponseType parse(String response) throws ResponseException {
        String fullPrefix = "%1" + this.prefix;
        if (!response.toUpperCase().startsWith(fullPrefix)) {
            throw new ResponseException(
                    MessageFormat.format("Expected prefix ''{0}'', instead got ''{1}''", fullPrefix, response));
        }
        String result = response.substring(fullPrefix.length());
        ErrorCode.checkForErrorStatus(result, this.specifiedErrors);
        return parse0(result);
    }

    protected abstract ResponseType parse0(String responseWithoutPrefix) throws ResponseException;
}
