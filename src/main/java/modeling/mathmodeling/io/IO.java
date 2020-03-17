package modeling.mathmodeling.io;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

public class IO {
    public static void fileToMap(String path, Map<String, String> map, String delimiter){
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(path));
            while (true) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }
                String[] keyValue = line.split(delimiter);
                map.put(keyValue[0], keyValue[1]);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
