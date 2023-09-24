package org.openhab.binding.mercedesme.internal.handler;

import java.util.Map;

import org.apache.commons.collections4.map.HashedMap;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mercedesme.internal.MercedesMeDynamicStateDescriptionProvider;
import org.openhab.core.thing.ChannelUID;

/**
 * StateDescriptionProvider to change Channel State Pattern
 *
 * @author Bernd Weymann - Initial contribution
 * @param <V>
 */
@NonNullByDefault
public class MercedesMeDynamicStateDescriptionProviderMock<V> extends MercedesMeDynamicStateDescriptionProvider {
    public Map<String, String> patternMap = new HashedMap<String, String>();

    @Override
    public void setStatePattern(ChannelUID channelUID, String pattern) {
        patternMap.put(channelUID.toString(), pattern);
        // System.out.println("Pattern " + pattern + " for " + channelUID.toString());
    }
}
