package br.edu.infnet.dr3tp2.service;

import br.edu.infnet.dr3tp2.model.Consulta;

import java.util.ArrayList;
import java.util.List;

/**
 * Spy para auditoria - registra chamadas - EX7
 */
public class AuditoriaSpy implements Auditoria {

    private boolean foiChamado = false;
    private int quantidadeChamadas = 0;
    private List<Consulta> consultasRegistradas = new ArrayList<>();
    private Consulta ultimaConsultaRegistrada;

    @Override
    public void registrarConsulta(Consulta consulta) {
        // Spy observa e registra a chamada
        this.foiChamado = true;
        this.quantidadeChamadas++;
        this.ultimaConsultaRegistrada = consulta;

        if (consulta != null) {
            this.consultasRegistradas.add(consulta);
        }

        System.out.println("Auditoria: Consulta registrada - Valor: " +
                (consulta != null ? consulta.getValor() : "null"));
    }

    // Métodos de verificação para ser usados nos testes
    public boolean foiChamado() {
        return foiChamado;
    }

    public int getQuantidadeChamadas() {
        return quantidadeChamadas;
    }

    public List<Consulta> getConsultasRegistradas() {
        return new ArrayList<>(consultasRegistradas);
    }

    public Consulta getUltimaConsultaRegistrada() {
        return ultimaConsultaRegistrada;
    }

    public boolean foiChamadoCom(Consulta consulta) {
        return consultasRegistradas.contains(consulta);
    }

    // Método para resetar o spy entre os testes
    public void reset() {
        this.foiChamado = false;
        this.quantidadeChamadas = 0;
        this.consultasRegistradas.clear();
        this.ultimaConsultaRegistrada = null;
    }
}
