package org.midnightas.hashmap;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;

public class Hashmap implements Runnable {

	public static Scanner scanner = new Scanner(System.in);

	public static void main(String[] args) {
		Options options = new Options();
		options.addOption("i", "input", true, "The input file.");
		options.addOption("e", "encoding", true, "The encoding to read the file in, UTF-8 is recommended.");
		try {
			new Hashmap(new DefaultParser().parse(options, args)).run();
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
			} else
				System.out.print(o);
	}

	public static <T> List<T> arrayToList(T[] t) {
		List<T> list = new ArrayList<T>();
		for (T obj : t)
			list.add(obj);
		return list;
	}

	public List<ArrayList<Object>> arrays = new ArrayList<>();
	public HashMap<Character, Object> scope = new HashMap<>();
	public List<Object> stack = new ArrayList<>();
	public String content;

	public Hashmap(CommandLine args) throws HashmapException, UnsupportedEncodingException, IOException {
		if (!args.hasOption("i"))
			throw new IllegalArgumentException("You forgot the -i option.");
		this.content = new String(Files.readAllBytes(new File(args.getOptionValue("i")).toPath()),
				args.hasOption("e") ? args.getOptionValue("e") : "UTF-8");
	}

	public void run() {
		interpret(0, false);
		printList(stack);
	}

	@SuppressWarnings("unchecked")
	public boolean interpret(int tlstart, boolean isWhile) {
		int deep = 0;
		for (int tl = tlstart; tl < content.length(); tl++) {
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
				interpret(pop(Codeblock.class).tl, false);
			} else if (c == '\"') {
				String s = "";
				for (int tl0 = tl + 1; tl0 < content.length(); tl0++) {
					char c0 = content.charAt(tl0);
					if (c0 == '\"' && content.charAt(tl0 - 1) != '\\') {
						tl = tl0;
						break;
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
				else
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
			else if (c == ']')
				push(arrays.remove(arrays.size() - 1));
			else if (c == 'a') {
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
				isWhile = false;
			} else if (c == '|') {
				printList(stack);
				System.out.println();
			} else if (c == 'l') {
				printList(new ArrayList<Object>(Arrays.asList(peek())));
				System.out.println();
			} else if (c == ':') {
				char c0 = content.charAt(++tl);
				scope.remove(c0);
				scope.put(c0, pop(Object.class));
			} else if (c == ';')
				push(scope.get(content.charAt(++tl)));
			else if (c == '=')
				push(pop(Object.class).equals(pop(Object.class)));
			else if (c == '?') {
				Codeblock codeblock = pop(Codeblock.class);
				if (pop(Boolean.class))
					if (!interpret(codeblock.tl, false))
						return false;
			} else if (c == 'f') {
				char charToPutVarInto = pop(Character.class);
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
					scope.put(charToPutVarInto, o);
					interpret(codeblock.tl, false);
				}
			} else if (c == '.')
				stack.clear();
			else if(c == 'p')
				pop(Object.class);
			else if (c == '!')
				return false;
			else if (c == 'ģ')
				System.out.println(peek().getClass());
		}
		return true;
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
		return type.cast(stack.remove(stack.size() - 1 - offset));
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
		return stack.get(stack.size() - 1 - offset);
	}

	public Object peek(double offset) {
		return peek((int) offset);
	}

}
