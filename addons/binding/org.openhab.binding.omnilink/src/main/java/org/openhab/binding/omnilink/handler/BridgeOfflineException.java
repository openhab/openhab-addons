/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.omnilink.handler;

/**
 *
 * @author Craig Hamilton
 *
 */
public class BridgeOfflineException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = -9081729691518514097L;

    public BridgeOfflineException(Exception e) {
        super(e);
    }

}
