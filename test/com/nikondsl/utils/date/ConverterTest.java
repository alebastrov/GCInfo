package com.nikondsl.utils.date;


import com.nikondsl.utils.convertions.ComputerBytesConverter;
import com.nikondsl.utils.convertions.Converter;
import com.nikondsl.utils.convertions.ConvertionUtils;
import com.nikondsl.utils.convertions.HumanBytesConverter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConverterTest {
	@Test
	public void testComputer() {
		Converter converter = ComputerBytesConverter.createConverter();
		assertEquals("1 byte", ConvertionUtils.convertToString(converter, 1.0,2));
		assertEquals("123 bytes", ConvertionUtils.convertToString(converter, 123.0,2));
		assertEquals("1 KiB 210 bytes", ConvertionUtils.convertToString(converter, 1234.0,2));
		assertEquals("12 KiB 57 bytes", ConvertionUtils.convertToString(converter, 12345.0,2));
		assertEquals("120 KiB 576 bytes", ConvertionUtils.convertToString(converter, 123456.0,2));
		assertEquals("120 KiB 1 byte", ConvertionUtils.convertToString(converter, 123456.0-575.0,2));
		assertEquals("117 MiB 755 KiB", ConvertionUtils.convertToString(converter, 123456789.0,2));
		assertEquals("11 GiB 509 MiB 775 KiB 52 bytes", ConvertionUtils.convertToString(converter, 12345678900.0,4));
	}
	
	@Test
	public void testHuman() {
		Converter converter = HumanBytesConverter.createConverter();
		assertEquals("1 byte", ConvertionUtils.convertToString(converter, 1.0,2));
		assertEquals("123 bytes", ConvertionUtils.convertToString(converter, 123.0,2));
		assertEquals("1 KB 234 bytes", ConvertionUtils.convertToString(converter, 1234.0,2));
		assertEquals("12 KB 345 bytes", ConvertionUtils.convertToString(converter, 12345.0,2));
		assertEquals("123 KB 456 bytes", ConvertionUtils.convertToString(converter, 123456.0,2));
		assertEquals("122 KB 1 byte", ConvertionUtils.convertToString(converter, 122001.0,2));
		assertEquals("123 MB 456 KB", ConvertionUtils.convertToString(converter, 123456789.0,2));
		assertEquals("12 GB 345 MB 678 KB 900 bytes", ConvertionUtils.convertToString(converter, 12345678900.0,4));
	}
}
