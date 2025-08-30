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

    // EX10 - Função de apoio para comparação com margem de erro
    private void assertEqualsComMargem(BigDecimal esperado, BigDecimal atual, String mensagem) {
        BigDecimal diferenca = esperado.subtract(atual).abs();
        BigDecimal margemErro = new BigDecimal("0.01");
        assertTrue(diferenca.compareTo(margemErro) <= 0, mensagem);
    }

    @Test
    @DisplayName("Deve calcular reembolso com plano básico - R$ 200 com 50% = R$ 100")
    void deveCalcularReembolsoComPlanoBasico() {
        // Arrange
        Consulta consulta = new Consulta(new BigDecimal("200.00"), null);
        BigDecimal reembolsoEsperado = new BigDecimal("100.00");

        // Act - 50%
        BigDecimal reembolsoCalculado = calculadora.calcularComPlano(consulta, planoBasico);

        // Assert - EX10 - Usando comparação com margem de erro
        assertEqualsComMargem(reembolsoEsperado, reembolsoCalculado,
                "Reembolso deve ser 50% de R$ 200,00 = R$ 100,00");
        assertEquals("Plano Básico", planoBasico.getNome());
    }

    @Test
    @DisplayName("Deve calcular reembolso com plano premium - R$ 200 com 80% = R$ 150 (limitado)")
    void deveCalcularReembolsoComPlanoPremium() {
        // Arrange
        Consulta consulta = new Consulta(new BigDecimal("200.00"), null);
        BigDecimal reembolsoEsperado = new BigDecimal("150.00"); // EX11 - Limitado pelo valor de teto

        // Act - 80% de R$ 200 = R$ 160, mas limitado a R$ 150 devido ao valor de teto estabelecido
        BigDecimal reembolsoCalculado = calculadora.calcularComPlano(consulta, planoPremium);

        // Assert - EX10 - Usando comparação com margem de erro
        assertEqualsComMargem(reembolsoEsperado, reembolsoCalculado,
                "Reembolso Deve ser limitado ao valor do teto de R$ 150,00");
        assertEquals("Plano Premium", planoPremium.getNome());
    }

    @Test
    @DisplayName("Deve calcular diferentes valores com mesmo plano stub")
    void deveCalcularDiferentesValoresComMesmoPlano() {
        // Arrange
        Consulta consulta1 = new Consulta(new BigDecimal("100.00"), null);
        Consulta consulta2 = new Consulta(new BigDecimal("300.00"), null);

        // Act
        BigDecimal reembolso1 = calculadora.calcularComPlano(consulta1, planoBasico);
        BigDecimal reembolso2 = calculadora.calcularComPlano(consulta2, planoBasico);

        // Assert - EX10 - Usando comparação com margem de erro
        assertEqualsComMargem(new BigDecimal("50.00"), reembolso1, "Reembolso1 deve ser R$ 50,00");
        assertEqualsComMargem(new BigDecimal("150.00"), reembolso2, "Reembolso2 deve ser R$ 150,00"); // EX11 - Limitado pelo valor de teto
    }

    @Test
    @DisplayName("Deve comparar diferentes stubs de planos")
    void deveCompararDiferentesStubsPlanos() {
        // Arrange
        Consulta consulta = new Consulta(new BigDecimal("1000.00"), null);

        // Act
        BigDecimal reembolsoBasico = calculadora.calcularComPlano(consulta, planoBasico);
        BigDecimal reembolsoPremium = calculadora.calcularComPlano(consulta, planoPremium);

        // Assert - EX11 - Ambos limitados ao teto de R$ 150
        assertEqualsComMargem(new BigDecimal("150.00"), reembolsoBasico, "Reembolso básico limitado ao teto");
        assertEqualsComMargem(new BigDecimal("150.00"), reembolsoPremium, "Reembolso premium limitado ao teto");

        // EX11
        assertEquals(0, reembolsoPremium.compareTo(reembolsoBasico));
    }

    // EX11 - Testes para validar valor de teto com os planos básico e premium

    @Test
    @DisplayName("Deve limitar plano premium ao teto - R$ 300 com 80%")
    void deveLimitarPlanoPremiumAoTeto() {
        // Arrange - Consulta que resultaria em R$ 240 (80% de R$ 300)
        Consulta consulta = new Consulta(new BigDecimal("300.00"), null);
        BigDecimal tetoEsperado = new BigDecimal("150.00");

        // Act
        BigDecimal reembolsoCalculado = calculadora.calcularComPlano(consulta, planoPremium);

        // Assert - EX11 - Deve ser limitado ao valor do teto
        assertEqualsComMargem(tetoEsperado, reembolsoCalculado,
                "Reembolso Deve ser limitado ao valor do teto de R$ 150,00");
    }

    @Test
    @DisplayName("Deve limitar plano básico ao teto - R$ 400 com 50%")
    void deveLimitarPlanoBasicoAoTeto() {
        // Arrange - Consulta que resultaria em R$ 200 (50% de R$ 400)
        Consulta consulta = new Consulta(new BigDecimal("400.00"), null);
        BigDecimal tetoEsperado = new BigDecimal("150.00");

        // Act
        BigDecimal reembolsoCalculado = calculadora.calcularComPlano(consulta, planoBasico);

        // Assert - EX11 - Deve ser limitado ao valor do teto
        assertEqualsComMargem(tetoEsperado, reembolsoCalculado,
                "Reembolso Deve ser limitado ao valor do teto de R$ 150,00");
    }

    @Test
    @DisplayName("Não deve alterar plano abaixo do teto - R$ 200 com 50%")
    void naoDeveAlterarPlanoAbaixoDoTeto() {
        // Arrange - Consulta que resulta em R$ 100 (abaixo do teto)
        Consulta consulta = new Consulta(new BigDecimal("200.00"), null);
        BigDecimal reembolsoEsperado = new BigDecimal("100.00");

        // Act
        BigDecimal reembolsoCalculado = calculadora.calcularComPlano(consulta, planoBasico);

        // Assert - EX11
        assertEqualsComMargem(reembolsoEsperado, reembolsoCalculado,
                "Reembolso de R$ 100,00 não deve ser alterado (abaixo do teto)");
    }

    @Test
    @DisplayName("Deve lançar exceção para plano nulo")
    void deveLancarExcecaoParaPlanoNulo() {
        // Arrange
        Consulta consulta = new Consulta(new BigDecimal("200.00"), null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> calculadora.calcularComPlano(consulta, null),
                "Deve lançar exceção para plano nulo");
    }

    @Test
    @DisplayName("Deve lançar exceção para consulta nula com plano")
    void deveLancarExcecaoParaConsultaNulaComPlano() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> calculadora.calcularComPlano(null, planoBasico),
                "Deve lançar exceção para consulta nula");
    }

    @Test
    @DisplayName("Deve lançar exceção para valor negativo")
    void deveLancarExcecaoParaValorNegativo() {
        // Arrange
        Consulta consulta = new Consulta(new BigDecimal("-100.00"), null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> calculadora.calcularComPlano(consulta, planoBasico),
                "Deve lançar exceção para valor negativo");
    }
}
