package org.openhab.binding.habpanelfilter.internal.sse;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class SseSinkTopicInfo {
    private final List<String> regexFilters;

    public SseSinkTopicInfo(String topicFilter) {
        this.regexFilters = SseUtil.convertToRegex(topicFilter);
    }

    public static Predicate<SseSinkTopicInfo> matchesTopic(String topic) {
        return (info) -> {
            Stream var10000 = info.regexFilters.stream();
            topic.getClass();
            return var10000.anyMatch(topic::matches);
        };
    }
}
