package br.com.schedule.application.service;

import br.com.schedule.application.dto.PeriodoDTO;
import br.com.schedule.application.dto.request.AgendamentoRequestDTO;
import br.com.schedule.application.dto.request.CancelamentoRequestDTO;
import br.com.schedule.application.dto.request.ReagendamentoRequestDTO;
import br.com.schedule.application.dto.response.AgendamentoResponseDTO;
import br.com.schedule.application.exception.RecursoNaoEncontradoException;
import br.com.schedule.domain.exception.ConflitoDeAgendaException;
import br.com.schedule.domain.exception.InicioNoPassadoException;
import br.com.schedule.domain.exception.RondaInativaException;
import br.com.schedule.domain.exception.TransicaoDeStatusInvalidaException;
import br.com.schedule.domain.model.AgendamentoRonda;
import br.com.schedule.domain.model.Periodo;
import br.com.schedule.domain.repository.AgendamentoRondaRepository;
import br.com.schedule.domain.repository.RondaRepository;
import br.com.schedule.domain.service.VerificadorDeConflitoDeAgenda;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static br.com.schedule.application.ApplicationTestFixtures.AGORA;
import static br.com.schedule.application.ApplicationTestFixtures.FIM;
import static br.com.schedule.application.ApplicationTestFixtures.INICIO;
import static br.com.schedule.application.ApplicationTestFixtures.PERIODO;
import static br.com.schedule.application.ApplicationTestFixtures.agendamento;
import static br.com.schedule.application.ApplicationTestFixtures.ronda;
import static br.com.schedule.domain.model.StatusAgendamento.AGENDADA;
import static br.com.schedule.domain.model.StatusAgendamento.CANCELADA;
import static br.com.schedule.domain.model.StatusAgendamento.EM_ANDAMENTO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgendamentoServiceTest {

    private static final Clock CLOCK = Clock.fixed(AGORA, ZoneOffset.UTC);

    @Mock
    private RondaRepository rondaRepository;

    @Mock
    private AgendamentoRondaRepository agendamentoRepository;

    @Mock
    private VerificadorDeConflitoDeAgenda verificadorDeConflito;

    private AgendamentoService service;

    @BeforeEach
    void setUp() {
        service = new AgendamentoService(rondaRepository, agendamentoRepository, verificadorDeConflito, CLOCK);
    }

    @Test
    void shouldListWithFilters() {
        when(agendamentoRepository.listar(EM_ANDAMENTO, "Maria", "ronda-1"))
                .thenReturn(List.of(agendamento("agendamento-1", EM_ANDAMENTO)));

        List<AgendamentoResponseDTO> responses = service.listar(EM_ANDAMENTO, "Maria", "ronda-1");

        assertThat(responses).extracting(AgendamentoResponseDTO::id).containsExactly("agendamento-1");
        assertThat(responses).extracting(AgendamentoResponseDTO::status).containsExactly("EM_ANDAMENTO");
        verify(agendamentoRepository).listar(EM_ANDAMENTO, "Maria", "ronda-1");
    }

    @Test
    void shouldListWithoutFilters() {
        when(agendamentoRepository.listar(null, null, null))
                .thenReturn(List.of(agendamento("agendamento-1", AGENDADA),
                        agendamento("agendamento-2", CANCELADA)));

        List<AgendamentoResponseDTO> responses = service.listar(null, null, null);

        assertThat(responses).extracting(AgendamentoResponseDTO::id)
                .containsExactly("agendamento-1", "agendamento-2");
        verify(agendamentoRepository).listar(null, null, null);
    }

    @Test
    void shouldFindById() {
        when(agendamentoRepository.buscarPorId("agendamento-1"))
                .thenReturn(Optional.of(agendamento("agendamento-1", AGENDADA)));

        AgendamentoResponseDTO response = service.buscarPorId("agendamento-1");

        assertThat(response.id()).isEqualTo("agendamento-1");
        assertThat(response.idRonda()).isEqualTo("ronda-1");
        assertThat(response.responsavel()).isEqualTo("Maria");
        assertThat(response.periodoPrevisto()).isEqualTo(new PeriodoDTO(INICIO, FIM));
        assertThat(response.status()).isEqualTo("AGENDADA");
        assertThat(response.motivoCancelamento()).isNull();
    }

    @Test
    void shouldThrowWhenAgendamentoNotFound() {
        when(agendamentoRepository.buscarPorId("inexistente")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorId("inexistente"))
                .isExactlyInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessageContaining("Agendamento")
                .hasMessageContaining("inexistente");
    }

    @Test
    void shouldScheduleRonda() {
        when(rondaRepository.buscarPorId("ronda-1")).thenReturn(Optional.of(ronda("ronda-1")));
        when(agendamentoRepository.salvar(any(AgendamentoRonda.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        AgendamentoRequestDTO request = new AgendamentoRequestDTO("ronda-1", " Maria ",
                new PeriodoDTO(INICIO, FIM));

        AgendamentoResponseDTO response = service.agendar(request);

        assertThat(response.id()).isNotBlank();
        assertThat(response.idRonda()).isEqualTo("ronda-1");
        assertThat(response.responsavel()).isEqualTo("Maria");
        assertThat(response.periodoPrevisto()).isEqualTo(new PeriodoDTO(INICIO, FIM));
        assertThat(response.status()).isEqualTo("AGENDADA");
        assertThat(response.motivoCancelamento()).isNull();

        verify(verificadorDeConflito).verificar("Maria", PERIODO);

        ArgumentCaptor<AgendamentoRonda> captor = ArgumentCaptor.forClass(AgendamentoRonda.class);
        verify(agendamentoRepository).salvar(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(AGENDADA);
        assertThat(captor.getValue().getId()).isEqualTo(response.id());
    }

    @Test
    void shouldThrowWhenRondaNotFoundOnSchedule() {
        when(rondaRepository.buscarPorId("inexistente")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.agendar(new AgendamentoRequestDTO("inexistente", "Maria",
                new PeriodoDTO(INICIO, FIM))))
                .isExactlyInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessageContaining("Ronda");

        verifyNoInteractions(verificadorDeConflito);
        verify(agendamentoRepository, never()).salvar(any());
    }

    @Test
    void shouldPropagateConflictOnSchedule() {
        when(rondaRepository.buscarPorId("ronda-1")).thenReturn(Optional.of(ronda("ronda-1")));
        doThrow(new ConflitoDeAgendaException("Maria", PERIODO))
                .when(verificadorDeConflito).verificar("Maria", PERIODO);

        assertThatThrownBy(() -> service.agendar(new AgendamentoRequestDTO("ronda-1", "Maria",
                new PeriodoDTO(INICIO, FIM))))
                .isExactlyInstanceOf(ConflitoDeAgendaException.class);

        verify(agendamentoRepository, never()).salvar(any());
    }

    @Test
    void shouldNotScheduleInactiveRonda() {
        when(rondaRepository.buscarPorId("ronda-1")).thenReturn(Optional.of(ronda("ronda-1", false)));

        assertThatThrownBy(() -> service.agendar(new AgendamentoRequestDTO("ronda-1", "Maria",
                new PeriodoDTO(INICIO, FIM))))
                .isExactlyInstanceOf(RondaInativaException.class);

        verifyNoInteractions(verificadorDeConflito);
        verify(agendamentoRepository, never()).salvar(any());
    }

    @Test
    void shouldNotScheduleInThePast() {
        when(rondaRepository.buscarPorId("ronda-1")).thenReturn(Optional.of(ronda("ronda-1")));
        PeriodoDTO passado = new PeriodoDTO(AGORA.minusSeconds(3600), FIM);

        assertThatThrownBy(() -> service.agendar(new AgendamentoRequestDTO("ronda-1", "Maria", passado)))
                .isExactlyInstanceOf(InicioNoPassadoException.class);

        verifyNoInteractions(verificadorDeConflito);
        verify(agendamentoRepository, never()).salvar(any());
    }

    @Test
    void shouldStartAgendamento() {
        when(agendamentoRepository.buscarPorId("agendamento-1"))
                .thenReturn(Optional.of(agendamento("agendamento-1", AGENDADA)));
        when(agendamentoRepository.salvar(any(AgendamentoRonda.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(service.iniciar("agendamento-1").status()).isEqualTo("EM_ANDAMENTO");
    }

    @Test
    void shouldConcludeAgendamento() {
        when(agendamentoRepository.buscarPorId("agendamento-1"))
                .thenReturn(Optional.of(agendamento("agendamento-1", EM_ANDAMENTO)));
        when(agendamentoRepository.salvar(any(AgendamentoRonda.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(service.concluir("agendamento-1").status()).isEqualTo("CONCLUIDA");
    }

    @Test
    void shouldCancelAgendamento() {
        when(agendamentoRepository.buscarPorId("agendamento-1"))
                .thenReturn(Optional.of(agendamento("agendamento-1", AGENDADA)));
        when(agendamentoRepository.salvar(any(AgendamentoRonda.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AgendamentoResponseDTO response = service.cancelar("agendamento-1",
                new CancelamentoRequestDTO("Sem equipe"));

        assertThat(response.status()).isEqualTo("CANCELADA");
        assertThat(response.motivoCancelamento()).isEqualTo("Sem equipe");
    }

    @Test
    void shouldRejectInvalidTransition() {
        when(agendamentoRepository.buscarPorId("agendamento-1"))
                .thenReturn(Optional.of(agendamento("agendamento-1", AGENDADA)));

        assertThatThrownBy(() -> service.concluir("agendamento-1"))
                .isExactlyInstanceOf(TransicaoDeStatusInvalidaException.class);

        verify(agendamentoRepository, never()).salvar(any());
    }

    @Test
    void shouldRescheduleAgendamento() {
        when(agendamentoRepository.buscarPorId("agendamento-1"))
                .thenReturn(Optional.of(agendamento("agendamento-1", AGENDADA)));
        when(agendamentoRepository.salvar(any(AgendamentoRonda.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        Instant novoInicio = INICIO.plusSeconds(86_400);
        Instant novoFim = FIM.plusSeconds(86_400);
        Periodo novoPeriodo = new Periodo(novoInicio, novoFim);

        AgendamentoResponseDTO response = service.reagendar("agendamento-1",
                new ReagendamentoRequestDTO(new PeriodoDTO(novoInicio, novoFim)));

        assertThat(response.periodoPrevisto()).isEqualTo(new PeriodoDTO(novoInicio, novoFim));
        verify(verificadorDeConflito).verificar("Maria", novoPeriodo, "agendamento-1");
    }

    @Test
    void shouldThrowWhenReschedulingInexistentAgendamento() {
        when(agendamentoRepository.buscarPorId("inexistente")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.reagendar("inexistente",
                new ReagendamentoRequestDTO(new PeriodoDTO(INICIO, FIM))))
                .isExactlyInstanceOf(RecursoNaoEncontradoException.class);

        verifyNoInteractions(verificadorDeConflito);
        verify(agendamentoRepository, never()).salvar(any());
    }
}
