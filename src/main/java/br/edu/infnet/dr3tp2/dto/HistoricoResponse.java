package br.edu.infnet.dr3tp2.dto;

import br.edu.infnet.dr3tp2.model.Paciente;

/**
 * Record DTO para resposta de histórico de consultas
 */
public record HistoricoResponse(
        ReembolsoResponse reembolso,
        Paciente paciente
) {}
