package com.nikondsl.gcinfo.convertions.impl;

/**
 * Created by IntelliJ IDEA.
 * User: igor.nikonov
 * Date: 27 серп 2009
 * Time: 10:44:32
 * To change this template use File | Settings | File Templates.
 */
public class HumanBytesConvertor extends Convertor {
  private static final Convertor CONVERTOR = new HumanBytesConvertor();

  private HumanBytesConvertor() {
  }

  public Convertor.Names getNames() {
    Convertor.Names names=new Convertor.Names();
    names.add(new Convertor.Holder("TB", "TB", 1000*1000*1000*1000L));
    names.add(new Convertor.Holder("GB", "GB", 1000*1000*1000L));
    names.add(new Convertor.Holder("MB", "MB", 1000*1000L));
    names.add(new Convertor.Holder("KB", "KB", 1000L));
    names.add(new Convertor.Holder("byte", "bytes", 1L));
    return names;
  }

  public static Convertor getConvertor() {
    return CONVERTOR;
  }
}
