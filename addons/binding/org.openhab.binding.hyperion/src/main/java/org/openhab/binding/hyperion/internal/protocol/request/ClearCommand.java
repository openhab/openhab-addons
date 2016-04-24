package org.openhab.binding.hyperion.internal.protocol.request;

public class ClearCommand extends HyperionCommand {

    private final static String NAME = "clear";
    private int priority;

    public ClearCommand(int priority) {
        super(NAME);
        setPriority(priority);
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

}
