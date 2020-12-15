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
package org.openhab.binding.http.internal.config;

import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpMethod;

/**
 * The {@link HttpThingConfig} class contains fields mapping thing configuration parameters.
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class HttpThingConfig {
    public String baseURL = "";
    public int refresh = 30;
    public int timeout = 3000;

    public String username = "";
    public String password = "";

    public HttpAuthMode authMode = HttpAuthMode.BASIC;
    public HttpMethod commandMethod = HttpMethod.GET;
    public int bufferSize = 2048;

    public @Nullable String encoding = null;
    public @Nullable String contentType = null;

    public boolean ignoreSSLErrors = false;

    public List<String> headers = Collections.emptyList();
}
