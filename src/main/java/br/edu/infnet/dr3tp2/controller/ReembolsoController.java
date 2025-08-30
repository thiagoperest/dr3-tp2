package br.edu.infnet.dr3tp2.controller;

import br.edu.infnet.dr3tp2.dto.HistoricoResponse;
import br.edu.infnet.dr3tp2.dto.ReembolsoResponse;
import br.edu.infnet.dr3tp2.dto.StatusResponse;
import br.edu.infnet.dr3tp2.model.Consulta;
import br.edu.infnet.dr3tp2.service.PlanoSaudeStubBasico;
import br.edu.infnet.dr3tp2.service.PlanoSaudeStubPremium;
import br.edu.infnet.dr3tp2.service.PlanoSaude;
import br.edu.infnet.dr3tp2.service.ReembolsoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para operações de reembolso
 */
@RestController
@RequestMapping("/api/reembolso")
public class ReembolsoController {

    @Autowired
    private ReembolsoService reembolsoService;

    /**
     * Endpoint para calcular reembolso de uma consulta
     *
     * @param consulta Dados da consulta médica
     * @return Valor do reembolso calculado usando ReembolsoResponse Record
     */
    @PostMapping("/calcular")
    public ResponseEntity<?> calcularReembolso(@RequestBody Consulta consulta) {
        try {
            BigDecimal valorReembolso = reembolsoService.calcularReembolso(consulta);

            ReembolsoResponse response = new ReembolsoResponse(
                    consulta.getValor(),
                    consulta.getPercentualCobertura(),
                    valorReembolso,
                    "sucesso"
            );

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | SecurityException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "erro", e.getMessage(),
                    "status", "erro"
            ));
        }
    }

    /**
     * Endpoint para calcular reembolso com plano de saúde
     *
     * @param consulta Dados da consulta médica
     * @param tipoPlano Tipo do plano (basico ou premium)
     * @return Valor do reembolso calculado usando plano
     */
    @PostMapping("/calcular-com-plano")
    public ResponseEntity<?> calcularReembolsoComPlano(
            @RequestBody Consulta consulta,
            @RequestParam String tipoPlano) {
        try {
            // Factory para criar stubs
            PlanoSaude plano = criarPlano(tipoPlano);

            BigDecimal valorReembolso = reembolsoService.calcularReembolsoComPlano(consulta, plano);

            ReembolsoResponse response = new ReembolsoResponse(
                    consulta.getValor(),
                    plano.getPercentualCobertura(),
                    valorReembolso,
                    "sucesso"
            );

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | SecurityException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "erro", e.getMessage(),
                    "status", "erro"
            ));
        }
    }

    private PlanoSaude criarPlano(String tipoPlano) {
        return switch (tipoPlano.toLowerCase()) {
            case "basico" -> new PlanoSaudeStubBasico();
            case "premium" -> new PlanoSaudeStubPremium();
            default -> throw new IllegalArgumentException("Tipo de plano inválido: " + tipoPlano);
        };
    }

    /**
     * Endpoint para consultar histórico com dados dos pacientes
     *
     * @return Lista de histórico completo
     */
    @GetMapping("/historico")
    public ResponseEntity<List<HistoricoResponse>> consultarHistorico() {
        List<HistoricoResponse> historico = reembolsoService.buscarHistorico();
        return ResponseEntity.ok(historico);
    }

    /**
     * Endpoint para consultar histórico por CPF do paciente
     *
     * @param cpf CPF do paciente
     * @return Lista de histórico do paciente
     */
    @GetMapping("/historico/paciente/{cpf}")
    public ResponseEntity<List<HistoricoResponse>> consultarHistoricoPorPaciente(@PathVariable String cpf) {
        List<HistoricoResponse> historico = reembolsoService.buscarHistoricoPorPaciente(cpf);
        return ResponseEntity.ok(historico);
    }

    /**
     * Endpoint de teste para verificar se a API está funcionando
     */
    @GetMapping("/status")
    public ResponseEntity<StatusResponse> status() {
        StatusResponse response = new StatusResponse("API funcionando", "1.0.0");
        return ResponseEntity.ok(response);
    }
}
