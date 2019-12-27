package modeling.mathmodeling.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import modeling.mathmodeling.dto.InputDTO;
import modeling.mathmodeling.service.ModelingService;
import modeling.mathmodeling.storage.StaticStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Map;
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

    @GetMapping("/modeling/bstatus")
    public Boolean getModelingBStatus() {
        return StaticStorage.boolStatus;
    }


    @GetMapping("/modeling/output")
    public ConcurrentHashMap<Double, ArrayList<Double>> getModelingOutput() {
        return StaticStorage.modelServiceOutput;
    }

    @PostMapping(value = "/modeling/start")
    public void modelingStart(@RequestBody String input) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> map = mapper.readValue(input, Map.class);
        System.out.println(map);
        // TODO: Smells SOOOOOOOOOOOOOOO Bad
        modelingService.model(Integer.parseInt(map.get("n").toString()), Double.parseDouble(map.get("q").toString()), Double.parseDouble(map.get("qMax").toString()), Integer.parseInt(map.get("shellIndex").toString()), Double.parseDouble(map.get("d").toString()), Double.parseDouble(map.get("theta").toString()), Double.parseDouble(map.get("r").toString()), Double.parseDouble(map.get("R1").toString()), Double.parseDouble(map.get("R2").toString()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception exception) {
        StaticStorage.boolStatus = false;
        StaticStorage.status = "Ошибка";
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add("Content-Type", "text/html; charset=utf-8");
        return new ResponseEntity<>("На сервере произошла ошибка.", responseHeaders, HttpStatus.BAD_REQUEST);
    }
}
