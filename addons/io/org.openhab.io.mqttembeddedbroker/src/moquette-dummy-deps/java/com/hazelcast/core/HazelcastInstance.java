/**
 * Copyright (c) 2014,2019 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package com.hazelcast.core;

//Dummy to make moquette happy. Will not be used
public class HazelcastInstance<V> {
    public void shutdown() throws HazelcastInstanceNotActiveException {

    }

    public String getName() {
        return "";
    }

    public ITopic<V> getTopic(String topic) {
        return new ITopic<V>() {
            @Override
            public void publish(V a) {

            }

            @Override
            public void addMessageListener(Object o) {

            }
        };
    }

    public interface Cluster {
        Object getLocalMember();
    }

    public Cluster getCluster() {
        return null;
    }
}
