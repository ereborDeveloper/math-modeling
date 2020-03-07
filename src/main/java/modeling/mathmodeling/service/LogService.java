package modeling.mathmodeling.service;

import java.util.LinkedHashMap;

public interface LogService {
    void initialize();

    void start();

    void debug(String debugString);

    void next();

    void stop();

    void stop(Exception error);

    void setConsoleOutput(Boolean value);

    LinkedHashMap<String, String> getLog();

    Boolean getRunningStatus();
}
