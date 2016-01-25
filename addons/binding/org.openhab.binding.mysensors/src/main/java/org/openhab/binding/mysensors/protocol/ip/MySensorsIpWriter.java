package org.openhab.binding.mysensors.protocol.ip;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import org.openhab.binding.mysensors.handler.MySensorsStatusUpdateEvent;
import org.openhab.binding.mysensors.protocol.MySensorsWriter;

public class MySensorsIpWriter extends MySensorsWriter {
	
	private Socket sock = null;
	
	public MySensorsIpWriter(Socket sock, MySensorsIpConnection mysCon, int sendDelay){
		this.mysCon = mysCon;
		this.sock = sock;
		this.sendDelay = sendDelay;
		try {
			outs = new DataOutputStream(sock.getOutputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
