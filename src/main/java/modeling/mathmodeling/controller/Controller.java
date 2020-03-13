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

    @GetMapping("/log")
    public LinkedHashMap<String, String> getLog() {
//        System.out.println("Get log");
        return logService.getLog();
    }

    @PostMapping("/status-reset")
    public void resetStatus() {
//        System.out.println("Reset status");
        logService.stop();
    }

    @GetMapping("/running-status")
    public Boolean getRunningStatus() {
//        System.out.println("Get status");
        return logService.getRunningStatus();
    }

    @GetMapping("/output")
    public ConcurrentHashMap<Double, ArrayList<Double>> getModelingOutput() {
//        System.out.println("Get output");
        return StaticStorage.modelServiceOutput;
    }

    @PostMapping("/start")
    public void modelingStart(@RequestBody InputDTO input){
        System.out.println(input);
        modelingService.model(input);
    }

    @GetMapping("/settings")
    public SettingsDTO getSettings() {
//        System.out.println("Get settings");
        return new SettingsDTO();
    }

    @PostMapping("/settings")
    public void saveSettings(@RequestBody SettingsDTO dto) {
        System.out.println(dto);
        Settings.setSettings(dto);
    }

    @GetMapping("/storage/derivative")
    public ConcurrentHashMap<String, String> getStorageDerivatives() {
//        System.out.println("Get derivatives");
        return StaticStorage.alreadyComputedDerivatives;
    }

    @GetMapping("/storage/integral")
    public ConcurrentHashMap<String, String> getStorageIntegrals() {
//        System.out.println("Get integrals");
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
