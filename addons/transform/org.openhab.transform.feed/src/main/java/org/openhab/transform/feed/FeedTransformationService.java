/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.transform.feed;

import java.io.StringReader;
import java.util.Set;

import org.eclipse.smarthome.core.transform.TransformationException;
import org.eclipse.smarthome.core.transform.TransformationService;

import com.google.common.collect.Sets;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.SyndFeedOutput;

/**
 * Feed transformation service transforms a feed between some of the supported feed formats -
 * {@link #SUPPORTED_FEED_FORMATS}
 *
 * @author Svilen Valkanov
 *
 */
public class FeedTransformationService implements TransformationService {
    // List of supported formats
    public static final String RSS_2_00 = "rss_2.0";
    public static final String RSS_1_00 = "rss_1.0";
    public static final String RSS_0_94 = "rss_0.94";
    public static final String RSS_0_93 = "rss_0.93";
    public static final String RSS_0_92 = "rss_0.92";
    public static final String RSS_0_91_NETSCAPE = "rss_0.91N";
    public static final String RSS_0_91_USERLAND = "rss_0.91U";
    public static final String RSS_0_90 = "rss_0.9";
    public static final String ATOM_1_0 = "atom_1.0";
    public static final String ATOM_0_3 = "atom_0.3";

    /**
     * Feed formats, supported from the service - RSS 0.9, RSS 0.91 Netscape, RSS 0.91 Userland, RSS 0.92, RSS 0.93, RSS
     * 0.94, RSS, 1.0, RSS 2.0, Atom 0.3, Atom 1.0
     */
    public static final Set<String> SUPPORTED_FEED_FORMATS = Sets.newHashSet(RSS_2_00, RSS_1_00, RSS_0_94, RSS_0_93,
            RSS_0_92, RSS_0_91_USERLAND, RSS_0_91_NETSCAPE, RSS_0_90, ATOM_1_0, ATOM_0_3);

    @Override
    public String transform(String feedFormat, String source) throws TransformationException {
        if (feedFormat == null || source == null) {
            throw new TransformationException("The given parameters 'feedFormat' and 'source' must not be null!");
        }

        if (!SUPPORTED_FEED_FORMATS.contains(feedFormat)) {
            throw new TransformationException("The feed format '" + feedFormat
                    + "' is not supported. Supported formats -" + SUPPORTED_FEED_FORMATS.toString());
        }

        SyndFeedInput input = new SyndFeedInput();
        try (StringReader reader = new StringReader(source)) {
            SyndFeed feed = input.build(reader);
            changeFormat(feed, feedFormat);
            SyndFeedOutput outputBuilder = new SyndFeedOutput();
            return outputBuilder.outputString(feed);
        } catch (IllegalArgumentException e) {
            throw new TransformationException("No parser found for this feed type !", e);
        } catch (FeedException e) {
            throw new TransformationException("Feed content is invalid. Feed can not be parsed !", e);
        }
    }

    private void changeFormat(SyndFeed source, String destinationFormat) {
        if (destinationFormat.equals("rss_0.91N") || destinationFormat.equals("rss_0.91U")) {
            // RSS 0.91 has required language attribute. Default value is assigned, because if this value is
            // missing, the conversion to RSS 0.91 will fail
            if (source.getLanguage() == null) {
                source.setLanguage("en-us");
            }
        }
        source.setFeedType(destinationFormat);
    }

}
