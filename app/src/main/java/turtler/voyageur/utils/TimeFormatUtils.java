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
        Date startDate = null;
        try {
            startDate = df.parse(dateString);
            return startDate;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return startDate;
    }
}
