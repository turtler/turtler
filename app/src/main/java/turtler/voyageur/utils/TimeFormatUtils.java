package turtler.voyageur.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by cwong on 9/10/16.
 */
public class TimeFormatUtils {
    public static String dateToString(Date d) {
        String format = "MM/dd/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
        return sdf.format(d);
    }

    public static Date strToDate(String dateString) {
        SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");
        Date date = null;
        try {
            date = df.parse(dateString);
            return date;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public static String dateTimeToString(Date d) {
        String format = "MM/dd/yyyy HH:mm";
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
        return sdf.format(d);
    }

    public static Date strToDateTime(String dateString) {
        SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm");
        Date date = null;
        try {
            date = df.parse(dateString);
            return date;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }
}
