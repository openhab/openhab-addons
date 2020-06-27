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
package org.openhab.binding.warmup.internal.model.update;

import java.util.Arrays;
import java.util.List;

/**
 * @author James Melville - Initial contribution
 */
@SuppressWarnings("unused")
public class ModeDTO {

    private String method;
    private List<Integer> rooms;
    private Integer type;
    private Integer temp;
    private String until;

    public ModeDTO(String method, Integer roomId, Integer type, Integer temperature, String until) {
        setMethod(method);
        setRooms(Arrays.asList(roomId));
        setType(type);
        setTemp(temperature);
        setUntil(until);
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public void setRooms(List<Integer> rooms) {
        this.rooms = rooms;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public void setTemp(Integer temp) {
        this.temp = temp;
    }

    public void setUntil(String until) {
        this.until = until;
    }
}
