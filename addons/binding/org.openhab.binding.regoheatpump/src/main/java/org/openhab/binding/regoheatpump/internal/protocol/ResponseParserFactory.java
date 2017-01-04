/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.regoheatpump.internal.protocol;

public class ResponseParserFactory {
    public static final ResponseParser<Short> Short = new ShortResponseParser();
    public static final ResponseParser<String> String = new StringResponseParser();
    public static final ResponseParser<ErrorLine> ErrorLine = new ErrorLineResponseParser();
}
