package org.openhab.binding.threema.internal;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingHandler;

public class ThreemaActions implements ThingActions {

	private ThreemaHandler handler;

	@Override
	public void setThingHandler(ThingHandler handler) {
		this.handler = (ThreemaHandler) handler;

	}

	@Override
	public @Nullable ThingHandler getThingHandler() {
		return handler;
	}

	@RuleAction(label = "send a message (basic mode)", description = "Send a message using the Threema.Gateway in basic mode.")
	public boolean sendTextMessageSimple(
			@ActionInput(name = "message") String message) {
		return handler.sendTextMessageSimple(message);
	}
	
	@RuleAction(label = "send a message (basic mode)", description = "Send a message using the Threema.Gateway in basic mode.")
	public boolean sendTextMessageSimple(@ActionInput(name = "threemaId") String threemaId,
			@ActionInput(name = "message") String message) {
		return handler.sendTextMessageSimple(threemaId, message);
	}
}
