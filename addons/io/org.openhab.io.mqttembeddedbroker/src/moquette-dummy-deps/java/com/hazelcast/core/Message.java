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
package com.hazelcast.core;

//Dummy to make moquette happy. Will not be used
public class Message<V> {
    public V getMessageObject() {
        return null;
    }

    public Object getPublishingMember() {
        return null;
    }
}
