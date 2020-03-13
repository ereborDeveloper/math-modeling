package modeling.mathmodeling.webservice;

public interface PyMathService {
    String expand(String input);

    String d(String input, String diffVar);
}
