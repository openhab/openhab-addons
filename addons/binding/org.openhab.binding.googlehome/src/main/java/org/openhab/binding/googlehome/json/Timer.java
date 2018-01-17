/**
 * Copyright (c) 2014,2018 by the respective copyright holders.
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
package org.openhab.binding.googlehome.json;

/**
 *
 * @author Kuba Wolanin - Initial contribution
 */
public class Timer {
    private long fireTime;
    private String id;
    private long originalDuration;
    private long status;

    public long getFireTime() {
        return fireTime;
    }

    public String getId() {
        return id;
    }

    public long getOriginalDuration() {
        return originalDuration;
    }

    public long getStatus() {
        return status;
    }
}
