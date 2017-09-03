/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.icloud.internal.json.iCloud;

public class Timezone {
    private int currentOffset;

    public int getCurrentOffset() {
        return this.currentOffset;
    }

    public void setCurrentOffset(int currentOffset) {
        this.currentOffset = currentOffset;
    }

    private long previousTransition;

    public long getPreviousTransition() {
        return this.previousTransition;
    }

    public void setPreviousTransition(long previousTransition) {
        this.previousTransition = previousTransition;
    }

    private int previousOffset;

    public int getPreviousOffset() {
        return this.previousOffset;
    }

    public void setPreviousOffset(int previousOffset) {
        this.previousOffset = previousOffset;
    }

    private String tzCurrentName;

    public String getTzCurrentName() {
        return this.tzCurrentName;
    }

    public void setTzCurrentName(String tzCurrentName) {
        this.tzCurrentName = tzCurrentName;
    }

    private String tzName;

    public String getTzName() {
        return this.tzName;
    }

    public void setTzName(String tzName) {
        this.tzName = tzName;
    }
}
