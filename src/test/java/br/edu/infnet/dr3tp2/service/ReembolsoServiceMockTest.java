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
 * Testes do ReembolsoService usando Mock para AutorizadorReembolso - EX8
 *
 * Simula comportamento de dependências externas
 */
class ReembolsoServiceMockTest {

    @Mock
    private CalculadoraReembolso calculadoraReembolso;

    @Mock
    private AutorizadorReembolso autorizadorReembolso;

    @InjectMocks
    private ReembolsoService reembolsoService;

    private PlanoSaude planoBasico;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        planoBasico = new PlanoSaudeStubBasico();
    }

    @Test
    @DisplayName("Deve calcular reembolso quando autorizado - Consulta R$ 1.000")
    void deveCalcularReembolsoQuandoAutorizado() {
        // Arrange
        Consulta consulta = new Consulta(new BigDecimal("1000.00"), new BigDecimal("0.70"));
        BigDecimal reembolsoEsperado = new BigDecimal("700.00");

        // Mock configurado para autorizar
        when(autorizadorReembolso.isAutorizado(any(), any())).thenReturn(true);
        when(calculadoraReembolso.calcular(any(), any())).thenReturn(reembolsoEsperado);

        // Act
        BigDecimal resultado = reembolsoService.calcularReembolso(consulta);

        // Assert
        assertEquals(reembolsoEsperado, resultado);

        // Verificar interações com os mocks
        verify(autorizadorReembolso).isAutorizado(any(), any());
        verify(calculadoraReembolso).calcular(any(), any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando não autorizado - Consulta R$ 3.000")
    void deveLancarExcecaoQuandoNaoAutorizado() {
        // Arrange
        Consulta consulta = new Consulta(new BigDecimal("3000.00"), new BigDecimal("0.70"));

        // Mock configurado para negar autorização
        when(autorizadorReembolso.isAutorizado(any(), any())).thenReturn(false);
        when(autorizadorReembolso.getMotivoNegacao()).thenReturn("Valor excede limite de R$ 2.000,00");

        // Act & Assert
        SecurityException exception = assertThrows(SecurityException.class,
                () -> reembolsoService.calcularReembolso(consulta));

        assertTrue(exception.getMessage().contains("Consulta não autorizada"));
        assertTrue(exception.getMessage().contains("excede limite"));

        // Verificar que o cálculo NÃO foi executado
        verify(autorizadorReembolso).isAutorizado(any(), any());
        verify(autorizadorReembolso).getMotivoNegacao();
        verify(calculadoraReembolso, never()).calcular(any(), any());
    }

    @Test
    @DisplayName("Deve calcular reembolso com plano quando autorizado")
    void deveCalcularReembolsoComPlanoQuandoAutorizado() {
        // Arrange
        Consulta consulta = new Consulta(new BigDecimal("1500.00"), null);
        BigDecimal reembolsoEsperado = new BigDecimal("750.00");

        // Mocks configurados
        when(autorizadorReembolso.isAutorizado(any(), any())).thenReturn(true);
        when(calculadoraReembolso.calcularComPlano(any(), any(), any())).thenReturn(reembolsoEsperado);

        // Act
        BigDecimal resultado = reembolsoService.calcularReembolsoComPlano(consulta, planoBasico);

        // Assert
        assertEquals(reembolsoEsperado, resultado);
        verify(autorizadorReembolso).isAutorizado(any(), any());
        verify(calculadoraReembolso).calcularComPlano(any(), any(), any());
    }

    @Test
    @DisplayName("Deve lançar exceção com plano quando não autorizado")
    void deveLancarExcecaoComPlanoQuandoNaoAutorizado() {
        // Arrange
        Consulta consulta = new Consulta(new BigDecimal("2500.00"), null);

        // Mock configurado para negar
        when(autorizadorReembolso.isAutorizado(any(), any())).thenReturn(false);
        when(autorizadorReembolso.getMotivoNegacao()).thenReturn("Valor excede limite de R$ 2.000,00");

        // Act & Assert
        SecurityException exception = assertThrows(SecurityException.class,
                () -> reembolsoService.calcularReembolsoComPlano(consulta, planoBasico));

        assertTrue(exception.getMessage().contains("excede limite"));

        // Verificar interações
        verify(autorizadorReembolso).isAutorizado(any(), any());
        verify(calculadoraReembolso, never()).calcularComPlano(any(), any(), any());
    }

    @Test
    @DisplayName("Deve funcionar sem autorizador configurado")
    void deveFuncionarSemAutorizadorConfigurado() {
        // Arrange
        reembolsoService.autorizadorReembolso = null;
        Consulta consulta = new Consulta(new BigDecimal("3000.00"), new BigDecimal("0.70"));

        when(calculadoraReembolso.calcular(any(), any())).thenReturn(new BigDecimal("2100.00"));

        // Act & Assert - Não deve lançar exceção
        assertDoesNotThrow(() -> reembolsoService.calcularReembolso(consulta));

        verify(calculadoraReembolso).calcular(any(), any());
    }
}
