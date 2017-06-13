package com.moofMonkey;

import java.util.ArrayList;
import java.util.Random;

public class ClassNameGen extends Thread {
	public static String alphabet = "Ii{}%";
	static int count = 7;
	static String name;
	static String prefix = "";
	private static ArrayList<String> already = new ArrayList<String>();

	public static String get() {
		name = "";

		try {
			int i = count;
			Random rand = new Random();
			while (true) {
				if (i == 0)
					break;
				name += new Random().nextBoolean() ? alphabet.charAt(new Random().nextInt(alphabet.length()))
						: alphabet.charAt(rand.nextInt(alphabet.length()));
				i--;
				if (i % 5 == 0)
					System.gc();
			}
			if (new Random().nextBoolean() && new Random().nextBoolean())
				name += "$" + (char) ('a' + new Random().nextInt(22));
		} catch (Throwable t) {
			t.printStackTrace();
		}
		
		if(already.contains(prefix + name))
			return get();
		else {
			already.add(prefix + name);
			return prefix + name;
		}
	}
	
	public static String get(int c) {
		name = "";

		try {
			int i = c;
			Random rand = new Random();
			while (true) {
				if (i == 0)
					break;
				name += new Random().nextBoolean() ? alphabet.charAt(new Random().nextInt(alphabet.length()))
						: alphabet.charAt(rand.nextInt(alphabet.length()));
				i--;
				if (i % 5 == 0)
					System.gc();
			}
			if (new Random().nextBoolean() && new Random().nextBoolean())
				name += "$" + (char) ('a' + new Random().nextInt(22));
		} catch (Throwable t) {
			t.printStackTrace();
		}
		
		if(already.contains(prefix + name))
			return get(c);
		else {
			already.add(prefix + name);
			return prefix + name;
		}
	}
}
