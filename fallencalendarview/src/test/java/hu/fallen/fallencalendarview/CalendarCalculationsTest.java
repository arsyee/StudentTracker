package hu.fallen.fallencalendarview;

import org.junit.Test;

import java.util.Calendar;

import static org.junit.Assert.*;

public class CalendarCalculationsTest {
    @Test
    public void firstDayOfWeek() {
        Calendar calendar2018 = Calendar.getInstance();
        calendar2018.set(2018, 5-1, 4);
        assertEquals(Calendar.MONDAY, YearView.firstDayOfYear(calendar2018));

        Calendar calendar2017 = Calendar.getInstance();
        calendar2017.set(2017, 12-1, 5);
        assertEquals(Calendar.SUNDAY, YearView.firstDayOfYear(calendar2017));

        Calendar calendar2017_1 = Calendar.getInstance();
        calendar2017_1.set(2017, 10-1, 1);
        assertEquals(Calendar.SUNDAY, YearView.firstDayOfYear(calendar2017_1));

        Calendar calendar2017_2 = Calendar.getInstance();
        calendar2017_2.set(2017, 10-1, 1);
        calendar2017_2.setFirstDayOfWeek(Calendar.MONDAY);
        assertEquals(Calendar.SUNDAY, YearView.firstDayOfYear(calendar2017_2));

        for (int year = 1992; year < 2007; ++year) {
            Calendar firstOfJan = Calendar.getInstance();
            firstOfJan.set(year, 0, 1);
            assertEquals(firstOfJan.get(Calendar.DAY_OF_WEEK), YearView.firstDayOfYear(firstOfJan));
            for (int month = 2; month < 12; month += 2) {
                for (int day = 3; day < 29; day += 4) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(year, month, day);
                    calendar.setFirstDayOfWeek(year*month*day % 7 + 1);
                    assertEquals(firstOfJan.get(Calendar.DAY_OF_WEEK), YearView.firstDayOfYear(calendar));
                }
            }
        }
    }

}
