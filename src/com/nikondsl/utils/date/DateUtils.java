package com.nikondsl.utils.date;

import com.nikondsl.utils.AppUtil;
import com.nikondsl.utils.convertions.ConvertionUtils;
import com.nikondsl.utils.convertions.DateConvertor;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class DateUtils {
    private GregorianCalendar calendar = new GregorianCalendar();

    private static final Map<Locale, Map<String, String>> translations = new HashMap<>();
    private static final String TIME_MAP_YEAR = "year";
    private static final String TIME_MAP_MONTH = "month";
    private static final String TIME_MAP_DAY = "day";
    private static final String TIME_MAP_HOUR = "hour";
    private static final String TIME_MAP_MINUTE = "minute";
    private static final String TIME_MAP_SECOND = "second";
    private static final String TIME_MAP_MILISECOND = "milisecond";


    /**
     * @param from  long - must be in UTC timezone
     * @param to    long - must be in UTC timezone
     * @param count int @return String
     */
    public static String getRemainingTime(Locale locale, long from, long to, int count) {
        DateUtils sd = new DateUtils();
        sd.setTime(to);
        Map<String, Integer> remTimeMap = sd.getRemainTime(new java.util.Date(from));
        String remain = "";
        int yyyy = AppUtil.getIntValue("" + remTimeMap.get(TIME_MAP_YEAR));
        int mmm = AppUtil.getIntValue("" + remTimeMap.get(TIME_MAP_MONTH));
        int dd = AppUtil.getIntValue("" + remTimeMap.get(TIME_MAP_DAY));
        int hh = AppUtil.getIntValue("" + remTimeMap.get(TIME_MAP_HOUR));
        int mm = AppUtil.getIntValue("" + remTimeMap.get(TIME_MAP_MINUTE));
        int ss = AppUtil.getIntValue("" + remTimeMap.get(TIME_MAP_SECOND));
        if (count > 0 && yyyy > 0) {
            remain += yyyy + " " + getTranslation(locale, TIME_MAP_YEAR, yyyy) + " ";//"year"
            count--;
        }
        if (count > 0 && mmm > 0) {
            remain += "" + mmm + " " + getTranslation(locale, TIME_MAP_MONTH, mmm) + " ";//"month";
            count--;
        }
        if (count > 0 && dd > 0) {
            remain += dd + " " + getTranslation(locale, TIME_MAP_DAY, dd) + " ";//"day";
            count--;
        }
        if (count > 0 && hh > 0) {
            remain += hh + " " + getTranslation(locale, TIME_MAP_HOUR, hh) + " ";//"hour";
            count--;
        }
        if (count > 0 && mm > 0) {
            remain += mm + " " + getTranslation(locale, TIME_MAP_MINUTE, mm) + " ";//"min";
            count--;
        }
        if (count > 0 && ss > 0) {
            remain += ss + " " + getTranslation(locale, TIME_MAP_SECOND, ss) + " ";//"sec";
        }
        if (yyyy + mmm + dd + hh + mm + ss == 0) {
            remain += (to - from) + " " + getTranslation(locale, TIME_MAP_MILISECOND, 0) + " ";//"ms";
        }
        return remain.trim();
    }

    public Map<String, Integer> getRemainTime(Date date) {
        Map<String, Integer> result = new HashMap<>();
        String converted = ConvertionUtils.convertToString(DateConvertor.getConvertor(), Math.abs((getTime() - date.getTime()) / 1000.0), 0);
        String[] str = converted.split("\\s+", 0);
        int number;
        int i = 0;
        for (String s : str) {
            if (s.startsWith("year")) {
                number = AppUtil.getIntValue(str[i - 1]);
                result.put(TIME_MAP_YEAR, number);
            } else if (s.startsWith("month")) {
                number = AppUtil.getIntValue(str[i - 1]);
                result.put(TIME_MAP_MONTH, number);
            } else if (s.startsWith("day")) {
                number = AppUtil.getIntValue(str[i - 1]);
                result.put(TIME_MAP_DAY, number);
            } else if (s.startsWith("hour")) {
                number = AppUtil.getIntValue(str[i - 1]);
                result.put(TIME_MAP_HOUR, number);
            } else if (s.startsWith("minute")) {
                number = AppUtil.getIntValue(str[i - 1]);
                result.put(TIME_MAP_MINUTE, number);
            } else if (s.startsWith("second")) {
                number = AppUtil.getIntValue(str[i - 1]);
                result.put(TIME_MAP_SECOND, number);
            }
            i++;
        }
        int millis = (int) (date.getTime() - (date.getTime() / 1000L) * 1000L);
        result.put(TIME_MAP_MILISECOND, millis);
        return result;
    }

    private static String getTranslation(Locale loc, String key, int number) {
        synchronized (translations) {
            Map<String, String> transMap = translations.get(loc);
            if (transMap == null) transMap = translations.get(Locale.US);
            if ((loc == Locale.ENGLISH || loc == Locale.US || loc == Locale.UK) && number > 1) {
                return transMap.get(key) + (key==TIME_MAP_MINUTE || key==TIME_MAP_SECOND?"":"s");
            }
            return transMap.get(key);
        }
    }

    public void setTime(long time) {
        calendar.setTime(new Date(time));
    }

    public long getTime() {
        return calendar.getTimeInMillis();
    }

    public static void addTranslation(Locale locale, String[] translate) {
        if (locale == null) return;
        Map<String, String> transMap = new HashMap<>(9);
        transMap.put(TIME_MAP_YEAR, translate[0]);
        transMap.put(TIME_MAP_MONTH, translate[1]);
        transMap.put(TIME_MAP_DAY, translate[2]);
        transMap.put(TIME_MAP_HOUR, translate[3]);
        transMap.put(TIME_MAP_MINUTE, translate[4]);
        transMap.put(TIME_MAP_SECOND, translate[5]);
        transMap.put(TIME_MAP_MILISECOND, translate[6]);
        synchronized (translations) {
            if (translations.get(locale) == null) translations.put(locale, transMap);
        }
    }

    static {
        addTranslation(Locale.ENGLISH, new String[]{"year", "month", "day", "hour", "min", "sec", "ms"});
        addTranslation(Locale.forLanguageTag("ru"), new String[]{"год", "месяц", "день", "час", "мин", "сек", "миллисек"});
    }
}
