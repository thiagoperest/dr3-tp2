package br.edu.infnet.dr3tp2.model;

import java.math.BigDecimal;

public class Consulta {

    private BigDecimal valor;
    private BigDecimal percentualCobertura;

    public Consulta() {}

    public Consulta(BigDecimal valor, BigDecimal percentualCobertura) {
        this.valor = valor;
        this.percentualCobertura = percentualCobertura;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public BigDecimal getPercentualCobertura() {
        return percentualCobertura;
    }

    public void setPercentualCobertura(BigDecimal percentualCobertura) {
        this.percentualCobertura = percentualCobertura;
    }
}
