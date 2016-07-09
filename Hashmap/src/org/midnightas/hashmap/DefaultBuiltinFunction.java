package org.midnightas.hashmap;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.commons.io.FileUtils;

public class DefaultBuiltinFunction {

	public static void register(Hashmap map) {
		new BuiltinFunction("io", "read", "_hashmap_builtin.io_read") {
			@Override
			public void call(Hashmap hashmap) {
				try {
					hashmap.push(FileUtils.readFileToString(new File(hashmap.pop(String.class))));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}.register(map);
		new BuiltinFunction("io", "overwrite", "_hashmap_builtin.io_write") {
			@Override
			public void call(Hashmap hashmap) {
				try {
					FileWriter fileWriter = new FileWriter(new File(hashmap.pop(String.class, 1)), false);
					fileWriter.write(hashmap.pop(Object.class).toString());
					fileWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}.register(map);
		new BuiltinFunction("io", "write", "_hashmap_builtin.io_append") {
			@Override
			public void call(Hashmap hashmap) {
				try {
					FileWriter fileWriter = new FileWriter(new File(hashmap.pop(String.class, 1)), true);
					fileWriter.write(hashmap.pop(Object.class).toString());
					fileWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}.register(map);
		new BuiltinFunction("io", "curdir", "_hashmap_builtin.io_currentdir") {
			@Override
			public void call(Hashmap hashmap) {
				hashmap.push(System.getProperty("user.dir"));
			}
		}.register(map);
		new BuiltinFunction("io", "curfile", "_hashmap_builtin.io_currentfile") {
			@Override
			public void call(Hashmap hashmap) {
				hashmap.push(hashmap.workingFile.getAbsolutePath());
			}
		}.register(map);
		new BuiltinFunction("io", "curexecutor", "_hashmap_builtin.io_currentexecutor") {
			@Override
			public void call(Hashmap hashmap) {
				try {
					hashmap.push(Hashmap.class.getProtectionDomain().getCodeSource().getLocation().toURI().toString());
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
			}
		}.register(map);
	}

}
