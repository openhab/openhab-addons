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
package org.openhab.binding.nikohomecontrol.internal.protocol.nhc1;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Class {@link NhcMessageList1} used as output from gson for cmd or event feedback from Niko Home Control where the
 * data part is enclosed by [] and contains a list. Extends {@link NhcMessageBase1}.
 * <p>
 * Example: <code>{"cmd":"getenergydata","data":[   0,   0,   12,   18,   7]}</code>
 *
 * @author Mark Herwege - Initial Contribution
 */
@NonNullByDefault
class NhcMessageList1 extends NhcMessageBase1 {

    private List<String> data = new ArrayList<>();

    List<String> getData() {
        return data;
    }

    void setData(List<String> data) {
        this.data = data;
    }
}
