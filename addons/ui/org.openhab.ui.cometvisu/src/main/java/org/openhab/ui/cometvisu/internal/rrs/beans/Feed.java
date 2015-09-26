package org.openhab.ui.cometvisu.internal.rrs.beans;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link Feed} is used by the CometVisu rss-plugin
 * 
 * @author Tobias Bräutigam
 * @since 2.0.0
 */
public class Feed {
    public String feedUrl;
    public String title;
    public String link;
    public String author;
    public String description;
    public String type;
    public List<Entry> entries = new ArrayList<Entry>();
}
