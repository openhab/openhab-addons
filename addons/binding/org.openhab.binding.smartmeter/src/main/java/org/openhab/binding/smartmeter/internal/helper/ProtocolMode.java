/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.smartmeter.internal.helper;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 *
 * @author Matthias Steigenberger - Initial contribution
 *
 */
@NonNullByDefault
public enum ProtocolMode {

    ABC("A,B,C"),
    D("D"),
    SML("SML");

    private String label;

    private <T> ProtocolMode(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
