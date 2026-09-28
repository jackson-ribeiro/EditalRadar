package br.com.editalradar.dashboard;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService servico;

    public DashboardController(DashboardService servico) {
        this.servico = servico;
    }

    @GetMapping("/resumo")
    public ResumoDto resumo() {
        return servico.resumo();
    }

    @GetMapping("/por-uf")
    public List<TotalPorUf> porUf() {
        return servico.porUf();
    }

    @GetMapping("/por-banca")
    public List<TotalPorBanca> porBanca() {
        return servico.porBanca();
    }

    @GetMapping("/por-faixa-salarial")
    public List<TotalPorFaixa> porFaixaSalarial() {
        return servico.porFaixaSalarial();
    }
}
