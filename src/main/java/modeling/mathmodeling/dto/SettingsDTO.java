package modeling.mathmodeling.dto;

import lombok.Data;
import modeling.mathmodeling.storage.Settings;

@Data
public class SettingsDTO {
    private int availableCores;
    private boolean isDerivativeCached;
    private boolean isIntegrateCached;

    public SettingsDTO() {
        availableCores = Settings.getAvailableCores();
        isDerivativeCached = Settings.getIsDerivativeCached();
        isIntegrateCached = Settings.getIsIntegrateCached();
    }
}
