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
package com.librato.metrics.reporter;

import java.util.concurrent.TimeUnit;

//Dummy to make moquette happy. Will not be used
public class Librato {
    public static Librato reporter(Object o1, String a, String b) {
        return new Librato();
    }

    public Librato setSource(String a) {
        return this;
    }

    public void start(long time, TimeUnit unit) {
    }
}
