# openHAB MCP Server

The openHAB MCP add-on lets AI assistants — Claude Desktop, Claude Code, ChatGPT, and other [Model Context Protocol](https://modelcontextprotocol.io) (MCP) clients read and control your openHAB smart home.

Once you've connected a client, you can say things like:

- _"What's the status of my home?"_
- _"Dim the office to 30%."_
- _"Turn on the porch light every day at sunset."_
- _"Let me know if the garage door opens while we're talking."_
- _"Turn off the kitchen light at 10pm tonight."_
- _"Modernize all my openHAB UI Pages."_

The assistant uses your home's semantic model (rooms, equipment, devices) along with fuzzy matching logic to understand which items you mean to monitor or control without having to use the exact item name.

## Installation

Install the **Model Context Protocol (MCP) Server** add-on in openHAB under **Settings → Add-on Store → System Integrations**.

The server starts automatically on `http://<your-openhab>:8080/mcp`.
Every connection must present an openHAB bearer token (see [Authentication](#authentication) below).

## Settings

| Setting                | Default | Advanced | Description                                                                                                                                                                                                                                                                        |
| ---------------------- | ------- | -------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `enabled`              | `true`  | NO       | Turn the server on or off without uninstalling.                                                                                                                                                                                                                                    |
| `enableFullApiAccess`  | `false` | NO       | Give the assistant access to the **full openHAB REST API** — including destructive endpoints. Only turn this on if you trust the assistant with that scope. See [Full REST API access](#full-rest-api-access-opt-in).                                                              |
| `enableScripting`      | `false` | NO       | Let the assistant include JavaScript snippets as rule actions/conditions and run scripts ad-hoc via `execute_script`. Requires the `openhab-automation-jsscripting` add-on. See [Scripting](#scripting-opt-in).                                                                    |
| `enableLoggingAccess`  | `false` | NO       | Give the assistant tools to read recent log entries and adjust logger verbosity for diagnostics. Level changes require an ADMIN-scoped token. See [Diagnostic logging](#diagnostic-logging-opt-in).                                                                                |
| `enableUiDesign`       | `false` | NO       | Let the assistant design Main UI pages and reusable custom widgets via `list_widgets`, `describe_widget`, `get_page_skeleton`, `manage_ui_component`, and `validate_ui_component`. Writes require an ADMIN-scoped token. See [Main UI design](#main-ui-design-opt-in).             |
| `enableStaticAssets`   | `false` | NO       | Let the assistant list, read, upload, and delete files in `$OPENHAB_CONF/html` (the folder served at `/static/*`). Useful for SVG icons, images, CSS overrides, and other static files the UI references. ADMIN-scoped token required. See [Static assets](#static-assets-opt-in). |
| `registerCloudWebhook` | `false` | NO       | Let remote MCP clients reach this server through openHAB Cloud (`myopenhab.org`) with no port forwarding. Requires the openHAB Cloud add-on. See [openHAB Cloud](#2-openhab-cloud) under Connecting a client.                                                                      |
| `exposeUntaggedItems`  | `false` | YES      | Also include items that aren't assigned to a Location/Equipment/Point. Most people leave this off.                                                                                                                                                                                 |
| `maxItemsPerPage`      | `100`   | YES      | Maximum items returned in one paginated response.                                                                                                                                                                                                                                  |
| `resourceCoalesceMs`   | `500`   | YES      | Rate-limit update notifications for chatty items like power meters.                                                                                                                                                                                                                |

## Authentication

Every connection needs an **openHAB API token**.

Tokens can be obtained in 2 different ways:

1. Clients that support oAuth will automatically redirect users to login to openHAB and obtain a user token. This will likely require the [openHAB Cloud](#2-openhab-cloud) webhook option to allow outside ingress to the binding.
1. Clients can be [manually configured](#manual-token-generation) with a token and either used locally, directly to openHAB, or with the [openHAB Cloud](#2-openhab-cloud) webhook.

### Manual Token Generation

Generate a token in the openHAB UI:

1. Click your user menu (bottom-left) → **Profile**.
1. Open the **API Tokens** section → **+ Add API Token**.
1. Give it a name (e.g. _"Claude"_) and a scope (leave blank for full access).
1. Copy the token — it looks like `oh.abcDEF123…`. You won't see it again.

Paste the token into your MCP client as an `Authorization: Bearer oh.…` header — the [examples](#client-setup) below show how for each client.

Tokens are tied to the user that created them, so the assistant's permissions match whatever that user can do through the openHAB UI.
If you want to restrict what the assistant can touch, create a dedicated openHAB user with limited permissions and generate the token from that account.

> Modern MCP clients (recent Claude, ChatGPT, `mcp-remote`) can drive a browser-based OAuth login — no token pasting needed.
> The server advertises the necessary OAuth metadata automatically; just give the client your server URL and it'll walk you through sign-in the first time.

## Connecting a client

There are three ways to connect an MCP client to your openHAB server.
Which one you use depends on whether the client can reach your openHAB instance directly and whether it speaks Streamable HTTP natively.

### Connection methods

#### 1. Direct connection (LAN or VPN)

Point the client at `http://<your-openhab>:8080/mcp` with an `Authorization: Bearer oh.…` header.
The client must be able to reach your openHAB instance over the network.
Use this when the client is on the same LAN, connected via VPN, or you have a reverse proxy set up.

#### 2. openHAB Cloud

> **Warning:** Exposing the MCP service via a webhook will make the openHAB user login UI available to anyone with the unique generated cloud URL.
> This URL should be considered private and not shared outside of using it with trusted AI providers.
> To remove the webhook, disable the option from the settings menu. Enabling will generate a new unique URL.

Enable **`registerCloudWebhook`** in the add-on settings.
The server registers with your openHAB Cloud service and you get a public HTTPS URL:

```text
https://myopenhab.org/api/hooks/<uuid>
```

The generated URL will be visible in the configuration menu under **Settings → Add-on Settings → Model Context Protocol (MCP) Server**.
Use this URL in any MCP client — no port forwarding or reverse proxy needed.
You can either include a static `oh.*` token in the client config, or let the client handle sign-in automatically via OAuth (Claude Desktop, ChatGPT, and `mcp-remote` all support this).

> **First-time OAuth login:** you'll sign in twice — once to the openHAB Cloud service, then again to openHAB itself to authorize the MCP client.
> Subsequent connections reuse stored tokens.

#### 3. mcp-remote bridge (stdio)

Some clients (like Claude Desktop) can also connect using a local Node.js bridge that translates between stdio and HTTP.
This requires [Node.js 18+](https://nodejs.org) installed on the machine running the client.
The bridge can point at either a direct LAN URL or a Cloud URL.

### Client setup

#### Claude Desktop

Edit `claude_desktop_config.json` (macOS: `~/Library/Application Support/Claude/claude_desktop_config.json`, Windows: `%APPDATA%\Claude\claude_desktop_config.json`).
Completely quit and reopen Claude Desktop after editing — you should see "openhab" in the MCP indicator.

**Option A — Local network with token:**

```json
{
  "mcpServers": {
    "openhab": {
      "command": "npx",
      "args": [
        "mcp-remote@latest",
        "http://openhab.local:8080/mcp",
        "--allow-http",
        "--header",
        "Authorization: Bearer oh.YOUR_TOKEN"
      ]
    }
  }
}
```

**Option B — openHAB Cloud with token:**

```json
{
  "mcpServers": {
    "openhab": {
      "command": "npx",
      "args": [
        "mcp-remote@latest",
        "https://myopenhab.org/api/hooks/<uuid>",
        "--header",
        "Authorization: Bearer oh.YOUR_TOKEN"
      ]
    }
  }
}
```

**Option C — openHAB Cloud with OAuth (no token needed):**

1. In the Claude app, go to **Settings → Connectors → Add Connector**.
1. Enter the Cloud hook URL (e.g. `https://myopenhab.org/api/hooks/<uuid>`).
1. Claude will open a browser to sign in the first time you connect.

No config file editing or Node.js required.

Options A and B require [Node.js 18+](https://nodejs.org).

#### Claude Code (CLI)

```bash
claude mcp add --transport http openhab http://openhab.local:8080/mcp \
    --header "Authorization: Bearer oh.YOUR_TOKEN"
```

Works with both direct LAN URLs and Cloud URLs.

#### ChatGPT (chatgpt.com, desktop, mobile)

ChatGPT requires a **public HTTPS URL**, so use the **openHAB Cloud** method.
ChatGPT handles sign-in through a browser OAuth prompt — no token pasting needed.
Requires a paid ChatGPT plan (Plus, Pro, Business, Enterprise, Education).

1. Turn on **`registerCloudWebhook`** in the add-on settings and copy the hook URL from the openHAB log.
1. In ChatGPT: **Settings → Apps & Connectors → Advanced settings** → toggle **Developer mode** on.
1. **Settings → Connectors → Create**.
1. Name: `openHAB`. Description: _"Smart-home control. Use this to query device state, turn things on/off, check temperatures, and create automations."_
1. Connector URL: the hook URL from step 1 (e.g. `https://myopenhab.org/api/hooks/<uuid>`).
1. Save — ChatGPT will walk you through sign-in the first time, then list the tools.

## Example prompts

### Query & control

- _"What's the status of my home right now?"_
- _"What rooms are in my house?"_
- _"Turn the kitchen lights off."_
- _"Dim the office to 30%."_

### Automations

- _"Turn on the office light at 7pm tonight."_ — one-shot rule, auto-deletes after it fires.
- _"Every day at sunset, turn on the porch light."_ — recurring schedule.
- _"When the garage door opens, turn on the garage light."_ — event-driven rule that runs even when you're not chatting.
- _"Delete the rules you created earlier."_ — the assistant can list and remove rules it set up.

### Watching for changes

- _"Keep an eye on the garage door — tell me if it opens while we're talking."_
- _"Is the dishwasher done yet?"_ — start watching once, ask again later for a summary.

> Current MCP clients don't surface server-pushed notifications to the LLM yet, so the assistant can only report on changes **when you ask** — it won't interrupt you proactively.
> That will improve as clients add support; the server is already ready for it.

## What the assistant can do

### Items

| Tool                 | What it does                                                                                                                                                                                                                                                                                                                                                                                                                                         |
| -------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `get_semantic_model` | Returns the home layout: Locations → Equipment → Points. The assistant usually calls this first to orient itself.                                                                                                                                                                                                                                                                                                                                    |
| `search_items`       | Fuzzy search across names, labels, and synonyms. Tolerates typos and word reordering.                                                                                                                                                                                                                                                                                                                                                                |
| `get_item`           | Current state and details for one or more items by exact name.                                                                                                                                                                                                                                                                                                                                                                                       |
| `manage_item`        | Create, update, or delete an item. `action='create'` (requires `name`+`type`), `action='update'` (label/tags/groups), `action='delete'` (also removes its links).                                                                                                                                                                                                                                                                                    |
| `set_item`           | Change an item's value. `action='command'` sends a command to control the device (ON/OFF, dimmer levels, etc.); `action='state'` sends an update to the item.                                                                                                                                                                                                                                                                                        |
| `get_home_status`    | A single snapshot: security (open doors/windows), active lights, climate, energy, device health.                                                                                                                                                                                                                                                                                                                                                     |
| `get_system_info`    | openHAB version, item/thing/rule counts, installed bindings, and the server's current date/time + timezone. Relative offsets like `"+30s"` or `"+1h"` on a `datetime` trigger are resolved server-side, so this tool isn't needed for simple "in N minutes" scheduling — the assistant calls it when it needs the server's wall-clock for reasoning ("is it morning?", "what day is it?") or for absolute scheduling against an unfamiliar timezone. |

### Things & links

| Tool                | What it does                                                                                                                                      |
| ------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------- |
| `get_things`        | Lists Things with online/offline status.                                                                                                          |
| `get_thing_details` | Channels, configuration, and linked items for one Thing.                                                                                          |
| `manage_link`       | List, create, or delete item-channel links. `action='get'` (filter by item name and/or channel UID prefix), `action='create'`, `action='delete'`. |

### Rules

| Tool          | What it does                                                                                                                                                                                    |
| ------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `get_rules`   | Lists automation rules. `tag='MCP'` scopes the list to rules the assistant created.                                                                                                             |
| `create_rule` | Creates a rule with one or more triggers, optional conditions, and a list of actions. See [Rule building blocks](#rule-building-blocks) below for the supported trigger/condition/action types. |
| `update_rule` | Modify a rule's name, description, tags, conditions, or actions without recreating it.                                                                                                          |
| `manage_rule` | Enable, disable, manually trigger, or delete a rule.                                                                                                                                            |

#### Rule building blocks

Each `create_rule` call has a `triggers` array (or a single `trigger`), an optional `conditions` array, and a required `actions` array. Each entry has a `type` discriminator plus type-specific fields.

**Triggers** (any matching trigger fires the rule):

| `type`              | Fields                                                                                                                                                                                                                                    |
| ------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `time_of_day`       | `time: "HH:MM"`                                                                                                                                                                                                                           |
| `cron`              | `cronExpression: "0 0 8 ? * MON-FRI"`                                                                                                                                                                                                     |
| `item_state_change` | `itemName`, optional `state`, optional `previousState`                                                                                                                                                                                    |
| `item_command`      | `itemName`, optional `command`                                                                                                                                                                                                            |
| `datetime`          | `datetime: "2026-04-17T15:00:00"` (absolute one-shot, auto-deleted) or a relative offset resolved against the server's clock: `"+30s"`, `"+5m"`, `"+2h"`, `"+1d"`, `"+1w"`; ISO duration `"PT30M"`, `"PT2H"`; ISO period `"P1D"`, `"P2W"` |

**Conditions** (all must pass for actions to run):

| `type`        | Fields                                                                                           |
| ------------- | ------------------------------------------------------------------------------------------------ |
| `item_state`  | `itemName`, `operator` (`=`/`!=`/`<`/`<=`/`>`/`>=`, default `=`), `state`                        |
| `time_of_day` | `startTime`, `endTime` (HH:MM)                                                                   |
| `day_of_week` | `days: ["MONDAY", "TUESDAY", …]`                                                                 |
| `ephemeris`   | `kind: "weekend"`/`"weekday"`/`"holiday"`/`"not_holiday"`/`"dayset"`, optional `offset`/`dayset` |
| `script`      | `script: "…"` — needs `enableScripting` and the JS scripting add-on                              |

**Actions** (executed in order when triggered and conditions pass):

| `type`              | Fields                                                                                                                                                                                                         |
| ------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `item_command`      | `itemName`, `command`                                                                                                                                                                                          |
| `item_state_update` | `itemName`, `state`                                                                                                                                                                                            |
| `notification`      | `scope: "user"` (default, requires `userId`) / `"broadcast"` / `"log"`; `message`; optional `title`, `icon`, `tag`, `referenceId`, `mediaAttachmentUrl`, `actionButton1-3`. Requires the openHAB Cloud add-on. |
| `run_rule`          | `ruleUIDs: [...]`, optional `considerConditions` (default `true`)                                                                                                                                              |
| `rule_enablement`   | `ruleUIDs: [...]`, `enable: true` or `false`                                                                                                                                                                   |
| `script`            | `script: "…"` — JavaScript via openhab-js; needs `enableScripting` and the JS scripting add-on                                                                                                                 |

### Watching for changes

| Tool          | What it does                                                                                                                                                |
| ------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `watch_items` | Start or stop tracking items for state changes in this session. `action='start'` (with `itemNames`), `action='stop'` (omit `itemNames` to stop everything). |
| `get_events`  | Return any state changes buffered since the last call and drain the buffer.                                                                                 |

### Improving how the assistant resolves names

Items have a built-in `synonyms` metadata namespace.
Add a comma-separated list of nicknames (e.g. `den, study, office nook` on your family room light) and the assistant will match them when you use those words.
Items can also be associated with a Location via the `hasLocation` metadata, not just Group membership.

## Full REST API access (opt-in)

> **Note:** With this flag on, the assistant can call **any** openHAB REST endpoint, including destructive ones (delete items, modify Things, change service configs).
> It uses your bearer token, so it can only do things your user could do from the UI.
> Only turn this on if you trust the assistant with that scope.

Setting `enableFullApiAccess=true` exposes three extra tools:

| Tool                    | What it does                                                                                                   |
| ----------------------- | -------------------------------------------------------------------------------------------------------------- |
| `list_api_endpoints`    | Enumerate every REST endpoint from openHAB's live OpenAPI spec. Filter by tag (`items`, `rules`, …) or method. |
| `describe_api_endpoint` | Return the schema fragment for one endpoint so the assistant knows what parameters to pass.                    |
| `call_api`              | Invoke any endpoint. Supports every HTTP method.                                                               |

Useful when the built-in tools don't cover the task, for example:

- _"Rename the metadata namespace 'alexa' to 'matter' on all items."_
- _"Query the last 24 hours of temperature persistence for the hallway sensor."_
- _"Approve the pending Zigbee discovery result for the bulb I just paired."_

## Scripting (opt-in)

> **Note:** Scripts run with the same privileges as any openHAB script — they can access every item, make HTTP requests via `actions.HTTP`, execute system commands via `actions.Exec`, look up OSGi services, and even create new rules.
> Only enable this if you trust the assistant (and the human directing it) with that scope.

Setting `enableScripting=true` does two things:

1. Unlocks `script` as a valid action and condition `type` in `create_rule` / `update_rule`, with a `script` field holding the JS source.
1. Exposes an extra `execute_script` tool.

| Tool             | What it does                                                                                                                                                                                                                                                                                                                                 |
| ---------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `execute_script` | Runs a JavaScript snippet immediately against the openHAB scripting engine and returns its result. Primary use is to **dry-run a script before adding it as a `script` action in a scheduled rule** — syntax and runtime errors come back structured so the assistant can fix them now rather than discover them when the rule fires at 3am. |

Scripts get the full openhab-js environment (`items`, `actions`, `things`, `rules`, `cache`, `time`, …).
Requires the `openhab-automation-jsscripting` add-on.

Things you can ask once scripting is on:

- _"At 7pm every weekday, dim the living room lights to 40% only if someone is home — write that as a script action."_
- _"My morning routine should check three weather items and adjust the thermostat — set that up as a rule."_
- _"Test a snippet that calculates the average of these temperature items before adding it to a rule."_

## Diagnostic logging (opt-in)

> **Note:** Reading logs is gated by `enableLoggingAccess` alone.
> Changing log levels additionally requires the connected user's bearer token to have **administrator** scope — openHAB's REST API enforces this, the MCP server just forwards the token.

Setting `enableLoggingAccess=true` exposes two tools:

| Tool               | What it does                                                                                                                                                                                                                                                                                                                                                         |
| ------------------ | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `get_logs`         | Reads recent log entries from openHAB's in-memory buffer (typically the last 500-5000 entries, with stack traces). Filters: `loggerFilter` (regex), `minLevel`, `sinceMs`, `sinceSequence`, `search`, `limit` (default 100, max 1000).                                                                                                                               |
| `manage_log_level` | `action='get'` lists current effective log levels via `GET /rest/logging/` (optional `loggerFilter` substring). `action='set'` changes one logger via `PUT /rest/logging/{name}` (or DELETE when `level="DEFAULT"`). Auto-reverts to the previous level after `revertAfterSeconds` (default **1800** = 30 min). Pass `revertAfterSeconds=0` for a persistent change. |

Things you can ask once this is enabled:

- _"My Zigbee binding has been flaky — what does the log say?"_
- _"Why didn't my evening lights rule fire last night?"_
- _"Turn on DEBUG logging for the Hue binding for a few minutes so I can reproduce something."_
- _"Has anything errored out in the last hour?"_

The assistant will scope log reads to the relevant binding by default. When increasing log verbosity, it auto-reverts after 30 minutes so DEBUG won't be left on overnight by accident.
It's also instructed to **ask for confirmation** before bumping infrastructure loggers (`org.eclipse.jetty.*`, `org.apache.karaf.*`, `org.apache.cxf.*`, `org.ops4j.pax.web.*`) since those can flood logs or hurt performance.

## Main UI design (opt-in)

Setting `enableUiDesign=true` lets the assistant design your Main UI: create pages, edit existing ones, and build reusable custom widgets.
The assistant knows about every page type (layout, home, tabbed, chart, map, floorplan), all the standard cards (toggle, slider, color picker, etc.), how to wire them to your items, and how to compose them — so you can describe what you want in plain English instead of editing YAML.

Things you can ask once this is enabled:

- _"Build me a kitchen dashboard with toggles for the lights, a slider for the dimmer, and a temperature reading at the top."_
- _"Create a chart page showing the last 24 hours of outdoor temperature and humidity."_
- _"Add a floor-plan page using the image I just uploaded — and place markers for each room's main light."_
- _"Make a tabbed page with a tab for each floor of the house."_
- _"Add a Sonos tab to my media page with controls for all four speakers."_
- _"Design a custom widget I can reuse for each smart blind — show its position and provide up/down/stop buttons."_
- _"Rename the 'Living Room' page title to 'Family Room'."_
- _"Delete the test page I asked you to make yesterday."_

The five UI tools the assistant has available (you don't need to call these directly — just ask in plain English):

| Tool                    | What it does                                                                                                                                                                                          |
| ----------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `list_widgets`          | Browse the catalog of available components, optionally by category (page types, standard cards, layout primitives, list items, map/plan markers).                                                     |
| `describe_widget`       | Look up the full prop and slot schema for any component, so the assistant writes valid configuration the first time.                                                                                  |
| `get_page_skeleton`     | Get a starter scaffold for any of the 6 page types with sensible defaults.                                                                                                                            |
| `manage_ui_component`   | Create / read / update / patch / delete pages and custom widgets through `/rest/ui/components/{namespace}`. Single-field edits use an efficient patch path instead of re-sending the whole component. |
| `validate_ui_component` | Pre-check a composed page or widget against the schema before saving — catches typos, missing required props, and bad slot names.                                                                     |

**Custom widgets:** when the assistant creates a custom widget, it lives in the `widget` namespace and can be reused across any page as `widget:<your-uid>`.
The widget can declare its own parameters (item name, color, threshold, etc.) so a single widget definition serves many devices.

**Tip — visual verification:** if your agent also has a browser-automation server connected (e.g. Claude in Chrome, Playwright MCP), the assistant will offer to screenshot the rendered page so you can both see what was built.
It'll ask you once for the URL you use to reach openHAB (e.g. `http://openhab.local:8080`) since the internal hostname isn't always reachable from a remote browser.

> **UI Design Token Usage:** UI design uses noticeably more of an agent's context window than simple item control. Pages and especially custom widgets are large JSON structures (tens of KB each), and the assistant often reads, edits, and re-reads them several times in one design session. Expect to start fresh chats more often when working on the UI — particularly for big dashboards or multi-widget projects. The assistant has been tuned to keep responses small (minimal write confirmations, in-place edits instead of full rewrites) but the underlying data is inherently large.
>
> **Recommended clients for UI design:** because of the above, coding-oriented agents like [Claude Code](https://www.claude.com/product/claude-code), [Codex CLI](https://github.com/openai/codex), or [Cursor](https://cursor.com) work much better here than chat-only clients. They have larger context windows, can spawn sub-agents that each get their own fresh context for focused sub-tasks (e.g. "design this one widget"), and can combine the MCP server with a browser-automation MCP (Claude in Chrome, Playwright MCP) in the same session so the agent can build a page and then screenshot the result to verify it. Chat-only clients like Claude Desktop and ChatGPT still work for small edits, but you'll hit context limits sooner on larger projects.

## Static assets (opt-in)

Setting `enableStaticAssets=true` lets the assistant manage the files in `$OPENHAB_CONF/html`, which is the folder served at `http://<your-openhab>:8080/static/*`.
These are static assets: SVGs, images (PNG/JPG/GIF/WebP/ICO), CSS overrides, and small text/JS files — anything a Main UI component or rule references by URL.

> SVGs can be particularly useful for agents to manipulate, the assistant can author and edit the markup directly, and openHAB can bind to elements inside an SVG to make it dynamic, so you can ask for a custom icon or diagram and have it react to item states.

Pairs naturally with `enableUiDesign`, but mind the split: this flag manages the **asset** (the image or SVG file), while the **page or widget** that displays it is a Main UI component handled by the design tools.

Things you can ask once this is enabled (typically combined with UI design):

- _"Draw an SVG icon for a garage door and upload it as `icons/garage-door.svg`."_ — the assistant writes the SVG markup and uploads it, ready to reference from a widget.
- _"Upload the attached photo to `images/living-room.jpg`."_
- _"What images do I have under `images/`?"_ — lists the folder so you can reuse one.
- _"Replace my custom CSS with this update."_ — reads the current `css/custom.css`, applies the change, writes it back.
- _"Delete the test icon you uploaded yesterday."_

| Tool                  | What it does                                                                                           |
| --------------------- | ------------------------------------------------------------------------------------------------------ |
| `manage_static_asset` | List, read, upload, and delete files under `$OPENHAB_CONF/html`. Uploads are capped at 10 MB per call. |

Only common asset types are allowed (images, SVG, CSS, JS, and a few text formats), and the assistant needs an ADMIN-scoped token to use the tool at all.

## Real-time subscriptions (advanced)

> Most clients (including Claude Desktop) don't expose this to the LLM yet.
> Use `watch_items` / `get_events` instead for now — they work with every client.

MCP clients that honour `notifications/resources/updated` can subscribe to these URIs and receive push notifications:

| URI template                    | Fires on                               |
| ------------------------------- | -------------------------------------- |
| `openhab://item/{itemName}`     | Item state changes                     |
| `openhab://thing/{thingUID}`    | Thing online/offline transitions       |
| `openhab://rule/{ruleUID}`      | Rule status transitions                |
| `openhab://home/semantic-model` | The full Location/Equipment/Point tree |

High-frequency items (power meters, fast sensors) are coalesced to at most one notification per `resourceCoalesceMs` window.

## Troubleshooting

Enable debug logging from the openHAB console to see every request and response:

```text
log:set DEBUG org.openhab.io.mcp
```

For raw wire-level dumps (headers and full body), turn on `TRACE`:

```text
log:set TRACE org.openhab.io.mcp
```
