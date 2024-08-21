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
package org.openhab.binding.remoteopenhab.internal.exceptions;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.i18n.AbstractI18nException;

/**
 * Exceptions thrown by this binding.
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings("serial")
public class RemoteopenhabException extends AbstractI18nException {

    public RemoteopenhabException(Throwable cause) {
        super(cause);
    }

    public RemoteopenhabException(String message, @Nullable Object @Nullable... msgParams) {
        super(message, msgParams);
    }

    public RemoteopenhabException(String message, Throwable cause, @Nullable Object @Nullable... msgParams) {
        super(message, cause, msgParams);
    }
}
