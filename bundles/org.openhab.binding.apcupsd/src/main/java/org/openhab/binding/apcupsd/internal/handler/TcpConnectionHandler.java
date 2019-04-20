package org.openhab.binding.apcupsd.internal.handler;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TcpConnectionHandler {
	
	private final Logger logger = LoggerFactory.getLogger(TcpConnectionHandler.class);
	
	private String ip;
	private Integer port;
	private final Integer CONNECTION_TIMEOUT = 3000;
	private final Integer COMUNICATION_TIMEOUT = 2000;
	private final Integer RECONNECT_INTERVAL = 5000;
	
	private boolean isConnected = false;
	
	private Socket tcpSocket;
	
	private TcpConnectorThread tcpConnectorThread;
	private TcpConnectionListener tcpConnectionListener = null;
		
	public TcpConnectionHandler(String ip, Integer port) {
		this.ip = ip;
		this.port = port;
	}
	
	public void start() {
		logger.debug("Starting TcpConnectionHandler");
		reconnect();
	}
	
	public void stop() {
		logger.debug("Stopping TcpConnectionHandler");
		if (tcpConnectorThread != null) {
			tcpConnectorThread.interrupt();
			try {
				tcpConnectorThread.join();
			} catch (InterruptedException e) {
			}
			tcpConnectorThread = null;
		}
	}
	
	private void reconnect() {
		logger.debug("Establishing TcpConnection...");
		tcpConnectorThread = new TcpConnectorThread();
		tcpConnectorThread.start();
	}
	
	private class TcpConnectorThread extends Thread {
		@Override
    	public void run() {
			isConnected = false;
			if (tcpConnectionListener != null)
   				tcpConnectionListener.onConnectionStatusChange(isConnected);
			try {
				while(!connect()) {
					Thread.sleep(RECONNECT_INTERVAL);
				}
			} catch (InterruptedException e) {
				logger.warn(e.getMessage());
				return;
			}
			isConnected = true;
   			tcpConnectorThread = null;
   			if (tcpConnectionListener != null)
   				tcpConnectionListener.onConnectionStatusChange(isConnected);
   			logger.trace("TcpConnection stablished, exiting TcpConnectorThread");
       	}
	}
	
	private boolean connect() {
		tcpSocket = new Socket();
		try {
			tcpSocket.connect(new InetSocketAddress(this.ip, this.port), this.CONNECTION_TIMEOUT);
			tcpSocket.setSoTimeout(this.COMUNICATION_TIMEOUT);
			logger.debug("TcpConnection to port '{}' on '{}' completed", this.port, this.ip);
			return true;
		} catch (SocketException e) {
			if (this.isConnected)
				logger.warn("Cannot stablish TcpConnection: {}", e.getMessage());
		} catch (IOException e) {
			if (this.isConnected)
				logger.warn("Cannot stablish TcpConnection: {}", e.getMessage());
		}
		return false;
	}
	
	public boolean sendStatusRequest() {
		//     CMD_STATUS =>  0,    6,    s,    t,    a,    t,    u,    s
    	byte[] CMD_STATUS = { 0x00, 0x06, 0x73, 0x74, 0x61, 0x74, 0x75, 0x73 };
    	
    	try {
			tcpSocket.getOutputStream().write(CMD_STATUS);
			logger.trace("Status request sent");
			return true;
		} catch (IOException e) {
			if (this.isConnected) {
				logger.warn("TcpConnection lost: {}", e.getMessage());
				reconnect();
			}
			return false;
		}
		
	}
	
	public String[] readStatusRequest() {
		StringBuilder buffer = new StringBuilder();
    	
		// End of transmission sequence: "'\n','0x00','0x00'"
    	int[] end_seq = new int[] {0, 0, 10};
    	int[] end_buffer = new int[] {1, -1, -1};
    	
    	try {
    		int ch;
    		InputStream br = tcpSocket.getInputStream();
    		while((ch = br.read()) != -1) {
    			// Only accept alphanumeric and new line characters
    			if (ch == 10 || ch >= 32)
    				buffer.append((char) ch);  			
			
    			// Detect end of transmission
    			end_buffer[2] = end_buffer[1];
    			end_buffer[1] = end_buffer[0];
    			end_buffer[0] = ch;
    			if (Arrays.equals(end_buffer, end_seq))
    				break;
    		}
    	} catch (IOException e) {
    		if (this.isConnected) {
    			logger.warn("TcpConnection lost: {}", e.getMessage());
    			reconnect();
    		}
			return null;
    	}
    		
    	String result = buffer.toString();
    	return result.split("\n");
	}
	
	public void addConnectionListener(TcpConnectionListener listener) {
		logger.trace("Registering a new TcpConnectionListener: {}", listener.getClass().toString());
		this.tcpConnectionListener = listener;
	}

}
