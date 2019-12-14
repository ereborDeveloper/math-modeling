package modeling.mathmodeling.service;

import org.matheclipse.core.eval.ExprEvaluator;

public interface IntegrateService {

    String partialIntegrate(ExprEvaluator util, String body, String variable, double from, double to, String type);

}
