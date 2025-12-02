package golpeservice.controller;

import java.util.List;

import golpeservice.model.GolpeModel;
import golpeservice.repository.GolpeRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/golpes")
public class GolpeController {

    private final GolpeRepository golpeRepository;

    public GolpeController(GolpeRepository golpeRepository) {
        this.golpeRepository = golpeRepository;
    }

    // Health check endpoint (no authentication required)
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Golpes service is running");
    }

    // 📌 Cadastro de golpes (somente EMPRESA pode registrar)
    @PreAuthorize("hasRole('EMPRESA')")
    @PostMapping
    public ResponseEntity<?> cadastrarGolpe(@RequestBody GolpeModel golpe, 
                                           jakarta.servlet.http.HttpServletRequest request) {
        // Extract empresaId from JWT token (stored in request by JwtAuthFilter)
        Integer empresaId = (Integer) request.getAttribute("empresaId");
        
        if (empresaId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Não foi possível identificar a empresa. Token inválido.");
        }
        
        if (golpe.getEmpresa() == null || golpe.getEmpresa().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("O nome da empresa é obrigatório");
        }

        // Set empresaId from token (ignore any value sent in request body)
        golpe.setEmpresaId(empresaId);
        golpe.setEmpresa(golpe.getEmpresa().trim().toUpperCase());
        
        GolpeModel salvo = golpeRepository.save(golpe);
        return ResponseEntity.ok(salvo);
    }

    // 📌 Listar todos os golpes (somente ADMIN)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<?> listarGolpes() {
        try {
            System.out.println(">>> [GolpeController] Listing all golpes - ADMIN access");
            List<GolpeModel> golpes = golpeRepository.findAll();
            System.out.println(">>> [GolpeController] Found " + golpes.size() + " golpes");
            return ResponseEntity.ok(golpes);
        } catch (Exception e) {
            System.err.println(">>> [GolpeController] Error listing golpes: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving scam reports: " + e.getMessage());
        }
    }

    // 📌 Buscar golpes por nome da empresa (EMPRESA pode consultar os seus, ADMIN pode consultar todos)
    @PreAuthorize("hasAnyRole('EMPRESA','ADMIN')")
    @GetMapping("/empresa/{nome}")
    public List<GolpeModel> listarPorEmpresa(@PathVariable String nome) {
        System.out.println(">>> [GolpeController] Buscando golpes para empresa (exato): " + nome);
        List<GolpeModel> result = golpeRepository.findByEmpresaIgnoreCase(nome.trim().toUpperCase());
        System.out.println(">>> [GolpeController] Encontrados " + result.size() + " golpes");
        return result;
    }

    // 📌 Buscar golpes por nome da empresa (busca parcial/LIKE)
    @PreAuthorize("hasAnyRole('EMPRESA','ADMIN')")
    @GetMapping("/empresa/buscar/{nome}")
    public List<GolpeModel> buscarPorEmpresa(@PathVariable String nome) {
        System.out.println(">>> [GolpeController] Buscando golpes para empresa (parcial): " + nome);
        List<GolpeModel> result = golpeRepository.findByEmpresaContainingIgnoreCase(nome.trim());
        System.out.println(">>> [GolpeController] Encontrados " + result.size() + " golpes");
        return result;
    }

    // 📌 Buscar golpes por ID da empresa
    @PreAuthorize("hasAnyRole('EMPRESA','ADMIN')")
    @GetMapping("/empresa/id/{empresaId}")
    public List<GolpeModel> listarPorEmpresaId(@PathVariable Integer empresaId) {
        return golpeRepository.findByEmpresaId(empresaId);
    }

    // 📌 Ranking de empresas com mais golpes (público)
    @GetMapping("/ranking")
    public ResponseEntity<?> rankingEmpresas() {
        try {
            System.out.println(">>> [GolpeController] Buscando ranking de empresas");
            List<GolpeModel> golpes = golpeRepository.findAll();
            
            // Agrupa por empresa e conta
            java.util.Map<String, Long> ranking = golpes.stream()
                .filter(g -> g.getEmpresa() != null && !g.getEmpresa().isEmpty())
                .collect(java.util.stream.Collectors.groupingBy(
                    GolpeModel::getEmpresa,
                    java.util.stream.Collectors.counting()
                ));
            
            // Converte para lista e ordena
            java.util.List<java.util.Map<String, Object>> rankingList = ranking.entrySet().stream()
                .map(entry -> {
                    java.util.Map<String, Object> item = new java.util.HashMap<>();
                    item.put("empresa", entry.getKey());
                    item.put("count", entry.getValue());
                    return item;
                })
                .sorted((a, b) -> Long.compare((Long)b.get("count"), (Long)a.get("count")))
                .collect(java.util.stream.Collectors.toList());
            
            System.out.println(">>> [GolpeController] Ranking gerado com " + rankingList.size() + " empresas");
            return ResponseEntity.ok(rankingList);
        } catch (Exception e) {
            System.err.println(">>> [GolpeController] Erro ao gerar ranking: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao gerar ranking: " + e.getMessage());
        }
    }

    // ✏️ Atualizar golpe (somente ADMIN)
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<?> atualizarGolpe(@PathVariable Integer id, @RequestBody GolpeModel dadosAtualizados) {
        GolpeModel golpe = golpeRepository.findById(id)
                .orElse(null);

        if (golpe == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Golpe não encontrado");
        }

        golpe.setDescricao(dadosAtualizados.getDescricao());
        golpe.setEmpresa(dadosAtualizados.getEmpresa().trim().toUpperCase());
        golpe.setEmpresaId(dadosAtualizados.getEmpresaId());

        GolpeModel atualizado = golpeRepository.save(golpe);
        return ResponseEntity.ok(atualizado);
    }

    // 🗑️ Excluir golpe (somente ADMIN)
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> excluirGolpe(@PathVariable Integer id) {
        if (!golpeRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Golpe não encontrado");
        }
        golpeRepository.deleteById(id);
        return ResponseEntity.ok("Golpe excluído com sucesso");
    }
}
