/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.canrelay.internal.protocol;

/**
 * Helper interface in order to be able to use lambdas easily to run logic acquiring a lock
 *
 * @author Lubos Housa - Initial contribution
 */
public interface RunnableInterruptible {

    /**
     * Run some custom logic
     *
     * @throws InterruptedException if the logic was interrupted
     */
    void run() throws InterruptedException;
}
