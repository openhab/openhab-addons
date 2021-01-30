package org.openhab.binding.resol.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.BaseThingHandler;

import de.resol.vbus.Packet;
import de.resol.vbus.Specification;
import de.resol.vbus.SpecificationFile.Language;

/**
 * The {@link ResolBaseThingHandler} class is a common ancestor for Resol thing handlers, capabale of handling vbus
 * packets
 *
 * @author Raphael Mack - Initial contribution
 */
@NonNullByDefault
public abstract class ResolBaseThingHandler extends BaseThingHandler {

    public ResolBaseThingHandler(Thing thing) {
        super(thing);
    }

    public abstract void packetReceived(Specification spec, Language lang, Packet packet);

}
