package br.edu.infnet.dr3tp2.service;

import br.edu.infnet.dr3tp2.model.Consulta;
import br.edu.infnet.dr3tp2.model.Paciente;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Classe para cálculo de reembolso
 */
@Component
public class CalculadoraReembolso {

    // EX11 - Aplicando o teto máximo de reembolso por consulta de R$ 150.00
    private static final BigDecimal TETO_REEMBOLSO = new BigDecimal("150.00");

    /**
     * Calcula o valor de reembolso de uma consulta médica
     *
     * @param consulta Consulta com valor e percentual de cobertura
     * @param paciente Paciente (dummy object - EX4)
     * @return Valor do reembolso calculado
     * @throws IllegalArgumentException para dados inválidos
     */
    public BigDecimal calcular(Consulta consulta, Paciente paciente) {
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
        BigDecimal reembolsoCalculado = consulta.getValor()
                .multiply(consulta.getPercentualCobertura())
                .setScale(2, RoundingMode.HALF_UP);

        // EX11 - Aplicar teto de R$ 150,00
        return aplicarTeto(reembolsoCalculado);
    }

    /**
     * Calcula o valor de reembolso usando plano de saúde
     *
     * @param consulta Consulta com valor
     * @param planoSaude Plano que define percentual de cobertura
     * @return Valor do reembolso calculado
     * @throws IllegalArgumentException para dados inválidos
     */
    public BigDecimal calcularComPlano(Consulta consulta, PlanoSaude planoSaude) {
        // Validações obrigatórias
        if (consulta == null) {
            throw new IllegalArgumentException("Consulta não pode ser nula");
        }
        if (planoSaude == null) {
            throw new IllegalArgumentException("Plano de saúde não pode ser nulo");
        }

        // Validação do valor da consulta
        if (consulta.getValor() == null || consulta.getValor().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Valor da consulta deve ser maior ou igual a zero");
        }

        BigDecimal percentualPlano = planoSaude.getPercentualCobertura();

        // Cálculo do reembolso usando percentual do plano
        BigDecimal reembolsoCalculado = consulta.getValor()
                .multiply(percentualPlano)
                .setScale(2, RoundingMode.HALF_UP);

        // EX11 - Aplicar teto de R$ 150,00
        return aplicarTeto(reembolsoCalculado);
    }

    // EX11 - Método para aplicar o teto de reembolso
    private BigDecimal aplicarTeto(BigDecimal valorCalculado) {
        if (valorCalculado.compareTo(TETO_REEMBOLSO) > 0) {
            return TETO_REEMBOLSO;
        }
        return valorCalculado;
    }
}
