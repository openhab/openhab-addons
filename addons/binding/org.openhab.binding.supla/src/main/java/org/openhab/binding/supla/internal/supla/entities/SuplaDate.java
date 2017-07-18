/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.supla.internal.supla.entities;

public final class SuplaDate {
    private final long date;
    private final String ipV4;

    public SuplaDate(long date, String ipV4) {
        this.date = date;
        this.ipV4 = ipV4;
    }

    public long getDate() {
        return date;
    }

    public String getIpV4() {
        return ipV4;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SuplaDate)) return false;

        SuplaDate suplaDate = (SuplaDate) o;

        if (date != suplaDate.date) return false;
        return ipV4 != null ? ipV4.equals(suplaDate.ipV4) : suplaDate.ipV4 == null;
    }

    @Override
    public int hashCode() {
        return (int) (date ^ (date >>> 32));
    }

    @Override
    public String toString() {
        return "SuplaDate{" +
                "date=" + date +
                ", ipV4='" + ipV4 + '\'' +
                '}';
    }
}
