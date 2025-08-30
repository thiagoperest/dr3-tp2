package br.edu.infnet.dr3tp2.service;

import java.math.BigDecimal;

/**
 * Stub para plano premium com 80% de cobertura - EX6
 */
public class PlanoSaudeStubPremium implements PlanoSaude {

    @Override
    public BigDecimal getPercentualCobertura() {
        return new BigDecimal("0.80"); // 80% de cobertura
    }

    @Override
    public String getNome() {
        return "Plano Premium";
    }
}
