/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.gardena.internal.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Represents a List of Gardena errors.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class Errors {

    private List<Error> errors = new ArrayList<>();

    /**
     * Returns a list of Gardena errors.
     */
    public List<Error> getErrors() {
        return errors;
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        for (Error error : errors) {
            tsb.append(error);
        }
        return tsb.toString();
    }
}
