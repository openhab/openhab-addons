/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.sleepiq.internal.api.dto;

/**
 * The {@link FoundationPosition} holds the head or foot position of a bed side.
 *
 * @author Mark Hilbush - Initial contribution
 */
public class FoundationPosition {
    private Integer foundationPosition;

    public Integer getFoundationPosition() {
        return foundationPosition;
    }

    public void setFoundationPosition(Integer foundationPosition) {
        this.foundationPosition = foundationPosition;
    }

    public FoundationPosition withFoundationPosition(Integer foundationPosition) {
        setFoundationPosition(foundationPosition);
        return this;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("FoundationPosition [foundationPosition=");
        builder.append(foundationPosition);
        builder.append("]");
        return builder.toString();
    }
}
