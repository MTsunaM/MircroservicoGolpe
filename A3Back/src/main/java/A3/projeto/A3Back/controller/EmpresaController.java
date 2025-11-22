package A3.projeto.A3Back.controller;

import A3.projeto.A3Back.DTO.EmpresaRequest;
import A3.projeto.A3Back.DTO.EmpresaResponse;
import A3.projeto.A3Back.Repository.EmpresaRepository;
import A3.projeto.A3Back.model.EmpresaModel;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class EmpresaController {

    @Autowired
    private EmpresaRepository repository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();


    @PostMapping("/cadastroempresas")
    public ResponseEntity<EmpresaResponse> cadastrarEmpresa(@Valid @RequestBody EmpresaRequest req) {
        EmpresaModel empresa = new EmpresaModel();
        empresa.setUsuario(req.getUsuario().trim().toUpperCase());
        empresa.setCnpj(req.getCnpj());
        empresa.setPasswordHash(encoder.encode(req.getPassword()));
        empresa.setRole(EmpresaModel.Role.EMPRESA);
        empresa.setAtivo(true);

        EmpresaModel salva = repository.save(empresa);
        return ResponseEntity.ok(new EmpresaResponse(salva));
    }



}
