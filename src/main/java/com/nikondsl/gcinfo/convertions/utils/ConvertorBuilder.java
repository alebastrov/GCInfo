package com.nikondsl.gcinfo.convertions.utils;

import com.nikondsl.gcinfo.convertions.ConvertorType;
import com.nikondsl.gcinfo.convertions.impl.Convertor;

import java.lang.reflect.Method;

public class ConvertorBuilder {
	private ConvertorBuilder() {
	}
	
	public static Convertor create(ConvertorType convertorType) throws ReflectiveOperationException {
		try{
			Method method = convertorType.getConvertorClass().getMethod("getConvertor");
			return (Convertor)method.invoke(null);
		} catch (ReflectiveOperationException ex) {
			throw new ReflectiveOperationException("Make sure your convertor class has method 'public static Convertor getConvertor()'", ex);
		}
		
	}
	
}
