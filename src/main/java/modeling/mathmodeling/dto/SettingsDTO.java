package modeling.mathmodeling.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import modeling.mathmodeling.storage.Settings;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SettingsDTO {
    private int availableCores;
    private boolean isIntegrateCached;

    public static SettingsDTO makeDTO()
    {
        SettingsDTO dto = new SettingsDTO();
        dto.setAvailableCores(Settings.getAvailableCores());
        dto.setIntegrateCached(Settings.getIsIntegrateCached());
        return dto;
    }
}
