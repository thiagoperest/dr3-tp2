package br.edu.infnet.dr3tp2.service;

import br.edu.infnet.dr3tp2.dto.HistoricoResponse;
import br.edu.infnet.dr3tp2.dto.ReembolsoResponse;
import br.edu.infnet.dr3tp2.model.Consulta;
import br.edu.infnet.dr3tp2.model.Paciente;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementação fake do histórico de consultas finalidade para testes - EX5
 * Armazena dados em memória
 */
@Component
public class HistoricoConsultasFake implements HistoricoConsultas {

    private final List<Consulta> todasConsultas = new ArrayList<>();
    private final Map<String, List<Consulta>> consultasPorPaciente = new HashMap<>();
    private final Map<Consulta, Paciente> consultaPaciente = new HashMap<>();
    private final Map<Consulta, BigDecimal> consultaReembolso = new HashMap<>();

    /**
     * Armazena uma consulta no histórico com valor do reembolso
     *
     * @param consulta Consulta a ser armazenada
     * @param paciente Paciente da consulta
     * @param valorReembolso Valor do reembolso calculado
     */
    public void salvarComReembolso(Consulta consulta, Paciente paciente, BigDecimal valorReembolso) {
        salvar(consulta, paciente);
        consultaReembolso.put(consulta, valorReembolso);
    }

    /**
     * Armazena uma consulta no histórico
     *
     * @param consulta Consulta a ser armazenada
     * @param paciente Paciente da consulta
     */
    @Override
    public void salvar(Consulta consulta, Paciente paciente) {
        if (consulta == null || paciente == null) {
            throw new IllegalArgumentException("Consulta e paciente não podem ser nulos");
        }

        // Adiciona na lista geral
        todasConsultas.add(consulta);

        // Adiciona na lista do paciente específico
        String cpf = paciente.getCpf();
        consultasPorPaciente.computeIfAbsent(cpf, k -> new ArrayList<>()).add(consulta);

        // Mapeia consulta
        consultaPaciente.put(consulta, paciente);
    }

    /**
     * Busca histórico completo com dados do paciente
     *
     * @return Lista de histórico com paciente
     */
    @Override
    public List<HistoricoResponse> buscarHistorico() {
        List<HistoricoResponse> historico = new ArrayList<>();

        for (Consulta consulta : todasConsultas) {
            Paciente paciente = consultaPaciente.get(consulta);
            BigDecimal valorReembolso = consultaReembolso.get(consulta);

            if (paciente != null && valorReembolso != null) {
                ReembolsoResponse reembolsoResponse = new ReembolsoResponse(
                        consulta.getValor(),
                        consulta.getPercentualCobertura(),
                        valorReembolso,
                        "sucesso"
                );
                HistoricoResponse item = new HistoricoResponse(reembolsoResponse, paciente);
                historico.add(item);
            }
        }

        return historico;
    }

    /**
     * Busca histórico de um paciente específico
     *
     * @param cpf CPF do paciente
     * @return Lista de histórico do paciente
     */
    @Override
    public List<HistoricoResponse> buscarHistoricoPorPaciente(String cpf) {
        if (cpf == null) {
            return new ArrayList<>();
        }

        List<HistoricoResponse> historico = new ArrayList<>();
        List<Consulta> consultasDoPaciente = consultasPorPaciente.getOrDefault(cpf, new ArrayList<>());

        for (Consulta consulta : consultasDoPaciente) {
            Paciente paciente = consultaPaciente.get(consulta);
            BigDecimal valorReembolso = consultaReembolso.get(consulta);

            if (paciente != null && valorReembolso != null) {
                ReembolsoResponse reembolsoResponse = new ReembolsoResponse(
                        consulta.getValor(),
                        consulta.getPercentualCobertura(),
                        valorReembolso,
                        "sucesso"
                );
                HistoricoResponse item = new HistoricoResponse(reembolsoResponse, paciente);
                historico.add(item);
            }
        }

        return historico;
    }
}
