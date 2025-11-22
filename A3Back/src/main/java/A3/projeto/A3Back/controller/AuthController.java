package A3.projeto.A3Back.controller;

import A3.projeto.A3Back.DTO.AuthRequest;
import A3.projeto.A3Back.Repository.EmpresaRepository;
import A3.projeto.A3Back.model.EmpresaModel;
import A3.projeto.A3Back.Security.JwtUtil;



import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private EmpresaRepository empresaRepository;



    @Autowired
    private JwtUtil jwtUtil;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest req) {
        System.out.println(">>> [AuthController] Tentativa de login: usuário=" + req.getUsuario());

        EmpresaModel emp = empresaRepository.findByUsuario(req.getUsuario().trim().toUpperCase())
                .orElse(null);

        if (emp == null) {
            System.out.println(">>> [AuthController] Usuário não encontrado");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciais inválidas");
        }

        if (!emp.isAtivo()) {
            System.out.println(">>> [AuthController] Usuário inativo: " + emp.getUsuario());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciais inválidas");
        }

        if (!encoder.matches(req.getPassword(), emp.getPasswordHash())) {
            System.out.println(">>> [AuthController] Senha incorreta para usuário: " + emp.getUsuario());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciais inválidas");
        }

        String token = jwtUtil.generateToken(emp.getUsuario(), Map.of("empresaId", emp.getId()));
        System.out.println(">>> [AuthController] Token gerado com sucesso para usuário: " + emp.getUsuario());


        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("empresa", emp.getUsuario());


        return ResponseEntity.ok(response);
    }

    @GetMapping("/validate")
    public ResponseEntity<String> validateToken(@RequestHeader("Authorization") String authHeader) {
        System.out.println(">>> [AuthController] Validando token: " + authHeader);

        String token = authHeader.replace("Bearer ", "");
        boolean valido = jwtUtil.validateToken(token);

        System.out.println(">>> [AuthController] Resultado da validação: " + valido);

        if (valido) {
            return ResponseEntity.ok("Token válido");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token inválido");
        }
    }

}
