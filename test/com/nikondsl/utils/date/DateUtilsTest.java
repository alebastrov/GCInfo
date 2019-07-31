package com.nikondsl.utils.date;

import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DateUtilsTest {
	@Test
	public void remainTest() throws Exception {
		assertEquals("2 час 50 мин 47 сек", DateUtils.getRemainingTime(Locale.forLanguageTag("ru"), 0, 10247666, 4));
		assertEquals("2 hours 50 min 47 sec", DateUtils.getRemainingTime(Locale.ENGLISH, 0, 10247666, 4));
	}
}
