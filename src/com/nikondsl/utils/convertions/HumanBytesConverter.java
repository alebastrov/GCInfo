package com.nikondsl.utils.convertions;

/**
 * Created by IntelliJ IDEA.
 * User: igor.nikonov
 * Date: 27 серп 2009
 * Time: 10:44:32
 * To change this template use File | Settings | File Templates.
 */
public class HumanBytesConverter extends Converter {
  private static final Converter CONVERTER = new HumanBytesConverter();

  private HumanBytesConverter() {
  }

  public Converter.Names getNames() {
    Converter.Names names=new Converter.Names();
    names.add(new Converter.Holder("TB", "TB", 1000*1000*1000*1000L));
    names.add(new Converter.Holder("GB", "GB", 1000*1000*1000L));
    names.add(new Converter.Holder("MB", "MB", 1000*1000L));
    names.add(new Converter.Holder("KB", "KB", 1000L));
    names.add(new Converter.Holder("byte", "bytes", 1L));
    return names;
  }

  public static Converter createConverter() {
    return CONVERTER;
  }
}
