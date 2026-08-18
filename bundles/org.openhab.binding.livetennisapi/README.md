# Live Tennis API Binding

This binding integrates the [Live Tennis API](https://livetennisapi.com), a cloud service for real-time tennis scores covering ATP, WTA, Challenger, ITF and junior Grand Slam draws.
It tracks players and tournaments: live match state (score line, sets, points, serving, break point), the next scheduled match and current rankings.

The binding is developed and maintained by the Live Tennis API team.
It uses only endpoints included in the API's free tier; an API key is self-serve at [livetennisapi.com/subscribe/free](https://livetennisapi.com/subscribe/free), no card required.
See the [API documentation](https://docs.livetennisapi.com) for details on the underlying data.

## Supported Things

| Thing ID     | Type   | Description                                                                     |
|--------------|--------|---------------------------------------------------------------------------------|
| `account`    | Bridge | One Live Tennis API key; polls the live match snapshot and shares it downstream  |
| `player`     | Thing  | Tracks one player or doubles team                                               |
| `tournament` | Thing  | Tracks one tournament of the catalogue                                          |

## Discovery

Auto-discovery is not supported.
Things must be added manually.

## Singles and Doubles

Singles and doubles matches share the same shape.
For a doubles match each participant is a team rather than an individual: the opponent and player names hold the pairing (for example `Bopanna / Ebden`), and the `live#discipline` channel reads `doubles`.
Configure a `player` thing with a doubles team's id to track that team exactly as you would a singles player.

## Thing Configuration

### `account` Bridge

| Parameter         | Type      | Required | Default | Description                                                                                                                                          |
|-------------------|-----------|----------|---------|----------------------------------------------------------------------------------------------------------------------------------------------------|
| `apiKey`          | `text`    | yes      | —       | API key for the Live Tennis API                                                                                                                     |
| `refreshInterval` | `integer` | no       | 1800    | How often to poll the live match snapshot in seconds (min: 60). The default keeps the bridge plus one player thing within the free tier's daily quota; lower values need a paid tier. |

### `player` Thing

| Parameter               | Type      | Required | Default | Description                                                                                        |
|-------------------------|-----------|----------|---------|----------------------------------------------------------------------------------------------------|
| `playerId`              | `integer` | yes      | —       | The player's or doubles team's id in the Live Tennis API; look it up with `GET /players?search=name` |
| `detailRefreshEnabled`  | `boolean` | no       | `true`  | Whether to refresh the next match and ranking. Turn it off to track only live match state (pushed by the bridge at no extra cost) and spend no quota of this thing's own |
| `detailRefreshInterval` | `integer` | no       | 7200    | How often to refresh the next match and ranking in seconds when enabled (min: 300); two API requests per cycle |

### `tournament` Thing

| Parameter      | Type   | Required | Default | Description                                                                                        |
|----------------|--------|----------|---------|----------------------------------------------------------------------------------------------------|
| `tournamentId` | `text` | yes      | —       | The tournament's stable id in the Live Tennis API; look it up with `GET /tournaments?search=name`   |

## Channels

All channels are read-only.

### `account` Bridge

| Channel                 | Type     | Description                                                  |
|-------------------------|----------|--------------------------------------------------------------|
| `usage#tier`            | `String` | Access tier of the API key (`free`, `basic`, `pro`, `ultra`) |
| `usage#calls-today`     | `Number` | API calls made today with this key                           |
| `usage#remaining-today` | `Number` | API calls remaining today within the daily quota             |

Reading usage is quota-exempt on the API side, so these channels never consume the quota they report.

### `player` Thing

Score channels list the tracked player first.
All `live` channels are `UNDEF` while the player has no match in progress.

| Channel                  | Type       | Description                                                                                                                                   |
|--------------------------|------------|----------------------------------------------------------------------------------------------------------------------------------------------|
| `live#status`            | `String`   | Lifecycle status of the live match                                                                                                           |
| `live#discipline`        | `String`   | `singles` or `doubles`; `UNDEF` when the feed states neither                                                                                 |
| `live#tournament`        | `String`   | Tournament of the live match                                                                                                                 |
| `live#round`             | `String`   | Round of the live match                                                                                                                      |
| `live#opponent`          | `String`   | Name of the opponent (the opposing team in a doubles match)                                                                                  |
| `live#score-line`        | `String`   | Games per set, e.g. `6-4 3-2`                                                                                                                |
| `live#sets`              | `String`   | Sets won, e.g. `1-0`                                                                                                                         |
| `live#points`            | `String`   | In-game points, e.g. `40-15` or `AD-40`; `UNDEF` when the feed states no points                                                             |
| `live#serving`           | `Switch`   | `ON` while the tracked player is serving; `UNDEF` when no game is in progress                                                                |
| `live#break-point`       | `Switch`   | `ON` while the current game stands at break point (receiver at `AD`, or receiver at `40` with the server at `0`/`15`/`30`); never `ON` in a tiebreak; `UNDEF` when the score state does not allow deriving it, including at `40-40` where the API does not state whether advantage or no-advantage scoring is in play |
| `live#tiebreak`          | `Switch`   | `ON` while a tiebreak is being played                                                                                                        |
| `next-match#opponent`    | `String`   | Opponent in the next scheduled match                                                                                                        |
| `next-match#start-time`  | `DateTime` | Scheduled start of the next match; `UNDEF` until the order of play assigns a time                                                            |
| `next-match#tournament`  | `String`   | Tournament of the next scheduled match                                                                                                       |
| `next-match#round`       | `String`   | Round of the next scheduled match                                                                                                           |
| `profile#ranking`        | `Number`   | The player's current ranking                                                                                                                |
| `profile#ranking-points` | `Number`   | The player's current ranking points                                                                                                         |

### `tournament` Thing

Match channels show the tournament's first listed live match.

| Channel            | Type     | Description                                                     |
|--------------------|----------|-----------------------------------------------------------------|
| `info#name`        | `String` | Name of the tournament                                          |
| `info#surface`     | `String` | Court surface (`hard`, `clay`, `grass`)                         |
| `info#category`    | `String` | Tournament category where curated, e.g. `grand_slam`            |
| `live#match-count` | `Number` | Number of this tournament's matches currently in progress       |
| `live#players`     | `String` | Both participants of the featured live match                    |
| `live#discipline`  | `String` | `singles` or `doubles` for the featured live match              |
| `live#status`      | `String` | Lifecycle status of the featured live match                     |
| `live#score-line`  | `String` | Games per set of the featured live match                        |
| `live#sets`        | `String` | Sets won in the featured live match                             |
| `live#points`      | `String` | In-game points of the featured live match                       |
| `live#server`      | `String` | Name of the player serving in the featured live match           |
| `live#break-point` | `Switch` | `ON` while the featured live match's game stands at break point  |
| `live#tiebreak`    | `Switch` | `ON` while a tiebreak is being played in the featured live match |

## Request Budget and the Free Tier

The free tier allows 100 requests per day.
The bridge makes one counted request per refresh cycle (the usage read is quota-exempt), shared by all player and tournament things, so at the default 1800 s interval the bridge uses 48 requests per day.
Each player thing additionally makes two requests per detail refresh cycle — 24 per day at the default 7200 s interval; setting `detailRefreshEnabled=false` drops those to zero and the thing then tracks only live match state.

At the defaults a free key therefore fits the bridge plus one player thing (48 + 24 = 72 requests per day, within the 100 per day allowance).
For several player things or faster live updates, a paid tier with a higher daily quota is required.
The API answers requests over quota with HTTP 429; the bridge then goes `OFFLINE` with a communication error until a later poll succeeds, and the child things retry their own detail requests with a short backoff.

The live match list is paginated. The bridge reads the `meta.has_more` flag and pages forward until the snapshot is complete, so more than 200 concurrent live matches are not silently dropped; in practice the whole live board is well under one page, so this remains a single request per cycle.

## Full Example

### `livetennisapi.things`

```java
Bridge livetennisapi:account:myaccount "Live Tennis API" [ apiKey="XXXX", refreshInterval=1800 ] {
    Thing player alcaraz "Carlos Alcaraz" [ playerId=1234, detailRefreshInterval=7200 ]
    Thing tournament cincinnati "Cincinnati Open" [ tournamentId="atp-cincinnati" ]
}
```

### `livetennisapi.items`

```java
String Alcaraz_MatchStatus "Match Status" { channel="livetennisapi:player:myaccount:alcaraz:live#status" }
String Alcaraz_ScoreLine "Score [%s]" { channel="livetennisapi:player:myaccount:alcaraz:live#score-line" }
String Alcaraz_Points "Points [%s]" { channel="livetennisapi:player:myaccount:alcaraz:live#points" }
Switch Alcaraz_Serving "Serving" { channel="livetennisapi:player:myaccount:alcaraz:live#serving" }
Switch Alcaraz_BreakPoint "Break Point" { channel="livetennisapi:player:myaccount:alcaraz:live#break-point" }
String Alcaraz_NextOpponent "Next Opponent [%s]" { channel="livetennisapi:player:myaccount:alcaraz:next-match#opponent" }
DateTime Alcaraz_NextStart "Next Match [%1$td.%1$tm. %1$tH:%1$tM]" { channel="livetennisapi:player:myaccount:alcaraz:next-match#start-time" }
Number Alcaraz_Ranking "Ranking [%d]" { channel="livetennisapi:player:myaccount:alcaraz:profile#ranking" }
Number Api_CallsToday "API Calls Today [%d]" { channel="livetennisapi:account:myaccount:usage#calls-today" }
```

### Example Rule

```java
rule "Announce a break point"
when
    Item Alcaraz_BreakPoint changed to ON
then
    logInfo("tennis", "Break point at {} in the game!", Alcaraz_Points.state)
end
```
