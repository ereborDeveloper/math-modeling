package modeling.mathmodeling.service;

import modeling.mathmodeling.storage.StaticStorage;
import org.springframework.stereotype.Service;

@Service
public class LogServiceImpl implements LogService {
    @Override
    public void initializeStatus() {
        StaticStorage.status.put("Запуск", null);
        StaticStorage.status.put("Раскрытие скобок под интегралом", null);
        StaticStorage.status.put("Подстановка аппроксимирующих функций", null);
        StaticStorage.status.put("Взятие производных (оптимизация)", null);
        StaticStorage.status.put("Раскрытие посчитанных производных и аппроксимирующих функций (оптимизация)", null);
        StaticStorage.status.put("Подготовка к интегрированию (оптимизация)", null);
    }
}
