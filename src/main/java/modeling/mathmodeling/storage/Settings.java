package modeling.mathmodeling.storage;

import lombok.Data;
import modeling.mathmodeling.dto.SettingsDTO;

@Data
public class Settings {
    private static int availableCores;
    private static boolean isDerivativeCached;
    private static boolean isIntegrateCached;

    public static void setIsDerivativeCached(boolean isDerivativeCached)
    {
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

    public static void setSettings(SettingsDTO dto)
    {
        availableCores = dto.getAvailableCores();
        isDerivativeCached = dto.isDerivativeCached();
        isIntegrateCached = dto.isIntegrateCached();
    }
}
