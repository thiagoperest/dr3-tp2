package br.edu.infnet.dr3tp2.service;

import br.edu.infnet.dr3tp2.model.Consulta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Classe Service responsável por calcular reembolsos de consultas médicas
 */
@Service
public class ReembolsoService {

    @Autowired
    private CalculadoraReembolso calculadoraReembolso;

    /**
     * Calcula o valor de reembolso de uma consulta médica
     *
     * @param consulta Consulta com valor e percentual de cobertura
     * @return Valor do reembolso calculado
     * @throws IllegalArgumentException para dados inválidos
     */
    public BigDecimal calcularReembolso(Consulta consulta) {
        return calculadoraReembolso.calcular(consulta);
    }
}
