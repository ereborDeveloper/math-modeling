package modeling.mathmodeling.storage;

import lombok.Data;
import modeling.mathmodeling.dto.SettingsDTO;

@Data
public class Settings {
    private static int availableCores = 6;
    private static boolean isDerivativeCached = true;
    private static boolean isIntegrateCached = true;

    public static void setIsDerivativeCached(boolean isDerivativeCached) {
        Settings.isDerivativeCached = isDerivativeCached;
    }

    public static void setIsIntegrateCached(boolean isIntegrateCached) {
        Settings.isIntegrateCached = isIntegrateCached;
    }

    public static boolean getIsDerivativeCached() {
        return isDerivativeCached;
    }

    public static boolean getIsIntegrateCached() {
        return isIntegrateCached;
    }

    public static void setAvailableCores(int availableCores) {
        Settings.availableCores = availableCores;
    }

    public static int getAvailableCores() {
        return availableCores;
    }

    public static void setSettings(SettingsDTO dto) {
        if (dto.getAvailableCores() > Runtime.getRuntime().availableProcessors() / 2) {
            availableCores = Runtime.getRuntime().availableProcessors() / 2;
        } else {
            availableCores = dto.getAvailableCores();
        }
        isDerivativeCached = dto.isDerivativeCached();
        if(!isDerivativeCached)
        {
            StaticStorage.alreadyComputedDerivatives.clear();
        }
        isIntegrateCached = dto.isIntegrateCached();
        if(!isIntegrateCached)
        {
            StaticStorage.alreadyComputedIntegrals.clear();
        }
    }
}
