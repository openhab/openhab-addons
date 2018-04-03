/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homematic.internal.communicator.parser;

import java.io.IOException;

/**
 * Interface for all message parsers.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public interface RpcParser<M, R> {

    /**
     * Parses the message returns the result.
     */
    public R parse(M message) throws IOException;

}
