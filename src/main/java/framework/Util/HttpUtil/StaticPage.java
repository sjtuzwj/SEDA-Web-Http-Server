package framework.Util.HttpUtil;

import framework.Stage.StageAPI;
import framework.Stage.StageMap;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

public class StaticPage {
        public HashMap<String, String> pageMap = new HashMap<>();
        private StaticPage() {
        }
        private static class SingletonInner {
            private static StaticPage staticPage = new StaticPage();
        }
        public static StaticPage getInstance() {
            return StaticPage.SingletonInner.staticPage;
        }
        public String get(String name) {
            if(pageMap.containsKey(name))
                return pageMap.get(name);
            else {
                String res = getFile(name);
                if(!res.isEmpty()){
                    pageMap.put(name,res);
                    return res;
                }
                else return get("404.html");

            }
        }


    private String getFile(String fileName) {
        try {
            fileName = this.getClass().getClassLoader().getResource(fileName).getFile();
            System.out.println(fileName);
            final Path path = Paths.get(fileName);
            String contents = new String(Files.readAllBytes(path));
            System.out.println("C:" +contents);
            AbstractQueuedSynchronizer

            return contents;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
}
