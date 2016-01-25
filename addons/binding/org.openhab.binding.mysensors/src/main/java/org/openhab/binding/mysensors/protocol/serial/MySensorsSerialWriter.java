package org.openhab.binding.mysensors.protocol.serial;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Date;

import org.openhab.binding.mysensors.internal.MySensorsMessageParser;
import org.openhab.binding.mysensors.protocol.MySensorsWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.io.NRSerialPort;

public class MySensorsSerialWriter extends MySensorsWriter {
	
	private NRSerialPort serialConnection = null;
	
	public MySensorsSerialWriter(NRSerialPort serialConnection, MySensorsSerialConnection mysCon, int sendDelay){
		this.mysCon = mysCon;
		this.serialConnection = serialConnection;
		this.sendDelay = sendDelay;
		
		outs = new DataOutputStream(serialConnection.getOutputStream());
	}
}
