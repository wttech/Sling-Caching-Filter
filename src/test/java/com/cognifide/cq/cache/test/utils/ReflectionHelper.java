package com.cognifide.cq.cache.test.utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author Bartosz Rudnicki
 */
public class ReflectionHelper {

	@SuppressWarnings("unchecked")
	public static <T> T get(Class<?> clazz, String fieldName, Object object) throws NoSuchFieldException,
			IllegalAccessException {
		return (T) getField(clazz, fieldName).get(object);
	}

	@SuppressWarnings("unchecked")
	public static <T> T get(Class<?> clazz, String fieldName) throws NoSuchFieldException,
			IllegalAccessException {
		return (T) getField(clazz, fieldName).get(null);
	}

	public static void set(Class<?> clazz, String fieldName, Object instance, Object value)
			throws IllegalAccessException, NoSuchFieldException {
		getField(clazz, fieldName).set(instance, value);
	}

	public static void invoke(Class<?> clazz, String methodName, Class<?>[] parameterTypes, Object instance,
			Object[] parameterValues) throws IllegalAccessException, InvocationTargetException,
			NoSuchMethodException {
		getMethod(clazz, methodName, parameterTypes).invoke(instance, parameterValues);
	}

	public static Object invoke(Class<?> clazz, String methodName, Object instance)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		return getMethod(clazz, methodName, new Class<?>[] {}).invoke(instance, new Object[] {});
	}

	private static Field getField(Class<?> clazz, String fieldName) throws NoSuchFieldException {
		Field field = null;
		while ((field == null) && (clazz != null)) {
			try {
				field = clazz.getDeclaredField(fieldName);
			} catch (NoSuchFieldException noSuchFieldException) {
				clazz = clazz.getSuperclass();
			}
		}
		if (field == null) {
			throw new NoSuchFieldException();
		}
		field.setAccessible(true);
		return field;
	}

	private static Method getMethod(Class<?> clazz, String methodName, Class<?>[] parameterTypes)
			throws NoSuchMethodException {
		Method method = null;
		while ((method == null) && (clazz != null)) {
			try {
				method = clazz.getDeclaredMethod(methodName, parameterTypes);
			} catch (NoSuchMethodException noSuchMethodException) {
				clazz = clazz.getSuperclass();
			}
		}
		if (method == null) {
			throw new NoSuchMethodException();
		}
		method.setAccessible(true);
		return method;
	}
}
