# Pi-hole Binding

The Pi-hole Binding is a bridge between openHAB and Pi-hole, enabling users to integrate Pi-hole statistics and controls into their home automation setup. Pi-hole is a DNS-based ad blocker that can run on a variety of platforms, including Raspberry Pi.

Pi-hole is a powerful network-level advertisement and internet tracker blocking application.
By intercepting DNS requests, it can prevent unwanted content from being displayed on devices connected to your network.
The Pi-hole Binding allows you to monitor Pi-hole statistics and control its functionality directly from your openHAB setup.

### Features

- Real-time Statistics: Monitor key metrics such as the number of domains being blocked, DNS queries made today, ads blocked today, and more.
- Control: Enable or disable Pi-hole's blocking functionality, configure blocking options, and adjust privacy settings directly from openHAB.
- Integration: Seamlessly integrate Pi-hole data and controls with other openHAB items and rules to create advanced automation scenarios.

## Supported Things

- `server`: Pi-hole server

## Thing Configuration

### `server` Thing Configuration

| Name            | Type    | Description                                                                               | Default | Required | Advanced |
|-----------------|---------|-------------------------------------------------------------------------------------------|---------|----------|----------|
| hostname        | text    | Hostname or IP address of the device                                                      | N/A     | yes      | no       |
| token           | text    | Token to access the device. To generate token go to `settings` > `API` > `Show API token` | N/A     | yes      | no       |
| refreshInterval | integer | Interval the device is polled in sec.                                                     | 600     | no       | yes      |

## Channels

| Channel                 | Type     | Read/Write | Description                                                |
|-------------------------|----------|------------|------------------------------------------------------------|
| domains-being-blocked   | Number   | RO         | The total number of domains currently being blocked.       |
| dns-queries-today       | Number   | RO         | The count of DNS queries made today.                       |
| ads-blocked-today       | Number   | RO         | The number of ads blocked today.                           |
| ads-percentage-today    | Number   | RO         | The percentage of ads blocked today.                       |
| unique-domains          | Number   | RO         | The count of unique domains queried.                       |
| queries-forwarded       | Number   | RO         | The number of queries forwarded to an external DNS server. |
| queries-cached          | Number   | RO         | The number of queries served from the cache.               |
| clients-ever-seen       | Number   | RO         | The total number of unique clients ever seen.              |
| unique-clients          | Number   | RO         | The current count of unique clients.                       |
| dns-queries-all-types   | Number   | RO         | The total number of DNS queries of all types.              |
| reply-unknown           | Number   | RO         | DNS replies with an unknown status.                        |
| reply-nodata            | Number   | RO         | DNS replies indicating no data.                            |
| reply-nxdomain          | Number   | RO         | DNS replies indicating non-existent domain.                |
| reply-cname             | Number   | RO         | DNS replies with a CNAME record.                           |
| reply-ip                | Number   | RO         | DNS replies with an IP address.                            |
| reply-domain            | Number   | RO         | DNS replies with a domain name.                            |
| reply-rrname            | Number   | RO         | DNS replies with a resource record name.                   |
| reply-servfail          | Number   | RO         | DNS replies indicating a server failure.                   |
| reply-refused           | Number   | RO         | DNS replies indicating refusal.                            |
| reply-notimp            | Number   | RO         | DNS replies indicating not implemented.                    |
| reply-other             | Number   | RO         | DNS replies with other statuses.                           |
| reply-dnssec            | Number   | RO         | DNS replies with DNSSEC information.                       |
| reply-none              | Number   | RO         | DNS replies with no data.                                  |
| reply-blob              | Number   | RO         | DNS replies with a BLOB (binary large object).             |
| dns-queries-all-replies | Number   | RO         | The total number of DNS queries with all reply types.      |
| privacy-level           | Number   | RO         | The privacy level setting.                                 |
| enabled                 | Switch   | RO         | The current status of blocking                             |
| disable-enable          | String   | RW         | Is blocking enabled/disabled                               |
| gravity-last-update     | DateTime | RO         | Last update of gravity                                     |
| gravity-file-exists     | DateTime | RO         | Does gravity file exists                                   |

## Full Example

### Thing Configuration

```java
Thing pihole:server:a4a077edb8 "Pi-hole" @ "Location"
[
    refreshIntervalSeconds=600,
    hostname="http://123.456.7.89",
    token="as654gadf3h1dsfh654dfh6fh7et654asd3g21fh654eth8t4swd4g3s1g65sfg5"
] {
    Channels:
        Type number : domains_being_blocked "Domains Blocked" [ ]
        Type number : dns_queries_today "DNS Queries Today" [ ]
        Type number : ads_blocked_today "Ads Blocked Today" [ ]
        Type number : ads_percentage_today "Ads Percentage Today" [ ]
        Type number : unique_domains "Unique Domains" [ ]
        Type number : queries_forwarded "Queries Forwarded" [ ]
        Type number : queries_cached "Queries Cached" [ ]
        Type number : clients_ever_seen "Clients Ever Seen" [ ]
        Type number : unique_clients "Unique Clients" [ ]
        Type number : dns_queries_all_types "DNS Queries (All Types)" [ ]
        Type number : reply_UNKNOWN "Reply UNKNOWN" [ ]
        Type number : reply_NODATA "Reply NODATA" [ ]
        Type number : reply_NXDOMAIN "Reply NXDOMAIN" [ ]
        Type number : reply_CNAME "Reply CNAME" [ ]
        Type number : reply_IP "Reply IP" [ ]
        Type number : reply_DOMAIN "Reply DOMAIN" [ ]
        Type number : reply_RRNAME "Reply RRNAME" [ ]
        Type number : reply_SERVFAIL "Reply SERVFAIL" [ ]
        Type number : reply_REFUSED "Reply REFUSED" [ ]
        Type number : reply_NOTIMP "Reply NOTIMP" [ ]
        Type number : reply_OTHER "Reply OTHER" [ ]
        Type number : reply_DNSSEC "Reply DNSSEC" [ ]
        Type number : reply_NONE "Reply NONE" [ ]
        Type number : reply_BLOB "Reply BLOB" [ ]
        Type number : dns_queries_all_replies "DNS Queries (All Replies)" [ ]
        Type number : privacy_level "Privacy Level" [ ]
        Type switch : enabled "Status" [ ]
        Type string : disable-enable "Disable Blocking" [ ]
}
```

### Item Configuration

```java
Number domains_being_blocked "Domains Blocked" { channel="pihole:server:a4a077edb8:domains_being_blocked" }
Number dns_queries_today "DNS Queries Today" { channel="pihole:server:a4a077edb8:dns_queries_today" }
Number ads_blocked_today "Ads Blocked Today" { channel="pihole:server:a4a077edb8:ads_blocked_today" }
Number ads_percentage_today "Ads Percentage Today" { channel="pihole:server:a4a077edb8:ads_percentage_today" }
Number unique_domains "Unique Domains" { channel="pihole:server:a4a077edb8:unique_domains" }
Number queries_forwarded "Queries Forwarded" { channel="pihole:server:a4a077edb8:queries_forwarded" }
Number queries_cached "Queries Cached" { channel="pihole:server:a4a077edb8:queries_cached" }
Number clients_ever_seen "Clients Ever Seen" { channel="pihole:server:a4a077edb8:clients_ever_seen" }
Number unique_clients "Unique Clients" { channel="pihole:server:a4a077edb8:unique_clients" }
Number dns_queries_all_types "DNS Queries (All Types)" { channel="pihole:server:a4a077edb8:dns_queries_all_types" }
Number reply_UNKNOWN "Reply UNKNOWN" { channel="pihole:server:a4a077edb8:reply_UNKNOWN" }
Number reply_NODATA "Reply NODATA" { channel="pihole:server:a4a077edb8:reply_NODATA" }
Number reply_NXDOMAIN "Reply NXDOMAIN" { channel="pihole:server:a4a077edb8:reply_NXDOMAIN" }
Number reply_CNAME "Reply CNAME" { channel="pihole:server:a4a077edb8:reply_CNAME" }
Number reply_IP "Reply IP" { channel="pihole:server:a4a077edb8:reply_IP" }
Number reply_DOMAIN "Reply DOMAIN" { channel="pihole:server:a4a077edb8:reply_DOMAIN" }
Number reply_RRNAME "Reply RRNAME" { channel="pihole:server:a4a077edb8:reply_RRNAME" }
Number reply_SERVFAIL "Reply SERVFAIL" { channel="pihole:server:a4a077edb8:reply_SERVFAIL" }
Number reply_REFUSED "Reply REFUSED" { channel="pihole:server:a4a077edb8:reply_REFUSED" }
Number reply_NOTIMP "Reply NOTIMP" { channel="pihole:server:a4a077edb8:reply_NOTIMP" }
Number reply_OTHER "Reply OTHER" { channel="pihole:server:a4a077edb8:reply_OTHER" }
Number reply_DNSSEC "Reply DNSSEC" { channel="pihole:server:a4a077edb8:reply_DNSSEC" }
Number reply_NONE "Reply NONE" { channel="pihole:server:a4a077edb8:reply_NONE" }
Number reply_BLOB "Reply BLOB" { channel="pihole:server:a4a077edb8:reply_BLOB" }
Number dns_queries_all_replies "DNS Queries (All Replies)" { channel="pihole:server:a4a077edb8:dns_queries_all_replies" }
Number privacy_level "Privacy Level" { channel="pihole:server:a4a077edb8:privacy_level" }
Switch enabled "Status" { channel="pihole:server:a4a077edb8:enabled" }
String disable_enable "Disable Blocking" { channel="pihole:server:a4a077edb8:disable-enable" }
```

### Actions

Pi-hole binding provides actions to use in rules:

```java
import java.util.concurrent.TimeUnit

rule "test"
when
    /* when */
then
	val actions = getActions("pihole", "pihole:server:as8af03m38")
	if (actions !== null) {
            // disable blocking for 5 * 60 seconds (5 minutes)
            actions.disableBlocking(5 * 60)

            // disable blocking for 5 minutes
            actions.disableBlocking(5, TimeUnit.MINUTES)

            // disable blocking for infinity
            actions.disableBlocking(0)
            actions.disableBlocking()

            // enable blocking
            actions.enableBlocking()
	}
end
```
