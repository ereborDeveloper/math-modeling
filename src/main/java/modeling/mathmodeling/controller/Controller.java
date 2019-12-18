package modeling.mathmodeling.controller;

import modeling.mathmodeling.dto.InputDTO;
import modeling.mathmodeling.service.ModelingService;
import modeling.mathmodeling.storage.StaticStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ConcurrentHashMap;

@RestController
public class Controller {
    @Autowired
    ModelingService modelingService;

    @GetMapping("/hello")
    public String hello(){
        return "Hello!";
    }
    @GetMapping("/modeling/status")
    public Boolean getModelingStatus(){
        return StaticStorage.isModeling;
    }
    @GetMapping("/modeling/output")
    public ConcurrentHashMap<Double, Double> getModelingOutput()
    {
        return StaticStorage.modelServiceOutput;
    }
    @PostMapping(value = "/modeling/start", params = {"n"})
    public void modelingStart(@RequestBody InputDTO input)
    {
        modelingService.model(input.getN());
    }
}
