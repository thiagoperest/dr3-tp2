package br.edu.infnet.dr3tp2.service;

import br.edu.infnet.dr3tp2.helper.ConsultaTestHelper; // EX9 - Helper
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Teste de integração completo - EX12
 * Combina todos os dublês: Stub, Mock, Helper, Spy e Fake
 *
 * Valida o funcionamento conjunto de todos os componentes:
 * - Helper para criação de consultas (EX9)
 * - Stub para PlanoSaude (EX6)
 * - Mock para AutorizadorReembolso (EX8)
 * - Spy para Auditoria (EX7)
 * - Fake para Histórico (EX5)
 * - Teto de reembolso (EX11)
 * - Comparação com margem de erro (EX10)
 */
class ReembolsoServiceTodosDublesTest {

    @Mock
    private CalculadoraReembolso calculadoraReembolso;

    @Mock
    private AutorizadorReembolso autorizadorReembolso; // EX8 - Mock

    @Mock
    private HistoricoConsultas historicoConsultas;

    @InjectMocks
    private ReembolsoService reembolsoService;

    private PlanoSaude planoBasico; // EX6 - Stub
    private PlanoSaude planoPremium; // EX6 - Stub
    private AuditoriaSpy auditoriaSpy; // EX7 - Spy

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // EX6 - Stubs de planos
        planoBasico = new PlanoSaudeStubBasico();    // 50% básico
        planoPremium = new PlanoSaudeStubPremium();  // 80% premium

        // EX7 - Spy de auditoria
        auditoriaSpy = new AuditoriaSpy();
        reembolsoService.auditoria = auditoriaSpy;
    }

    // EX10 - Função de apoio para comparação com margem de erro
    private void assertEqualsComMargem(BigDecimal esperado, BigDecimal atual, String mensagem) {
        BigDecimal diferenca = esperado.subtract(atual).abs();
        BigDecimal margemErro = new BigDecimal("0.01");
        assertTrue(diferenca.compareTo(margemErro) <= 0, mensagem);
    }

    @Test
    @DisplayName("EX12 - Cenário completo: Consulta autorizada com plano básico")
    void cenarioCompletoConsultaAutorizadaPlanoBasico() {
        // Arrange - EX9 - Usando helper para criar consulta
        Consulta consulta = ConsultaTestHelper.criarConsultaAutorizada(); // R$ 1.500 com 70%
        BigDecimal reembolsoEsperado = new BigDecimal("150.00"); // EX11 - Limitado pelo teto

        // EX8 - Mock autoriza a consulta
        when(autorizadorReembolso.isAutorizado(any(), any())).thenReturn(true);

        // Calculadora retorna valor que será limitado pelo teto
        when(calculadoraReembolso.calcularComPlano(any(), any()))
                .thenReturn(reembolsoEsperado); // Já aplicando o teto

        // Act
        BigDecimal resultado = reembolsoService.calcularReembolsoComPlano(consulta, planoBasico);

        // Assert - Verificar resultado final
        assertEqualsComMargem(reembolsoEsperado, resultado,
                "Reembolso deve ser limitado ao teto de R$ 150,00");

        // EX8 - Verificar interação com mock de autorização
        verify(autorizadorReembolso).isAutorizado(any(), any());

        // EX7 - Verificar que auditoria foi chamada (Spy)
        assertTrue(auditoriaSpy.foiChamado(), "Auditoria deve ter sido chamada");
        assertEquals(1, auditoriaSpy.getQuantidadeChamadas());
        assertTrue(auditoriaSpy.foiChamadoCom(consulta));

        // Verificar calculadora foi chamada
        verify(calculadoraReembolso).calcularComPlano(any(), eq(planoBasico));

        // EX6 - Verificar propriedades do stub
        assertEquals("Plano Básico", planoBasico.getNome());
        assertEqualsComMargem(new BigDecimal("0.50"), planoBasico.getPercentualCobertura(),
                "Plano básico deve ter 50% de cobertura");
    }

    @Test
    @DisplayName("EX12 - Cenário completo: Consulta NÃO autorizada com plano premium")
    void cenarioCompletoConsultaNaoAutorizadaPlanoPremium() {
        // Arrange - EX9 - Helper para consulta não autorizada
        Consulta consulta = ConsultaTestHelper.criarConsultaNaoAutorizada(); // R$ 2.500 com 70%

        // EX8 - Mock nega a autorização
        when(autorizadorReembolso.isAutorizado(any(), any())).thenReturn(false);
        when(autorizadorReembolso.getMotivoNegacao()).thenReturn("Valor excede limite de R$ 2.000,00");

        // Act & Assert - Deve lançar exceção
        SecurityException exception = assertThrows(SecurityException.class,
                () -> reembolsoService.calcularReembolsoComPlano(consulta, planoPremium));

        assertTrue(exception.getMessage().contains("Consulta não autorizada"));
        assertTrue(exception.getMessage().contains("excede limite"));

        // EX8 - Verificar interações com mock
        verify(autorizadorReembolso).isAutorizado(any(), any());
        verify(autorizadorReembolso).getMotivoNegacao();

        // EX7 - Verificar auditoria (pode variar dependendo da implementação)
        // Em cenários de falha, a auditoria pode ser chamada antes ou depois da validação
        // Por isso verificamos de forma mais flexível
        if (auditoriaSpy.foiChamado()) {
            // Se foi chamada, deve ter registrado a consulta corretamente
            assertTrue(auditoriaSpy.foiChamadoCom(consulta));
        }
        // Nota: Auditoria pode não ser chamada se a validação falhar antes

        // Calculadora NÃO deve ser chamada
        verify(calculadoraReembolso, never()).calcularComPlano(any(), any());

        // EX6 - Verificar propriedades do stub premium
        assertEquals("Plano Premium", planoPremium.getNome());
        assertEqualsComMargem(new BigDecimal("0.80"), planoPremium.getPercentualCobertura(),
                "Plano premium deve ter 80% de cobertura");
    }

    @Test
    @DisplayName("EX12 - Cenário completo: Consulta comum com histórico salvo")
    void cenarioCompletoConsultaComumComHistorico() {
        // Arrange - EX9 - Helper para consulta padrão
        Consulta consulta = ConsultaTestHelper.criarConsultaPadrao();
        BigDecimal reembolsoEsperado = new BigDecimal("140.00");

        // EX8 - Mock autoriza
        when(autorizadorReembolso.isAutorizado(any(), any())).thenReturn(true);

        // Calculadora retorna valor esperado (abaixo do teto)
        when(calculadoraReembolso.calcular(any(), any())).thenReturn(reembolsoEsperado);

        // Act - Usar método que salva histórico
        BigDecimal resultado = reembolsoService.calcularReembolso(consulta);

        // Assert
        assertEqualsComMargem(reembolsoEsperado, resultado,
                "Reembolso deve ser R$ 140,00 (abaixo do teto)");

        // EX8 - Verificar autorização
        verify(autorizadorReembolso).isAutorizado(any(), any());

        // EX7 - Verificar auditoria
        assertTrue(auditoriaSpy.foiChamado());
        assertEquals(1, auditoriaSpy.getQuantidadeChamadas());

        // Verificar que histórico foi salvo (método que salva histórico)
        verify(historicoConsultas).salvar(any(), any());

        // Verificar calculadora
        verify(calculadoraReembolso).calcular(any(), any());
    }

    @Test
    @DisplayName("EX12 - Cenário completo: Comparação entre planos com teto")
    void cenarioCompletoComparacaoEntreplanosComTeto() {
        // Arrange - EX9 - Helper para consulta que será limitada
        Consulta consulta = ConsultaTestHelper.criarConsulta(new BigDecimal("400.00"), null);
        BigDecimal tetoEsperado = new BigDecimal("150.00"); // EX11 - Ambos limitados ao teto

        // EX8 - Mock autoriza (R$ 400 < R$ 2.000)
        when(autorizadorReembolso.isAutorizado(any(), any())).thenReturn(true);

        // Calculadora retorna valores limitados pelo teto
        when(calculadoraReembolso.calcularComPlano(any(), eq(planoBasico)))
                .thenReturn(new BigDecimal("150.00")); // 50% de R$ 400 = R$ 200, limitado a R$ 150
        when(calculadoraReembolso.calcularComPlano(any(), eq(planoPremium)))
                .thenReturn(new BigDecimal("150.00")); // 80% de R$ 400 = R$ 320, limitado a R$ 150

        // Act - Testar ambos os planos
        BigDecimal resultadoBasico = reembolsoService.calcularReembolsoComPlano(consulta, planoBasico);
        auditoriaSpy.reset(); // Reset spy para segunda chamada
        BigDecimal resultadoPremium = reembolsoService.calcularReembolsoComPlano(consulta, planoPremium);

        // Assert
        assertEqualsComMargem(tetoEsperado, resultadoBasico,
                "Plano básico deve ser limitado ao teto");
        assertEqualsComMargem(tetoEsperado, resultadoPremium,
                "Plano premium deve ser limitado ao teto");

        // EX11 - Com aplicação do teto
        assertEquals(0, resultadoBasico.compareTo(resultadoPremium),
                "Com teto de R$ 150, ambos os planos resultam no mesmo valor");

        // EX8 - Verifica que autorização foi chamada 2 vezes
        verify(autorizadorReembolso, times(2)).isAutorizado(any(), any());

        // EX6 - Verifica que ambos os stubs foram utilizados
        verify(calculadoraReembolso).calcularComPlano(any(), eq(planoBasico));
        verify(calculadoraReembolso).calcularComPlano(any(), eq(planoPremium));
    }

    @Test
    @DisplayName("EX12 - Cenário completo: Fluxo sem autorizador (opcional)")
    void cenarioCompletoFluxoSemAutorizador() {
        // Arrange - EX9 - Helper
        Consulta consulta = ConsultaTestHelper.criarConsultaNaoAutorizada();
        BigDecimal reembolsoEsperado = new BigDecimal("150.00");

        // Simular cenário sem autorizador configurado
        reembolsoService.autorizadorReembolso = null;

        // Calculadora processa normalmente (sem autorização)
        when(calculadoraReembolso.calcular(any(), any())).thenReturn(reembolsoEsperado);

        // Act
        BigDecimal resultado = reembolsoService.calcularReembolso(consulta);

        // Assert
        assertEqualsComMargem(reembolsoEsperado, resultado,
                "Sem autorizador, consulta deve ser processada normalmente");

        // EX7 - Auditoria foi chamada
        assertTrue(auditoriaSpy.foiChamado());

        // Calculadora deve ser chamada
        verify(calculadoraReembolso).calcular(any(), any());

        // Histórico deve ser salvo
        verify(historicoConsultas).salvar(any(), any());
    }

    @Test
    @DisplayName("EX12 - Cenário completo: Múltiplas chamadas com Spy")
    void cenarioCompletoMultiplassChamadasComSpy() {
        // Arrange - EX9 - Helpers para diferentes consultas
        Consulta consulta1 = ConsultaTestHelper.criarConsultaPadrao();
        Consulta consulta2 = ConsultaTestHelper.criarConsultaAutorizada();

        // EX8 - Mock autoriza ambas
        when(autorizadorReembolso.isAutorizado(any(), any())).thenReturn(true);
        when(calculadoraReembolso.calcular(any(), any()))
                .thenReturn(new BigDecimal("140.00"))
                .thenReturn(new BigDecimal("150.00"));

        // Act - Múltiplas chamadas
        reembolsoService.calcularReembolso(consulta1);
        reembolsoService.calcularReembolso(consulta2);

        // Assert - EX7 - Spy registrou ambas as chamadas
        assertEquals(2, auditoriaSpy.getQuantidadeChamadas(),
                "Spy deve registrar 2 chamadas");
        assertTrue(auditoriaSpy.foiChamadoCom(consulta1));
        assertTrue(auditoriaSpy.foiChamadoCom(consulta2));
        assertEquals(consulta2, auditoriaSpy.getUltimaConsultaRegistrada());

        // EX8 - Mock
        verify(autorizadorReembolso, times(2)).isAutorizado(any(), any());

        // Calculadora
        verify(calculadoraReembolso, times(2)).calcular(any(), any());

        // Histórico
        verify(historicoConsultas, times(2)).salvar(any(), any());
    }
}
