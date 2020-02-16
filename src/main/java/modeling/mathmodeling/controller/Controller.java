package modeling.mathmodeling.controller;

import modeling.mathmodeling.dto.InputDTO;
import modeling.mathmodeling.dto.SettingsDTO;
import modeling.mathmodeling.service.LogService;
import modeling.mathmodeling.service.ModelingService;
import modeling.mathmodeling.storage.Settings;
import modeling.mathmodeling.storage.StaticStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class Controller {
    @Autowired
    ModelingService modelingService;

    @Autowired
    LogService logService;

    @GetMapping("/hello")
    public String hello() {
        return "Hello!";
    }

    @GetMapping("/log")
    public LinkedHashMap<String, String> getLog() {
        return logService.getLog();
    }

    @PostMapping("/status-reset")
    public void resetStatus() {
        logService.stop();
    }

    @GetMapping("/running-status")
    public Boolean getRunningStatus() {
        return logService.getRunningStatus();
    }

    @GetMapping("/output")
    public ConcurrentHashMap<Double, ArrayList<Double>> getModelingOutput() {
        return StaticStorage.modelServiceOutput;
    }

    @PostMapping("/start")
    public void modelingStart(@RequestBody InputDTO input) throws Exception {
        System.out.println(input);
        modelingService.model(input);
    }

    @GetMapping("/settings")
    public SettingsDTO getSettings(){
        return new SettingsDTO();
    }

    @PostMapping("/settings")
    public void saveSettings(@RequestBody SettingsDTO dto){
        System.out.println(dto);
        Settings.setSettings(dto);
    }

    @GetMapping("/storage/derivative")
    public ConcurrentHashMap<String, String> getStorageDerivatives(){
        return StaticStorage.alreadyComputedDerivatives;
    }

    @GetMapping("/storage/integral")
    public ConcurrentHashMap<String, String> getStorageIntegrals(){
        return StaticStorage.alreadyComputedIntegrals;
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception exception) {
        exception.printStackTrace();
        logService.stop(exception);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add("Content-Type", "text/html; charset=utf-8");
        return new ResponseEntity<>("На сервере произошла ошибка.", responseHeaders, HttpStatus.BAD_REQUEST);
    }
}
