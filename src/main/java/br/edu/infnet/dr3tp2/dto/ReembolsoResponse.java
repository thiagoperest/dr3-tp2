package br.edu.infnet.dr3tp2.dto;

import java.math.BigDecimal;

/**
 * Record DTO para resposta de cálculo de reembolso
 */
public record ReembolsoResponse(
        BigDecimal valorConsulta,
        BigDecimal percentualCobertura,
        BigDecimal valorReembolso,
        String status
) {}
