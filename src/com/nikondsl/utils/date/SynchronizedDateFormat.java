package com.nikondsl.utils.date;

import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.TimeZone;

/**
 *
 * @author: Taras Puchko (taras.puchko).
 * @date: Jul 13, 2007
 */
public class SynchronizedDateFormat {

    private SimpleDateFormat format;

    public SynchronizedDateFormat(String pattern) {
        this.format = new SimpleDateFormat(pattern);
    }

    public synchronized String format(Date date) {
        return format.format(date);
    }

    public synchronized Date parse(String date) throws ParseException {
        return format.parse(date);
    }

    public synchronized void setTimeZone(TimeZone timeZone) {
        format.setTimeZone(timeZone);
    }
}
