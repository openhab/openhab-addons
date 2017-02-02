/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.blueiris.internal.data;

import com.google.gson.annotations.Expose;

/**
 * Reply to the sysconfig command to the blue iris system.
 *
 * @author David Bennett - Initial Contribution
 */
public class SysConfigReply {
    @Expose
    private String result;
    @Expose
    private Data data;

    public String getResult() {
        return result;
    }

    public Data getData() {
        return data;
    }

    public class Data {
        @Expose
        private boolean archive;
        @Expose
        private boolean schedule;

        public boolean isArchive() {
            return archive;
        }

        public boolean isSchedule() {
            return schedule;
        }
    }
}
