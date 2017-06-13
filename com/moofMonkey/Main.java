/** ______  __  __   ______  ______   ______   __   __    __   ______   __   __   ______     __     __   __  __   ______   ______  
   /\  ___\/\_\_\_\ /\  == \/\  ___\ /\  == \ /\ \ /\ "-./  \ /\  ___\ /\ "-.\ \ /\__  _\   /\ \  _ \ \ /\ \_\ \ /\  __ \ /\__  _\ 
   \ \  __\\/_/\_\/_\ \  _-/\ \  __\ \ \  __< \ \ \\ \ \-./\ \\ \  __\ \ \ \-.  \\/_/\ \/   \ \ \/ ".\ \\ \  __ \\ \  __ \\/_/\ \/ 
    \ \_____\/\_\/\_\\ \_\   \ \_____\\ \_\ \_\\ \_\\ \_\ \ \_\\ \_____\\ \_\\"\_\  \ \_\    \ \__/".~\_\\ \_\ \_\\ \_\ \_\  \ \_\ 
     \/_____/\/_/\/_/ \/_/    \/_____/ \/_/ /_/ \/_/ \/_/  \/_/ \/_____/ \/_/ \/_/   \/_/     \/_/   \/_/ \/_/\/_/ \/_/\/_/   \/_/ 
  */

package com.moofMonkey;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import javassist.ClassMap;
import javassist.ClassPool;
import javassist.CodeConverter;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewConstructor;

public class Main extends Thread {
	static CodeConverter codeConv = new CodeConverter();
	static ArrayList<String> 
			classes = new ArrayList<String>(), 
			fields = new ArrayList<String>(),
			methods = new ArrayList<String>();
	static ArrayList<FieldRename> frnl = new ArrayList<FieldRename>();
	static ArrayList<CtClass> modClasses = new ArrayList<CtClass>();
	static HashMap<String, String> 
			classes2 = new HashMap<String, String>(), 
			fields2 = new HashMap<String, String>(),
			methods2 = new HashMap<String, String>();
	static ClassMap cm = new ClassMap();
	static BufferedReader in;
	static ClassPool pool = ClassPool.getDefault();
	static String line;
	static String[] args;
	static boolean isDemo;
	static Random rnd = new Random();
	
	public static String sign = "monkey. moofMonkey.";

	public static void addFakeClass() throws Throwable {
		CtClass cl = pool.makeClass("remaper.by.moofMonkey.demo.REMAPxDEMO");
		
		cl.addConstructor(CtNewConstructor.make("public REMAPxDEMO() {  }", cl));
		
		pool.importPackage("remaper.by.moofMonkey.demo");
		modClasses.add(cl);
	}
	
	public static void addFakeCode(CtBehavior cb) throws Throwable {
		cb.insertAfter("new REMAPxDEMO();");
	}
	
	public static void addMethod(CtClass cl, int rand) throws Throwable {
		cl.addMethod(CtMethod.make("private static void pushTextToNull(String text) { System.out.print(\"\" + (char)" + rand + "); }", cl));
	}

	public static void checkArgs() throws Throwable {
		try {
			Class.forName("com.moofMonkey.Main");
			isDemo = false;
		} catch(Throwable t) {
			isDemo = true;
		}
		
		if (args.length < 1 || args[0].length() < 1) {
			System.out.println("Give me class remap file name as first parameter, please");
			System.exit(-3);
		}

		if (args.length < 2 || args[1].length() < 1) {
			System.out.println("Give me field remap file name as second parameter, please");
			System.exit(-4);
		}
		
		if (args.length < 3 || args[2].length() < 1) {
			System.out.println("Give me method remap file name as third parameter, please");
			System.exit(-5);
		}

		if (args.length >= 4) {
			System.out.println("Loading classes from " + args[3] + " ...");
			if (!new File(args[3]).exists())
				System.out.println("Not found file: " + args[3] + "!");
			else
				pool.insertClassPath(args[3]);
		}
	}

	public static void classesRename() throws Throwable {
		ArrayList<CtClass> modClasses2 = new ArrayList<CtClass>(), modClasses3 = new ArrayList<CtClass>();
		
		if(isDemo)
			addFakeClass();
		
		for (String clName : classes) {
			System.out.println("> " + clName + " => " + classes2.get(clName));

			CtClass cl = pool.get(clName);
			
			if(isDemo && !cl.isInterface()) {
				long rand = Math.abs(rnd.nextLong());
				String str = "demo_" + rand + ": This .class file "
						+ "is modified by moofMonkey's remapper [DEMO]";
				
				CtConstructor staticC = cl.getClassInitializer();
				if(staticC == null) {
					cl.makeClassInitializer();
					staticC = cl.getClassInitializer();
				}
				
				addMethod(cl, rnd.nextInt((int) (rand % 999999999)));
				
				addFakeCode(staticC);
				staticC.insertAfter("pushTextToNull(\"" + str + "\");");
				addFakeCode(staticC);
			}
			
			for(CtMethod md : cl.getDeclaredMethods()) {
				if(Modifier.isNative(md.getModifiers()))
					continue;
				
				String newName = 
						methods.contains(md.getName())
						?methods2.get(md.getName())
						:ClassNameGen.get();
				
				CtMethod md1 = new CtMethod(md.getReturnType(), newName, md.getParameterTypes(), cl);
				md1.setBody(md, null);
				md1.setModifiers(md.getModifiers());
				
				cl.addMethod(md1);
				cl.removeMethod(md);
				
				codeConv.redirectMethodCall(md, md1);
			}
			
			for(CtField fd : cl.getDeclaredFields()) {
				if(Modifier.isNative(fd.getModifiers()))
					continue;
				
				String newName = 
						fields.contains(fd.getName())
						?fields2.get(fd.getName())
						:ClassNameGen.get();
				codeConv.redirectFieldAccess(fd, cl, newName);
				frnl.add(new FieldRename(cl, fd, newName));
			}
			modClasses2.add(cl);
		}
		
		for(CtClass cl : modClasses2) {
			cl.instrument(codeConv);
			
			modClasses3.add(cl);
		}
		
		for(CtClass cl : modClasses3) {
			cl.setName(classes2.get(cl.getName()));
			cl.replaceClassName(cm);
			
			modClasses.add(cl);
		}
	}

	public static void classMapCreate() throws Throwable {
		in = new BufferedReader(new InputStreamReader(new FileInputStream(args[0]), "Cp1251"));

		while ((line = in.readLine()) != null) {
			while (line.indexOf("%random%") > -1) {
				String r = ClassNameGen.get();

				line = line.substring(0, line.indexOf("%random%")) + r
						+ line.substring(line.indexOf("%random%") + "%random%".length());
			}

			System.out.println("> " + line);

			String[] format = line.split(" \\=\\> ");

			classes.add(format[0]);
			classes2.put(format[0], format[1]);
			cm.put(format[0], format[1]);
		}

		in.close();
	}
	
	public static void methodMapCreate() throws Throwable {
		in = new BufferedReader(new InputStreamReader(new FileInputStream(args[2]), "Cp1251"));

		while ((line = in.readLine()) != null) {
			while (line.indexOf("%random%") > -1) {
				String r = ClassNameGen.get();

				line = line.substring(0, line.indexOf("%random%")) + r
						+ line.substring(line.indexOf("%random%") + "%random%".length());
			}

			System.out.println("> " + line);

			String[] format = line.split(" \\=\\> ");

			methods.add(format[0]);
			methods2.put(format[0], format[1]);
		}

		in.close();
	}

	public static void fieldMapCreate() throws Throwable {
		in = new BufferedReader(new InputStreamReader(new FileInputStream(args[1]), "Cp1251"));

		while ((line = in.readLine()) != null) {
			while (line.indexOf("%random%") > -1) {
				String r = ClassNameGen.get();

				line = line.substring(0, line.indexOf("%random%")) + r
						+ line.substring(line.indexOf("%random%") + "%random%".length());
			}

			System.out.println("> " + line);

			String[] format = line.split(" \\=\\> ");

			fields.add(format[0]);
			fields2.put(format[0], format[1]);
		}

		in.close();
	}

	public static void fieldsRename() throws Throwable {
		for (FieldRename frn : frnl) {
			CtClass cl = frn.getCl();
			CtField fd = frn.getFd();

			System.out.println(cl.getName() + " : " + fd.getName() + " => " + frn.getNewName());

			fd.setName(frn.getNewName());

			if (!modClasses.contains(cl))
				modClasses.add(cl);
		}
	}

	public static void main(String[] args) throws Throwable {
		System.out.println("|----------------------------------------------------------------------|");
		System.out.println("|                                                                      |");
		System.out.println("|   __   __   ______   ______   __   __   __    __   ______   ______   |");
		System.out.println("|  /\\ \"-.\\ \\ /\\  ___\\ /\\  __ \\ /\\ \"-.\\ \\ /\\ \"-./  \\ /\\  __ \\ /\\  == \\  |");
		System.out.println("|  \\ \\ \\-.  \\\\ \\  __\\ \\ \\ \\/\\ \\\\ \\ \\-.  \\\\ \\ \\-./\\ \\\\ \\  __ \\\\ \\  _-/  |");
		System.out.println("|   \\ \\_\\\\\"\\_\\\\ \\_____\\\\ \\_____\\\\ \\_\\\\\"\\_\\\\ \\_\\ \\ \\_\\\\ \\_\\ \\_\\\\ \\_\\    |");
		System.out.println("|    \\/_/ \\/_/ \\/_____/ \\/_____/ \\/_/ \\/_/ \\/_/  \\/_/ \\/_/\\/_/ \\/_/    |");
		System.out.println("|                                                                      |");
		System.out.println("|----------------------------------------------------------------------|");
		
		Main.args = args;

		checkArgs();

		System.out.println("Starting create class map...");

		classMapCreate();

		System.out.println("Class Map created!");

		System.out.println("---------------------------------------");
		System.out.println("Starting create field map...");

		fieldMapCreate();

		System.out.println("Field Map created!");
		System.out.println("---------------------------------------");
		System.out.println("");
		System.out.println("---------------------------------------");
		System.out.println("---------MAPS-CREATING-ENDED-----------");
		System.out.println("---------------------------------------");
		System.out.println("");
		System.out.println("---------------------------------------");

		try {
			System.out.println("Starting renaming classes && methods...");

			classesRename();

			System.out.println("Classes renamed!");
			System.out.println("---------------------------------------");
			System.out.println("Starting renaming fields...");

			fieldsRename();

			System.out.println("Fields renamed!");
			System.out.println("---------------------------------------");
			System.out.println("Starting saving classes...");

			saveClasses();

			System.out.println("Classes saved!");

			System.out.println("Remapped. For many protect - try obfuscate it with Stringer/NeonObf to effect.");
		} catch (Throwable t) {
			System.out.println("!!CRITICAL ERROR!!");
			t.printStackTrace();
			System.exit(-1);
		}
	}

	public static void saveClasses() throws Throwable {
		for (CtClass cl : modClasses) {
			cl.stopPruning(true);
			stupidWriteFile(cl, "./ModifiedClasses");
			cl.stopPruning(false);
		}
	}
	
	/**
	 * Магия. Не трогать
	 * @param cl - класс
	 * @param directoryName - директория куда сохранять
	 * @throws Throwable - МБ что-то пойдёт не так
	 */
	public static void stupidWriteFile(CtClass cl, String directoryName) throws Throwable {
		String s = cl.getClassFile().getSourceFile();
		
		System.out.println(">> " + cl.getName());

		byte[] bc = cl.toBytecode();
		
		int index = new String(bc).indexOf(s);
		for(int i = 0; i < s.length(); i++)  //KILL SOURCEFILE (c) moofMonkey
			bc[index + i] = '-';
		
		DataOutputStream out = cl.makeFileOutput(directoryName);
		
		out.write(bc);
		
		out.flush();
		out.close();
	}
}
