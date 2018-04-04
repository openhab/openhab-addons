/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
