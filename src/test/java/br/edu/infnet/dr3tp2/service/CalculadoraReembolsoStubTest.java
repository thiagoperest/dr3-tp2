package br.edu.infnet.dr3tp2.service;

import br.edu.infnet.dr3tp2.model.Consulta;
import br.edu.infnet.dr3tp2.model.Paciente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes para CalculadoraReembolso usando Stubs
 *
 * CICLO TDD:
 * 1. RED - Escrever teste que falha
 * 2. GREEN - Implementar código mínimo para passar
 * 3. REFACTOR - Melhorar código mantendo testes passando
 */
class CalculadoraReembolsoStubTest {

    private CalculadoraReembolso calculadora;
    private Paciente pacienteDummy;
    private PlanoSaude planoBasico;
    private PlanoSaude planoPremium;

    @BeforeEach
    void setUp() {
        calculadora = new CalculadoraReembolso();
        pacienteDummy = new Paciente("João Silva", "123.456.789-00");

        planoBasico = new PlanoSaudeStubBasico();   // 50%
        planoPremium = new PlanoSaudeStubPremium(); // 80%
    }

    @Test
    @DisplayName("Deve calcular reembolso com plano básico - R$ 200 com 50% = R$ 100")
    void deveCalcularReembolsoComPlanoBasico() {
        // Arrange
        Consulta consulta = new Consulta(new BigDecimal("200.00"), null);
        BigDecimal reembolsoEsperado = new BigDecimal("100.00");

        // Act - 50%
        BigDecimal reembolsoCalculado = calculadora.calcularComPlano(consulta, pacienteDummy, planoBasico);

        // Assert
        assertEquals(0, reembolsoEsperado.compareTo(reembolsoCalculado),
                "Reembolso deve ser 50% de R$ 200,00 = R$ 100,00");
        assertEquals("Plano Básico", planoBasico.getNome());
    }

    @Test
    @DisplayName("Deve calcular reembolso com plano premium - R$ 200 com 80% = R$ 160")
    void deveCalcularReembolsoComPlanoPremium() {
        // Arrange
        Consulta consulta = new Consulta(new BigDecimal("200.00"), null);
        BigDecimal reembolsoEsperado = new BigDecimal("160.00");

        // Act - 80%
        BigDecimal reembolsoCalculado = calculadora.calcularComPlano(consulta, pacienteDummy, planoPremium);

        // Assert
        assertEquals(0, reembolsoEsperado.compareTo(reembolsoCalculado),
                "Reembolso deve ser 80% de R$ 200,00 = R$ 160,00");
        assertEquals("Plano Premium", planoPremium.getNome());
    }

    @Test
    @DisplayName("Deve calcular diferentes valores com mesmo plano stub")
    void deveCalcularDiferentesValoresComMesmoPlano() {
        // Arrange
        Consulta consulta1 = new Consulta(new BigDecimal("100.00"), null);
        Consulta consulta2 = new Consulta(new BigDecimal("300.00"), null);

        // Act
        BigDecimal reembolso1 = calculadora.calcularComPlano(consulta1, pacienteDummy, planoBasico);
        BigDecimal reembolso2 = calculadora.calcularComPlano(consulta2, pacienteDummy, planoBasico);

        // Assert - 50%
        assertEquals(0, new BigDecimal("50.00").compareTo(reembolso1));
        assertEquals(0, new BigDecimal("150.00").compareTo(reembolso2));
    }

    @Test
    @DisplayName("Deve comparar diferentes stubs de planos")
    void deveCompararDiferentesStubsPlanos() {
        // Arrange
        Consulta consulta = new Consulta(new BigDecimal("1000.00"), null);

        // Act
        BigDecimal reembolsoBasico = calculadora.calcularComPlano(consulta, pacienteDummy, planoBasico);
        BigDecimal reembolsoPremium = calculadora.calcularComPlano(consulta, pacienteDummy, planoPremium);

        // Assert
        assertEquals(0, new BigDecimal("500.00").compareTo(reembolsoBasico)); // 50%
        assertEquals(0, new BigDecimal("800.00").compareTo(reembolsoPremium)); // 80%

        assertTrue(reembolsoPremium.compareTo(reembolsoBasico) > 0);
    }

    @Test
    @DisplayName("Deve lançar exceção para plano nulo")
    void deveLancarExcecaoParaPlanoNulo() {
        // Arrange
        Consulta consulta = new Consulta(new BigDecimal("200.00"), null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> calculadora.calcularComPlano(consulta, pacienteDummy, null),
                "Deve lançar exceção para plano nulo");
    }

    @Test
    @DisplayName("Deve lançar exceção para consulta nula com plano")
    void deveLancarExcecaoParaConsultaNulaComPlano() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> calculadora.calcularComPlano(null, pacienteDummy, planoBasico),
                "Deve lançar exceção para consulta nula");
    }

    @Test
    @DisplayName("Deve lançar exceção para valor negativo")
    void deveLancarExcecaoParaValorNegativo() {
        // Arrange
        Consulta consulta = new Consulta(new BigDecimal("-100.00"), null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> calculadora.calcularComPlano(consulta, pacienteDummy, planoBasico),
                "Deve lançar exceção para valor negativo");
    }
}
