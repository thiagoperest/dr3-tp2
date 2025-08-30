package br.edu.infnet.dr3tp2.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import br.edu.infnet.dr3tp2.dto.HistoricoResponse;
import br.edu.infnet.dr3tp2.dto.ReembolsoResponse;
import br.edu.infnet.dr3tp2.model.Consulta;
import br.edu.infnet.dr3tp2.model.Paciente;
import br.edu.infnet.dr3tp2.service.ReembolsoService;
import br.edu.infnet.dr3tp2.service.PlanoSaude;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Classe de teste de integração para ReembolsoController
 *
 * Usa @WebMvcTest para testar a camada web
 * @MockitoBean para simular dependências (dublês de teste)
 */
@WebMvcTest(ReembolsoController.class)
class ReembolsoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReembolsoService reembolsoService;

    @Test
    @DisplayName("Deve calcular reembolso via API - R$ 200 com 70% = R$ 140")
    void deveCalcularReembolsoViaAPI() throws Exception {
        // Arrange - Preparar dados e comportamento do mock para o caso de teste
        Consulta consulta = new Consulta(new BigDecimal("200.00"), new BigDecimal("0.70"));
        BigDecimal reembolsoEsperado = new BigDecimal("140.00");

        // Configura o comportamento do mock
        when(reembolsoService.calcularReembolso(any(Consulta.class)))
                .thenReturn(reembolsoEsperado);

        // Act & Assert - Executar requisição e verificar a resposta da request
        mockMvc.perform(post("/api/reembolso/calcular")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(consulta)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valorConsulta").value(200.00))
                .andExpect(jsonPath("$.percentualCobertura").value(0.70))
                .andExpect(jsonPath("$.valorReembolso").value(140.00))
                .andExpect(jsonPath("$.status").value("sucesso"));
    }

    @Test
    @DisplayName("Deve retornar erro para dados inválidos")
    void deveRetornarErroParaDadosInvalidos() throws Exception {
        // Arrange - Mock configurado para lançar exceção
        when(reembolsoService.calcularReembolso(any(Consulta.class)))
                .thenThrow(new IllegalArgumentException("Percentual inválido"));

        Consulta consultaInvalida = new Consulta(new BigDecimal("200.00"), new BigDecimal("1.50"));

        // Act & Assert
        mockMvc.perform(post("/api/reembolso/calcular")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(consultaInvalida)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Percentual inválido"))
                .andExpect(jsonPath("$.status").value("erro"));
    }

    @Test
    @DisplayName("Deve retornar status da API")
    void deveRetornarStatusDaAPI() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/reembolso/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("API funcionando"))
                .andExpect(jsonPath("$.versao").value("1.0.0"));
    }

    @Test
    @DisplayName("Deve consultar histórico completo via API")
    void deveConsultarHistoricoCompletoViaAPI() throws Exception {
        // Arrange - Preparar dados do histórico
        ReembolsoResponse reembolso1 = new ReembolsoResponse(
                new BigDecimal("200.00"),
                new BigDecimal("0.70"),
                new BigDecimal("140.00"),
                "sucesso"
        );
        ReembolsoResponse reembolso2 = new ReembolsoResponse(
                new BigDecimal("150.00"),
                new BigDecimal("0.80"),
                new BigDecimal("120.00"),
                "sucesso"
        );

        Paciente paciente = new Paciente("Dummy", "000.000.000-00");

        List<HistoricoResponse> historicoEsperado = Arrays.asList(
                new HistoricoResponse(reembolso1, paciente),
                new HistoricoResponse(reembolso2, paciente)
        );

        // Configurar comportamento do mock
        when(reembolsoService.buscarHistorico()).thenReturn(historicoEsperado);

        // Act & Assert - Executar requisição e verificar a resposta da request
        mockMvc.perform(get("/api/reembolso/historico"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].reembolso.valorConsulta").value(200.00))
                .andExpect(jsonPath("$[0].reembolso.valorReembolso").value(140.00))
                .andExpect(jsonPath("$[0].paciente.nome").value("Dummy"))
                .andExpect(jsonPath("$[0].paciente.cpf").value("000.000.000-00"))
                .andExpect(jsonPath("$[1].reembolso.valorConsulta").value(150.00))
                .andExpect(jsonPath("$[1].reembolso.valorReembolso").value(120.00));
    }

    @Test
    @DisplayName("Deve consultar histórico por CPF do paciente via API")
    void deveConsultarHistoricoPorPacienteViaAPI() throws Exception {
        // Arrange - Preparar dados do histórico de um paciente
        ReembolsoResponse reembolso = new ReembolsoResponse(
                new BigDecimal("200.00"),
                new BigDecimal("0.70"),
                new BigDecimal("140.00"),
                "sucesso"
        );

        Paciente paciente = new Paciente("João Silva", "123.456.789-00");

        List<HistoricoResponse> historicoEsperado = Arrays.asList(
                new HistoricoResponse(reembolso, paciente)
        );

        // Configura o comportamento do mock
        when(reembolsoService.buscarHistoricoPorPaciente(anyString()))
                .thenReturn(historicoEsperado);

        // Act & Assert - Executar requisição e verificar a resposta da request
        mockMvc.perform(get("/api/reembolso/historico/paciente/123.456.789-00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].reembolso.valorConsulta").value(200.00))
                .andExpect(jsonPath("$[0].reembolso.valorReembolso").value(140.00))
                .andExpect(jsonPath("$[0].paciente.nome").value("João Silva"))
                .andExpect(jsonPath("$[0].paciente.cpf").value("123.456.789-00"));
    }

    @Test
    @DisplayName("Deve retornar lista vazia para paciente sem histórico")
    void deveRetornarListaVaziaParaPacienteSemHistorico() throws Exception {
        // Arrange
        when(reembolsoService.buscarHistoricoPorPaciente(anyString()))
                .thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/api/reembolso/historico/paciente/999.999.999-99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("Deve calcular reembolso com plano básico via API")
    void deveCalcularReembolsoComPlanoBasicoViaAPI() throws Exception {
        // Arrange
        Consulta consulta = new Consulta(new BigDecimal("200.00"), null);
        BigDecimal reembolsoEsperado = new BigDecimal("100.00");

        // Mock
        when(reembolsoService.calcularReembolsoComPlano(any(Consulta.class), any(PlanoSaude.class)))
                .thenReturn(reembolsoEsperado);

        // Act & Assert
        mockMvc.perform(post("/api/reembolso/calcular-com-plano")
                        .param("tipoPlano", "basico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(consulta)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valorConsulta").value(200.00))
                .andExpect(jsonPath("$.percentualCobertura").value(0.50))
                .andExpect(jsonPath("$.valorReembolso").value(100.00))
                .andExpect(jsonPath("$.status").value("sucesso"));
    }

    @Test
    @DisplayName("Deve calcular reembolso com plano premium via API")
    void deveCalcularReembolsoComPlanoPremiumViaAPI() throws Exception {
        // Arrange
        Consulta consulta = new Consulta(new BigDecimal("200.00"), null);
        BigDecimal reembolsoEsperado = new BigDecimal("160.00");

        // Mock
        when(reembolsoService.calcularReembolsoComPlano(any(Consulta.class), any(PlanoSaude.class)))
                .thenReturn(reembolsoEsperado);

        // Act & Assert
        mockMvc.perform(post("/api/reembolso/calcular-com-plano")
                        .param("tipoPlano", "premium")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(consulta)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valorConsulta").value(200.00))
                .andExpect(jsonPath("$.percentualCobertura").value(0.80))
                .andExpect(jsonPath("$.valorReembolso").value(160.00))
                .andExpect(jsonPath("$.status").value("sucesso"));
    }

    @Test
    @DisplayName("Deve retornar erro para tipo de plano inválido")
    void deveRetornarErroParaTipoPlanoInvalido() throws Exception {
        // Arrange
        Consulta consulta = new Consulta(new BigDecimal("200.00"), null);

        // Act & Assert
        mockMvc.perform(post("/api/reembolso/calcular-com-plano")
                        .param("tipoPlano", "inexistente")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(consulta)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Tipo de plano inválido: inexistente"))
                .andExpect(jsonPath("$.status").value("erro"));
    }

    @Test
    @DisplayName("Deve retornar erro quando service lança exceção com plano")
    void deveRetornarErroQuandoServiceLancaExcecaoComPlano() throws Exception {
        // Arrange
        Consulta consulta = new Consulta(new BigDecimal("-100.00"), null);

        // Mock
        when(reembolsoService.calcularReembolsoComPlano(any(Consulta.class), any(PlanoSaude.class)))
                .thenThrow(new IllegalArgumentException("Valor da consulta deve ser maior ou igual a zero"));

        // Act & Assert
        mockMvc.perform(post("/api/reembolso/calcular-com-plano")
                        .param("tipoPlano", "basico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(consulta)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Valor da consulta deve ser maior ou igual a zero"))
                .andExpect(jsonPath("$.status").value("erro"));
    }

    @Test
    @DisplayName("Deve verificar comportamento de stub básico via API")
    void deveVerificarComportamentoStubBasicoViaAPI() throws Exception {
        // Arrange
        Consulta consulta1 = new Consulta(new BigDecimal("100.00"), null);
        Consulta consulta2 = new Consulta(new BigDecimal("300.00"), null);

        when(reembolsoService.calcularReembolsoComPlano(any(Consulta.class), any(PlanoSaude.class)))
                .thenReturn(new BigDecimal("50.00"))
                .thenReturn(new BigDecimal("150.00"));

        // Act & Assert
        mockMvc.perform(post("/api/reembolso/calcular-com-plano")
                        .param("tipoPlano", "basico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(consulta1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valorReembolso").value(50.00))
                .andExpect(jsonPath("$.percentualCobertura").value(0.50));

        // Act & Assert
        mockMvc.perform(post("/api/reembolso/calcular-com-plano")
                        .param("tipoPlano", "basico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(consulta2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valorReembolso").value(150.00))
                .andExpect(jsonPath("$.percentualCobertura").value(0.50));
    }

    @Test
    @DisplayName("Deve comparar diferentes planos via API")
    void deveCompararDiferentesPlanosViaAPI() throws Exception {
        // Arrange
        Consulta consulta = new Consulta(new BigDecimal("1000.00"), null);

        when(reembolsoService.calcularReembolsoComPlano(any(Consulta.class), any(PlanoSaude.class)))
                .thenReturn(new BigDecimal("500.00"))
                .thenReturn(new BigDecimal("800.00"));

        // Act & Assert - Plano Básico
        mockMvc.perform(post("/api/reembolso/calcular-com-plano")
                        .param("tipoPlano", "basico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(consulta)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valorReembolso").value(500.00))
                .andExpect(jsonPath("$.percentualCobertura").value(0.50));

        // Act & Assert - Plano Premium
        mockMvc.perform(post("/api/reembolso/calcular-com-plano")
                        .param("tipoPlano", "premium")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(consulta)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valorReembolso").value(800.00))
                .andExpect(jsonPath("$.percentualCobertura").value(0.80));
    }

    @Test
    @DisplayName("Deve propagar exceção da calculadora via API")
    void devePropagarExcecaoDaCalculadoraViaAPI() throws Exception {
        // Arrange
        Consulta consulta = new Consulta(new BigDecimal("-100.00"), null);

        when(reembolsoService.calcularReembolsoComPlano(any(Consulta.class), any(PlanoSaude.class)))
                .thenThrow(new IllegalArgumentException("Valor inválido"));

        // Act & Assert
        mockMvc.perform(post("/api/reembolso/calcular-com-plano")
                        .param("tipoPlano", "basico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(consulta)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Valor inválido"))
                .andExpect(jsonPath("$.status").value("erro"));
    }

    // EX8

    @Test
    @DisplayName("Deve bloquear consulta acima de R$ 2.000 via API")
    void deveBloquerConsultaAcimaLimiteViaAPI() throws Exception {
        // Arrange - Consulta não autorizada (acima de R$ 2.000)
        Consulta consulta = new Consulta(new BigDecimal("2500.00"), new BigDecimal("0.70"));

        // Mock configurado para lançar SecurityException
        when(reembolsoService.calcularReembolso(any(Consulta.class)))
                .thenThrow(new SecurityException("Consulta não autorizada para reembolso: Valor da consulta excede o limite de R$ 2.000,00 para reembolso"));

        // Act & Assert
        mockMvc.perform(post("/api/reembolso/calcular")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(consulta)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Consulta não autorizada para reembolso: Valor da consulta excede o limite de R$ 2.000,00 para reembolso"))
                .andExpect(jsonPath("$.status").value("erro"));
    }

    @Test
    @DisplayName("Deve bloquear plano com valor acima de R$ 2.000 via API")
    void deveBloquerPlanoAcimaLimiteViaAPI() throws Exception {
        // Arrange - Plano com valor não autorizado
        Consulta consulta = new Consulta(new BigDecimal("3000.00"), null);

        // Mock configurado para lançar SecurityException
        when(reembolsoService.calcularReembolsoComPlano(any(Consulta.class), any(PlanoSaude.class)))
                .thenThrow(new SecurityException("Consulta não autorizada para reembolso: Valor da consulta excede o limite de R$ 2.000,00 para reembolso"));

        // Act & Assert
        mockMvc.perform(post("/api/reembolso/calcular-com-plano")
                        .param("tipoPlano", "premium")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(consulta)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Consulta não autorizada para reembolso: Valor da consulta excede o limite de R$ 2.000,00 para reembolso"))
                .andExpect(jsonPath("$.status").value("erro"));
    }

    @Test
    @DisplayName("Deve calcular reembolso autorizado abaixo do limite - R$ 1.500")
    void deveCalcularReembolsoAutorizadoAbaixoLimite() throws Exception {
        // Arrange - Consulta autorizada (abaixo do limite de R$ 2.000)
        Consulta consulta = new Consulta(new BigDecimal("1500.00"), new BigDecimal("0.70"));
        BigDecimal reembolsoEsperado = new BigDecimal("1050.00");

        // Configurar comportamento do mock
        when(reembolsoService.calcularReembolso(any(Consulta.class)))
                .thenReturn(reembolsoEsperado);

        // Act & Assert - Executar requisição e verificar resposta
        mockMvc.perform(post("/api/reembolso/calcular")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(consulta)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valorConsulta").value(1500.00))
                .andExpect(jsonPath("$.percentualCobertura").value(0.70))
                .andExpect(jsonPath("$.valorReembolso").value(1050.00))
                .andExpect(jsonPath("$.status").value("sucesso"));
    }

    @Test
    @DisplayName("Deve calcular reembolso exatamente no limite - R$ 2.000")
    void deveCalcularReembolsoExatamenteNoLimite() throws Exception {
        // Arrange - Consulta no limite (exatamente R$ 2.000)
        Consulta consulta = new Consulta(new BigDecimal("2000.00"), new BigDecimal("0.80"));
        BigDecimal reembolsoEsperado = new BigDecimal("1600.00");

        // Configurar Mock
        when(reembolsoService.calcularReembolso(any(Consulta.class)))
                .thenReturn(reembolsoEsperado);

        // Act & Assert - Executar requisição e verificar resposta
        mockMvc.perform(post("/api/reembolso/calcular")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(consulta)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valorConsulta").value(2000.00))
                .andExpect(jsonPath("$.percentualCobertura").value(0.80))
                .andExpect(jsonPath("$.valorReembolso").value(1600.00))
                .andExpect(jsonPath("$.status").value("sucesso"));
    }
}
