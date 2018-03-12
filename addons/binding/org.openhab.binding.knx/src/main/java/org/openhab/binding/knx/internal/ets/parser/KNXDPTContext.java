/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.knx.internal.ets.parser;

/**
 * The {@link KNXDPTContext} is a helper class to store a DPT in relation to a choosen context
 *
 * @author Karel Goderis - Initial contribution
 *
 */
public class KNXDPTContext {

    private String dpt;
    private String context;

    public KNXDPTContext(String dpt, String context) {
        this.dpt = dpt;
        this.context = context;
    }

    public String getDpt() {
        return dpt;
    }

    public String getContext() {
        return context;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        KNXDPTContext other = (KNXDPTContext) obj;
        if (dpt == null && context != null) {
            if (other.dpt != null || !context.equals(other.context)) {
                return false;
            }
        } else if (context == null && dpt != null) {
            if (other.context != null || !dpt.equals(other.dpt)) {
                return false;
            }
        } else if (context == null && dpt == null) {
            if (other.context != null || other.dpt != null) {
                return false;
            }
        } else if (!dpt.equals(other.dpt) || !context.equals(other.context)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((dpt == null) ? 0 : dpt.hashCode()) + ((context == null) ? 0 : context.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "[" + dpt + "," + context + "]";
    }
}
