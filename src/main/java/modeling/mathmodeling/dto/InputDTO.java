package modeling.mathmodeling.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class InputDTO {
    @JsonProperty("n")
    private int n;
    @JsonProperty("qstep")
    private double qStep;
    @JsonProperty("qmax")
    private double qMax;
    @JsonProperty("shellindex")
    private int shellIndex;
    @JsonProperty("d")
    private double d;
    @JsonProperty("theta")
    private double theta;
    @JsonProperty("r")
    private double r;
    @JsonProperty("r1")
    private double R1;
    @JsonProperty("r2")
    private double R2;
    @JsonProperty("mu12")
    private double mu12;
    @JsonProperty("mu21")
    private double mu21;
    @JsonProperty("E1")
    private double E1;
    @JsonProperty("E2")
    private double E2;
    @JsonProperty("h")
    private double h;
    @JsonProperty("k")
    private double k;
    @JsonProperty("G")
    private double G;
    @JsonProperty("z")
    private double z;
    @JsonProperty("a0")
    private double a0;
    @JsonProperty("a1")
    private double a1;
    @JsonProperty("b0")
    private double b0;
    @JsonProperty("b1")
    private double b1;
}
