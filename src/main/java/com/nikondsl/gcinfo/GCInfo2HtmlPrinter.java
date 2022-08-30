package com.nikondsl.gcinfo;

import com.nikondsl.gcinfo.convertions.ConvertionUtils;
import com.nikondsl.gcinfo.convertions.ConvertorType;
import com.nikondsl.gcinfo.date.DateUtils;
import com.nikondsl.gcinfo.monitoring.gc.ColoredUsagePercentage;
import com.nikondsl.gcinfo.monitoring.gc.GCInfoBlock;
import com.nikondsl.gcinfo.monitoring.gc.GCInfoCollector;
import com.nikondsl.gcinfo.monitoring.gc.GCInfoReflector;

import java.lang.management.ManagementFactory;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class GCInfo2HtmlPrinter {

    public String getHtml() throws Exception {

        GCInfoCollector collector = GCInfoCollector.getGCInfoCollector(TimeUnit.SECONDS.toMillis(10));
        List<GCInfoBlock> all = collector.getAll();
        GCInfoReflector reflector = new GCInfoReflector();

        /**
         * collector.gcNotificationInfo.getGcInfo().toCompositeData(null).get("xxx"), where "xxx"
         *
         * "0cThreadCount"
         * "duration"
         * "endTime"
         * "id"
         * "memoryUsageAfterGc"
         * "memoryUsageBeforeGc"
         * "startTime"
         *
         *
         * for instance -> collector.gcNotificationInfo.getGcInfo().toCompositeData(null).get("memoryUsageAfterGc")
         * ((TabularDataSupport) collector.gcNotificationInfo.getGcInfo().toCompositeData(null).get("memoryUsageAfterGc")).values().stream().map( cds -> ((CompositeDataSupport)cds).values().stream().map(map -> map.getClass()).collect(Collectors.toList())).collect(Collectors.toList());
         *
         * that returns a table AfterGC
         * ((TabularDataSupport) collector.gcNotificationInfo.getGcInfo().toCompositeData(null).get("memoryUsageAfterGc")).values().stream().map( cds -> ((CompositeDataSupport)cds).values().stream()
         *         .map(map -> {
         *             if (map.getClass().isAssignableFrom(String.class)) {
         *                 return map.toString();
         *             }
         *             return ((CompositeDataSupport)map).get("used");
         *         })
         *         .collect(Collectors.toList())).collect(Collectors.toList());
         *
         * !!!!!!!!!!for CMS GC:
         *     "Compressed Class Space" -> 687096
         *     "PS Survivor Space" -> 0
         *     "PS Old Gen" -> 1263640
         *     "Metaspace" -> 6315920
         *     "PS Eden Space" -> 0
         *     "Code Cache" -> 3962304
         *
         *  !!!!!!!!!!for ZGC:
         *     "Compressed Class Space" -> 687096
         *     "PS Survivor Space" -> 0
         *     "PS Old Gen" -> 1263102
         *     "Metaspace" -> 6314776
         *     "PS Eden Space" -> 0
         *     "Code Cache" -> 3962304
         */


        StringBuilder result = new StringBuilder(1024);
        result.append("<div class='title'><title>GC Statistics - " + collector.getLastGcState() + "</title></div>");
        String color = collector.getLastGcState() == GCInfoBlock.Payloads.SLOWDOWN ? "red" : "green";
        result.append("<div class='gcstatus'>GC Statistics - " +
                "<span style=\"color:"+color+"\">" + collector.getLastGcState() + "</span></div>");
        result.append("<div class='gctotalinfo'><p>Uptime (according to gc.log): <b>" +
                DateUtils.getRemainingTime(Locale.ENGLISH, 0L, ManagementFactory.getRuntimeMXBean().getUptime(), 2) +
                " (since " + new Date(System.currentTimeMillis() - ManagementFactory.getRuntimeMXBean().getUptime()) + ")</b></p></div>");
        //horizontal used memory bar
        double maxMemory = Runtime.getRuntime().maxMemory();
        double totalMemory = Runtime.getRuntime().totalMemory();
        double freeMemory = Runtime.getRuntime().freeMemory();
        double percGreen = 100.0 * (maxMemory - totalMemory) / maxMemory;
        double percRed = 100.0 * (totalMemory - freeMemory) / maxMemory;
        result.append("<div class='currentmemory'><p><u>Current memory</u></p>");

        result.append("<p>Max Java memory (-Xmx): <b>"+ ConvertionUtils.convertToString(ConvertorType.Long2ComputerBytes, maxMemory, 2)+"</b><br>");
        result.append("Total memory: <b>"+ConvertionUtils.convertToString(ConvertorType.Long2ComputerBytes, totalMemory, 2)+"</b><br>");
        result.append("Free allocated memory: <b>"+ConvertionUtils.convertToString(ConvertorType.Long2ComputerBytes, freeMemory, 2)+"</b><br>");
        result.append("Used memory: <b style='color:red;'>"+ConvertionUtils.convertToString(ConvertorType.Long2ComputerBytes, (totalMemory - freeMemory), 2)+"</b></p>");

        result.append("<div class='memorytable'><table style='width:100%; height:10px; border:1px solid black;' cellpadding='1' cellspacing='0'>\n<tr>");
        result.append("\n<td style='background-color:red; width:"+percRed+ "%;'></td>");
        result.append("\n<td style='background-color:white; width:"+(100.0-percGreen-percRed)+ "%;'></td>");
        result.append("\n<td style='background-color:green; width:"+percGreen+ "%;'></td>");
        result.append("</tr>\n</table></div></div>");

        //vertical GC bars
        result.append("<p><u>GC history</u></p><p>Events recorded: <b>"+all.size()+"</b></p>");
        result.append("<div class='gctable'><table width='100%' align='center' cellpadding='1' " +
                "cellspacing='0' height='400' " +
                "style='border-color:gray; border-style: solid; border-width: 1px'><tr>");
        for (GCInfoBlock info :  all) {
            result.append("<td width='3' valign='bottom'>\n" +
                    reflector.getImage(info, reflector.getBlockPixelsCount(ColoredUsagePercentage.RED, info.getMaxMemory(), info.getUsedMemory()), ColoredUsagePercentage.RED, 1) +
                    reflector.getImage(info, reflector.getBlockPixelsCount(ColoredUsagePercentage.YELLOW, info.getMaxMemory(), info.getUsedMemory()), ColoredUsagePercentage.YELLOW, 2) +
                    reflector.getImage(info, reflector.getBlockPixelsCount(ColoredUsagePercentage.GREEN, info.getMaxMemory(), info.getUsedMemory()), ColoredUsagePercentage.GREEN, 3) +
                    "\n</td>");
        }
        result.append("<td width='99%' valign='bottom' colspan='3'>&nbsp;</td>");
        result.append("</tr></table></div>");
        String blackBar = getExampleImage("black", "reflects serious slowdown or STW of GC");
        String redBar = getExampleImage("red", "reflects slight slowdown or high amount of work for GC");
        String yellowBar = getExampleImage("yellow", "reflects GC is under pressure");
        String greenBar = getExampleImage("green", "reflects small GC job");
        String smallGreenBar = getExampleImage("green", "",20, 10);
        String narrowGreenBar = getExampleImage("green", "",4, 20);
        String blueBar = getExampleImage("blue", "reflects idle of GC", 20, 2);
        result.append("<br/><div class='gclegend' id='gclegendid'><u>GC legend</u><br/><table><tr>" +
                "<td width='20'>"+blackBar+"</td><td>GC initiates StopTheWorld or takes too long time</td></tr>" +
                "<tr><td width='20'>"+redBar+"</td><td>GC takes too long time and under high pressure</td></tr>" +
                "<tr><td width='20'>"+yellowBar+"</td><td>GC is under pressure</td></tr>" +
                "<tr><td width='20'>"+greenBar+"</td><td>GC does not a big deal amount of work</td></tr>" +
                "<tr><td width='20' height='2'>"+blueBar+"</td><td>Blue bar reflects GC's NOP or idle</td></tr>" +
                "<tr><td width='4'>"+narrowGreenBar+"</td><td>Bar width depends on how long GC takes</td></tr>" +
                "<tr><td width='20' height='10'>"+smallGreenBar+"</td><td>Bar height depends on how much memory GC freed</td></tr>" +
                "</table></div>");
        return result.toString();
    }

    private String getExampleImage(String color, String title, int width, int height) {
        return "<img src='"+GCInfoReflector.TRANSPARENT_1X1_IMAGE_SOURCE +"' width='"+width+"' height='"+height+"' " +
                "style='background-color:" + color + "'" +
                " alt='"+title+"'" +
                " title='" + title + "' />";
    }
    private String getExampleImage(String color, String title) {
        return getExampleImage(color, title, 20, 20);
    }

    public static void main(String[] args) throws Exception {
        GCInfoCollector.getGCInfoCollector(TimeUnit.SECONDS.toMillis(10));
        for(int j = 0; j< 1000; j++) {
            int max = 100;
            double[][] arr = new double[max][];
            for (int i = 0; i < max; i++) {
                arr[i] = new double[100000];
                Thread.sleep(10);
            }
            System.err.println(j);
            arr = null;

            Thread.sleep(100);
            GCInfo2HtmlPrinter printer = new GCInfo2HtmlPrinter();
            System.gc();
            if (j%16==0) {
                System.err.println("<html><body>" + printer.getHtml() + "</body></html>");
            }
        }
    }
}
