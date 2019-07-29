package com.nikondsl.utils.convertions;

/**
 * Created by IntelliJ IDEA.
 * User: igor.nikonov
 * Date: 27 серп 2009
 * Time: 10:41:09
 * To change this template use File | Settings | File Templates.
 */
public class ComputerBytesConvertor extends Convertor {
  private static final Convertor convertor = new ComputerBytesConvertor();

  public static final int KiB = 1024;
  public static final int MiB = KiB * KiB;
  public static final int GiB = MiB * KiB;
  public static final long TiB = (long) GiB * KiB;

  private ComputerBytesConvertor() {
  }

  public Names getNames() {
    Names names=new Names();
    names.add(new Holder("Tb", "Tb", TiB));
    names.add(new Holder("Gb", "Gb", GiB));
    names.add(new Holder("Mb", "Mb", MiB));
    names.add(new Holder("Kb", "Kb", KiB));
    names.add(new Holder("byte", "bytes", 1L));
    return names;
  }

  public static Convertor createConvertor() {
    return convertor;
  }
}
