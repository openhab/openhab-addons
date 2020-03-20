package org.openhab.binding.fox.internal.core;

import java.util.Scanner;

class FoxMessageMe extends FoxMessage {

	private String type = "";
	private String version = "";
	
	public FoxMessageMe() {
		super();
	}
	
	@Override
	protected void prepareMessage() {
		
	}

	@Override
	protected void interpretMessage() {
		type = "";
		version = "";
		if (message.matches("me .+ .+")) {
			Scanner scanner = new Scanner (message);
			scanner.next();
			type = scanner.next();
			version = scanner.next();
			scanner.close();
		}
	}
	
	String getType() {
		return type;
	}
	
	String getVersion() {
		return version;
	}

}
