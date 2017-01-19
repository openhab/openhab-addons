/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.enigma2.internal;

/**
 * The {@link Enigma2CommandExecutorListener} is an interface, to listen to the
 * {@link Enigma2CommandExecutor}
 *
 * @author Thomas Traunbauer - Initial contribution
 */
public interface Enigma2CommandExecutorListener {

    void getUpdate();

}
