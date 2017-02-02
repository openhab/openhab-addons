/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.blueiris.internal.data;

import java.util.List;

import com.google.gson.annotations.Expose;

/**
 * Alertlist data from blue iris.
 *
 * @author David Bennett - Initial COntribution
 *
 */
public class AlertListReply {
    private List<Data> alertlist;

    /** Get all the alert list data bits. */
    public List<Data> getAlertlist() {
        return alertlist;
    }

    /**
     * Contains the data about each individual alert.
     *
     * @author David Bennett - Initial Contribution
     */
    public static class Data {
        @Expose
        private String camera;
        @Expose
        private String jpeg;
        @Expose
        private String path;
        @Expose
        private Integer offset;
        @Expose
        private Integer flags;
        @Expose
        private Integer date;
        @Expose
        private Integer color;

        public String getCamera() {
            return camera;
        }

        public String getJpeg() {
            return jpeg;
        }

        public String getPath() {
            return path;
        }

        public Integer getOffset() {
            return offset;
        }

        public Integer getFlags() {
            return flags;
        }

        public Integer getDate() {
            return date;
        }

        public Integer getColor() {
            return color;
        }

    }
}
