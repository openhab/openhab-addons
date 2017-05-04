/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nikohomecontrol.internal.protocol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Class {@link NHCMessageListMap} used as output from gson for cmd or event feedback from Niko Home Control where the
 * data part is enclosed by [] and contains a list of json strings. Extends {@link NHCBaseMessage}.
 * <p>
 * Example: <code>{"cmd":"listactions","data":[{"id":1,"name":"Garage","type":1,"location":1,"value1":0},
 * {"id":25,"name":"Frontdoor","type":2,"location":2,"value1":0}]}</code>
 *
 * @author Mark Herwege
 */
public class NHCMessageListMap extends NHCBaseMessage {

    private List<HashMap<String, String>> data = new ArrayList<>();

    public List<HashMap<String, String>> getData() {
        return this.data;
    }

    public void setData(List<HashMap<String, String>> data) {
        this.data = data;
    }

}
