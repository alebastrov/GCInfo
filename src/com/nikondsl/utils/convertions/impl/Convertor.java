package com.nikondsl.utils.convertions.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: igor.nikonov
 * Date: 27 серп 2009
 * Time: 10:38:34
 * To change this template use File | Settings | File Templates.
 */
public abstract class Convertor {
  public static class Holder {
    private String namePlural;
    private double divider;
    private String nameSingular;

    Holder(String nameSingular, String namePlural, double divider){
      this.namePlural = namePlural;
      this.divider = divider;
      this.nameSingular = nameSingular;
    }

    public double getDivider() {
      return divider;
    }

    public String getNameSingular() {
      return nameSingular;
    }

    public String getNamePlural() {
      return namePlural;
    }
  }

  public static class Names {
    private List<Holder> nameHolders =new ArrayList<>();
    protected void add(Holder holder){
      nameHolders.add(holder);
    }
    public List<Holder> getNameHolders(){
      return Collections.unmodifiableList(nameHolders);
    }
  }
  public abstract Names getNames();
}
