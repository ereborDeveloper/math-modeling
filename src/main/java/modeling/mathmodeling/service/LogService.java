package modeling.mathmodeling.service;

import java.util.LinkedHashMap;

public interface LogService {
    void initialize();

    void start();

    void next();

    void stop();

    void stop(Exception error);

    void setConsoleOutput(Boolean value);

    LinkedHashMap<String, String> getLog();

    Boolean getRunningStatus();
}
