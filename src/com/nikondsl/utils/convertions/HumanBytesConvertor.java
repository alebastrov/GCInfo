package com.nikondsl.utils.convertions;

/**
 * Created by IntelliJ IDEA.
 * User: igor.nikonov
 * Date: 27 серп 2009
 * Time: 10:44:32
 * To change this template use File | Settings | File Templates.
 */
public class HumanBytesConvertor extends Convertor {
  private static final Convertor convertor = new HumanBytesConvertor();

  private HumanBytesConvertor() {
  }

  public Convertor.Names getNames() {
    Convertor.Names names=new Convertor.Names();
    names.add(new Convertor.Holder("Tb", "Tb", 1000*1000*1000*1000L));
    names.add(new Convertor.Holder("Gb", "Gb", 1000*1000*1000L));
    names.add(new Convertor.Holder("Mb", "Mb", 1000*1000L));
    names.add(new Convertor.Holder("Kb", "Kb", 1000L));
    names.add(new Convertor.Holder("byte", "bytes", 1L));
    return names;
  }

  public static Convertor createConvertor() {
    return convertor;
  }
}
