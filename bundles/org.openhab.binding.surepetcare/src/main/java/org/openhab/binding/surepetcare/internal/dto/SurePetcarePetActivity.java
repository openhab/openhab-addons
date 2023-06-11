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
package org.openhab.binding.surepetcare.internal.dto;

import java.time.ZonedDateTime;

/**
 * The {@link SurePetcarePetActivity} is the Java class used to represent the
 * status of a pet. It's used to deserialize JSON API results.
 *
 * @author Rene Scherer - Initial contribution
 * @author Holger Eisold - Added pet feeder status
 */
public class SurePetcarePetActivity {

    public Long tagId;
    public Long deviceId;
    public Long userId;
    public Integer where;
    public ZonedDateTime since;

    public SurePetcarePetActivity() {
    }

    public SurePetcarePetActivity(Integer location, ZonedDateTime since) {
        this.where = location;
        this.since = since;
    }
}
