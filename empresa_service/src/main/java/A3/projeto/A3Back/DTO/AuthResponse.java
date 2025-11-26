package A3.projeto.A3Back.DTO;

import java.util.List;

public class AuthResponse {
    private String token;
    private String empresa;
    private List<GolpeDTO> scamReports;

    public AuthResponse(String token, String empresa, List<GolpeDTO> scamReports) {
        this.token = token;
        this.empresa = empresa;
        this.scamReports = scamReports;
    }

    public String getToken() {
        return token;
    }

    public String getEmpresa() {
        return empresa;
    }

    public List<GolpeDTO> getScamReports() {
        return scamReports;
    }
}
