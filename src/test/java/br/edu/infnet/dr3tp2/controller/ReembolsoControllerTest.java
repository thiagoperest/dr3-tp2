package br.edu.infnet.dr3tp2.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import br.edu.infnet.dr3tp2.model.Consulta;
import br.edu.infnet.dr3tp2.service.ReembolsoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
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
}
