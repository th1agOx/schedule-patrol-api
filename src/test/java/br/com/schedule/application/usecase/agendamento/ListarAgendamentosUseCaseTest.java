package br.com.schedule.application.usecase.agendamento;

import br.com.schedule.application.query.ListarAgendamentosQuery;
import br.com.schedule.application.response.AgendamentoRondaResponse;
import br.com.schedule.domain.model.StatusAgendamento;
import br.com.schedule.domain.repository.AgendamentoRondaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static br.com.schedule.application.ApplicationTestFixtures.agendamento;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListarAgendamentosUseCaseTest {

    @Mock
    private AgendamentoRondaRepository agendamentoRepository;

    private ListarAgendamentosUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ListarAgendamentosUseCase(agendamentoRepository);
    }

    @Test
    void shouldListWithoutFilters() {
        when(agendamentoRepository.listar(null, null, null)).thenReturn(List.of(
                agendamento("agendamento-1", StatusAgendamento.AGENDADA),
                agendamento("agendamento-2", StatusAgendamento.CANCELADA)));

        List<AgendamentoRondaResponse> responses = useCase.executar(
                new ListarAgendamentosQuery(null, null, null));

        assertThat(responses).extracting(AgendamentoRondaResponse::id)
                .containsExactly("agendamento-1", "agendamento-2");
        verify(agendamentoRepository).listar(null, null, null);
    }

    @Test
    void shouldListWithAllFilters() {
        when(agendamentoRepository.listar(StatusAgendamento.EM_ANDAMENTO, "Maria", "ronda-1"))
                .thenReturn(List.of(agendamento("agendamento-1", StatusAgendamento.EM_ANDAMENTO)));

        List<AgendamentoRondaResponse> responses = useCase.executar(
                new ListarAgendamentosQuery(StatusAgendamento.EM_ANDAMENTO, "Maria", "ronda-1"));

        assertThat(responses).extracting(AgendamentoRondaResponse::id)
                .containsExactly("agendamento-1");
        verify(agendamentoRepository).listar(StatusAgendamento.EM_ANDAMENTO, "Maria", "ronda-1");
    }
}
