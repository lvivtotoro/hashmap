package org.midnightas.hashmap;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.util.ArrayList;
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
		interpret(0);
		for (Object o : stack)
			if(o instanceof Double) {
				if(Math.floor((double) o) == (double) o)
					System.out.print((int) Math.floor((double) o));
				else
					System.out.print(o);
			} else
				System.out.print(o);
	}

	public void interpret(int tl) {
		int deep = 0;
		for (; tl < content.length(); tl++) {
			char c = content.charAt(tl);
			if (c >= '0' && c <= '9')
				stack.add(Double.parseDouble(c + ""));
			else if (c == '+') {
				if (peek() instanceof Double && peek(1) instanceof Double)
					stack.add(pop(Double.class) + pop(Double.class));
				else
					stack.add(pop(Object.class, 1) + "" + pop(Object.class));
			} else if (c == '-')
				stack.add(-(pop(Double.class)) + pop(Double.class));
			else if (c == '*') {
				if (peek() instanceof Double && peek(1) instanceof Double)
					stack.add(pop(Double.class) * pop(Double.class));
				else if (peek() instanceof Double && peek(1) instanceof Character) {
					char ch = pop(Character.class, 1);
					double amount = pop(Double.class);
					for (int i = 0; i < amount; i++)
						stack.add(ch);
				}
			} else if (c == '/')
				stack.add(pop(Double.class, 1) / pop(Double.class));
			else if (c == '{') {
				stack.add(new Codeblock(tl + 1));
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
				if (deep-- == 0)
					return;
			} else if (c == '>') {
				interpret(pop(Codeblock.class).tl);
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
				stack.add(s);
			} else if (c == '\'') {
				tl++;
				stack.add(content.charAt(tl));
			} else if(c == '½')
				stack.add(pop(Double.class) / 2d);
			else if(c == '¼')
				stack.add(pop(Double.class) / 4d);
			else if(c == '¾')
				stack.add((pop(Double.class) / 4d) * 3d);
			else if(c == 'i')
				stack.add(scanner.nextLine());
			else if(c == 'h')
				stack.add(Double.parseDouble(scanner.nextLine()));
			else if(c == 'd')
				stack.add(Double.parseDouble(pop(Object.class) + ""));
			else if(c == 's')
				stack.add(pop(Object.class) + "");
		}
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

	public Object peek(int offset) {
		return stack.get(stack.size() - 1 - offset);
	}

}
