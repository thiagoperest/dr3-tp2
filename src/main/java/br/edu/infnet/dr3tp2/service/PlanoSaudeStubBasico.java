package br.edu.infnet.dr3tp2.service;

import java.math.BigDecimal;

/**
 * Stub para plano básico com 50% de cobertura - EX6
 */
public class PlanoSaudeStubBasico implements PlanoSaude {

    @Override
    public BigDecimal getPercentualCobertura() {
        return new BigDecimal("0.50"); // 50% de cobertura
    }

    @Override
    public String getNome() {
        return "Plano Básico";
    }
}
