package br.com.editalradar.coleta;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final SincronizacaoExecutor executor;
    private final ColetaRepository coletas;

    public AdminController(SincronizacaoExecutor executor, ColetaRepository coletas) {
        this.executor = executor;
        this.coletas = coletas;
    }

    @PostMapping("/sync")
    public ResponseEntity<SyncIniciadoDto> sincronizar() {
        Long coletaId = executor.disparar(OrigemColeta.MANUAL).orElseThrow(ColetaEmAndamentoException::new);
        return ResponseEntity.accepted().body(new SyncIniciadoDto(coletaId));
    }

    @GetMapping("/coletas/ultima")
    public ResponseEntity<ColetaDto> ultimaColeta() {
        return coletas.findFirstByOrderByIniciadaEmDesc()
                .map(ColetaDto::de)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
}
