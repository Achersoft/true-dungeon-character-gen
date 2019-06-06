package com.achersoft;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class NameChanger {

    final static String VERSION_TAG = "@{TDCC_VERSION}";
    final static String VERSION =  UUID.randomUUID().toString();

    public static void main(String[] argv) throws IOException {
        renameAllFiles(new File("./"));

        System.out.println("conversion is done");
    }

    private static void renameAllFiles(File folder) throws IOException {
        File[] listOfFiles = folder.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isDirectory()) {
               // renameAllFiles(listOfFiles[i]);
            }
            if (listOfFiles[i].isFile()) {
                changeFileContent(listOfFiles[i]);

                if (listOfFiles[i].getName().contains(VERSION_TAG)) {
                    System.out.println(listOfFiles[i].getName());
                    //System.out.println(.re);
                    listOfFiles[i].renameTo(new File(listOfFiles[i].getPath().replace(VERSION_TAG, VERSION)));
                }
            }
        }
    }

    private static void changeFileContent(File file) throws IOException {
        Charset charset = StandardCharsets.UTF_8;
        String content = new String(Files.readAllBytes(file.toPath()), charset);
        content = content.replace(VERSION_TAG, VERSION);
        Files.write(file.toPath(), content.getBytes(charset));
    }
}
