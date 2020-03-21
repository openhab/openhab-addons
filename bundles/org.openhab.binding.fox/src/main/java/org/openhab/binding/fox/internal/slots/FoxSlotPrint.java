package org.openhab.binding.fox.internal.slots;

import java.nio.charset.StandardCharsets;

import org.openhab.binding.fox.internal.core.FoxException;
import org.openhab.binding.fox.internal.core.FoxSlot;

public class FoxSlotPrint extends FoxSlot {
	
	public FoxSlotPrint() {
		
	}
	
	private void write(int align, String message) throws FoxException {
		byte[] asciiBytes = message.getBytes(StandardCharsets.US_ASCII);
	    Integer[] data = new Integer[asciiBytes.length + 2];
	    data[0] = align;
	    for (int i = 0; i < asciiBytes.length; i++)
	        data[i + 1] = asciiBytes[i] & 0xff;
	    data[data.length - 1] = 0;
		writeSet(data);
	}
	
	public void writeTopLeft(String message) throws FoxException {
		write(0x10, message);
	}
	
	public void writeTopCenter(String message) throws FoxException {
		write(0x11, message);
	}
	
	public void writeTopRight(String message) throws FoxException {
		write(0x12, message);
	}
	
	public void writeBottomLeft(String message) throws FoxException {
		write(0x18, message);
	}
	
	public void writeBottomCenter(String message) throws FoxException {
		write(0x19, message);
	}
	
	public void writeBottomRight(String message) throws FoxException {
		write(0x1a, message);
	}
}
