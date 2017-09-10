/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nest.internal;

import java.util.Map;

/**
 * Contains the data needed to do an update request back to nest.
 *
 * @author David Bennett - Initial Contribution
 */
public class NestUpdateRequest {
    public String getUpdateUrl() {
        return updateUrl;
    }

    public void setUpdateUrl(String updateUrl) {
        this.updateUrl = updateUrl;
    }

    public Map<String, Object> getValues() {
        return values;
    }

    public void addValue(String key, Object value) {
        values.put(key, value);
    }

    private String updateUrl;
    private Map<String, Object> values;
}
