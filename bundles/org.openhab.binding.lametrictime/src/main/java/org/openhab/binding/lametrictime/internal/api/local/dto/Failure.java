/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.lametrictime.internal.api.local.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Pojo for failure.
 *
 * @author Gregory Moyer - Initial contribution
 */
public class Failure {
    private List<Error> errors = new ArrayList<Error>();

    public List<Error> getErrors() {
        return errors;
    }

    public void setErrors(List<Error> errors) {
        this.errors = errors;
    }

    public Failure withErrors(List<Error> errors) {
        this.errors = errors;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Failure [errors=");
        builder.append(errors);
        builder.append("]");
        return builder.toString();
    }
}
