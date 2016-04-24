package org.openhab.binding.hyperion.internal.protocol.request;

import org.openhab.binding.hyperion.internal.protocol.effect.Effect;

public class EffectCommand extends HyperionCommand {

    private final static String NAME = "effect";
    private Effect effect;
    private int priority;

    public EffectCommand(Effect effect, int priority) {
        super(NAME);
        setEffect(effect);
        setPriority(priority);
    }

    public Effect getEffect() {
        return effect;
    }

    public void setEffect(Effect effect) {
        this.effect = effect;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

}
