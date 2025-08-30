package br.edu.infnet.dr3tp2.service;

import br.edu.infnet.dr3tp2.model.Consulta;

/**
 * Interface para serviço de auditoria - EX7
 */
public interface Auditoria {

    /**
     * Registra uma consulta no sistema de auditoria
     *
     * @param consulta Consulta a ser auditada
     */
    void registrarConsulta(Consulta consulta);
}
