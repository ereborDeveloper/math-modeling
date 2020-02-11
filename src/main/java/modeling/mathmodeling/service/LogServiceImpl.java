package modeling.mathmodeling.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

@Service
public class LogServiceImpl implements LogService {

    private LinkedHashMap<String, String> status;
    private LinkedList<String> statusList;
    private Boolean isRunning = false;
    private int calculatingStep;
    private String calculatingStatusName;
    private Boolean isConsoleOutputEnabled = true;
    private long endTime;
    private long startTime;
    private long currentTaskTime;

    private DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

    @Override
    public void initialize() {
        statusList = new LinkedList<>();
        statusList.add("Запуск");
        statusList.add("Раскрытие скобок под интегралом");
        statusList.add("Подстановка аппроксимирующих функций");
        statusList.add("Взятие производных (оптимизация)");
        statusList.add("Раскрытие посчитанных производных и аппроксимирующих функций (оптимизация)");
        statusList.add("Подготовка к интегрированию (оптимизация)");
        statusList.add("Взятие двойного интеграла");
        statusList.add("Подготовка к взятию производных (оптимизация)");
        statusList.add("Рассчет градиента");
        statusList.add("Рассчет матрицы Гесса");
        statusList.add("Выполнение метода Ньютона и отрисовки");
        clearLogTime();
    }

    private void clearLogTime() {
        status = new LinkedHashMap<>();
        for (int i = 0; i < statusList.size(); i++) {
            status.put(statusList.get(i), null);
        }
    }

    @Override
    public void start() {
        isRunning = true;
        startTime = System.nanoTime();
        currentTaskTime = System.nanoTime();
        clearLogTime();
        calculatingStep = 0;
        next();
    }

    @Override
    public void next() {
        if (calculatingStep > 0) {
            status.replace(calculatingStatusName, TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - currentTaskTime) + " с.");
        }

        LocalDateTime now = LocalDateTime.now();
        if (isConsoleOutputEnabled) {
            System.out.println(dtf.format(now) + " | " + calculatingStatusName);
        }
        calculatingStatusName = statusList.get(calculatingStep);
        calculatingStep++;
        currentTaskTime = System.nanoTime();
    }

    @Override
    public void stop() {
        isRunning = false;
        status.replace(statusList.get(calculatingStep - 1), TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - currentTaskTime) + " с.");

        endTime = System.nanoTime();
        long fullTime = endTime - startTime;
        if (isConsoleOutputEnabled) {
            System.out.println("Время выполнения: " + TimeUnit.NANOSECONDS.toSeconds(fullTime) + "с.");
        }
    }

    @Override
    public void stop(Exception error) {
        isRunning = false;
    }

    @Override
    public void setConsoleOutput(Boolean value) {
        this.isConsoleOutputEnabled = value;
    }

    @Override
    public LinkedHashMap<String, String> getLog() {
        long currentTime = System.nanoTime();
        if (!isRunning) {
            currentTime = endTime;
        }
        status.put("Общее время", TimeUnit.NANOSECONDS.toSeconds(currentTime - startTime) + " с.");
        return status;
    }

    @Override
    public Boolean getRunningStatus() {
        return isRunning;
    }
}
