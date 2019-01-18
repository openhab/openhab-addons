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
package org.eclipse.smarthome.binding.sonos.internal;

/**
 * The {@link SonosMusicService} is a datastructure to describe a Sonos music service
 *
 * @author Laurent Garnier - Initial contribution
 */
public class SonosMusicService {

    private String id;
    private String name;
    private Integer type;

    public SonosMusicService(String id, String name, Integer type) {
        this.id = id;
        this.name = name;
        this.type = type;
    }

    public SonosMusicService(String id, String name) {
        this(id, name, null);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

}
