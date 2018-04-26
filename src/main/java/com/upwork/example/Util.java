package com.upwork.example;

public class Util {

	public static String getRandomlyName(final int characterLength) {
		String name = org.apache.commons.lang.RandomStringUtils.randomAlphanumeric(
				org.apache.commons.lang.math.RandomUtils.nextInt(characterLength - 1) + 1);
		return name;

	}
}