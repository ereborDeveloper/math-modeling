package modeling.mathmodeling;

import modeling.mathmodeling.dto.InputDTO;
import modeling.mathmodeling.service.ModelingService;
import modeling.mathmodeling.service.ParseService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.matheclipse.core.basic.Config;
import org.matheclipse.core.eval.ExprEvaluator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class MathModelingApplicationTests {

    @Autowired
    ParseService parseService;

    @Autowired
    ModelingService modelingService;

    private ExprEvaluator util = new ExprEvaluator(true, 50000);

    @Test
    void contextLoads() {
    }

    @Test
    void modeling_shell_index_1() {
        InputDTO dto = new InputDTO();
        dto.setShellIndex(1);
        dto.setD(0);
        dto.setTheta(0);
        dto.setR(0.05);
        dto.setR1(20.25);
        dto.setR2(20.25);
        dto.setA0(0);
        dto.setA1(5.4);
        dto.setB0(0);
        dto.setB1(5.4);
        dto.setN(1);
        dto.setE1(210000);
        dto.setE2(210000);
        dto.setMu12(0.3);
        dto.setMu21(0.3);
        dto.setH(0.09);
        dto.setZ(-0.045);
        dto.setK(0.8333333);
        dto.setG(33000);
        dto.setQMax(3.3);
        dto.setQStep(0.01);
        dto.setStepCount(50);
        dto.setOptimizationBreak(50);
        dto.setEps(0.000001);
        modelingService.model(dto);
    }

    @Test
    void symjaBug() {
        String in = "0.1758368*0.4112540785271148*0.0*9.986163076795545E-11";
        System.out.println(util.eval("(" + in + ")"));
    }
    @Test
    void symjaBugChanged() {
        Config.EXPLICIT_TIMES_OPERATOR = true;
        System.out.println(util.eval("+0.4263527439844309*v11*w11*0.0*2.025*2.0*1+3514.81638215433*2.7*2.6999999999999993*2.0*1+0.018094972454910804*v11^2.0*2.025*2.025*2.0*1+12.857124397455252*w11^2.0*2.025*0.675*2.0*1+8.633643065684728*v11*0.0*2.291831180523293*2.0*1+0.015439396292586009*2.025*2.025*4.0*3.0*u11^2.0-1.6257684296093071*w11*2.291831180523293*2.291831180523293*2.0*1+49.64135729857474*w11*1.1459155902616465*2.291831180523293*2.0*1+3.0178326474622765*2.7*2.7*2.0*1+14.733179292977349*2.291831180523293*0.0*3.0*2.0*u11+347.14235873129184*w11*2.291831180523293*1.1459155902616465*2.0*1+502.61874264806926*2.6999999999999993*2.7*2.0*1+2.5114249656362593*w11^2.0*0.675*2.025*2.0*1+0.7275644095297455*w11*2.025*0.0*3.0*2.0*u11"));
    }


}
