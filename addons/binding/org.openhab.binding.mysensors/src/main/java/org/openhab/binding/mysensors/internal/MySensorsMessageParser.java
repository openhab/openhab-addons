package org.openhab.binding.mysensors.internal;

/**
 * @author Tim Oberföll
 * 
 * Parser for the MySensors network.
 */
public class MySensorsMessageParser {
	
	/**
	 * @param line Input is a String containing the message received from the MySensors network
	 * @return Returns the content of the message as a MySensorsMessage
	 */
	public static MySensorsMessage parse(String line) {
		String[] splitMessage = line.split(";");
		if(splitMessage.length > 4) {
		
			MySensorsMessage mysensorsmessage = new MySensorsMessage();
			
			
			int nodeId = Integer.parseInt(splitMessage[0]);
			
			/*
			 *  It's a message from the gateway (ourself).
			 *  We only want the "Gateway startup complete"
			 */
			if(nodeId == 0)
				if(!splitMessage[5].equals(MySensorsMessage.GATEWAY_STARTUP_NOTIFICATION))
					return null;
			
			mysensorsmessage.setNodeId(nodeId);
			mysensorsmessage.setChildId(Integer.parseInt(splitMessage[1]));
			mysensorsmessage.setMsgType(Integer.parseInt(splitMessage[2]));
			mysensorsmessage.setAck(Integer.parseInt(splitMessage[3]));
			mysensorsmessage.setSubType(Integer.parseInt(splitMessage[4]));
			if(splitMessage.length == 6) {
				String msg = splitMessage[5].replaceAll("\\r|\\n", "").trim();
				mysensorsmessage.setMsg(msg);
			} else
				mysensorsmessage.setMsg("");
			
			return mysensorsmessage;
		} else {
			return null;
		}
		
	}
	
	public static String generateAPIString(MySensorsMessage msg) {
		String APIString = "";
		APIString += msg.getNodeId() + ";";
		APIString += msg.getChildId() + ";";
		APIString += msg.getMsgType() + ";";
		APIString += msg.getAck() + ";";
		APIString += msg.getSubType() + ";";
		APIString += msg.getMsg() + "\n";
		
		return APIString;
	}
}
