/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.gruenbecksoftener.data;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The POJO which holds all data for editing a value from the Softener device.
 *
 * @author Matthias Steigenberger - Initial contribution
 *
 */
@NonNullByDefault
public class SoftenerEditData {

    private @NonNullByDefault({}) String datapointId;
    private @NonNullByDefault({}) String value;
    private @NonNullByDefault({}) String code;

    public String getDatapointId() {
        return datapointId;
    }

    public void setDatapointId(String datapointId) {
        this.datapointId = datapointId;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getCode() {
        if (code == null) {
            return "";
        }
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return "SoftenerEditData [datapointId=" + datapointId + ", value=" + value + ", code=" + code + "]";
    }

}
