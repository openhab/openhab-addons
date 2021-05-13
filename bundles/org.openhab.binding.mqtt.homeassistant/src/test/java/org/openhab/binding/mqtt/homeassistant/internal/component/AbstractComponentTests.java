package org.openhab.binding.mqtt.homeassistant.internal.component;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.mqtt.generic.AvailabilityTracker;
import org.openhab.binding.mqtt.generic.ChannelStateUpdateListener;
import org.openhab.binding.mqtt.generic.values.Value;
import org.openhab.binding.mqtt.homeassistant.internal.AbstractHomeAssistantTests;
import org.openhab.binding.mqtt.homeassistant.internal.DiscoverComponents;
import org.openhab.binding.mqtt.homeassistant.internal.HaID;
import org.openhab.binding.mqtt.homeassistant.internal.config.AbstractChannelConfiguration;
import org.openhab.binding.mqtt.homeassistant.internal.config.ChannelConfigurationTypeAdapterFactory;
import org.openhab.core.common.ThreadPoolManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@ExtendWith(MockitoExtension.class)
public abstract class AbstractComponentTests extends AbstractHomeAssistantTests {
    protected LatchComponentDiscoveredListener componentDiscoveredListener = new LatchComponentDiscoveredListener();
    protected ScheduledExecutorService executorService = ThreadPoolManager
            .getScheduledPool(ThreadPoolManager.THREAD_POOL_NAME_COMMON);
    protected @Mock ChannelStateUpdateListener channelStateUpdateListener;
    protected @Mock AvailabilityTracker availabilityTracker;
    protected Gson gson = new GsonBuilder().registerTypeAdapterFactory(new ChannelConfigurationTypeAdapterFactory())
            .create();
    protected DiscoverComponents discoverComponents = new DiscoverComponents(HA_UID, executorService,
            channelStateUpdateListener, availabilityTracker, gson, transformationServiceProvider);

    protected AbstractComponent<@NonNull ? extends AbstractChannelConfiguration> discoverComponent(String topic,
            String json) {
        return discoverComponent(topic, json.getBytes(StandardCharsets.UTF_8));
    }

    protected AbstractComponent<@NonNull ? extends AbstractChannelConfiguration> discoverComponent(String topic,
            byte[] jsonPayload) {
        var haID = new HaID(topic);
        var latch = componentDiscoveredListener.createWaitForComponentDiscoveredLatch(1);
        discoverComponents.startDiscovery(bridgeConnection, 0, Set.of(haID), componentDiscoveredListener);
        discoverComponents.processMessage(topic, jsonPayload);
        try {
            assert latch.await(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
            assertThat(e.getMessage(), false);
        }
        var component = componentDiscoveredListener.getDiscoveredComponent();
        assertThat(component, CoreMatchers.notNullValue());
        return component;
    }

    @SuppressWarnings("ConstantConditions")
    protected static void assertChannel(AbstractComponent<@NonNull ? extends AbstractChannelConfiguration> component,
            String channelId, String stateTopic, String commandTopic, String label, Class<? extends Value> valueClass) {
        var stateChannel = component.getChannel(channelId);
        assertThat(stateChannel.getChannel().getLabel(), is(label));
        assertThat(stateChannel.getState().getStateTopic(), is(stateTopic));
        assertThat(stateChannel.getState().getCommandTopic(), is(commandTopic));
        assertThat(stateChannel.getState().getCache(), is(instanceOf(valueClass)));
    }

    @NonNullByDefault
    protected static class LatchComponentDiscoveredListener implements DiscoverComponents.ComponentDiscovered {
        private @Nullable CountDownLatch latch;
        private @Nullable AbstractComponent<@NonNull ? extends AbstractChannelConfiguration> discoveredComponent;

        public void componentDiscovered(HaID homeAssistantTopicID, AbstractComponent<@NonNull ?> component) {
            discoveredComponent = component;
            if (latch != null) {
                latch.countDown();
            }
        }

        public CountDownLatch createWaitForComponentDiscoveredLatch(int count) {
            final var newLatch = new CountDownLatch(count);
            latch = newLatch;
            return newLatch;
        }

        public @Nullable AbstractComponent<@NonNull ? extends AbstractChannelConfiguration> getDiscoveredComponent() {
            return discoveredComponent;
        }
    }
}
