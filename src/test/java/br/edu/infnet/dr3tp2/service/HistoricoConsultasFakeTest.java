package br.edu.infnet.dr3tp2.service;

import br.edu.infnet.dr3tp2.dto.HistoricoResponse;
import br.edu.infnet.dr3tp2.model.Consulta;
import br.edu.infnet.dr3tp2.model.Paciente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Classe de testes para HistoricoConsultasFake
 *
 * CICLO TDD:
 * 1. RED - Escrever teste que falha
 * 2. GREEN - Implementar código mínimo para passar
 * 3. REFACTOR - Melhorar código mantendo testes passando
 */
class HistoricoConsultasFakeTest {

    private HistoricoConsultasFake historico;
    private Consulta consulta1;
    private Consulta consulta2;
    private Paciente paciente1;
    private Paciente paciente2;

    @BeforeEach
    void setUp() {
        // Configuração inicial para cada teste, instanciando a classe a ser testada
        historico = new HistoricoConsultasFake();

        consulta1 = new Consulta(new BigDecimal("200.00"), new BigDecimal("0.70"));
        consulta2 = new Consulta(new BigDecimal("150.00"), new BigDecimal("0.80"));

        paciente1 = new Paciente("João Silva", "123.456.789-00");
        paciente2 = new Paciente("Maria Santos", "987.654.321-00");
    }

    @Test
    @DisplayName("Deve salvar consulta no histórico")
    void deveSalvarConsultaNoHistorico() {
        // Arrange
        BigDecimal reembolso = consulta1.getValor().multiply(consulta1.getPercentualCobertura());

        // Act
        historico.salvarComReembolso(consulta1, paciente1, reembolso);

        // Assert
        List<HistoricoResponse> historicoCompleto = historico.buscarHistorico();
        assertEquals(1, historicoCompleto.size());
        assertEquals(consulta1.getValor(), historicoCompleto.get(0).reembolso().valorConsulta());
    }

    @Test
    @DisplayName("Deve buscar histórico completo por paciente")
    void deveBuscarHistoricoCompletoPorPaciente() {
        // Arrange
        BigDecimal reembolso1 = consulta1.getValor().multiply(consulta1.getPercentualCobertura());
        BigDecimal reembolso2 = consulta2.getValor().multiply(consulta2.getPercentualCobertura());

        historico.salvarComReembolso(consulta1, paciente1, reembolso1);
        historico.salvarComReembolso(consulta2, paciente2, reembolso2);

        // Act
        List<HistoricoResponse> historicoPaciente1 = historico.buscarHistoricoPorPaciente("123.456.789-00");
        List<HistoricoResponse> historicoPaciente2 = historico.buscarHistoricoPorPaciente("987.654.321-00");

        // Assert
        assertEquals(1, historicoPaciente1.size());
        assertEquals(consulta1.getValor(), historicoPaciente1.get(0).reembolso().valorConsulta());

        assertEquals(1, historicoPaciente2.size());
        assertEquals(consulta2.getValor(), historicoPaciente2.get(0).reembolso().valorConsulta());
    }

    @Test
    @DisplayName("Deve retornar lista vazia para paciente sem consultas no histórico completo")
    void deveRetornarListaVaziaParaPacienteSemConsultasHistoricoCompleto() {
        // Act
        List<HistoricoResponse> historico = this.historico.buscarHistoricoPorPaciente("000.000.000-00");

        // Assert
        assertTrue(historico.isEmpty());
    }

    @Test
    @DisplayName("Deve buscar múltiplas consultas do mesmo paciente no histórico completo")
    void deveBuscarMultiplasConsultasMesmoPacienteHistoricoCompleto() {
        // Arrange
        BigDecimal reembolso1 = consulta1.getValor().multiply(consulta1.getPercentualCobertura());
        BigDecimal reembolso2 = consulta2.getValor().multiply(consulta2.getPercentualCobertura());

        // Act
        historico.salvarComReembolso(consulta1, paciente1, reembolso1);
        historico.salvarComReembolso(consulta2, paciente1, reembolso2);

        // Assert
        List<HistoricoResponse> historicoPaciente = historico.buscarHistoricoPorPaciente("123.456.789-00");
        assertEquals(2, historicoPaciente.size());

        boolean contemConsulta1 = historicoPaciente.stream()
                .anyMatch(h -> h.reembolso().valorConsulta().equals(consulta1.getValor()));
        boolean contemConsulta2 = historicoPaciente.stream()
                .anyMatch(h -> h.reembolso().valorConsulta().equals(consulta2.getValor()));

        assertTrue(contemConsulta1);
        assertTrue(contemConsulta2);
    }

    @Test
    @DisplayName("Deve buscar histórico completo de todas as consultas")
    void deveBuscarHistoricoCompletoTodasConsultas() {
        // Arrange
        BigDecimal reembolso1 = consulta1.getValor().multiply(consulta1.getPercentualCobertura());
        BigDecimal reembolso2 = consulta2.getValor().multiply(consulta2.getPercentualCobertura());

        historico.salvarComReembolso(consulta1, paciente1, reembolso1);
        historico.salvarComReembolso(consulta2, paciente2, reembolso2);

        // Act
        List<HistoricoResponse> historicoCompleto = historico.buscarHistorico();

        // Assert
        assertEquals(2, historicoCompleto.size());

        boolean contemConsulta1 = historicoCompleto.stream()
                .anyMatch(h -> h.reembolso().valorConsulta().equals(consulta1.getValor()));
        boolean contemConsulta2 = historicoCompleto.stream()
                .anyMatch(h -> h.reembolso().valorConsulta().equals(consulta2.getValor()));

        assertTrue(contemConsulta1);
        assertTrue(contemConsulta2);
    }

    @Test
    @DisplayName("Deve lançar exceção para consulta nula")
    void deveLancarExcecaoParaConsultaNula() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> historico.salvar(null, paciente1),
                "Deve lançar exceção para consulta nula");
    }

    @Test
    @DisplayName("Deve lançar exceção para paciente nulo")
    void deveLancarExcecaoParaPacienteNulo() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> historico.salvar(consulta1, null),
                "Deve lançar exceção para paciente nulo");
    }

    @Test
    @DisplayName("Deve retornar lista vazia para CPF nulo no histórico completo")
    void deveRetornarListaVaziaParaCpfNuloHistoricoCompleto() {
        // Act
        List<HistoricoResponse> historico = this.historico.buscarHistoricoPorPaciente(null);

        // Assert
        assertTrue(historico.isEmpty());
    }
}
