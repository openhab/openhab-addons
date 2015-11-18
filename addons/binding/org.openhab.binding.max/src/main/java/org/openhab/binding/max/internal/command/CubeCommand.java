/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.max.internal.command;

/**
 * {@link CubeCommand} is the base class for commands to be send to the MAX! Cube.
 * 
 * @author Marcel Verpaalen - Initial contribution
 */
public abstract class CubeCommand {

	/**
	 * @return the String to be send to the MAX! Cube
	 */
	public abstract String getCommandString();

	/**
	 * @return the String expected to be received from the Cube to signify the
	 *         end of the message
	 */
	public abstract String getReturnStrings();

}
