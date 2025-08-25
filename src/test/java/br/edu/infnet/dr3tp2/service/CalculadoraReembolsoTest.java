package br.edu.infnet.dr3tp2.service;

import br.edu.infnet.dr3tp2.model.Consulta;
import br.edu.infnet.dr3tp2.model.Paciente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Classe de testes para CalculadoraReembolso
 *
 * CICLO TDD:
 * 1. RED - Escrever teste que falha
 * 2. GREEN - Implementar código mínimo para passar
 * 3. REFACTOR - Melhorar código mantendo testes passando
 */
class CalculadoraReembolsoTest {

    private CalculadoraReembolso calculadora;
    private Paciente pacienteDummy;
    private ReembolsoService reembolsoService;
    private HistoricoConsultasFake historico;

    @BeforeEach
    void setUp() {
        // Configuração inicial para cada teste, instanciando a classe a ser testada
        calculadora = new CalculadoraReembolso();
        pacienteDummy = new Paciente("João Silva", "123.456.789-00");

        // Setup para testes de integração com histórico
        historico = new HistoricoConsultasFake();
        reembolsoService = new ReembolsoService();
    }

    @Test
    @DisplayName("Deve calcular reembolso básico: R$ 200 com 70% = R$ 140")
    void deveCalcularReembolsoBasico() {
        // Arrange - Dados de entrada isolados
        Consulta consulta = new Consulta(
                new BigDecimal("200.00"),
                new BigDecimal("0.70")  // 70% de cobertura do reembolso
        );
        BigDecimal reembolsoEsperado = new BigDecimal("140.00");

        // Act - Executar ação a ser testada
        BigDecimal reembolsoCalculado = calculadora.calcular(consulta, pacienteDummy);

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
        BigDecimal reembolsoCalculado = calculadora.calcular(consulta, pacienteDummy);

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
        BigDecimal reembolsoCalculado = calculadora.calcular(consulta, pacienteDummy);

        // Assert
        assertEquals(0, reembolsoEsperado.compareTo(reembolsoCalculado),
                "Reembolso com 100% de cobertura deve ser igual ao valor da consulta");
    }

    @Test
    @DisplayName("Deve lançar exceção para consulta nula")
    void deveLancarExcecaoParaConsultaNula() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> calculadora.calcular(null, pacienteDummy),
                "Deve lançar exceção para consulta nula");
    }

    @Test
    @DisplayName("Deve lançar exceção para percentual de cobertura inválido")
    void deveLancarExcecaoParaPercentualInvalido() {
        // Arrange - Percentual maior que 100%
        Consulta consulta = new Consulta(
                new BigDecimal("200.00"),
                new BigDecimal("1.50")  // 150% - inválido, máximo de 100%
        );

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> calculadora.calcular(consulta, pacienteDummy),
                "Deve lançar exceção para percentual maior que 100%");
    }

    @Test
    @DisplayName("Deve salvar consulta no histórico após cálculo")
    void deveSalvarConsultaNoHistoricoAposCalculo() {
        // Arrange
        Consulta consulta = new Consulta(new BigDecimal("200.00"), new BigDecimal("0.70"));
        BigDecimal reembolso = consulta.getValor().multiply(consulta.getPercentualCobertura());

        // Act
        historico.salvarComReembolso(consulta, pacienteDummy, reembolso);

        // Assert
        assertEquals(1, historico.buscarHistorico().size());
        var registro = historico.buscarHistorico().get(0);
        assertEquals(consulta.getValor(), registro.reembolso().valorConsulta());
        assertEquals(pacienteDummy.getNome(), registro.paciente().getNome());
    }

    @Test
    @DisplayName("Deve buscar histórico completo por paciente específico")
    void deveBuscarHistoricoCompletoPorPaciente() {
        // Arrange
        Consulta consulta1 = new Consulta(new BigDecimal("200.00"), new BigDecimal("0.70"));
        Consulta consulta2 = new Consulta(new BigDecimal("150.00"), new BigDecimal("0.80"));
        Paciente paciente2 = new Paciente("Maria Santos", "987.654.321-00");

        BigDecimal reembolso1 = consulta1.getValor().multiply(consulta1.getPercentualCobertura());
        BigDecimal reembolso2 = consulta2.getValor().multiply(consulta2.getPercentualCobertura());

        // Act
        historico.salvarComReembolso(consulta1, pacienteDummy, reembolso1);
        historico.salvarComReembolso(consulta2, paciente2, reembolso2);

        // Assert
        var historicoPaciente1 = historico.buscarHistoricoPorPaciente("123.456.789-00");
        assertEquals(1, historicoPaciente1.size());

        var registro = historicoPaciente1.get(0);
        assertEquals(consulta1.getValor(), registro.reembolso().valorConsulta());
        assertEquals(pacienteDummy.getNome(), registro.paciente().getNome());
        assertEquals(pacienteDummy.getCpf(), registro.paciente().getCpf());
    }
}
