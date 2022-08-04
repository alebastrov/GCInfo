package com.nikondsl.gcinfo.convertions;

import com.nikondsl.gcinfo.convertions.utils.ConvertorBuilder;
import com.nikondsl.gcinfo.convertions.impl.Convertor;

/**
 * Created by IntelliJ IDEA.
 * User: igor.nikonov
 * Date: 27 серп 2009
 * Time: 10:08:04
 * This class is start point of convertion:
 * Usage:
 *
 * ConvertionUtils.convertToString(ConvertorType.Long2ComputerBytes, 1025, 2) will give you result '1 KiB 1 byte'
 * ConvertionUtils.convertToString(ConvertorType.Millis2Date, 3661.0, 1) will give you result '1 hour'
 * ConvertionUtils.convertToString(ConvertorType.Millis2Date, 3661.0, 2) will give you result '1 hour 1 minute'
 * ConvertionUtils.convertToString(ConvertorType.Millis2Date, 3661.0, 3) will give you result '1 hour 1 minute 1 second'
 * ConvertionUtils.convertToString(ConvertorType.Millis2Date, 3662.0, 3) will give you result '1 hour 1 minute 2 seconds'
 */
public class ConvertionUtils {
  private ConvertionUtils(){}

  private static String convertToString(final Convertor convertor, Double number, int blocks) {
    if (convertor == null) throw new IllegalArgumentException();
    if (number == null || number.isNaN()) throw new IllegalArgumentException("Cannot be null or NaN, but was "+number);
    if (number < 0.0) throw new IllegalArgumentException("Number should be more than, or equals to  0, but was "+number);
    if (number.isInfinite()) return "Infinite";
    if (number == 0.0) return "0";
    Convertor.Names names = convertor.getNames();
    //divide from max divider
    int count=1;
    StringBuilder result = new StringBuilder(32);
    int i = 0;
    while (i < names.getNameHolders().size()) {
      Convertor.Holder holder=names.getNameHolders().get(i);
      double head = Math.floor(number / holder.getDivider());
      if (head <= 0) {
        i++;
        continue;
      }
      number = number - head*holder.getDivider();
      result.append((long)head).append(" ").append((long)head == 1
              ? holder.getNameSingular()
              : holder.getNamePlural()).append(" ");
      if (blocks > 0 && count >= blocks) {
        break;
      }
      count++;
      i++;
    }
    return result.toString().trim();
  }
  
  public static String convertToString(final ConvertorType convertorType, Double number) throws Exception {
    return convertToString(convertorType, number, 2);
  }

  public static String convertToString(final ConvertorType convertorType, Double number, int blocks) throws ReflectiveOperationException {
    Convertor convertor = ConvertorBuilder.create(convertorType);
    return convertToString(convertor, number, blocks);
  }
}
