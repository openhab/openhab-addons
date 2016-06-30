/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homematic.handler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.smarthome.core.common.ThreadPoolManager;
import org.openhab.binding.homematic.internal.common.ThreadPoolFactory;

/**
 * OpenHab implementation of the Homematic ThreadPoolFactory.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class OpenHabThreadPoolFactory implements ThreadPoolFactory {

    /**
     * {@inheritDoc}
     */
    @Override
    public ScheduledExecutorService getScheduledPool(String poolName) {
        return ThreadPoolManager.getScheduledPool(poolName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExecutorService getPool(String poolName) {
        return ThreadPoolManager.getPool(poolName);
    }

}
