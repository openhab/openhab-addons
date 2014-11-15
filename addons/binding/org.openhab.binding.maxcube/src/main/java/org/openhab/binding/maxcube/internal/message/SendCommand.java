/**
 * Copyright (c) 2010-2014, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.maxcube.internal.message;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.types.Command;


/**
 * Class for sending a command. 
 * 
 * @author Marcel Verpaalen 
 * 
 */
public final class SendCommand {

	private int id ;
	private static int commandId = -1;

	private ChannelUID channelUID;
	private Command command;
	private String serialNumber;
	private String key;


	public SendCommand(String serialNumber,ChannelUID channelUID,Command command) {
		commandId +=1;
		id = commandId;
		this.serialNumber = serialNumber;
		this.channelUID=channelUID;
		this.command=command;
		setKey();
	}

	/**
	 * Sets the key based on the serial and channel
	 * This is can be used to find duplicated commands in the queue
	 */
	private  void setKey() {
		key = serialNumber+"-"+channelUID.getId() ;
	}

	/**
	 * @return the key based on the serial and channel
	 * This is can be used to find duplicated commands in the queue
	 */
	public String getKey() {
		return key ;
	}


	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}





	/**
	 * @return the channelUID
	 */
	public ChannelUID getChannelUID() {
		return channelUID;
	}



	/**
	 * @param channelUID the channelUID to set
	 */
	public void setChannelUID(ChannelUID channelUID) {
		this.channelUID = channelUID;
		setKey();
	}



	/**
	 * @return the command
	 */
	public Command getCommand() {
		return command;
	}



	/**
	 * @param command the command to set
	 */
	public void setCommand(Command command) {
		this.command = command;
	}



	/**
	 * @return the device
	 */
	public String getDeviceSerial() {
		return serialNumber;
	}



	/**
	 * @param device the device to set
	 */
	public void setDeviceSerial(String device) {
		this.serialNumber = device;
		setKey();
	}


}