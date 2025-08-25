package br.edu.infnet.dr3tp2.service;

import java.math.BigDecimal;

/**
 * Interface para representar planos de saúde
 */
public interface PlanoSaude {

    /**
     * Retorna o percentual de cobertura do plano
     *
     * @return Percentual de cobertura (0.0 a 1.0) - 0% - 100%
     */
    BigDecimal getPercentualCobertura();

    /**
     * Retorna o nome do plano
     *
     * @return Nome do plano
     */
    String getNome();
}
