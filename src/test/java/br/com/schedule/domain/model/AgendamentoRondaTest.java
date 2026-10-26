package br.com.schedule.domain.model;

import br.com.schedule.domain.exception.AgendamentoInvalidoException;
import br.com.schedule.domain.exception.IdentificadorObrigatorioException;
import br.com.schedule.domain.exception.InicioNoPassadoException;
import br.com.schedule.domain.exception.MotivoCancelamentoObrigatorioException;
import br.com.schedule.domain.exception.ReagendamentoNaoPermitidoException;
import br.com.schedule.domain.exception.ResponsavelObrigatorioException;
import br.com.schedule.domain.exception.RondaInativaException;
import br.com.schedule.domain.exception.TransicaoDeStatusInvalidaException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static br.com.schedule.domain.model.StatusAgendamento.AGENDADA;
import static br.com.schedule.domain.model.StatusAgendamento.CANCELADA;
import static br.com.schedule.domain.model.StatusAgendamento.CONCLUIDA;
import static br.com.schedule.domain.model.StatusAgendamento.EM_ANDAMENTO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AgendamentoRondaTest {

    private static final Instant AGORA = Instant.parse("2026-10-26T12:00:00Z");
    private static final Instant INICIO_FUTURO = Instant.parse("2026-11-05T11:00:00Z");
    private static final Instant FIM_FUTURO = Instant.parse("2026-11-05T13:00:00Z");
    private static final Periodo PERIODO = new Periodo(INICIO_FUTURO, FIM_FUTURO);
    private static final Periodo OUTRO_PERIODO = new Periodo(INICIO_FUTURO.plusSeconds(86_400),
            FIM_FUTURO.plusSeconds(86_400));

    @Test
    void shouldScheduleActiveAgendamento() {
        Ronda ronda = rondaAtiva();

        AgendamentoRonda agendamento = AgendamentoRonda.agendar(ronda, "  Maria  ", PERIODO, AGORA);

        assertThat(agendamento.getId()).isNotBlank();
        assertThat(UUID.fromString(agendamento.getId())).isNotNull();
        assertThat(agendamento.getIdRonda()).isEqualTo(ronda.getId());
        assertThat(agendamento.getResponsavel()).isEqualTo("Maria");
        assertThat(agendamento.getPeriodoPrevisto()).isEqualTo(PERIODO);
        assertThat(agendamento.getStatus()).isEqualTo(AGENDADA);
        assertThat(agendamento.estaAtivo()).isTrue();
        assertThat(agendamento.getMotivoCancelamento()).isEmpty();
    }

    @Test
    void shouldRejectAgendamentoWithoutRonda() {
        assertThatThrownBy(() -> AgendamentoRonda.agendar(null, "Maria", PERIODO, AGORA))
                .isExactlyInstanceOf(AgendamentoInvalidoException.class)
                .hasMessageContaining("ronda");
    }

    @Test
    void shouldRejectAgendamentoWithoutPeriodo() {
        assertThatThrownBy(() -> AgendamentoRonda.agendar(rondaAtiva(), "Maria", null, AGORA))
                .isExactlyInstanceOf(AgendamentoInvalidoException.class)
                .hasMessageContaining("período");
    }

    @Test
    void shouldRejectAgendamentoWithoutCurrentInstant() {
        assertThatThrownBy(() -> AgendamentoRonda.agendar(rondaAtiva(), "Maria", PERIODO, null))
                .isExactlyInstanceOf(AgendamentoInvalidoException.class)
                .hasMessageContaining("instante atual");
    }

    @Test
    void shouldRejectInactiveRonda() {
        Ronda ronda = rondaAtiva();
        ronda.desativar();

        assertThatThrownBy(() -> AgendamentoRonda.agendar(ronda, "Maria", PERIODO, AGORA))
                .isExactlyInstanceOf(RondaInativaException.class)
                .hasMessageContaining(ronda.getId());
    }

    @Test
    void shouldRejectNullResponsavel() {
        assertThatThrownBy(() -> AgendamentoRonda.agendar(rondaAtiva(), null, PERIODO, AGORA))
                .isExactlyInstanceOf(ResponsavelObrigatorioException.class);
    }

    @Test
    void shouldRejectBlankResponsavel() {
        assertThatThrownBy(() -> AgendamentoRonda.agendar(rondaAtiva(), "   ", PERIODO, AGORA))
                .isExactlyInstanceOf(ResponsavelObrigatorioException.class);
    }

    @Test
    void shouldRejectStartEqualToCurrentInstant() {
        Periodo periodo = new Periodo(AGORA, AGORA.plusSeconds(3_600));

        assertThatThrownBy(() -> AgendamentoRonda.agendar(rondaAtiva(), "Maria", periodo, AGORA))
                .isExactlyInstanceOf(InicioNoPassadoException.class)
                .hasMessageContaining("posterior ao instante atual");
    }

    @Test
    void shouldRejectStartInThePast() {
        Periodo periodo = new Periodo(AGORA.minusSeconds(3_600), AGORA.plusSeconds(3_600));

        assertThatThrownBy(() -> AgendamentoRonda.agendar(rondaAtiva(), "Maria", periodo, AGORA))
                .isExactlyInstanceOf(InicioNoPassadoException.class)
                .hasMessageContaining("posterior ao instante atual");
    }

    @Test
    void shouldStartAgendamento() {
        AgendamentoRonda agendamento = novoAgendamento();

        agendamento.iniciar();

        assertThat(agendamento.getStatus()).isEqualTo(EM_ANDAMENTO);
        assertThat(agendamento.estaAtivo()).isTrue();
    }

    @Test
    void shouldNotStartTwice() {
        AgendamentoRonda agendamento = novoAgendamento();
        agendamento.iniciar();

        assertThatThrownBy(agendamento::iniciar)
                .isExactlyInstanceOf(TransicaoDeStatusInvalidaException.class)
                .hasMessage("Transição de status inválida: EM_ANDAMENTO -> EM_ANDAMENTO.");
    }

    @Test
    void shouldConcludeAgendamentoInProgress() {
        AgendamentoRonda agendamento = novoAgendamento();
        agendamento.iniciar();

        agendamento.concluir();

        assertThat(agendamento.getStatus()).isEqualTo(CONCLUIDA);
        assertThat(agendamento.estaAtivo()).isFalse();
    }

    @Test
    void shouldNotConcludeAgendamentoThatWasNotStarted() {
        AgendamentoRonda agendamento = novoAgendamento();

        assertThatThrownBy(agendamento::concluir)
                .isExactlyInstanceOf(TransicaoDeStatusInvalidaException.class)
                .hasMessage("Transição de status inválida: AGENDADA -> CONCLUIDA.");
    }

    @Test
    void shouldNotStartConcludedAgendamento() {
        AgendamentoRonda agendamento = novoAgendamento();
        agendamento.iniciar();
        agendamento.concluir();

        assertThatThrownBy(agendamento::iniciar)
                .isExactlyInstanceOf(TransicaoDeStatusInvalidaException.class)
                .hasMessage("Transição de status inválida: CONCLUIDA -> EM_ANDAMENTO.");
    }

    @Test
    void shouldCancelAgendamentoWithReason() {
        AgendamentoRonda agendamento = novoAgendamento();

        agendamento.cancelar("  Sem equipe disponível  ");

        assertThat(agendamento.getStatus()).isEqualTo(CANCELADA);
        assertThat(agendamento.getMotivoCancelamento()).contains("Sem equipe disponível");
        assertThat(agendamento.estaAtivo()).isFalse();
    }

    @Test
    void shouldRequireReasonToCancel() {
        AgendamentoRonda agendamento = novoAgendamento();

        assertThatThrownBy(() -> agendamento.cancelar(null))
                .isExactlyInstanceOf(MotivoCancelamentoObrigatorioException.class);
        assertThatThrownBy(() -> agendamento.cancelar("   "))
                .isExactlyInstanceOf(MotivoCancelamentoObrigatorioException.class);

        assertThat(agendamento.getStatus()).isEqualTo(AGENDADA);
        assertThat(agendamento.getMotivoCancelamento()).isEmpty();
    }

    @Test
    void shouldNotCancelConcludedAgendamento() {
        AgendamentoRonda agendamento = novoAgendamento();
        agendamento.iniciar();
        agendamento.concluir();

        assertThatThrownBy(() -> agendamento.cancelar("Sem equipe"))
                .isExactlyInstanceOf(TransicaoDeStatusInvalidaException.class)
                .hasMessage("Transição de status inválida: CONCLUIDA -> CANCELADA.");
    }

    @Test
    void shouldNotCancelTwice() {
        AgendamentoRonda agendamento = novoAgendamento();
        agendamento.cancelar("Sem equipe");

        assertThatThrownBy(() -> agendamento.cancelar("Outro motivo"))
                .isExactlyInstanceOf(TransicaoDeStatusInvalidaException.class)
                .hasMessage("Transição de status inválida: CANCELADA -> CANCELADA.");
    }

    @Test
    void shouldRescheduleAgendamentoThatIsStillAgendada() {
        AgendamentoRonda agendamento = novoAgendamento();

        agendamento.reagendar(OUTRO_PERIODO, AGORA);

        assertThat(agendamento.getPeriodoPrevisto()).isEqualTo(OUTRO_PERIODO);
        assertThat(agendamento.getStatus()).isEqualTo(AGENDADA);
    }

    @Test
    void shouldNotRescheduleAgendamentoOutsideAgendadaStatus() {
        AgendamentoRonda emAndamento = novoAgendamento();
        emAndamento.iniciar();

        AgendamentoRonda concluida = novoAgendamento();
        concluida.iniciar();
        concluida.concluir();

        AgendamentoRonda cancelada = novoAgendamento();
        cancelada.cancelar("Sem equipe");

        for (AgendamentoRonda agendamento : List.of(emAndamento, concluida, cancelada)) {
            assertThatThrownBy(() -> agendamento.reagendar(OUTRO_PERIODO, AGORA))
                    .isExactlyInstanceOf(ReagendamentoNaoPermitidoException.class)
                    .hasMessageContaining(agendamento.getStatus().name());
        }
    }

    @Test
    void shouldRejectRescheduleToThePast() {
        AgendamentoRonda agendamento = novoAgendamento();
        Periodo periodoNoPassado = new Periodo(AGORA.minusSeconds(3_600), AGORA.plusSeconds(3_600));

        assertThatThrownBy(() -> agendamento.reagendar(periodoNoPassado, AGORA))
                .isExactlyInstanceOf(InicioNoPassadoException.class)
                .hasMessageContaining("posterior ao instante atual");
    }

    @Test
    void shouldRejectRescheduleWithoutNewPeriod() {
        AgendamentoRonda agendamento = novoAgendamento();

        assertThatThrownBy(() -> agendamento.reagendar(null, AGORA))
                .isExactlyInstanceOf(AgendamentoInvalidoException.class)
                .hasMessageContaining("período");
    }

    @Test
    void shouldRejectRescheduleWithoutCurrentInstant() {
        AgendamentoRonda agendamento = novoAgendamento();

        assertThatThrownBy(() -> agendamento.reagendar(OUTRO_PERIODO, null))
                .isExactlyInstanceOf(AgendamentoInvalidoException.class)
                .hasMessageContaining("instante atual");
    }

    @Test
    void shouldReconstituteCancelledAgendamento() {
        AgendamentoRonda agendamento = AgendamentoRonda.reconstituir("id-1", "ronda-1", "Maria", PERIODO,
                CANCELADA, "  Sem equipe  ");

        assertThat(agendamento.getId()).isEqualTo("id-1");
        assertThat(agendamento.getIdRonda()).isEqualTo("ronda-1");
        assertThat(agendamento.getResponsavel()).isEqualTo("Maria");
        assertThat(agendamento.getPeriodoPrevisto()).isEqualTo(PERIODO);
        assertThat(agendamento.getStatus()).isEqualTo(CANCELADA);
        assertThat(agendamento.getMotivoCancelamento()).contains("Sem equipe");
    }

    @Test
    void shouldRejectCancelledAgendamentoWithoutReasonOnReconstitution() {
        assertThatThrownBy(() -> AgendamentoRonda.reconstituir("id-1", "ronda-1", "Maria", PERIODO,
                CANCELADA, "   "))
                .isExactlyInstanceOf(MotivoCancelamentoObrigatorioException.class);
    }

    @Test
    void shouldRejectReasonOnNonCancelledAgendamentoOnReconstitution() {
        assertThatThrownBy(() -> AgendamentoRonda.reconstituir("id-1", "ronda-1", "Maria", PERIODO,
                AGENDADA, "Sem equipe"))
                .isExactlyInstanceOf(AgendamentoInvalidoException.class)
                .hasMessageContaining("só pode existir");
    }

    @Test
    void shouldValidateRequiredFieldsOnReconstitution() {
        assertThatThrownBy(() -> AgendamentoRonda.reconstituir("  ", "ronda-1", "Maria", PERIODO,
                AGENDADA, null))
                .isExactlyInstanceOf(IdentificadorObrigatorioException.class)
                .hasMessageContaining("agendamento");

        assertThatThrownBy(() -> AgendamentoRonda.reconstituir("id-1", " ", "Maria", PERIODO,
                AGENDADA, null))
                .isExactlyInstanceOf(IdentificadorObrigatorioException.class)
                .hasMessageContaining("ronda");

        assertThatThrownBy(() -> AgendamentoRonda.reconstituir("id-1", "ronda-1", " ", PERIODO,
                AGENDADA, null))
                .isExactlyInstanceOf(ResponsavelObrigatorioException.class);

        assertThatThrownBy(() -> AgendamentoRonda.reconstituir("id-1", "ronda-1", "Maria", null,
                AGENDADA, null))
                .isExactlyInstanceOf(AgendamentoInvalidoException.class)
                .hasMessageContaining("período");

        assertThatThrownBy(() -> AgendamentoRonda.reconstituir("id-1", "ronda-1", "Maria", PERIODO,
                null, null))
                .isExactlyInstanceOf(AgendamentoInvalidoException.class)
                .hasMessageContaining("status");
    }

    @Test
    void shouldBeEqualById() {
        AgendamentoRonda agendamento = AgendamentoRonda.reconstituir("id-1", "ronda-1", "Maria", PERIODO,
                AGENDADA, null);
        AgendamentoRonda mesmoAgendamento = AgendamentoRonda.reconstituir("id-1", "outra-ronda", "João", OUTRO_PERIODO,
                CANCELADA, "Sem equipe");
        AgendamentoRonda outroAgendamento = AgendamentoRonda.reconstituir("id-2", "ronda-1", "Maria", PERIODO,
                AGENDADA, null);

        assertThat(agendamento).isEqualTo(mesmoAgendamento);
        assertThat(agendamento).hasSameHashCodeAs(mesmoAgendamento);
        assertThat(agendamento).isNotEqualTo(outroAgendamento);
    }

    private static Ronda rondaAtiva() {
        return Ronda.criar("Ronda Norte", "Inspeção diária", List.of(new Ponto("P-01", "Portão principal")));
    }

    private static AgendamentoRonda novoAgendamento() {
        return AgendamentoRonda.agendar(rondaAtiva(), "Maria", PERIODO, AGORA);
    }
}
