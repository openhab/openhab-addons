/*
 * Copyright (c) 2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.http.model;

import org.openhab.binding.http.HttpBindingConstants;

import java.time.Duration;

/**
 * A class describing configuration for the HTTP binding..
 *
 * @author Brian J. Tarricone
 */
public class HttpHandlerFactoryConfig {
    private long connectTimeout = HttpBindingConstants.DEFAULT_CONNECT_TIMEOUT.toMillis();
    private long requestTimeout = HttpBindingConstants.DEFAULT_REQUEST_TIMEOUT.toMillis();

    public Duration getConnectTimeout() {
        return Duration.ofMillis(this.connectTimeout);
    }

    public Duration getRequestTimeout() {
        return Duration.ofMillis(this.requestTimeout);
    }
}
