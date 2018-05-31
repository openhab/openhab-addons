/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.icloud.internal.json.response;

/**
 * Serializable class to parse json response received from the Apple server.
 *
 * @author Patrik Gfeller - Initial Contribution
 *
 */
public class ICloudServerContextTimezone {
    private int currentOffset;

    private int previousOffset;

    private long previousTransition;

    private String tzCurrentName;

    private String tzName;

    public int getCurrentOffset() {
        return this.currentOffset;
    }

    public int getPreviousOffset() {
        return this.previousOffset;
    }

    public long getPreviousTransition() {
        return this.previousTransition;
    }

    public String getTzCurrentName() {
        return this.tzCurrentName;
    }

    public String getTzName() {
        return this.tzName;
    }
}
