/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.rfxcom.internal.connector;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import jd2xx.JD2XX;
import jd2xx.JD2XXInputStream;
import jd2xx.JD2XXOutputStream;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RFXCOM connector for direct access via D2XX driver.
 * 
 * @author Pauli Anttila - Initial contribution
 */
public class RFXComJD2XXConnector implements RFXComConnectorInterface {

	private static final Logger logger = LoggerFactory
			.getLogger(RFXComJD2XXConnector.class);

	private static List<RFXComEventListener> _listeners = new ArrayList<RFXComEventListener>();

	JD2XX serialPort = null;
	JD2XXInputStream in = null;
	JD2XXOutputStream out = null;

	Thread readerThread = null;

	public RFXComJD2XXConnector() {
	}

	@Override
	public void connect(String device) throws IOException {
		logger.info("Connecting to RFXCOM device '{}'.", device);

		if (serialPort == null) {
			serialPort = new JD2XX();
		}
		serialPort.openBySerialNumber(device);
		serialPort.setBaudRate(38400);
		serialPort.setDataCharacteristics(8, JD2XX.STOP_BITS_1,
				JD2XX.PARITY_NONE);
		serialPort.setFlowControl(JD2XX.FLOW_NONE, 0, 0);
		serialPort.setTimeouts(100, 100);
		
		in = new JD2XXInputStream(serialPort);
		out = new JD2XXOutputStream(serialPort);

		out.flush();
		if (in.markSupported()) {
			in.reset();
		}

		readerThread = new SerialReader(in);
		readerThread.start();
	}

	@Override
	public void disconnect() {
		logger.debug("Disconnecting");

		if (readerThread != null) {
			logger.debug("Interrupt serial listener");
			readerThread.interrupt();
		}

		if (out != null) {
			logger.debug("Close serial out stream");
			IOUtils.closeQuietly(out);
		}
		if (in != null) {
			logger.debug("Close serial in stream");
			IOUtils.closeQuietly(in);
		}

		if (serialPort != null) {
			logger.debug("Close serial port");
			try {
				serialPort.close();

				readerThread = null;
				serialPort = null;
				out = null;
				in = null;

				logger.debug("Closed");

			} catch (IOException e) {
				logger.warn("Serial port closing error", e);
			}
		}
	}

	@Override
	public void sendMessage(byte[] data) throws IOException {
		logger.trace("Send data (len={}): {}", data.length,
				DatatypeConverter.printHexBinary(data));
		out.write(data);
	}

	public synchronized void addEventListener(
			RFXComEventListener rfxComEventListener) {
		if (!_listeners.contains(rfxComEventListener)) {
			_listeners.add(rfxComEventListener);
		}
	}

	public synchronized void removeEventListener(RFXComEventListener listener) {
		_listeners.remove(listener);
	}

	public class SerialReader extends Thread {
		boolean interrupted = false;
		InputStream in;

		public SerialReader(InputStream in) {
			this.in = in;
		}

		@Override
		public void interrupt() {
			interrupted = true;
			super.interrupt();
			try {
				in.close();
			} catch (IOException e) {
			} // quietly close
		}

		public void run() {
			final int dataBufferMaxLen = Byte.MAX_VALUE;

			byte[] dataBuffer = new byte[dataBufferMaxLen];

			int msgLen = 0;
			int index = 0;
			boolean start_found = false;

			logger.debug("Data listener started");

			try {

				byte[] tmpData = new byte[20];
				int len = -1;

				while (interrupted != true) {

					if ((len = in.read(tmpData)) > 0) {

						byte[] logData = Arrays.copyOf(tmpData, len);
						logger.trace("Received data (len={}): {}", len,
								DatatypeConverter.printHexBinary(logData));

						for (int i = 0; i < len; i++) {

							if (index > dataBufferMaxLen) {
								// too many bytes received, try to find new
								// start
								start_found = false;
							}

							if (start_found == false && tmpData[i] > 0) {

								start_found = true;
								index = 0;
								dataBuffer[index++] = tmpData[i];
								msgLen = tmpData[i] + 1;

							} else if (start_found) {

								dataBuffer[index++] = tmpData[i];

								if (index == msgLen) {

									// whole message received, send an event

									byte[] msg = new byte[msgLen];

									for (int j = 0; j < msgLen; j++)
										msg[j] = dataBuffer[j];

									sendMsgToListeners(msg);

									// find new start
									start_found = false;
								}
							}
						}
					} else {
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
													}
					}
				}
			} catch (InterruptedIOException e) {
				Thread.currentThread().interrupt();
				logger.error("Interrupted via InterruptedIOException");
			} catch (IOException e) {
				logger.error("Reading from serial port failed", e);
				sendErrorToListeners(e.getMessage());
			}

			logger.debug("Data listener stopped");
		}
	}
	
	private void sendMsgToListeners(byte[] msg) {
		try {
			Iterator<RFXComEventListener> iterator = _listeners.iterator();

			while (iterator.hasNext()) {
				((RFXComEventListener) iterator.next()).packetReceived(msg);
			}

		} catch (Exception e) {
			logger.error("Event listener invoking error", e);
		}
	}
	
	private void sendErrorToListeners(String error) {
		try {
			Iterator<RFXComEventListener> iterator = _listeners.iterator();

			while (iterator.hasNext()) {
				((RFXComEventListener) iterator.next()).errorOccured(error);
			}

		} catch (Exception e) {
			logger.error("Event listener invoking error", e);
		}
	}
}
