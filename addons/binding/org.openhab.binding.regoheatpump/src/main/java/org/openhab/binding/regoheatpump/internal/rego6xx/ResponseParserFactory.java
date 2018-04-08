/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.regoheatpump.internal.rego6xx;

/**
 * The {@link ResponseParserFactory} is responsible for providing parsers for all known data
 * forms coming from the rego 6xx unit.
 *
 * @author Boris Krivonog - Initial contribution
 */
public class ResponseParserFactory {
    public static final ResponseParser<Short> SHORT = new ShortResponseParser();
    public static final ResponseParser<String> STRING = new StringResponseParser();
    public static final ResponseParser<ErrorLine> ERROR_LINE = new ErrorLineResponseParser();
}
