package modeling.mathmodeling.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
@ExtendWith(SpringExtension.class)
@SpringBootTest
class ModelingServiceImplTest {

    @Autowired
    ModelingService modelingService;

    private String[] coefficientsArray;

    @Test
    void newtonMethod() {
        HashMap<String, String> gradient = new HashMap<>();
        HashMap<String, String> hessian = new HashMap<>();

        modelingService.newtonMethod(5.4, 5.4,coefficientsArray, 3.5, 0.001, 1,  gradient, hessian);
    }
}