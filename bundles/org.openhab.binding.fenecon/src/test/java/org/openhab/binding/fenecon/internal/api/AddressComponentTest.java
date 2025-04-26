package org.openhab.binding.fenecon.internal.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link AddressComponent}.
 *
 * @author Philipp Schneider - Initial contribution
 */
@NonNullByDefault
public class AddressComponentTest {

    @Test
    void testFixComponent() {
        String component = "component";

        AddressComponent result = new AddressComponent(component);

        assertEquals("component", result.component());
    }

    @Test
    void testVariableComponentChangedForBundleRegexRequest1() {
        String component = "charger0";

        AddressComponent result = new AddressComponent(component);

        assertEquals("charger.+", result.component());
    }

    @Test
    void testVariableComponentChangedForBundleRegexRequest2() {
        String component = "charger1";

        AddressComponent result = new AddressComponent(component);

        assertEquals("charger.+", result.component());
    }
}
