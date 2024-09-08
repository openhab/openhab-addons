import static org.hamcrest.CoreMatchers.*
import static org.hamcrest.MatcherAssert.assertThat

assertThat(actions, instanceOf(Object))
assertThat(events, instanceOf(Object))
assertThat(ir, instanceOf(Object))
assertThat(itemRegistry, instanceOf(Object))
assertThat(ir, is(itemRegistry))
assertThat(items, instanceOf(Object))
assertThat(rules, instanceOf(Object))
assertThat(se, instanceOf(Object))
assertThat(scriptExtension, instanceOf(Object))
assertThat(se, is(scriptExtension))
assertThat(things, instanceOf(Object))

assertThat(State, instanceOf(Class))
assertThat(State.getCanonicalName(), is("org.openhab.core.types.State"))

assertThat(Command, instanceOf(Class))
assertThat(Command.getCanonicalName(), is("org.openhab.core.types.Command"))

assertThat(URLEncoder, instanceOf(Class))
assertThat(URLEncoder.getCanonicalName(), is("java.net.URLEncoder"))

assertThat(File, instanceOf(Class))
assertThat(File.getCanonicalName(), is("java.io.File"))

assertThat(Files, instanceOf(Class))
assertThat(Files.getCanonicalName(), is("java.nio.file.Files"))

assertThat(Path, instanceOf(Class))
assertThat(Path.getCanonicalName(), is("java.nio.file.Path"))

assertThat(Paths, instanceOf(Class))
assertThat(Paths.getCanonicalName(), is("java.nio.file.Paths"))

assertThat(IncreaseDecreaseType, instanceOf(Class))
assertThat(IncreaseDecreaseType.getCanonicalName(), is("org.openhab.core.library.types.IncreaseDecreaseType"))
assertThat(DECREASE, instanceOf(IncreaseDecreaseType))
assertThat(DECREASE, is(IncreaseDecreaseType.DECREASE))
assertThat(INCREASE, instanceOf(IncreaseDecreaseType))
assertThat(INCREASE, is(IncreaseDecreaseType.INCREASE))

assertThat(OnOffType, instanceOf(Class))
assertThat(OnOffType.getCanonicalName(), is("org.openhab.core.library.types.OnOffType"))
assertThat(ON, instanceOf(OnOffType))
assertThat(ON, is(OnOffType.ON))
assertThat(OFF, instanceOf(OnOffType))
assertThat(OFF, is(OnOffType.OFF))

assertThat(OpenClosedType, instanceOf(Class))
assertThat(OpenClosedType.getCanonicalName(), is("org.openhab.core.library.types.OpenClosedType"))
assertThat(CLOSED, instanceOf(OpenClosedType))
assertThat(CLOSED, is(OpenClosedType.CLOSED))
assertThat(OPEN, instanceOf(OpenClosedType))
assertThat(OPEN, is(OpenClosedType.OPEN))

assertThat(StopMoveType, instanceOf(Class))
assertThat(StopMoveType.getCanonicalName(), is("org.openhab.core.library.types.StopMoveType"))
assertThat(MOVE, instanceOf(StopMoveType))
assertThat(MOVE, is(StopMoveType.MOVE))
assertThat(STOP, instanceOf(StopMoveType))
assertThat(STOP, is(StopMoveType.STOP))

assertThat(UpDownType, instanceOf(Class))
assertThat(UpDownType.getCanonicalName(), is("org.openhab.core.library.types.UpDownType"))
assertThat(DOWN, instanceOf(UpDownType))
assertThat(DOWN, is(UpDownType.DOWN))
assertThat(UP, instanceOf(UpDownType))
assertThat(UP, is(UpDownType.UP))

assertThat(UnDefType, instanceOf(Class))
assertThat(UnDefType.getCanonicalName(), is("org.openhab.core.types.UnDefType"))
assertThat(NULL, instanceOf(UnDefType))
assertThat(NULL, is(UnDefType.NULL))
assertThat(UNDEF, instanceOf(UnDefType))
assertThat(UNDEF, is(UnDefType.UNDEF))

assertThat(RefreshType, instanceOf(Class))
assertThat(RefreshType.getCanonicalName(), is("org.openhab.core.types.RefreshType"))
assertThat(REFRESH, instanceOf(RefreshType))
assertThat(REFRESH, is(RefreshType.REFRESH))

assertThat(NextPreviousType, instanceOf(Class))
assertThat(NextPreviousType.getCanonicalName(), is("org.openhab.core.library.types.NextPreviousType"))
assertThat(NEXT, instanceOf(NextPreviousType))
assertThat(NEXT, is(NextPreviousType.NEXT))
assertThat(PREVIOUS, instanceOf(NextPreviousType))
assertThat(PREVIOUS, is(NextPreviousType.PREVIOUS))

assertThat(PlayPauseType, instanceOf(Class))
assertThat(PlayPauseType.getCanonicalName(), is("org.openhab.core.library.types.PlayPauseType"))
assertThat(PLAY, instanceOf(PlayPauseType))
assertThat(PLAY, is(PlayPauseType.PLAY))
assertThat(PAUSE, instanceOf(PlayPauseType))
assertThat(PAUSE, is(PlayPauseType.PAUSE))

assertThat(RewindFastforwardType, instanceOf(Class))
assertThat(RewindFastforwardType.getCanonicalName(), is("org.openhab.core.library.types.RewindFastforwardType"))
assertThat(REWIND, instanceOf(RewindFastforwardType))
assertThat(REWIND, is(RewindFastforwardType.REWIND))
assertThat(FASTFORWARD, instanceOf(RewindFastforwardType))
assertThat(FASTFORWARD, is(RewindFastforwardType.FASTFORWARD))

assertThat(QuantityType, instanceOf(Class))
assertThat(QuantityType.getCanonicalName(), is("org.openhab.core.library.types.QuantityType"))

assertThat(StringListType, instanceOf(Class))
assertThat(StringListType.getCanonicalName(), is("org.openhab.core.library.types.StringListType"))

assertThat(RawType, instanceOf(Class))
assertThat(RawType.getCanonicalName(), is("org.openhab.core.library.types.RawType"))

assertThat(DateTimeType, instanceOf(Class))
assertThat(DateTimeType.getCanonicalName(), is("org.openhab.core.library.types.DateTimeType"))

assertThat(DecimalType, instanceOf(Class))
assertThat(DecimalType.getCanonicalName(), is("org.openhab.core.library.types.DecimalType"))

assertThat(HSBType, instanceOf(Class))
assertThat(HSBType.getCanonicalName(), is("org.openhab.core.library.types.HSBType"))

assertThat(PercentType, instanceOf(Class))
assertThat(PercentType.getCanonicalName(), is("org.openhab.core.library.types.PercentType"))

assertThat(PointType, instanceOf(Class))
assertThat(PointType.getCanonicalName(), is("org.openhab.core.library.types.PointType"))

assertThat(StringType, instanceOf(Class))
assertThat(StringType.getCanonicalName(), is("org.openhab.core.library.types.StringType"))

assertThat(ImperialUnits, instanceOf(Class))
assertThat(ImperialUnits.getCanonicalName(), is("org.openhab.core.library.unit.ImperialUnits"))

assertThat(MetricPrefix, instanceOf(Class))
assertThat(MetricPrefix.getCanonicalName(), is("org.openhab.core.library.unit.MetricPrefix"))

assertThat(SIUnits, instanceOf(Class))
assertThat(SIUnits.getCanonicalName(), is("org.openhab.core.library.unit.SIUnits"))

assertThat(Units, instanceOf(Class))
assertThat(Units.getCanonicalName(), is("org.openhab.core.library.unit.Units"))

assertThat(BinaryPrefix, instanceOf(Class))
assertThat(BinaryPrefix.getCanonicalName(), is("org.openhab.core.library.unit.BinaryPrefix"))

assertThat(ChronoUnit, instanceOf(Class))
assertThat(ChronoUnit.getCanonicalName(), is("java.time.temporal.ChronoUnit"))

assertThat(DayOfWeek, instanceOf(Class))
assertThat(DayOfWeek.getCanonicalName(), is("java.time.DayOfWeek"))

assertThat(Duration, instanceOf(Class))
assertThat(Duration.getCanonicalName(), is("java.time.Duration"))

assertThat(Month, instanceOf(Class))
assertThat(Month.getCanonicalName(), is("java.time.Month"))

assertThat(ZoneId, instanceOf(Class))
assertThat(ZoneId.getCanonicalName(), is("java.time.ZoneId"))

assertThat(ZonedDateTime, instanceOf(Class))
assertThat(ZonedDateTime.getCanonicalName(), is("java.time.ZonedDateTime"))
