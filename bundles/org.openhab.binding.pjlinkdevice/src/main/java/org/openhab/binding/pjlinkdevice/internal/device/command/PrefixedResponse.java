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

/**
 * @author Nils Schnabel - Initial contribution
 */
public abstract class PrefixedResponse implements Response {
    protected String prefix;
    protected Set<ErrorCode> specifiedErrors;

    public PrefixedResponse(String prefix) {
        this(prefix, null);
    }

    public PrefixedResponse(String prefix, Set<ErrorCode> specifiedErrors) {
        this.prefix = prefix;
        this.specifiedErrors = specifiedErrors;
    }

    @Override
    public void parse(String response) throws ResponseException {
        String fullPrefix = "%1" + this.prefix;
        if (!response.toUpperCase().startsWith(fullPrefix)) {
            throw new ResponseException(
                    MessageFormat.format("Expected prefix ''{0}'', instead got ''{1}''", fullPrefix, response));
        }
        String result = response.substring(fullPrefix.length());
        ErrorCode.checkForErrorStatus(result, this.specifiedErrors);
        parse0(result);
    }

    protected abstract void parse0(String responseWithoutPrefix) throws ResponseException;
}
