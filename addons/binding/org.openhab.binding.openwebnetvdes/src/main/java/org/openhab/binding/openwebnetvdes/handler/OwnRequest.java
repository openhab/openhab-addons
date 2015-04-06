package org.openhab.binding.openwebnetvdes.handler;

import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.types.Command;

public class OwnRequest {

	private int id ;
	private static AtomicInteger commandId = new AtomicInteger(-1);

	private ChannelUID channelUID;
	private Command command;
	private int whereAddress;
	private String key;


	public OwnRequest(int whereAddress,ChannelUID channelUID,Command command) {
		id = commandId.addAndGet(1);		
		this.whereAddress = whereAddress;
		this.channelUID=channelUID;
		this.command=command;
		setKey();
	}

	/**
	 * Sets the key based on the serial and channel
	 * This is can be used to find duplicated commands in the queue
	 */
	private  void setKey() {
		key = whereAddress+"-"+channelUID.getId() ;
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

	public int getWhereAddress() {
		return whereAddress;
	}

	public void setWhereAddress(int whereAddress) {
		this.whereAddress = whereAddress;
	}

}
