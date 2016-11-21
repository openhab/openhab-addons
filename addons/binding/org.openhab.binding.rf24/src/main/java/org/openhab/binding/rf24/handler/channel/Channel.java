package org.openhab.binding.rf24.handler.channel;

import java.util.Set;
import java.util.function.Consumer;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;

public interface Channel extends AutoCloseable {
    static final Consumer<Updatable> DOING_NOTHING_CONSUMER = new Consumer<Channel.Updatable>() {

        @Override
        public void accept(Updatable t) {
        }
    };

    Set<String> whatChannelIdCanProcess();

    void process(ChannelUID channelUID, Command command);

    public interface Updatable {
        void updateState(ChannelUID channelUID, State state);
    }

    @Override
    void close();
}
