package br.edu.infnet.dr3tp2.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import br.edu.infnet.dr3tp2.dto.HistoricoResponse;
import br.edu.infnet.dr3tp2.dto.ReembolsoResponse;
import br.edu.infnet.dr3tp2.model.Consulta;
import br.edu.infnet.dr3tp2.model.Paciente;
import br.edu.infnet.dr3tp2.service.ReembolsoService;
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
 * Usa @WebMvcTest para testar apenas a camada web
 * e @MockitoBean para simular dependências (dublês de teste)
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
        // Arrange - Preparar dados e comportamento do mock
        Consulta consulta = new Consulta(new BigDecimal("200.00"), new BigDecimal("0.70"));
        BigDecimal reembolsoEsperado = new BigDecimal("140.00");

        // Configurar comportamento do mock
        when(reembolsoService.calcularReembolso(any(Consulta.class)))
                .thenReturn(reembolsoEsperado);

        // Act & Assert - Executar requisição e verificar resposta
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

        // Act & Assert - Executar requisição e verificar resposta
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
        // Arrange - Preparar dados do histórico de um paciente específico
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

        // Configurar comportamento do mock
        when(reembolsoService.buscarHistoricoPorPaciente(anyString()))
                .thenReturn(historicoEsperado);

        // Act & Assert - Executar requisição e verificar resposta
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
}
