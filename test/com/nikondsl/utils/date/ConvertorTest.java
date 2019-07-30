package com.nikondsl.utils.date;


import com.nikondsl.utils.convertions.ComputerBytesConvertor;
import com.nikondsl.utils.convertions.Convertor;
import com.nikondsl.utils.convertions.ConvertionUtils;
import com.nikondsl.utils.convertions.HumanBytesConvertor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConvertorTest {
	@Test
	public void testComputer() {
		Convertor convertor = ComputerBytesConvertor.getConvertor();
		assertEquals("1 byte", ConvertionUtils.convertToString(convertor, 1.0,2));
		assertEquals("123 bytes", ConvertionUtils.convertToString(convertor, 123.0,2));
		assertEquals("1 KiB 210 bytes", ConvertionUtils.convertToString(convertor, 1234.0,2));
		assertEquals("12 KiB 57 bytes", ConvertionUtils.convertToString(convertor, 12345.0,2));
		assertEquals("120 KiB 576 bytes", ConvertionUtils.convertToString(convertor, 123456.0,2));
		assertEquals("120 KiB 1 byte", ConvertionUtils.convertToString(convertor, 123456.0-575.0,2));
		assertEquals("117 MiB 755 KiB", ConvertionUtils.convertToString(convertor, 123456789.0,2));
		assertEquals("11 GiB 509 MiB 775 KiB 52 bytes", ConvertionUtils.convertToString(convertor, 12345678900.0,4));
	}
	
	@Test
	public void testHuman() {
		Convertor convertor = HumanBytesConvertor.getConvertor();
		assertEquals("1 byte", ConvertionUtils.convertToString(convertor, 1.0,2));
		assertEquals("123 bytes", ConvertionUtils.convertToString(convertor, 123.0,2));
		assertEquals("1 KB 234 bytes", ConvertionUtils.convertToString(convertor, 1234.0,2));
		assertEquals("12 KB 345 bytes", ConvertionUtils.convertToString(convertor, 12345.0,2));
		assertEquals("123 KB 456 bytes", ConvertionUtils.convertToString(convertor, 123456.0,2));
		assertEquals("122 KB 1 byte", ConvertionUtils.convertToString(convertor, 122001.0,2));
		assertEquals("123 MB 456 KB", ConvertionUtils.convertToString(convertor, 123456789.0,2));
		assertEquals("12 GB 345 MB 678 KB 900 bytes", ConvertionUtils.convertToString(convertor, 12345678900.0,4));
	}
}
