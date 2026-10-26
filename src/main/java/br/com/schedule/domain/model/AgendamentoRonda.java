package br.com.schedule.domain.model;

import br.com.schedule.domain.exception.AgendamentoInvalidoException;
import br.com.schedule.domain.exception.IdentificadorObrigatorioException;
import br.com.schedule.domain.exception.InicioNoPassadoException;
import br.com.schedule.domain.exception.MotivoCancelamentoObrigatorioException;
import br.com.schedule.domain.exception.ReagendamentoNaoPermitidoException;
import br.com.schedule.domain.exception.ResponsavelObrigatorioException;
import br.com.schedule.domain.exception.RondaInativaException;
import br.com.schedule.domain.exception.TransicaoDeStatusInvalidaException;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

 
public final class AgendamentoRonda {

    private final String id;
    private final String idRonda;
    private final String responsavel;
    private Periodo periodoPrevisto;
    private StatusAgendamento status;
    private String motivoCancelamento;

    private AgendamentoRonda(String id, String idRonda, String responsavel, Periodo periodoPrevisto,
            StatusAgendamento status, String motivoCancelamento) {
        this.id = requireId(id);
        this.idRonda = requireRondaId(idRonda);
        this.responsavel = requireResponsavel(responsavel);
        this.periodoPrevisto = requirePeriodo(periodoPrevisto);
        this.status = requireStatus(status);
        this.motivoCancelamento = resolveMotivoCancelamento(status, motivoCancelamento);
    }

    public static AgendamentoRonda agendar(Ronda ronda, String responsavel, Periodo periodo, Instant agora) {
        if (ronda == null) {
            throw new AgendamentoInvalidoException("A ronda é obrigatória para o agendamento.");
        }
        if (periodo == null) {
            throw new AgendamentoInvalidoException("O período é obrigatório para o agendamento.");
        }
        if (agora == null) {
            throw new AgendamentoInvalidoException("O instante atual é obrigatório para o agendamento.");
        }
        if (!ronda.podeSerAgendada()) {
            throw new RondaInativaException(ronda.getId());
        }

        String responsavelNormalizado = requireResponsavel(responsavel);
        requireInicioFuturo(periodo, agora);

        return new AgendamentoRonda(UUID.randomUUID().toString(), ronda.getId(), responsavelNormalizado, periodo,
                StatusAgendamento.AGENDADA, null);
    }


    public static AgendamentoRonda reconstituir(String id, String idRonda, String responsavel, Periodo periodoPrevisto,
            StatusAgendamento status, String motivoCancelamento) {
        return new AgendamentoRonda(id, idRonda, responsavel, periodoPrevisto, status, motivoCancelamento);
    }

    public void iniciar() {
        transicionarPara(StatusAgendamento.EM_ANDAMENTO);
    }

    public void concluir() {
        transicionarPara(StatusAgendamento.CONCLUIDA);
    }

    public void cancelar(String motivo) {
        validarTransicao(StatusAgendamento.CANCELADA);
        String motivoNormalizado = requireMotivo(motivo);

        this.status = StatusAgendamento.CANCELADA;
        this.motivoCancelamento = motivoNormalizado;
    }

    public void reagendar(Periodo novoPeriodo, Instant agora) {
        if (novoPeriodo == null) {
            throw new AgendamentoInvalidoException("O novo período é obrigatório para o reagendamento.");
        }
        if (agora == null) {
            throw new AgendamentoInvalidoException("O instante atual é obrigatório para o reagendamento.");
        }
        if (status != StatusAgendamento.AGENDADA) {
            throw new ReagendamentoNaoPermitidoException(status);
        }

        requireInicioFuturo(novoPeriodo, agora);
        this.periodoPrevisto = novoPeriodo;
    }

    public boolean estaAtivo() {
        return status.estaAtivo();
    }

    public String getId() {
        return id;
    }

    public String getIdRonda() {
        return idRonda;
    }

    public String getResponsavel() {
        return responsavel;
    }

    public Periodo getPeriodoPrevisto() {
        return periodoPrevisto;
    }

    public StatusAgendamento getStatus() {
        return status;
    }

    public Optional<String> getMotivoCancelamento() {
        return Optional.ofNullable(motivoCancelamento);
    }

    private void transicionarPara(StatusAgendamento destino) {
        validarTransicao(destino);
        this.status = destino;
    }

    private void validarTransicao(StatusAgendamento destino) {
        if (!status.podeTransicionarPara(destino)) {
            throw new TransicaoDeStatusInvalidaException(status, destino);
        }
    }

    private static void requireInicioFuturo(Periodo periodo, Instant agora) {
        if (!periodo.inicio().isAfter(agora)) {
            throw new InicioNoPassadoException(periodo.inicio(), agora);
        }
    }

    private static String requireId(String id) {
        if (id == null || id.isBlank()) {
            throw new IdentificadorObrigatorioException("O identificador do agendamento é obrigatório.");
        }
        return id;
    }

    private static String requireRondaId(String idRonda) {
        if (idRonda == null || idRonda.isBlank()) {
            throw new IdentificadorObrigatorioException("O identificador da ronda é obrigatório.");
        }
        return idRonda;
    }

    private static String requireResponsavel(String responsavel) {
        if (responsavel == null || responsavel.isBlank()) {
            throw new ResponsavelObrigatorioException();
        }
        return responsavel.trim();
    }

    private static Periodo requirePeriodo(Periodo periodo) {
        if (periodo == null) {
            throw new AgendamentoInvalidoException("O período do agendamento é obrigatório.");
        }
        return periodo;
    }

    private static StatusAgendamento requireStatus(StatusAgendamento status) {
        if (status == null) {
            throw new AgendamentoInvalidoException("O status do agendamento é obrigatório.");
        }
        return status;
    }

    private static String requireMotivo(String motivo) {
        if (motivo == null || motivo.isBlank()) {
            throw new MotivoCancelamentoObrigatorioException();
        }
        return motivo.trim();
    }

    private static String resolveMotivoCancelamento(StatusAgendamento status, String motivo) {
        if (status == StatusAgendamento.CANCELADA) {
            return requireMotivo(motivo);
        }
        if (motivo != null && !motivo.isBlank()) {
            throw new AgendamentoInvalidoException(
                    "O motivo de cancelamento só pode existir em agendamentos cancelados.");
        }
        return null;
    }

    @Override
    public boolean equals(Object outro) {
        if (this == outro) {
            return true;
        }
        if (!(outro instanceof AgendamentoRonda agendamento)) {
            return false;
        }
        return id.equals(agendamento.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
