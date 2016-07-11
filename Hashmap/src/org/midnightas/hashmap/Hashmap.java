package org.midnightas.hashmap;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.StringEscapeUtils;

public class Hashmap implements Runnable {

	public static Scanner scanner = new Scanner(System.in);

	public static HashMap<Byte, Character> codepage = new HashMap<Byte, Character>();

	public static void main(String[] programArgs) {
		Options options = new Options();
		options.addOption("i", "input", true, "The input file.");
		options.addOption("e", "encoding", true, "The encoding to read the file in, UTF-8 is recommended.");
		try {
			CommandLine args = new DefaultParser().parse(options, programArgs);
			if (!args.hasOption("i"))
				throw new IllegalArgumentException("You forgot the -i option.");
			File f = new File(args.getOptionValue("i"));
			new Hashmap(new String(Files.readAllBytes(f.toPath()),
					args.hasOption("e") ? args.getOptionValue("e") : "UTF-8"), f).registerDefaultFunctions().run();
		} catch (Exception e) {
			e.printStackTrace();
		}
		scanner.close();
	}

	public static void printList(List<Object> list) {
		for (Object o : list)
			if (o instanceof Double) {
				if (Math.floor((double) o) == (double) o)
					System.out.print((int) Math.floor((double) o));
				else
					System.out.print(o);
			} else if (o instanceof String)
				System.out.print(StringEscapeUtils.unescapeJava(o + ""));
			else
				System.out.print(o);
	}

	public static <T> List<T> arrayToList(T[] t) {
		List<T> list = new ArrayList<T>();
		for (T obj : t)
			list.add(obj);
		return list;
	}

	public static String removeCharFromString(String str, int index) {
		return str.substring(0, index) + str.substring(index + 1);
	}

	public HashMap<String, BuiltinFunction> functions = new HashMap<String, BuiltinFunction>();
	public List<ArrayList<Object>> arrays = new ArrayList<ArrayList<Object>>();
	public HashMap<String, Object> scope = new HashMap<String, Object>();
	public List<Object> stack = new ArrayList<Object>();
	public String content;
	public File workingFile;
	public boolean running = false;

	public Hashmap(String content, File workingFile)
			throws HashmapException, UnsupportedEncodingException, IOException {
		this.workingFile = workingFile;
		this.content = content;
	}

	public Hashmap registerDefaultFunctions() {
		DefaultBuiltinFunction.register(this);
		return this;
	}

	public void run() {
		running = true;
		interpret(0, false);
		printList(stack);
	}

	@SuppressWarnings("unchecked")
	public boolean interpret(int tlstart, boolean isWhile) {
		int deep = 0;
		HashMap<String, Object> tempVars = new HashMap<String, Object>();
		for (int tl = tlstart; tl < content.length(); tl++) {
			if (!running)
				return false;
			char c = content.charAt(tl);
			if (c >= '0' && c <= '9')
				push(Double.parseDouble(c + ""));
			else if (c == '+') {
				if (peek() instanceof Double && peek(1) instanceof Double)
					push(pop(Double.class) + pop(Double.class));
				else
					push(pop(Object.class, 1) + "" + pop(Object.class));
			} else if (c == '-') {
				if (peek() instanceof Double && peek(1) instanceof Double) {
					push(pop(Double.class, 1) - pop(Double.class));
				} else {
					String two = pop(Object.class) + "";
					String one = pop(Object.class) + "";
					push(one.replaceAll(two, ""));
				}
			} else if (c == '*') {
				if (peek() instanceof Double && peek(1) instanceof Double)
					push(pop(Double.class) * pop(Double.class));
				else if (peek() instanceof Double && peek(1) instanceof Character) {
					char ch = pop(Character.class, 1);
					double amount = pop(Double.class);
					for (int i = 0; i < amount; i++)
						push(ch);
				}
			} else if (c == '/')
				push(pop(Double.class, 1) / pop(Double.class));
			else if (c == '^')
				push(Math.pow(pop(Double.class, 1), pop(Double.class)));
			else if (c == '{') {
				push(new Codeblock(tl + 1));
				for (int tl0 = tl + 1, deep0 = 0; tl0 < content.length(); tl0++) {
					char c0 = content.charAt(tl0);
					if (c0 == '{')
						deep0++;
					else if (c0 == '}')
						if (deep0-- == 0) {
							tl = tl0;
							break;
						}
				}
			} else if (c == '}') {
				if (deep-- == 0) {
					if (isWhile) {
						interpret(tlstart, true);
					} else
						return false;
				}
			} else if (c == '>') {
				if (tl < content.length() - 2 && content.charAt(tl + 1) == '>' && content.charAt(tl + 2) == '>') {
					for (int tl0 = tl; tl0 < content.length(); tl0++)
						if (content.charAt(tl0) == '\n') {
							tl = tl0;
							break;
						}
				} else {
					if (peek() instanceof Codeblock)
						interpret(pop(Codeblock.class).tl, false);
					else if (peek() instanceof String && existsFunction(peek().toString()))
						functions.get(pop(String.class)).call(this);
				}
			} else if (c == '\"') {
				String s = "";
				for (int tl0 = tl + 1; tl0 < content.length(); tl0++) {
					char c0 = content.charAt(tl0);
					if (c0 == '\"') {
						if (content.charAt(tl0 - 1) == '\\') {
							s = s.substring(0, s.length() - 1);
							s += "\"";
						} else {
							tl = tl0;
							break;
						}
					} else
						s += c0;
				}
				push(s);
			} else if (c == '\'') {
				tl++;
				push(content.charAt(tl));
			} else if (c == '½')
				push(pop(Double.class) / 2d);
			else if (c == '¼')
				push(pop(Double.class) / 4d);
			else if (c == '¾')
				push((pop(Double.class) / 4d) * 3d);
			else if (c == 'i')
				push(scanner.nextLine());
			else if (c == 'h')
				push(Double.parseDouble(scanner.nextLine()));
			else if (c == 'd') {
				if (peek() instanceof Character)
					push((double) pop(Character.class));
				else if (peek() instanceof List) {
					List<Object> newList = new ArrayList<Object>();
					for (Object o : pop(List.class))
						if (o instanceof String)
							newList.add(Double.parseDouble(o.toString()));
						else if (o instanceof Character)
							newList.add((double) ((int) o));
					push(newList);
				} else
					push(Double.parseDouble(pop(Object.class) + ""));
			} else if (c == 's') {
				if (peek() instanceof String) {
					char c0 = content.charAt(++tl);
					if (c0 == 'l')
						push((pop(Object.class) + "").toLowerCase());
					else if (c0 == 'u')
						push((pop(Object.class) + "").toUpperCase());
					else if (c0 == 'r')
						push(new StringBuilder(pop(String.class)).reverse().toString());
					else if (c0 == 's') {
						Object o = pop(Object.class, 1);
						if (o instanceof Double)
							push(pop(String.class).substring(((Double) o).intValue()));
						else if (o instanceof List) {
							String s = pop(String.class);
							List<Object> l = (List<Object>) o;
							push(s.substring(((Double) l.get(l.size() - 2)).intValue(),
									((Double) l.get(l.size() - 1)).intValue()));
						}
					}
				} else if (peek() instanceof List) {
					List<Object> newList = new ArrayList<Object>();
					for (Object o : pop(List.class))
						newList.add(o.toString());
					push(newList);
				} else
					push(pop(Object.class) + "");
			} else if (c == 'c') {
				if (peek() instanceof String)
					push(pop(String.class).charAt(0));
				else if (peek() instanceof Double)
					push((char) pop(Double.class).intValue());
			} else if (c == 'm') {
				char c0 = content.charAt(++tl);
				if (c0 == 'q')
					push(Math.sqrt(pop(Double.class)));
				else if (c0 == 'f')
					push(Math.floor(pop(Double.class)));
				else if (c0 == 'c')
					push(Math.ceil(pop(Double.class)));
				else if (c0 == 'a')
					push(Math.abs(pop(Double.class)));
			} else if (c == '[')
				arrays.add(new ArrayList<>());
			else if (c == ']') {
				if (arrays.size() > 1) {
					arrays.get(arrays.size() - 2).add(arrays.remove(arrays.size() - 1));
				} else {
					stack.add(arrays.remove(arrays.size() - 1));
				}
			} else if (c == 'a') {
				char c0 = content.charAt(++tl);
				if (c0 == 'f')
					flatten(pop(List.class));
			} else if (c == '#')
				push(pop(Object.class, 1));
			else if (c == '$')
				push(pop(Object.class, 2));
			else if (c == '&')
				push(peek());
			else if (c == '@')
				push(peek(pop(Double.class)));
			else if (c == '<') {
				Codeblock codeblock = pop(Codeblock.class);
				interpret(codeblock.tl, true);
				System.out.println("test");
			} else if (c == '|') {
				printList(stack);
				System.out.println();
			} else if (c == 'l') {
				printList(new ArrayList<Object>(Arrays.asList(peek())));
				System.out.println();
			} else if (c == ':') {
				char c0 = content.charAt(++tl);
				String varName = "";
				if (c0 == '"') {
					for (int tl1 = tl + 1; tl1 < content.length(); tl1++) {
						char c1 = content.charAt(tl1);
						if (c1 == '"') {
							tl = tl1;
							break;
						} else
							varName += c1;
					}
				} else
					varName = c0 + "";
				scope.remove(varName);
				if (varName.startsWith("$"))
					tempVars.put(varName, pop(Object.class));
				else
					scope.put(varName, pop(Object.class));
			} else if (c == ';') {
				char c0 = content.charAt(++tl);
				String varName = "";
				if (c0 == '"') {
					for (int tl1 = tl + 1; tl1 < content.length(); tl1++) {
						char c1 = content.charAt(tl1);
						if (c1 == '"') {
							tl = tl1;
							break;
						} else
							varName += c1;
					}
				} else
					varName = c0 + "";
				if (varName.startsWith("$"))
					push(tempVars.get(varName));
				else
					push(scope.get(varName));
			} else if (c == '=')
				push(pop(Object.class).equals(pop(Object.class)));
			else if (c == '?') {
				boolean condition = pop(Boolean.class);
				Codeblock codeblock = pop(Codeblock.class);
				if (condition)
					if (!interpret(codeblock.tl, false))
						return false;
			} else if (c == 'f') {
				Object charToPutVarInto = pop(Object.class);
				Object objToIterate = pop(Object.class);
				Codeblock codeblock = pop(Codeblock.class);
				ArrayList<Object> iterator = new ArrayList<Object>();
				if (objToIterate instanceof String) {
					for (char charToIterate : objToIterate.toString().toCharArray())
						iterator.add(charToIterate);
				} else if (objToIterate instanceof List)
					iterator.addAll((List<?>) objToIterate);
				for (Object o : iterator) {
					scope.remove(charToPutVarInto);
					scope.put(charToPutVarInto + "", o);
					interpret(codeblock.tl, false);
				}
			} else if (c == '.')
				stack.clear();
			else if (c == 'p')
				pop(Object.class);
			else if (c == '!') {
				running = false;
				continue;
			} else if (c == 'ģ')
				System.out.println(peek().getClass());
			else if (c == 'Ĥ') {
				List<Object> list = pop(List.class);
				List<Object> newList = new ArrayList<Object>();
				List<Object> currentList = new ArrayList<Object>();
				Object obj = list.get(0);
				for (Object o : list) {
					if (!obj.equals(o)) {
						newList.add(currentList);
						currentList = new ArrayList<Object>();
					}
					currentList.add(o);
					obj = o;
				}
				newList.add(currentList);
				push(newList);
			} else if (c == 'ĥ') {
				String[] array = pop(String.class, 1).split(pop(String.class));
				List<Object> list = new ArrayList<Object>();
				for (String str : array)
					list.add(str);
				push(list);
			} else if (c == 'Đ')
				for (Object o : pop(List.class))
					push(o);
			else if (c == 'N')
				push(!pop(Boolean.class));
			else if (c == 'G') {
				String varName = pop(Object.class) + "";
				Object value = varName.startsWith("$") ? tempVars.get(varName) : scope.get(varName);
				push(value);
			} else if (c == 'S') {
				Object value = pop(Object.class);
				Object varName = pop(Object.class);
				if (varName instanceof Double && (varName + "").endsWith(".0"))
					varName = varName.toString().substring(0, varName.toString().length() - 2);
				if (varName.toString().startsWith("$"))
					tempVars.put(varName.toString(), value);
				else
					scope.put(varName.toString(), value);
			} else if (c == 'L')
				push((double) pop(Object.class).toString().length());
			else if(c == 'R') {
				char c0 = content.charAt(++tl);
				if(c0 == 'i')
					push(new Random().nextInt(pop(Double.class).intValue()));
				else if(c0 == 'd')
					push(ThreadLocalRandom.current().nextDouble(pop(Double.class)));
			} else if(c == 'r') {
				char c0 = content.charAt(++tl);
				char c01 = content.charAt(++tl);
				int i0 = Math.min(c0, c01);
				int i1 = Math.max(c0, c01);
				List<Character> chars = new ArrayList<Character>();
				for(; i0 <= i1; i0++)
					chars.add((char) i0);
				push(chars);
			}
		}
		return true;
	}

	public boolean existsFunction(String varName) {
		for (Map.Entry<String, BuiltinFunction> funcs : functions.entrySet())
			if (funcs.getKey().equals(varName))
				return true;
		return false;
	}

	@SuppressWarnings("unchecked")
	public void flatten(List<Object> list) {
		for (Object obj : list)
			if (obj instanceof List)
				flatten((List<Object>) obj);
			else
				push(obj);
	}

	public <T> T pop(Class<T> type) {
		return pop(type, 0);
	}

	public <T> T pop(Class<T> type, int offset) {
		return type.cast(arrays.size() > 0
				? arrays.get(arrays.size() - 1).remove(arrays.get(arrays.size() - 1).size() - 1 - offset)
				: stack.remove(stack.size() - 1 - offset));
	}

	public Object peek() {
		return peek(0);
	}

	public void push(Object o) {
		if (arrays.size() > 0)
			arrays.get(arrays.size() - 1).add(o);
		else
			stack.add(o);
	}

	public Object peek(int offset) {
		return arrays.size() > 0 ? arrays.get(arrays.size() - 1).get(arrays.get(arrays.size() - 1).size() - 1 - offset)
				: stack.get(stack.size() - 1 - offset);
	}

	public Object peek(double offset) {
		return peek((int) offset);
	}

	public static byte codepageByte = 0x00;

	public static byte registerCodepageCharacter(char c) {
		codepage.put(codepageByte, c);
		byte toReturn = codepageByte;
		codepageByte = (byte) (codepageByte + 0x01);
		return toReturn;
	}

	static {
		for (int i = 'a'; i <= 'z'; i++)
			registerCodepageCharacter((char) i);
		for (int i = 'A'; i <= 'Z'; i++)
			registerCodepageCharacter((char) i);
		for (int i = '0'; i <= '9'; i++)
			registerCodepageCharacter((char) i);
	}

}
