/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.knx.internal.handler;

/**
 * @author Karel Goderis - Initial contribution
 */
public enum LoadState {
    L0(0, "Unloaded"),
    L1(1, "Loaded"),
    L2(2, "Loading"),
    L3(3, "Error"),
    L4(4, "Unloading"),
    L5(5, "Load Completing");

    private int code;
    private String name;

    private LoadState(int code, String name) {
        this.code = code;
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    public static String getName(int code) {
        for (LoadState c : LoadState.values()) {
            if (c.code == code) {
                return c.name;
            }
        }
        return null;
    }

};
