package modeling.mathmodeling.webservice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class PyMathServiceImplTest {

    @Autowired
    PyMathService pyMathService;

    @Test
    void expand() {
        pyMathService.expand("x*(x+2)");
    }

    @Test
    void expand_list() throws Exception{
        CompletableFuture<String> bigRequestFuture = CompletableFuture.supplyAsync(() -> pyMathService.expand("((x+24)^22*(2*x*4y+2))^129"));
        CompletableFuture<String> smallRequestFuture = CompletableFuture.supplyAsync(() -> pyMathService.expand("x^2*(x+2)"));
        CompletableFuture<Void> combinedFuture
                = CompletableFuture.allOf(bigRequestFuture, smallRequestFuture);
        combinedFuture.get();
    }
}