/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homematic.internal.common;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Factory to get thread pools from the core runtime.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public interface ThreadPoolFactory {

    /**
     * Returns an instance of a scheduled thread pool service.
     */
    public ScheduledExecutorService getScheduledPool(String poolName);

    /**
     * Returns an instance of a thread pool service.
     */
    public ExecutorService getPool(String poolName);

}
