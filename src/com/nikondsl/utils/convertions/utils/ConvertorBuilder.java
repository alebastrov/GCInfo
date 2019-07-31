package com.nikondsl.utils.convertions.utils;

import com.nikondsl.utils.convertions.ConvertorType;
import com.nikondsl.utils.convertions.impl.Convertor;

import java.lang.reflect.Method;

public class ConvertorBuilder {
	public static Convertor create(ConvertorType convertorType) throws ReflectiveOperationException {
		try{
			Method method = convertorType.getConvertorClass().getMethod("getConvertor");
			Convertor result = (Convertor)method.invoke(null);
			return result;
		} catch (ReflectiveOperationException ex) {
			throw new ReflectiveOperationException("Make sure your convertor class has method 'public static Convertor getConvertor()'", ex);
		}
		
	}
	
}
