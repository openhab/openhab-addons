/**
 * Copyright (c) 2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.samsungtv.internal.service.api;

import org.eclipse.smarthome.core.types.State;

/**
 * Interface for receiving data from Samsung TV services.
 * 
 * @author Pauli Anttila - Initial contribution
 */
public interface ValueReceiver {
	/**
	 * Invoked when value is received from the TV.
	 *  
	 * @param variable Name of the variable.
	 * @param value	Value of the variable value.
	 */
	public void valueReceived(String variable, State value);
}
