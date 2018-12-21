/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.veluxklf200.internal.utility;

/**
 * Certain commands that are sent to the KLF200 unit require a session
 * identifier so that responses can be attributed to that particular session.
 * This is a helper singleton that provides unique session identifiers.
 *
 * @author MFK - Initial Contribution
 */
public class KLFSession {

    /** The instance. */
    private static KLFSession instance;

    /** Mutex to ensure thread safety */
    private static Object mutex = new Object();

    /** The session. */
    private short session;

    /**
     * Instantiates a new KLF session.
     */
    private KLFSession() {
        this.session = 1;
    }

    /**
     * Gets the session identifier.
     *
     * @return the session identifier
     */
    public short getSessionIdentifier() {
        return this.session++;
    }

    /**
     * Gets the single instance of KLFSession.
     *
     * @return single instance of KLFSession
     */
    public static KLFSession getInstance() {
        KLFSession result = instance;
        if (null == result) {
            synchronized (mutex) {
                result = instance;
                if (null == result) {
                    instance = result = new KLFSession();
                }
            }
        }
        return result;
    }
}
