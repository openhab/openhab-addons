/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
