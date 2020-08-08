package framework.Util.HttpUtil;

import framework.Stage.StageAPI;
import framework.Stage.StageMap;

import java.io.*;
import java.util.HashMap;
import java.util.Scanner;

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
                else return pageMap.get("404");

            }
        }


    private String getFile(String fileName) {

        StringBuilder result = new StringBuilder("");
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(fileName).getFile());

        try (Scanner scanner = new Scanner(file)) {

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                result.append(line).append("\n");
            }

            scanner.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return result.toString();

    }
}
