package org.openhab.binding.mercedesme.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.binding.BaseDynamicStateDescriptionProvider;
import org.openhab.core.thing.type.DynamicStateDescriptionProvider;
import org.osgi.service.component.annotations.Component;

/**
 * StateDescriptionProvider to change Channel State Pattern
 *
 * @author Bernd Weymann - Initial contribution
 */
@Component(service = { DynamicStateDescriptionProvider.class, MercedesMeDynamicStateDescriptionProvider.class })
@NonNullByDefault
public class MercedesMeDynamicStateDescriptionProvider extends BaseDynamicStateDescriptionProvider {

}
