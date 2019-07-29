package com.nikondsl.utils.convertions;

/**
 * Created with IntelliJ IDEA.
 * User: igor.nikonov
 * Date: 7/8/13
 * Time: 12:58 PM
 * To change this template use File | Settings | File Templates.
 */
public class KphConverter extends Converter {
  private static final Converter CONVERTER = new KphConverter();

  private KphConverter() {
  }

  public static Converter createConvertor() {
    return CONVERTER;
  }

  @Override
  public Names getNames() {
    Names names=new Names();
    names.add(new Holder("Tph", "Tph", 1000000000.0));
    names.add(new Holder("Gph", "Gph", 1000000.0));
    names.add(new Holder("Mph", "Mph", 1000.0));
    names.add(new Holder("Kph", "Kph", 1.0));
    names.add(new Holder("ph", "ph", 0.001));
    return names;
  }
}
