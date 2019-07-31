package com.nikondsl.utils.convertions;

import com.nikondsl.utils.convertions.impl.ComputerBytesConvertor;
import com.nikondsl.utils.convertions.impl.Convertor;
import com.nikondsl.utils.convertions.impl.DateConvertor;
import com.nikondsl.utils.convertions.impl.HumanBytesConvertor;
import com.nikondsl.utils.convertions.impl.KphConvertor;

public enum ConvertorType {
	//converts given double number to computer based format (KiB, MiB, etc) i.e. 1024 multiplicator is used
	Long2ComputerBytes(ComputerBytesConvertor.class),
	//converts given double number to human based format (KB, MB, etc) i.e. 1000 multiplicator is used
	Long2HumanBytes(HumanBytesConvertor.class),
	//converts given double number in seconds to format how long it takes i.e. 61 -> 1 minute 1 second
	Millis2Date(DateConvertor.class),
	//converts given double number to ticks per hour, using 1000 multiplicator, i.e. 3600 -> '1 Kph' means 1 tick per second
	TicksPerHour(KphConvertor.class);
	
	private Class<Convertor> convertorClass;
	
	ConvertorType(Class convertorClass) {
		this.convertorClass = convertorClass;
	}
	
	public Class<Convertor> getConvertorClass() {
		return this.convertorClass;
	}
}
