package com.nikondsl.utils.convertions;

/**
 * Created by IntelliJ IDEA.
 * User: igor.nikonov
 * Date: 27 серп 2009
 * Time: 10:08:04
 * To change this template use File | Settings | File Templates.
 */
public class ConvertionUtils {
  private ConvertionUtils(){}

  public static String convertToString(final Convertor convertor, Double number, int blocks, boolean withWhiteSpice) {
    if (convertor==null) throw new IllegalArgumentException();
    if (number==null || number.isNaN()) throw new IllegalArgumentException("Cannot be null or NaN, but was "+number);
    if (number<0.0) throw new IllegalArgumentException("Number should be more than 0, but was "+number);
    if (number.isInfinite()) return "Infinite";
    if (number==0.0) return "0";
    Convertor.Names names=convertor.getNames();
    //divide from max divider
    int count=1;
    StringBuilder result=new StringBuilder(32);
    for (int i=0; i<names.getNames().size(); i++){
      Convertor.Holder holder=names.getNames().get(i);
      double head=Math.floor(number.doubleValue()/holder.getDivider());
      if (head<=0){
        continue;
      }
      number=(double)(number-head*holder.getDivider());
      result.append((long)head).append(withWhiteSpice ? " " : "").append((long)head==1?holder.getNameSingular():holder.getNamePlural()).append(" ");
      if (blocks>0 && count>=blocks) break;
      count++;
    }
    return result.toString().trim();
  }

  public static String convertToString(final Convertor convertor, Double number, int blocks) {
    return convertToString(convertor, number, blocks, true);
  }
}
