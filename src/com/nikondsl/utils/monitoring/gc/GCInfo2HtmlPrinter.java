package com.nikondsl.utils.monitoring.gc;

import com.nikondsl.utils.convertions.ComputerBytesConverter;
import com.nikondsl.utils.convertions.ConvertionUtils;
import com.nikondsl.utils.date.DateUtils;

import java.lang.management.ManagementFactory;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class GCInfo2HtmlPrinter {
    public String getHtml() {

        GCInfoCollector collector = GCInfoCollector.getGCInfoCollector(TimeUnit.SECONDS.toMillis(10));
        List<GCInfoBlock> all = collector.getAll();
        GCInfoReflector reflector = new GCInfoReflector();

        StringBuilder result = new StringBuilder(1024);
        result.append("<title>GC Statistics - " + collector.getLastGcState() + "</title>");
        String color = collector.getLastGcState() == GCInfoBlock.Payloads.SLOWDOWN ? "red" : "green";
        result.append("<h3>GC Statistics - " +
                "<span style=\"color:"+color+"\">" + collector.getLastGcState() + "</span></h3>");
        result.append("<p>Uptime (according to gc.log): <b>" +
                DateUtils.getRemainingTime(Locale.ENGLISH, 0L, ManagementFactory.getRuntimeMXBean().getUptime(), 2) +
                " (since " + new Date(System.currentTimeMillis() - ManagementFactory.getRuntimeMXBean().getUptime()) + ")</b></p>");
        //horizontal used memory bar
        double maxMemory = Runtime.getRuntime().maxMemory();
        double totalMemory = Runtime.getRuntime().totalMemory();
        double freeMemory = Runtime.getRuntime().freeMemory();
        double percGreen = 100.0 * (maxMemory - totalMemory) / maxMemory;
        double percRed = 100.0 * (totalMemory - freeMemory) / maxMemory;
        result.append("<div class='current_memory'><p>Current memory usage:</p>");

        result.append("<p>Max Java memory (-Xmx): <b>"+ConvertionUtils.convertToString(ComputerBytesConverter.createConverter(), maxMemory, 2)+"</b><br>");
        result.append("Total memory: <b>"+ConvertionUtils.convertToString(ComputerBytesConverter.createConverter(), totalMemory, 2)+"</b><br>");
        result.append("Free allocated memory: <b>"+ConvertionUtils.convertToString(ComputerBytesConverter.createConverter(), freeMemory, 2)+"</b><br>");
        result.append("Used memory: <b style='color:red;'>"+ConvertionUtils.convertToString(ComputerBytesConverter.createConverter(), (totalMemory - freeMemory), 2)+"</b></p>");

        result.append("<p><table style='width: 100%; height:10px; border: 1px solid black;' cellpadding='1' cellspacing='0'>\n<tr>");
        result.append("\n<td style='background-color:red; width:"+percRed+ ";'></td>");
        result.append("\n<td style='background-color:white; width:"+(100.0-percGreen-percRed)+ ";'></td>");
        result.append("\n<td style='background-color:green; width:"+percGreen+ ";'></td>");
        result.append("</tr>\n</table></p></div>");

        //vertical GC bars
        result.append("<div class='current_gc'><p>Current GC statistics:</p><p> <table width='100%' align='center' cellpadding='1' cellspacing='0' height='400' " +
                "style='border-color:gray; border-style: solid; border-width: 1px'><tr>");
        for (GCInfoBlock info :  all) {
            result.append("<td width='3' valign='bottom'>\n" +
                    reflector.getImage(info, reflector.getBlockPixelsCount(Percentage.Red, info.getMaxMemory(), info.getUsedMemory()), Percentage.Red, 1) +
                    reflector.getImage(info, reflector.getBlockPixelsCount(Percentage.Yellow, info.getMaxMemory(), info.getUsedMemory()), Percentage.Yellow, 2) +
                    reflector.getImage(info, reflector.getBlockPixelsCount(Percentage.Green, info.getMaxMemory(), info.getUsedMemory()), Percentage.Green, 3) +
                    "\n</td>");
        }
        result.append("<td width='99%' valign='bottom' colspan='3'>&nbsp;</td>");
        result.append("</tr></table></p></div>");
        return result.toString();
    }

    public static void main(String[] args) throws InterruptedException {
        GCInfoCollector.getGCInfoCollector(TimeUnit.SECONDS.toMillis(10));
        int max = 100;
        double[][] arr = new double[max][];
        for (int i=0; i< max; i++) {
            arr[i] = new double[100000];
            Thread.sleep(100);
        }
        GCInfo2HtmlPrinter printer = new GCInfo2HtmlPrinter();
        System.err.println(""+printer.getHtml());
    }
}
