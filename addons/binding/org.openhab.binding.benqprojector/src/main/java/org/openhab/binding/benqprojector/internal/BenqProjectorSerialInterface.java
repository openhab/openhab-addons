/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.benqprojector.internal;

import java.io.IOException;

public interface BenqProjectorSerialInterface {

    public static class Response {
        public final boolean success;
        public final String value;
        public final String error;

        public Response(boolean success, String valueOrError) {
            this.success = success;
            if (success) {
                value = valueOrError;
                error = "";
            } else {
                value = "";
                error = valueOrError;
            }
        }
    }

    Response get(String key) throws IOException;

    Response put(String key, String value) throws IOException;

    boolean check();

    boolean reset();

    void close();
}
