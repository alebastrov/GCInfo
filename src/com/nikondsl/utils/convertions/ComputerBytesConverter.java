package com.nikondsl.utils.convertions;

/**
 * Created by IntelliJ IDEA.
 * User: igor.nikonov
 * Date: 27 серп 2009
 * Time: 10:41:09
 * To change this template use File | Settings | File Templates.
 */
public class ComputerBytesConverter extends Converter {
  private static final Converter CONVERTER = new ComputerBytesConverter();

  public static final int KiB = 1024;
  public static final int MiB = KiB * KiB;
  public static final int GiB = MiB * KiB;
  public static final long TiB = (long) GiB * KiB;

  private ComputerBytesConverter() {
  }

  public Names getNames() {
    Names names=new Names();
    names.add(new Holder("TiB", "TiB", TiB));
    names.add(new Holder("GiB", "GiB", GiB));
    names.add(new Holder("MiB", "MiB", MiB));
    names.add(new Holder("KiB", "KiB", KiB));
    names.add(new Holder("byte", "bytes", 1L));
    return names;
  }

  public static Converter createConverter() {
    return CONVERTER;
  }
}
