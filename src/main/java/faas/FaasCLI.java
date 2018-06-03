package faas;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class FaasCLI {
    private final static Logger logger = Logger.getLogger(FaasCLI.class.getName());

    /**
     * @return
     */
    public static List<String> functions() throws IOException, InterruptedException {
        Process exec = Runtime.getRuntime().exec("faas-cli list");
        exec.waitFor();

        BufferedReader br = new BufferedReader(new InputStreamReader(exec.getInputStream()));

        List<String> functions = new ArrayList<String>();
        String line;
        while ((line = br.readLine()) != null) {
            functions.add(line);
        }

        return functions;
    }
}
