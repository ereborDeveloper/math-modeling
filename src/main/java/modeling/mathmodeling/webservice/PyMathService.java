package modeling.mathmodeling.webservice;

import java.util.List;

public interface PyMathService {
    String expand(String input);

    String expand(List<String> input);

    String d(String input, String diffVar);
}
