package br.edu.infnet.dr3tp2.helper;

import br.edu.infnet.dr3tp2.model.Consulta;

import java.math.BigDecimal;

/**
 * Helper para criação de objetos Consulta nos testes - EX9
 */
public class ConsultaTestHelper {

    // Valores padrão usados nos testes unitários
    private static final BigDecimal VALOR_PADRAO = new BigDecimal("200.00");
    private static final BigDecimal PERCENTUAL_PADRAO = new BigDecimal("0.70");

    /**
     * Cria consulta com valores padrão: Exemplo -> R$ 200,00 com 70% de cobertura
     *
     * @return Consulta com dados padrão da consulta
     */
    public static Consulta criarConsultaPadrao() {
        return new Consulta(VALOR_PADRAO, PERCENTUAL_PADRAO);
    }

    /**
     * Cria consulta com valor personalizado e percentual padrão de 70%
     *
     * @param valor Valor da consulta
     * @return Consulta com valor personalizado
     */
    public static Consulta criarConsultaComValor(BigDecimal valor) {
        return new Consulta(valor, PERCENTUAL_PADRAO);
    }

    /**
     * Cria consulta com valor e percentual personalizados (informando na request)
     *
     * @param valor Valor da consulta
     * @param percentual Percentual de cobertura
     * @return Consulta personalizada
     */
    public static Consulta criarConsulta(BigDecimal valor, BigDecimal percentual) {
        return new Consulta(valor, percentual);
    }

    /**
     * Cria consulta para plano
     *
     * @param valor Valor da consulta
     * @return Consulta para uso com planos
     */
    public static Consulta criarConsultaParaPlano(BigDecimal valor) {
        return new Consulta(valor, null);
    }

    /**
     * Cria consulta autorizada (deve ser com valor abaixo de R$ 2.000 para ser autorizada)
     *
     * @return Consulta
     */
    public static Consulta criarConsultaAutorizada() {
        return new Consulta(new BigDecimal("1500.00"), PERCENTUAL_PADRAO);
    }

    /**
     * Cria consulta não autorizada (acima de R$ 2.000)
     *
     * @return Consulta que será bloqueada pela autorização com valores acima de R$ 2.000
     */
    public static Consulta criarConsultaNaoAutorizada() {
        return new Consulta(new BigDecimal("2500.00"), PERCENTUAL_PADRAO);
    }
}
