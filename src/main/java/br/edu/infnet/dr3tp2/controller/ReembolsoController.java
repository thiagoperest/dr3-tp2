package br.edu.infnet.dr3tp2.controller;

import br.edu.infnet.dr3tp2.dto.ReembolsoResponse;
import br.edu.infnet.dr3tp2.dto.StatusResponse;
import br.edu.infnet.dr3tp2.model.Consulta;
import br.edu.infnet.dr3tp2.service.ReembolsoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
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
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "erro", e.getMessage(),
                    "status", "erro"
            ));
        }
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
