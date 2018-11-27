/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.gardena.internal.model;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Represents a Gardena error.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class Error {

    private String id;
    private String status;
    private String title;
    private String detail;

    /**
     * Returns the id of the error.
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the status of the error.
     */
    public String getStatus() {
        return status;
    }

    /**
     * Returns the title of the error.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Returns the detail of the error.
     */
    public String getDetail() {
        return detail;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("id", id).append("status", status)
                .append("title", title).append("detail", detail).toString();
    }

}
