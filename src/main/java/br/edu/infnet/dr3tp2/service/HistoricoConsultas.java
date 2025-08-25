package br.edu.infnet.dr3tp2.service;

import br.edu.infnet.dr3tp2.dto.HistoricoResponse;
import br.edu.infnet.dr3tp2.model.Consulta;
import br.edu.infnet.dr3tp2.model.Paciente;

import java.util.List;

/**
 * Interface para armazenamento de histórico de consultas
 */
public interface HistoricoConsultas {

    /**
     * Armazena uma consulta no histórico
     *
     * @param consulta Consulta a ser armazenada
     * @param paciente Paciente da consulta
     */
    void salvar(Consulta consulta, Paciente paciente);

    /**
     * Busca histórico completo com dados do paciente
     *
     * @return Lista de histórico com paciente
     */
    List<HistoricoResponse> buscarHistorico();

    /**
     * Busca histórico de um paciente específico com dados completos
     *
     * @param cpf CPF do paciente
     * @return Lista de histórico do paciente
     */
    List<HistoricoResponse> buscarHistoricoPorPaciente(String cpf);
}
