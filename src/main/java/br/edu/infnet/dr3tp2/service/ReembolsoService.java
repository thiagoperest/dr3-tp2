package br.edu.infnet.dr3tp2.service;

import br.edu.infnet.dr3tp2.model.Consulta;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Classe Service responsável por calcular reembolsos de consultas médicas
 */
@Service
public class ReembolsoService {

    /**
     * Calcula o valor de reembolso de uma consulta médica
     *
     * @param consulta Consulta com valor e percentual de cobertura
     * @return Valor do reembolso calculado
     * @throws IllegalArgumentException para dados inválidos
     */
    public BigDecimal calcularReembolso(Consulta consulta) {
        // Validação obrigatória - consulta não pode ser nula
        if (consulta == null) {
            throw new IllegalArgumentException("Consulta não pode ser nula");
        }

        // Validação do valor da consulta
        if (consulta.getValor() == null || consulta.getValor().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Valor da consulta deve ser maior ou igual a zero");
        }

        // Validação do percentual de cobertura
        if (consulta.getPercentualCobertura() == null ||
                consulta.getPercentualCobertura().compareTo(BigDecimal.ZERO) < 0 ||
                consulta.getPercentualCobertura().compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException("Percentual de cobertura deve estar entre 0% e 100%");
        }

        // Cálculo do reembolso: valor * percentual de cobertura
        BigDecimal reembolso = consulta.getValor()
                .multiply(consulta.getPercentualCobertura())
                .setScale(2, RoundingMode.HALF_UP);

        return reembolso;
    }
}
