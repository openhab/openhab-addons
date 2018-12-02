/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.paradoxalarm.internal.communication;

/**
 * The {@link IParadoxCommunicator} is representing the functionality of communication implementation.
 * If another Paradox alarm system is used this interface must be implemented.
 *
 * @author Konstantin_Polihronov - Initial contribution
 */
public enum IP150Command {
    LOGOUT,
    LOGIN,
    RESET,
    UNIMPLEMENTED
}
