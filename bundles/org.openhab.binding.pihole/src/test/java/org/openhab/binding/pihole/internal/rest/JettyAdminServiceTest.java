/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.pihole.internal.rest;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.net.URI;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openhab.binding.pihole.internal.rest.model.DnsStatistics;
import org.openhab.binding.pihole.internal.rest.model.GravityLastUpdated;
import org.openhab.binding.pihole.internal.rest.model.Relative;

/**
 * @author Martin Grzeslowski - Initial contribution
 */
@NonNullByDefault
public class JettyAdminServiceTest {
    String content = """
            {
              "domains_being_blocked": 131355,
              "dns_queries_today": 27459,
              "ads_blocked_today": 2603,
              "ads_percentage_today": 9.479588,
              "unique_domains": 6249,
              "queries_forwarded": 16030,
              "queries_cached": 8525,
              "clients_ever_seen": 2,
              "unique_clients": 2,
              "dns_queries_all_types": 27459,
              "reply_UNKNOWN": 631,
              "reply_NODATA": 3168,
              "reply_NXDOMAIN": 492,
              "reply_CNAME": 9819,
              "reply_IP": 13224,
              "reply_DOMAIN": 48,
              "reply_RRNAME": 0,
              "reply_SERVFAIL": 0,
              "reply_REFUSED": 0,
              "reply_NOTIMP": 0,
              "reply_OTHER": 0,
              "reply_DNSSEC": 0,
              "reply_NONE": 0,
              "reply_BLOB": 77,
              "dns_queries_all_replies": 27459,
              "privacy_level": 0,
              "status": "enabled",
              "gravity_last_updated": {
                "file_exists": true,
                "absolute": 1712457841,
                "relative": {
                  "days": 0,
                  "hours": 7,
                  "minutes": 3
                }
              }
            }
            """;

    // Returns a DnsStatistics object when called with valid token and baseUrl
    @Test
    @DisplayName("Returns a DnsStatistics object when called with valid token and baseUrl")
    public void testReturnsDnsStatisticsObjectWithValidTokenAndBaseUrl() throws Exception {
        // Given
        var token = "validToken";
        var baseUrl = URI.create("https://example.com");
        var client = mock(HttpClient.class);
        var adminService = new JettyAdminService(token, baseUrl, client);
        var dnsStatistics = new DnsStatistics(131355, // domains_being_blocked
                27459, // dns_queries_today
                2603, // ads_blocked_today
                9.479588, // ads_percentage_today
                6249, // unique_domains
                16030, // queries_forwarded
                8525, // queries_cached
                2, // clients_ever_seen
                2, // unique_clients
                27459, // dns_queries_all_types
                631, // reply_UNKNOWN
                3168, // reply_NODATA
                492, // reply_NXDOMAIN
                9819, // reply_CNAME
                13224, // reply_IP
                48, // reply_DOMAIN
                0, // reply_RRNAME
                0, // reply_SERVFAIL
                0, // reply_REFUSED
                0, // reply_NOTIMP
                0, // reply_OTHER
                0, // reply_DNSSEC
                0, // reply_NONE
                77, // reply_BLOB
                27459, // dns_queries_all_replies
                0, // privacy_level
                "enabled", // status
                new GravityLastUpdated(true, 1712457841L, new Relative(0, 7, 3)));
        var response = mock(ContentResponse.class);
        var request = mock(Request.class);
        given(request.timeout(10, SECONDS)).willReturn(request);

        given(client.newRequest(URI.create("https://example.com/admin/api.php?summaryRaw&auth=validToken")))
                .willReturn(request);
        given(request.send()).willReturn(response);
        given(response.getContentAsString()).willReturn(content);

        // When
        var result = adminService.summary();

        // Then
        assertThat(result).contains(dnsStatistics);
    }
}
