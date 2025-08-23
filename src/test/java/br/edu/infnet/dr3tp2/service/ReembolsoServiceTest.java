package br.edu.infnet.dr3tp2.service;

import br.edu.infnet.dr3tp2.model.Consulta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Classe de testes para ReembolsoService
 *
 * CICLO TDD:
 * 1. RED - Escrever teste que falha
 * 2. GREEN - Implementar código mínimo para passar
 * 3. REFACTOR - Melhorar código mantendo testes passando
 */
class ReembolsoServiceTest {

    private ReembolsoService reembolsoService;

    @BeforeEach
    void setUp() {
        // Configuração inicial para cada teste, instanciando a classe a ser testada
        reembolsoService = new ReembolsoService();
    }

    @Test
    @DisplayName("Deve calcular reembolso básico: R$ 200 com 70% = R$ 140")
    void deveCalcularReembolsoBasico() {
        // Arrange - Preparar dados do teste
        Consulta consulta = new Consulta(
                new BigDecimal("200.00"),
                new BigDecimal("0.70")  // 70% de cobertura do reembolso
        );
        BigDecimal reembolsoEsperado = new BigDecimal("140.00");

        // Act - Executar ação a ser testada
        BigDecimal reembolsoCalculado = reembolsoService.calcularReembolso(consulta);

        // Assert - Verificar resultado
        assertEquals(0, reembolsoEsperado.compareTo(reembolsoCalculado),
                "Reembolso deve ser 70% de R$ 200,00 = R$ 140,00");
    }

    @Test
    @DisplayName("Deve calcular reembolso com valor zero")
    void deveCalcularReembolsoComValorZero() {
        // Arrange
        Consulta consulta = new Consulta(
                BigDecimal.ZERO,
                new BigDecimal("0.80")
        );
        BigDecimal reembolsoEsperado = BigDecimal.ZERO;

        // Act
        BigDecimal reembolsoCalculado = reembolsoService.calcularReembolso(consulta);

        // Assert
        assertEquals(0, reembolsoEsperado.compareTo(reembolsoCalculado),
                "Reembolso de consulta gratuita deve ser zero");
    }

    @Test
    @DisplayName("Deve calcular reembolso com cobertura 100%")
    void deveCalcularReembolsoComCoberturaTotal() {
        // Arrange
        Consulta consulta = new Consulta(
                new BigDecimal("150.00"),
                new BigDecimal("1.00")  // 100% de cobertura - reembolso total do valor da consulta
        );
        BigDecimal reembolsoEsperado = new BigDecimal("150.00");

        // Act
        BigDecimal reembolsoCalculado = reembolsoService.calcularReembolso(consulta);

        // Assert
        assertEquals(0, reembolsoEsperado.compareTo(reembolsoCalculado),
                "Reembolso com 100% de cobertura deve ser igual ao valor da consulta");
    }

    @Test
    @DisplayName("Deve lançar exceção para consulta nula")
    void deveLancarExcecaoParaConsultaNula() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> reembolsoService.calcularReembolso(null),
                "Deve lançar exceção para consulta nula");
    }
}
