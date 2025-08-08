/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.sungrow.internal.impl;

import org.openhab.binding.sungrow.internal.SungrowConfiguration;

/**
 * Simple Client for accessing iSolarCloud API. Should be replaced by sungrow-api-client from github
 *
 * @author Christian Kemper - Initial contribution
 */
public class ApiClient {

    private SungrowConfiguration configuration;

    ApiClient(SungrowConfiguration configuration) {
        this.configuration = configuration;
    }

    void initialize() {
        System.out.println(configuration.getAppKey());
    }
}
