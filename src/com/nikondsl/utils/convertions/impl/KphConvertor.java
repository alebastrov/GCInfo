package com.nikondsl.utils.convertions.impl;

/**
 * Created with IntelliJ IDEA.
 * User: igor.nikonov
 * Date: 7/8/13
 * Time: 12:58 PM
 * To change this template use File | Settings | File Templates.
 */
public class KphConvertor extends Convertor {
  private static final Convertor CONVERTOR = new KphConvertor();

  private KphConvertor() {
  }

  public static Convertor createConvertor() {
    return CONVERTOR;
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
