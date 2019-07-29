package com.nikondsl.utils.convertions;

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
  protected static class Holder{
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

  protected static class Names{
    private List<Holder> names=new ArrayList<Holder>();
    protected void add(Holder holder){
      names.add(holder);
    }
    List<Holder> getNames(){
      return Collections.unmodifiableList(names);
    }
  }
  public abstract Names getNames();
}
