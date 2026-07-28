package com.adrovis.adrovis_backend.tools;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;



public class ModuleGenerator {

    private static final String[] PACKAGES = {
        "controller",
        "service",
        "repository",
        "dto",
        "entity",
        "mapper",
        "exception"
    };


    public static void main(String [] args) throws IOException{
        if(args.length == 0){
            System.out.println("Usage: java ModuleGenerator <module-name>");
            return;
        }

        String module = args[0];

        Path base = Paths.get("src/main/java/com/adrovis/adrovis_backend/"+module);

        Files.createDirectories(base);

        for(String pkg: PACKAGES) {
            Files.createDirectories(base.resolve(pkg));

        }

        System.out.println();
        System.out.println("✅ Module created successfully!");
        System.out.println("Location : " + base.toAbsolutePath());
    }
}
