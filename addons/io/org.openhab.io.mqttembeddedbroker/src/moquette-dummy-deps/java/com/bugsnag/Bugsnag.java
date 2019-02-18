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
package com.bugsnag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//Dummy to make moquette happy. Will not be used
public class Bugsnag {
    private final Logger logger = LoggerFactory.getLogger(Bugsnag.class);

    public Bugsnag(String a) {

    }

    public void notify(Throwable t) {
        logger.warn("MQTT Broker crashed", t);
    }
}
