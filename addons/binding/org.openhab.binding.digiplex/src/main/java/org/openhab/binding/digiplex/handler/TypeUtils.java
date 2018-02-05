/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.digiplex.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.OpenClosedType;

/**
 * Utility classes for type conversions
 *
 * @author Robert Michalak - Initial contribution
 *
 */
@NonNullByDefault
public class TypeUtils {

    public static OpenClosedType openClosedFromBoolean(boolean value) {
        return value ? OpenClosedType.OPEN : OpenClosedType.CLOSED;
    }

}
