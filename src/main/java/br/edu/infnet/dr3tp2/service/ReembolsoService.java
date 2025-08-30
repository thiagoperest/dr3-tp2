package br.edu.infnet.dr3tp2.service;

import br.edu.infnet.dr3tp2.dto.HistoricoResponse;
import br.edu.infnet.dr3tp2.model.Consulta;
import br.edu.infnet.dr3tp2.model.Paciente;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * Classe Service responsável por calcular reembolsos de consultas médicas
 */
@Service
public class ReembolsoService {

    @Autowired
    private CalculadoraReembolso calculadoraReembolso;

    @Autowired
    private HistoricoConsultas historicoConsultas;

    @Autowired(required = false)
    Auditoria auditoria;

    // EX8
    @Autowired(required = false)
    AutorizadorReembolso autorizadorReembolso;

    /**
     * Calcula o valor de reembolso de uma consulta médica
     *
     * @param consulta Consulta com valor e percentual de cobertura
     * @return Valor do reembolso calculado
     * @throws IllegalArgumentException para dados inválidos
     * @throws SecurityException para consultas não autorizadas - EX8
     */
    public BigDecimal calcularReembolso(Consulta consulta) {
        Paciente pacienteDummy = new Paciente("Dummy", "000.000.000-00");

        // EX8 - Verificar autorização antes do cálculo
        if (autorizadorReembolso != null) {
            if (!autorizadorReembolso.isAutorizado(consulta, pacienteDummy)) {
                String motivo = autorizadorReembolso.getMotivoNegacao();
                throw new SecurityException("Consulta não autorizada para reembolso" +
                        (motivo != null ? ": " + motivo : ""));
            }
        }

        // EX7 - Registra auditoria
        if (auditoria != null) {
            auditoria.registrarConsulta(consulta);
        }

        // Calcular reembolso
        BigDecimal valorReembolso = calculadoraReembolso.calcular(consulta, pacienteDummy);

        // Salvar no fake histórico
        if (historicoConsultas instanceof HistoricoConsultasFake) {
            ((HistoricoConsultasFake) historicoConsultas).salvarComReembolso(consulta, pacienteDummy, valorReembolso);
        } else {
            historicoConsultas.salvar(consulta, pacienteDummy);
        }

        return valorReembolso;
    }

    /**
     * Calcula o valor de reembolso usando plano de saúde
     *
     * @param consulta Consulta com valor
     * @param planoSaude Plano que define percentual de cobertura
     * @return Valor do reembolso calculado
     * @throws IllegalArgumentException para dados inválidos
     * @throws SecurityException para consultas não autorizadas - EX8
     */
    public BigDecimal calcularReembolsoComPlano(Consulta consulta, PlanoSaude planoSaude) {
        Paciente pacienteDummy = new Paciente("Dummy", "000.000.000-00");

        // EX8 - Verificar autorização antes do cálculo
        if (autorizadorReembolso != null) {
            if (!autorizadorReembolso.isAutorizado(consulta, pacienteDummy)) {
                String motivo = autorizadorReembolso.getMotivoNegacao();
                throw new SecurityException("Consulta não autorizada para reembolso" +
                        (motivo != null ? ": " + motivo : ""));
            }
        }

        // EX7 - Registra auditoria
        if (auditoria != null) {
            auditoria.registrarConsulta(consulta);
        }

        return calculadoraReembolso.calcularComPlano(consulta, planoSaude);
    }

    /**
     * Busca histórico com dados do paciente
     *
     * @return Lista de histórico com paciente
     */
    public List<HistoricoResponse> buscarHistorico() {
        return historicoConsultas.buscarHistorico();
    }

    /**
     * Busca histórico de um paciente específico pelo número do CPF
     *
     * @param cpf CPF do paciente
     * @return Lista de histórico do paciente
     */
    public List<HistoricoResponse> buscarHistoricoPorPaciente(String cpf) {
        return historicoConsultas.buscarHistoricoPorPaciente(cpf);
    }
}
