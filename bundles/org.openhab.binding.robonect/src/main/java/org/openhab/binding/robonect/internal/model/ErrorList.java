/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.robonect.internal.model;

import java.util.List;

/**
 * Simple POJO for deserialize the list of errors from the errors command.
 * 
 * @author Marco Meyer - Initial contribution
 */
public class ErrorList extends RobonectAnswer {

    private List<ErrorEntry> errors;

    /**
     * @return - the list of errors.
     */
    public List<ErrorEntry> getErrors() {
        return errors;
    }
}
