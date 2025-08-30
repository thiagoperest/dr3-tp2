package br.edu.infnet.dr3tp2.service;

import br.edu.infnet.dr3tp2.model.Consulta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes de integração para ReembolsoService com Spy de Auditoria - EX7
 */
class ReembolsoServiceSpyTest {

    @Mock
    private CalculadoraReembolso calculadoraReembolso;

    @InjectMocks
    private ReembolsoService reembolsoService;

    private AuditoriaSpy auditoriaSpy;
    private PlanoSaude planoBasico;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Spy para verificar chamadas dos endpoints
        auditoriaSpy = new AuditoriaSpy();
        reembolsoService.auditoria = auditoriaSpy;

        // Stub para testes com plano (50% básico)
        planoBasico = new PlanoSaudeStubBasico();
    }

    @Test
    @DisplayName("Deve chamar auditoria ao calcular reembolso")
    void deveChamarAuditoriaAoCalcularReembolso() {
        // Arrange
        Consulta consulta = new Consulta(new BigDecimal("200.00"), new BigDecimal("0.70"));

        when(calculadoraReembolso.calcular(any(), any()))
                .thenReturn(new BigDecimal("140.00"));

        // Act
        reembolsoService.calcularReembolso(consulta);

        // Assert
        assertTrue(auditoriaSpy.foiChamado(), "Auditoria deve ser chamada");
        assertEquals(1, auditoriaSpy.getQuantidadeChamadas());
        assertTrue(auditoriaSpy.foiChamadoCom(consulta));
        assertEquals(consulta, auditoriaSpy.getUltimaConsultaRegistrada());
    }

    @Test
    @DisplayName("Deve chamar auditoria ao calcular reembolso com plano")
    void deveChamarAuditoriaAoCalcularReembolsoComPlano() {
        // Arrange
        Consulta consulta = new Consulta(new BigDecimal("200.00"), null);

        when(calculadoraReembolso.calcularComPlano(any(), any()))
                .thenReturn(new BigDecimal("100.00"));

        // Act
        reembolsoService.calcularReembolsoComPlano(consulta, planoBasico);

        // Assert
        assertTrue(auditoriaSpy.foiChamado(), "Auditoria deve ser chamada com plano");
        assertEquals(1, auditoriaSpy.getQuantidadeChamadas());
        assertTrue(auditoriaSpy.foiChamadoCom(consulta));
    }

    @Test
    @DisplayName("Deve chamar auditoria múltiplas vezes")
    void deveChamarAuditoriaMultiplasVezes() {
        // Arrange
        Consulta consulta1 = new Consulta(new BigDecimal("200.00"), new BigDecimal("0.70"));
        Consulta consulta2 = new Consulta(new BigDecimal("300.00"), new BigDecimal("0.80"));

        when(calculadoraReembolso.calcular(any(), any()))
                .thenReturn(new BigDecimal("140.00"))
                .thenReturn(new BigDecimal("240.00"));

        // Act
        reembolsoService.calcularReembolso(consulta1);
        reembolsoService.calcularReembolso(consulta2);

        // Assert
        assertEquals(2, auditoriaSpy.getQuantidadeChamadas());
        assertTrue(auditoriaSpy.foiChamadoCom(consulta1));
        assertTrue(auditoriaSpy.foiChamadoCom(consulta2));
        assertEquals(consulta2, auditoriaSpy.getUltimaConsultaRegistrada());
    }

    @Test
    @DisplayName("Deve funcionar sem auditoria configurada")
    void deveFuncionarSemAuditoriaConfigurada() {
        // Arrange
        reembolsoService.auditoria = null; // Remove auditoria para teste
        Consulta consulta = new Consulta(new BigDecimal("200.00"), new BigDecimal("0.70"));

        when(calculadoraReembolso.calcular(any(), any()))
                .thenReturn(new BigDecimal("140.00"));

        // Act & Assert - Não deve lançar exceção
        assertDoesNotThrow(() -> reembolsoService.calcularReembolso(consulta));

        // Neste caso o Spy não foi chamado pois auditoria é null
        assertFalse(auditoriaSpy.foiChamado());
    }

    @Test
    @DisplayName("Deve chamar auditoria mesmo se cálculo falha")
    void deveChamarAuditoriaMesmoSeCalculoFalha() {
        // Arrange
        Consulta consulta = new Consulta(new BigDecimal("-100.00"), new BigDecimal("0.70"));

        when(calculadoraReembolso.calcular(any(), any()))
                .thenThrow(new IllegalArgumentException("Valor inválido"));

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> reembolsoService.calcularReembolso(consulta));

        // Spy deve registrar a chamada mesmo com falha
        assertTrue(auditoriaSpy.foiChamado(), "Auditoria deve ser chamada antes do cálculo");
        assertTrue(auditoriaSpy.foiChamadoCom(consulta));
    }

    @Test
    @DisplayName("Deve verificar ordem das operações - auditoria antes do cálculo")
    void deveVerificarOrdemDasOperacoes() {
        // Arrange
        Consulta consulta = new Consulta(new BigDecimal("200.00"), new BigDecimal("0.70"));

        // Mock que verifica a auditoria quando cálculo é executado
        when(calculadoraReembolso.calcular(any(), any()))
                .thenAnswer(invocation -> {
                    assertTrue(auditoriaSpy.foiChamado(),
                            "Auditoria deve ser chamada antes do cálculo");
                    return new BigDecimal("140.00");
                });

        // Act
        reembolsoService.calcularReembolso(consulta);

        // Assert
        verify(calculadoraReembolso).calcular(any(), any());
    }
}
