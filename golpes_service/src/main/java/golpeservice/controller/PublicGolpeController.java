package golpeservice.controller;

import golpeservice.model.GolpeModel;
import golpeservice.repository.GolpeRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cadastrogolpes")
public class PublicGolpeController {

    private final GolpeRepository golpeRepository;

    public PublicGolpeController(GolpeRepository golpeRepository) {
        this.golpeRepository = golpeRepository;
    }

    // 📌 Public scam registration endpoint (no authentication required)
    @PostMapping
    public ResponseEntity<?> cadastrarGolpePublico(@RequestBody GolpeModel golpe) {
        System.out.println(">>> [PublicGolpeController] Public scam registration received");
        
        // Validate required fields
        if (golpe.getNome() == null || golpe.getNome().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("O nome é obrigatório");
        }
        if (golpe.getCidade() == null || golpe.getCidade().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("A cidade é obrigatória");
        }
        if (golpe.getCpf() == null || golpe.getCpf().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("O CPF é obrigatório");
        }
        if (golpe.getMeioDeContato() == null) {
            return ResponseEntity.badRequest().body("O meio de contato é obrigatório");
        }
        if (golpe.getDescricao() == null || golpe.getDescricao().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("A descrição é obrigatória");
        }
        if (golpe.getEmailOuTelefone() == null || golpe.getEmailOuTelefone().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("O email ou telefone é obrigatório");
        }

        // For public registration, empresaId and empresa are optional
        // They will be null if not provided
        if (golpe.getEmpresa() != null && !golpe.getEmpresa().trim().isEmpty()) {
            golpe.setEmpresa(golpe.getEmpresa().trim().toUpperCase());
        }
        
        GolpeModel salvo = golpeRepository.save(golpe);
        System.out.println(">>> [PublicGolpeController] Public scam registered with ID: " + salvo.getId());
        return ResponseEntity.ok(salvo);
    }

    // 📌 Public ranking endpoint (no authentication required)
    @GetMapping("/ranking")
    public ResponseEntity<?> rankingEmpresas() {
        try {
            System.out.println(">>> [PublicGolpeController] Buscando ranking de empresas");
            java.util.List<GolpeModel> golpes = golpeRepository.findAll();
            
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
            
            System.out.println(">>> [PublicGolpeController] Ranking gerado com " + rankingList.size() + " empresas");
            return ResponseEntity.ok(rankingList);
        } catch (Exception e) {
            System.err.println(">>> [PublicGolpeController] Erro ao gerar ranking: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao gerar ranking: " + e.getMessage());
        }
    }
}
