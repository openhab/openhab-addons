package org.openhab.binding.habpanelfilter.internal.sse;

import java.time.DateTimeException;
import java.util.*;

import javax.measure.Unit;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.sse.OutboundSseEvent;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemNotFoundException;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.transform.TransformationException;
import org.openhab.core.transform.TransformationHelper;
import org.openhab.core.types.State;
import org.openhab.core.types.StateDescription;
import org.openhab.core.types.StateOption;
import org.openhab.core.types.UnDefType;
import org.openhab.core.types.util.UnitUtils;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(service = { org.openhab.core.io.rest.sse.internal.SseItemStatesEventBuilder.class })
@NonNullByDefault
public class SseItemStatesEventBuilder {
    private final Logger logger = LoggerFactory
            .getLogger(org.openhab.core.io.rest.sse.internal.SseItemStatesEventBuilder.class);
    private final BundleContext bundleContext;
    private final ItemRegistry itemRegistry;

    @Activate
    public SseItemStatesEventBuilder(BundleContext bundleContext, @Reference ItemRegistry itemRegistry) {
        this.bundleContext = bundleContext;
        this.itemRegistry = itemRegistry;
    }

    @Nullable
    public OutboundSseEvent buildEvent(OutboundSseEvent.Builder eventBuilder, Set<String> itemNames) {
        Map<String, StateDTO> payload = new HashMap(itemNames.size());
        Iterator var5 = itemNames.iterator();

        while (var5.hasNext()) {
            String itemName = (String) var5.next();

            try {
                Item item = this.itemRegistry.getItem(itemName);
                StateDTO stateDto = new StateDTO();
                stateDto.state = item.getState().toString();
                String displayState = this.getDisplayState(item, Locale.getDefault());
                if (stateDto.state != null && !stateDto.state.equals(displayState)) {
                    stateDto.displayState = displayState;
                }

                payload.put(itemName, stateDto);
            } catch (ItemNotFoundException var9) {
                this.logger.warn("Attempting to send a state update of an item which doesn't exist: {}", itemName);
            }
        }

        return !payload.isEmpty() ? eventBuilder.mediaType(MediaType.APPLICATION_JSON_TYPE).data(payload).build()
                : null;
    }

    @Nullable
    private String getDisplayState(Item item, Locale locale) {
        StateDescription stateDescription = item.getStateDescription(locale);
        State state = item.getState();
        String displayState = state.toString();
        if (!(state instanceof UnDefType) && stateDescription != null) {
            if (!stateDescription.getOptions().isEmpty()) {
                Iterator var7 = stateDescription.getOptions().iterator();

                while (var7.hasNext()) {
                    StateOption option = (StateOption) var7.next();
                    if (option.getValue().equals(state.toString()) && option.getLabel() != null) {
                        displayState = option.getLabel();
                        break;
                    }
                }
            } else {
                String pattern = stateDescription.getPattern();
                if (pattern != null) {
                    if (TransformationHelper.isTransform(pattern)) {
                        try {
                            displayState = TransformationHelper.transform(this.bundleContext, pattern,
                                    state.toString());
                        } catch (NoClassDefFoundError var11) {
                        } catch (TransformationException var12) {
                            this.logger.warn("Failed transforming the state '{}' on item '{}' with pattern '{}': {}",
                                    new Object[] { state, item.getName(), pattern, var12.getMessage() });
                        }
                    } else {
                        if (state instanceof QuantityType) {
                            QuantityType<?> quantityState = (QuantityType) state;
                            Unit<?> patternUnit = UnitUtils.parseUnit(pattern);
                            if (patternUnit != null && !quantityState.getUnit().equals(patternUnit)) {
                                quantityState = quantityState.toUnit(patternUnit);
                            }

                            if (quantityState != null) {
                                state = quantityState;
                            }
                        } else if (state instanceof DateTimeType) {
                            try {
                                state = ((DateTimeType) state).toLocaleZone();
                            } catch (DateTimeException var10) {
                            }
                        }

                        try {
                            displayState = ((State) state).format(pattern);
                        } catch (IllegalArgumentException var9) {
                            this.logger.warn("Exception while formatting value '{}' of item {} with format '{}': {}",
                                    new Object[] { state, item.getName(), pattern, var9.getMessage() });
                            displayState = new String("Err");
                        }
                    }
                }
            }
        }

        return displayState;
    }
}
