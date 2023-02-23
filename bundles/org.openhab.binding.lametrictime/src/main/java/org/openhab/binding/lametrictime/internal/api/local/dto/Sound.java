/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.lametrictime.internal.api.local.dto;

/**
 * Pojo for sound.
 *
 * @author Gregory Moyer - Initial contribution
 */
public class Sound {
    private String category;
    private String id;
    private Integer repeat;

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Sound withCategory(String category) {
        this.category = category;
        return this;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Sound withId(String id) {
        this.id = id;
        return this;
    }

    public Integer getRepeat() {
        return repeat;
    }

    public void setRepeat(Integer repeat) {
        this.repeat = repeat;
    }

    public Sound withRepeat(Integer repeat) {
        this.repeat = repeat;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Sound [category=");
        builder.append(category);
        builder.append(", id=");
        builder.append(id);
        builder.append(", repeat=");
        builder.append(repeat);
        builder.append("]");
        return builder.toString();
    }
}
