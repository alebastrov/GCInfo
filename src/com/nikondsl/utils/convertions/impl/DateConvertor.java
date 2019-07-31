package com.nikondsl.utils.convertions.impl;

/**
 * Created by IntelliJ IDEA.
 * User: igor.nikonov
 * Date: 27 серп 2009
 * Time: 11:06:22
 * Use seconds
 */
public class DateConvertor extends Convertor {
  private static final Convertor CONVERTOR = new DateConvertor();
  private static final double secondsInYear = 525948.766 * 60.0;
  private static final double secondsInMonth = 43829.0639 * 60.0;
  private static final double secondsInDay = 24.0 * 3600.0;

  public Names getNames() {
    Names names=new Names();
    names.add(new Holder("year", "years", secondsInYear));
    names.add(new Holder("month", "months", secondsInMonth));
    names.add(new Holder("day", "days", secondsInDay));
    names.add(new Holder("hour", "hours", 3600.0));
    names.add(new Holder("minute", "minutes", 60.0));
    names.add(new Holder("second", "seconds", 1.0));
    names.add(new Holder("ms", "ms", 0.001));
    return names;
  }

  public static Convertor getConvertor() {
    return CONVERTOR;
  }
}
