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
public class Alarm {
    private DatePattern datePattern;
    private TimePattern timePattern;
    private double fireTime;
    private String id;
    private long status;

    public DatePattern getDatePattern() {
        return datePattern;
    }

    public TimePattern getTimePattern() {
        return timePattern;
    }

    public double getFireTime() {
        return fireTime;
    }

    public String getId() {
        return id;
    }

    public long getStatus() {
        return status;
    }
}
