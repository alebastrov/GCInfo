package com.nikondsl.utils.monitoring.gc;

public enum ColoredUsagePercentage {
    GREEN("green", 50, 0), YELLOW("yellow", 40, 50), RED("red", 10, 90);
    private int percents;
    private int eliminator;
    private String color;

    ColoredUsagePercentage(String color, int percents, int eliminator) {
        this.color = color;
        this.percents = percents;
        this.eliminator = eliminator;
    }

    public int getPercents() {
        return percents;
    }

    public int getEliminator() {
        return eliminator;
    }

    public String getColor() {
        return color;
    }
}
