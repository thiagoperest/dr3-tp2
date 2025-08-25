package br.edu.infnet.dr3tp2.service;

import br.edu.infnet.dr3tp2.model.Consulta;
import br.edu.infnet.dr3tp2.model.Paciente;

/**
 * Interface para autorização de reembolsos - EX8
 */
public interface AutorizadorReembolso {

    /**
     * Verifica se uma consulta está autorizada para reembolso
     *
     * @param consulta Consulta a ser verificada
     * @param paciente Paciente da consulta
     * @return true se autorizada, false caso contrário
     */
    boolean isAutorizado(Consulta consulta, Paciente paciente);

    /**
     * Obtém motivo da negação (opcional)
     *
     * @return Motivo da última negação ou null se autorizada
     */
    String getMotivoNegacao();
}
