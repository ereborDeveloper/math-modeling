package modeling.mathmodeling.network;

public class PyServer {
    private String serverName = "localhost";
    private String port = "5000";
    private String protocol = "http";

    private static PyServer instance;

    public static synchronized PyServer getInstance() {
        if (instance == null) {
            instance = new PyServer();
        }
        return instance;
    }

    public static synchronized String getURL() {
        PyServer _self = getInstance();
        return _self.protocol + "://" + _self.serverName + ":" + _self.port + "/";
    }
}