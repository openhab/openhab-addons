package org.openhab.binding.hyperion.internal.protocol.request;

public class ColorCommand extends HyperionCommand {

    private final static String NAME = "color";
    private int priority;
    private int[] color = new int[3];

    public ColorCommand(int red, int green, int blue, int priority) {
        super(NAME);
        setPriority(priority);
        setColor(red, green, blue);
    }

    public void setColor(int red, int green, int blue) {
        color[0] = red;
        color[1] = green;
        color[2] = blue;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int[] getColor() {
        return color;
    }

}
