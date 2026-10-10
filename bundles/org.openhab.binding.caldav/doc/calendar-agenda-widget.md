# Calendar Agenda Widget

This MainUI personal widget displays the events published by one CalDAV Calendar Thing.
It supports day grouping, current-event markers, all-day events and optional synchronization information.
The widget is an example that you install in MainUI.

## Items

Create Items linked to the Calendar Thing channels before configuring the widget.
The binding README includes an [Item configuration example](../README.md#item-configuration).

| Widget Parameter | Item Type | Channel | Required |
| --- | --- | --- | --- |
| `eventsItem` | String | `events#json` | Yes |
| `calendarColorItem` | String | `calendar-color` | No |
| `syncStatusItem` | String | `sync#status` | No |
| `lastSyncItem` | DateTime | `sync#last` | No |
| `truncatedItem` | Switch | `events#truncated` | No |

## Installation

1. Open **Developer Tools → Widgets** in MainUI and create a new personal widget.
1. Open its code editor and set the root `uid` to `caldav_agenda`.
1. From the complete YAML below, copy the contents of `widgets.caldav_agenda`: `tags`, `props`, `component`, `config` and `slots`.
   Remove their four-space indentation so they are at the same level as `uid`.
   The `version` and `widgets` wrapper belongs to the file format and is not part of the individual widget definition in this editor.
1. Save the widget, add it to a page and select the String Item linked to `events#json` for **Events**.
1. Configure any optional Items and display settings.

The widget's `maxEntries` parameter defaults to five and limits the displayed rows.
The Calendar Thing configuration controls the available time range.

## Calendar Color

Select the String Item linked to `calendar-color` in the optional `calendarColorItem` parameter to use the server-provided calendar color.
Set the optional `color` parameter to a CSS color when you want a manual override.

The color precedence is: **manual color → calendar-color Item → widget default**.
Empty manual values and missing, blank, `NULL` or `UNDEF` Item values fall through to the next choice.
The existing defaults inherit the event title color and use the theme primary color for the current-event marker.
Calendar color values are used as supplied, including case and alpha.

## Complete Widget YAML

The following is the complete existing widget source, including its file wrapper.

```yaml
# CalDAV Agenda — MainUI personal widget, concept v11 audit.
# Import in MainUI: Developer Tools > Widgets > Create > YAML editor.
# MainUI catches JSON parse errors; the outer repeater retains the result for validation.
# Time-dependent labels update when Item states change or MainUI renders the widget again.
# Date-only ends are exclusive. Text is rendered through Label, never as HTML.
version: 1
widgets:
  caldav_agenda:
    tags:
      - calendar
      - caldav
      - agenda
    props:
      parameterGroups:
        - name: data
          label: Daten / Data
        - name: display
          label: Darstellung / Display
      parameters:
        - name: eventsItem
          label: Termine / Events
          type: TEXT
          required: true
          groupName: data
          description: String-Item am Channel events#json auswählen. / Select the String item linked to events#json.
          context: item
        - name: calendarColorItem
          label: Kalenderfarbe / Calendar color
          type: TEXT
          required: false
          groupName: data
          description: 'Optional: String-Item am Channel calendar-color. / String item linked to calendar-color.'
          context: item
          filterCriteria:
            - name: type
              value: String
        - name: color
          label: Eigene Farbe / Manual color
          type: TEXT
          required: false
          groupName: display
          description: 'Optional: CSS-Farbe mit Vorrang vor der Kalenderfarbe. / CSS color overriding the calendar color.'
        - name: title
          label: Überschrift / Title
          type: TEXT
          required: false
          groupName: display
        - name: maxEntries
          label: Maximale Einträge / Maximum entries
          type: INTEGER
          required: false
          groupName: display
          default: 5
          description: Begrenzt nur die Anzeige, nicht den Binding-Zeitraum. / Limits displayed rows only, not the binding
            range.
        - name: groupByDay
          label: Nach Tagen gruppieren / Group by day
          type: BOOLEAN
          required: false
          groupName: display
          default: true
        - name: showCurrent
          label: Laufende Termine / Current events
          type: BOOLEAN
          required: false
          groupName: display
          default: true
        - name: showEndTime
          label: Endzeit / End time
          type: BOOLEAN
          required: false
          groupName: display
          default: true
        - name: showLocation
          label: Ort / Location
          type: BOOLEAN
          required: false
          groupName: display
          default: true
        - name: showDescription
          label: Beschreibung / Description
          type: BOOLEAN
          required: false
          groupName: display
          default: false
        - name: compact
          label: Kompakt / Compact
          type: BOOLEAN
          required: false
          groupName: display
          default: false
        - name: syncStatusItem
          label: Sync-Status / Sync status
          type: TEXT
          required: false
          groupName: data
          description: 'Optional: sync#status'
          context: item
        - name: lastSyncItem
          label: Letzter Sync / Last sync
          type: TEXT
          required: false
          groupName: data
          description: 'Optional: sync#last'
          context: item
        - name: truncatedItem
          label: Gekürzte Liste / Truncated list
          type: TEXT
          required: false
          groupName: data
          description: 'Optional: events#truncated'
          context: item
    component: oh-context
    config:
      functions:
        text: '=(de, en) => dayjs.locale().startsWith(''de'') ? de : en'
        state: '=(name) => name && items[name] ? items[name].state : '''''
        itemColor: >-
          =(value) => value === '' + value && ['NULL', 'UNDEF'].indexOf(value.trim()) < 0 ? value.trim() : ''
        # MainUI has no Array global; inspect the object tag of parsed data.
        list: =(value) => ({}).toString.call(value) === '[object Array]'
        # ISO offsets may contain seconds; Day.js needs those converted to an instant first.
        parseDate: >-
          =(value) => value === '' + value && /[+-]\d{2}:\d{2}:\d{2}$/.test(value) ? dayjs(value.substring(0, value.length - 9) + 'Z').subtract((+value.substring(value.length - 8, value.length - 6) * 3600 + +value.substring(value.length - 5, value.length - 3) * 60 + +value.substring(value.length - 2)) * (value.substring(value.length - 9, value.length - 8) === '-' ? -1 : 1), 'second') : dayjs(value)
    slots:
      default:
        - component: oh-context
          config:
            functions:
              color: >-
                =(fallback) => (props.color || '').trim() || fn.itemColor(fn.state(props.calendarColorItem)) || fallback
              date: >-
                =(value, allDay) => value === '' + value && (allDay ? /^\d{4}-\d{2}-\d{2}$/.test(value) : /^\d{4}-\d{2}-\d{2}T([01]\d|2[0-3]):[0-5]\d(:[0-5]\d(\.\d{1,9})?)?(Z|[+-]([01]\d|2[0-3]):[0-5]\d(:[0-5]\d)?)$/.test(value))
                && fn.parseDate(value).isValid() && dayjs(value.substring(0,10)).format('YYYY-MM-DD') === value.substring(0,10)
              current: >-
                =(event) => fn.parseDate(event.start).valueOf() <= dayjs().valueOf() && fn.parseDate(event.end).valueOf() > dayjs().valueOf()
              day: =(value) => fn.parseDate(value).format('YYYY-MM-DD')
              dayTitle: >-
                =(value) => fn.parseDate(value).isToday() ? (dayjs.locale().startsWith('de') ? 'Heute' : 'Today') : fn.parseDate(value).isTomorrow()
                ? (dayjs.locale().startsWith('de') ? 'Morgen' : 'Tomorrow') : fn.parseDate(value).format(dayjs.locale().startsWith('de')
                ? 'dddd, DD.MM.' : 'dddd, D MMM')
          slots:
            default:
              - component: oh-context
                config:
                  functions:
                    valid: >-
                      =(e) => !!e && JSON.stringify(e).startsWith('{') && e.instanceId === '' + e.instanceId && e.instanceId.length
                      > 0 && e.uid === '' + e.uid && e.title === '' + e.title && (e.allDay === true || e.allDay === false)
                      && fn.date(e.start, e.allDay) === true && fn.date(e.end, e.allDay) === true && fn.parseDate(e.end).valueOf() >= fn.parseDate(e.start).valueOf()
                    time: >-
                      =(e) => e.allDay ? fn.text('Ganztägig', 'All day') + (props.groupByDay === false ? ' · ' + fn.parseDate(e.start).format('L')
                      : '') + (props.showEndTime !== false && fn.parseDate(e.end).diff(fn.parseDate(e.start), 'day') > 1 ? ' · ' + fn.parseDate(e.start).format('L')
                      + ' – ' + fn.parseDate(e.end).subtract(1, 'day').format('L') : '') : (props.groupByDay === false || fn.day(e.start)
                      !== fn.day(e.end) ? fn.parseDate(e.start).format('L') + ' ' : '') + fn.parseDate(e.start).format('LT') + (props.showEndTime
                      !== false ? ' – ' + (fn.day(e.start) !== fn.day(e.end) ? fn.parseDate(e.end).format('L') + ' ' : '') + fn.parseDate(e.end).format('LT')
                      : '')
                slots:
                  default:
                    - component: oh-repeater
                      config:
                        for: data
                        sourceType: array
                        in:
                          - available: =!!fn.state(props.eventsItem) && ['NULL', 'UNDEF'].indexOf(fn.state(props.eventsItem))
                              < 0
                            events: =JSON.parse(fn.state(props.eventsItem) || 'null')
                        fragment: true
                      slots:
                        default:
                          - component: oh-repeater
                            config:
                              for: agenda
                              sourceType: array
                              in:
                                - valid: =fn.list(loop.data.events) === true && loop.data.events.every(e => fn.valid(e) === true)
                                  events: >-
                                    =fn.list(loop.data.events) === true && loop.data.events.every(e => fn.valid(e) === true) ? loop.data.events.filter(e
                                    => fn.parseDate(e.end).valueOf() > dayjs().valueOf() && (props.showCurrent !== false || !fn.current(e))).slice(0,
                                    Math.max(1, Math.floor(Number(props.maxEntries) || 5))) : []
                              fragment: true
                            slots:
                              default:
                                - component: f7-card
                                  config:
                                    title: =props.title
                                  slots:
                                    default:
                                      - component: f7-card-content
                                        slots:
                                          default:
                                            - component: Label
                                              config:
                                                text: =fn.text('Kalenderdaten noch nicht verfügbar', 'Calendar data not yet
                                                  available')
                                                visible: =loop.data.available !== true
                                            - component: Label
                                              config:
                                                text: =fn.text('Kalenderdaten konnten nicht dargestellt werden.', 'Calendar
                                                  data could not be displayed.')
                                                visible: =loop.data.available === true && (loop.agenda.valid !== true || fn.list(loop.agenda.events) !== true)
                                            - component: Label
                                              config:
                                                text: =fn.text('Keine kommenden Termine', 'No upcoming events')
                                                visible: =loop.data.available === true && loop.agenda.valid === true && fn.list(loop.agenda.events) === true && loop.agenda.events.length
                                                  === 0
                                            - component: f7-list
                                              config:
                                                mediaList: true
                                                style:
                                                  margin: '0'
                                              slots:
                                                default:
                                                  - component: oh-repeater
                                                    config:
                                                      for: event
                                                      sourceType: array
                                                      in: '=loop.agenda.valid === true && fn.list(loop.agenda.events) === true ? [].concat(loop.agenda.events) : []'
                                                      fragment: true
                                                    slots:
                                                      default:
                                                        - component: f7-list-item
                                                          config:
                                                            groupTitle: true
                                                            title: =fn.dayTitle(loop.event.start)
                                                            visible: >-
                                                              =props.groupByDay !== false && (loop.event_idx === 0 || fn.day(loop.event.start)
                                                              !== fn.day(loop.event_source[loop.event_idx - 1].start))
                                                        - component: f7-list-item
                                                          config:
                                                            style:
                                                              --f7-list-item-min-height: '=props.compact ? ''36px'' : ''52px'''
                                                          slots:
                                                            default:
                                                              - component: Label
                                                                config:
                                                                  text: '=fn.current(loop.event) ? fn.text(''JETZT'', ''NOW'')
                                                                    : '''''
                                                                  visible: =fn.current(loop.event)
                                                                  style:
                                                                    display: block
                                                                    color: =fn.color('var(--f7-theme-color)')
                                                                    font-weight: '700'
                                                                    font-size: 12px
                                                              - component: Label
                                                                config:
                                                                  text: =fn.time(loop.event)
                                                                  style:
                                                                    display: block
                                                                    color: var(--f7-list-item-subtitle-text-color)
                                                                    font-size: 13px
                                                              - component: Label
                                                                config:
                                                                  text: =loop.event.title
                                                                  style:
                                                                    display: block
                                                                    color: =fn.color('inherit')
                                                                    overflow-wrap: anywhere
                                                                    white-space: pre-wrap
                                                                    font-weight: '600'
                                                              - component: Label
                                                                config:
                                                                  text: =loop.event.location
                                                                  visible: >-
                                                                    =props.showLocation !== false && loop.event.location ===
                                                                    '' + loop.event.location && loop.event.location.length
                                                                    > 0
                                                                  style: &id001
                                                                    display: block
                                                                    overflow-wrap: anywhere
                                                                    white-space: pre-wrap
                                                              - component: Label
                                                                config:
                                                                  text: =loop.event.description
                                                                  visible: >-
                                                                    =props.showDescription === true && loop.event.description
                                                                    === '' + loop.event.description && loop.event.description.length
                                                                    > 0
                                                                  style: *id001
                                            - component: Label
                                              config:
                                                text: =fn.text('Teilweise synchronisiert', 'Partially synchronized')
                                                visible: =fn.state(props.syncStatusItem) === 'PARTIAL'
                                            - component: Label
                                              config:
                                                text: =fn.text('Daten möglicherweise veraltet', 'Data may be outdated')
                                                visible: >-
                                                  =loop.data.available === true && loop.agenda.valid === true && fn.list(loop.agenda.events) === true && ['ERROR', 'OFFLINE', 'COMMUNICATION_ERROR',
                                                  'AUTHENTICATION_ERROR', 'CONFIGURATION_ERROR', 'GONE'].indexOf(fn.state(props.syncStatusItem))
                                                  >= 0
                                            - component: Label
                                              config:
                                                text: =fn.text('Weitere Termine sind vorhanden.', 'Additional events are available.')
                                                visible: =fn.state(props.truncatedItem) === 'ON'
                                            - component: Label
                                              config:
                                                text: '=fn.text(''Letzter Sync: '', ''Last sync: '') + fn.parseDate(fn.state(props.lastSyncItem)).format(''L
                                                  LT'')'
                                                visible: =!!props.lastSyncItem && fn.date(fn.state(props.lastSyncItem), false) === true
                                                style:
                                                  display: block
                                                  font-size: 12px
                                                  opacity: '0.7'
                                                  margin-top: 8px
```
