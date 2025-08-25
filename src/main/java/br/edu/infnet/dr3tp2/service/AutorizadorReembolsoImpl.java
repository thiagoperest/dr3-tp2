package br.edu.infnet.dr3tp2.service;

import br.edu.infnet.dr3tp2.model.Consulta;
import br.edu.infnet.dr3tp2.model.Paciente;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Implementação do autorizador que bloqueia consultas acima de R$ 2.000,00 - EX8
 */
@Component
public class AutorizadorReembolsoImpl implements AutorizadorReembolso {

    private static final BigDecimal LIMITE_VALOR = new BigDecimal("2000.00");
    private String ultimoMotivoNegacao;

    @Override
    public boolean isAutorizado(Consulta consulta, Paciente paciente) {
        // Validação obrigatória
        if (consulta == null || consulta.getValor() == null) {
            ultimoMotivoNegacao = "Dados da consulta inválidos";
            return false;
        }

        // Bloqueia acima de R$ 2.000,00
        if (consulta.getValor().compareTo(LIMITE_VALOR) > 0) {
            ultimoMotivoNegacao = "Valor da consulta excede o limite de R$ 2.000,00 para reembolso!";
            return false;
        }

        ultimoMotivoNegacao = null;
        return true;
    }

    @Override
    public String getMotivoNegacao() {
        return ultimoMotivoNegacao;
    }
}
