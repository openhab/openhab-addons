/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.transport.upnp;

/**
 * The {@link UpnpIOParticipant} is an interface that needs to 
 * be implemented by classes that wants to participate in
 * UPNP communication
 * 
 * @author Karel Goderis - Initial contribution
 */
public interface UpnpIOParticipant {
	
	/** Get the UDN of the participant **/
	public String getUDN();

	/** Called when the UPNP IO service receives a {variable,value} tuple for the given UPNP service**/
	public void onValueReceived(String variable, String value, String service);
		
}
