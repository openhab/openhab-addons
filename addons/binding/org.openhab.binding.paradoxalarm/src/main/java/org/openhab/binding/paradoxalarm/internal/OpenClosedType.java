/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.paradoxalarm.internal;

import org.eclipse.smarthome.core.types.State;

/**
 * The {@link OpenClosedType} Used to present Opened/Closed state of zones.
 *
 * @author Konstantin_Polihronov - Initial contribution
 */
public enum OpenClosedType implements State {

    OPEN,
    CLOSED;

    public static OpenClosedType from(boolean arg) {
        return arg ? OPEN : CLOSED;
    }

    @Override
    public String format(String pattern) {
        return String.format(pattern, this.toFullString());
    }

    @Override
    public String toFullString() {
        return super.toString();
    }

}
