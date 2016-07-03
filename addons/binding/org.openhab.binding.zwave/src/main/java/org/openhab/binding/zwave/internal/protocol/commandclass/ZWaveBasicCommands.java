/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.internal.protocol.commandclass;

/**
 * Interface to implement for all command classes that implement the basic commands like GET/SET value.
 * @author Chris Jackson
 * @author Jan-Willem Spuij
 */
public interface ZWaveBasicCommands extends ZWaveGetCommands, ZWaveSetCommands {
}
