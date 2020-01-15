/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.tibber.internal.handler;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * The {@link TibberHomeidHandler} class contains fields mapping home id parameters.
 *
 * @author Stian Kjoglum - Initial contribution
 */
public class TibberHomeidHandler {
    public String query = "{\"query\": \"{viewer {homes {id }}}\"}";
    
    public InputStream getInputStream() {
        InputStream myInputStream = new ByteArrayInputStream(query.getBytes(Charset.forName("UTF-8")));
        return myInputStream;
    }
}
