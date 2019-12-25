package modeling.mathmodeling.controller;

import modeling.mathmodeling.dto.InputDTO;
import modeling.mathmodeling.service.ModelingService;
import modeling.mathmodeling.storage.StaticStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class Controller {
    @Autowired
    ModelingService modelingService;

    @GetMapping("/hello")
    public String hello() {
        return "Hello!";
    }

    @GetMapping("/modeling/status")
    public String getModelingStatus() {
        return StaticStorage.status;
    }

    @GetMapping("/modeling/output")
    public ConcurrentHashMap<Double, ArrayList<Double>> getModelingOutput() {
        return StaticStorage.modelServiceOutput;
    }

    @PostMapping(value = "/modeling/start")
    public void modelingStart(@RequestBody InputDTO input) throws Exception {
        System.out.println(input);
        modelingService.model(input.getN());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception exception) {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add("Content-Type", "text/html; charset=utf-8");
        return new ResponseEntity<>("На сервере произошла ошибка.", responseHeaders, HttpStatus.BAD_REQUEST);
    }
}
