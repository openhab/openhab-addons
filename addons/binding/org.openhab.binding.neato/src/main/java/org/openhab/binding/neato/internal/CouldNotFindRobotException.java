/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.neato.internal;

/**
 * The {@link CouldNotFindRobotException} is the internal excepton class for the case when robot could not be found.
 *
 * @author Patrik Wimnell - Initial contribution
 */
public class CouldNotFindRobotException extends Exception {

    private static final long serialVersionUID = 1L;

    public CouldNotFindRobotException(String message) {
        super(message);
    }
}
