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
package org.openhab.binding.regoheatpump.internal.rego6xx;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link ResponseParserFactory} is responsible for providing parsers for all known data
 * forms coming from the rego 6xx unit.
 *
 * @author Boris Krivonog - Initial contribution
 */
@NonNullByDefault
public class ResponseParserFactory {
    public static final ResponseParser<Short> SHORT = new ShortResponseParser();
    public static final ResponseParser<String> STRING = new StringResponseParser();
    public static final ResponseParser<ErrorLine> ERROR_LINE = new ErrorLineResponseParser();
    public static final ResponseParser<String> WRITE = new WriteResponse();
}
