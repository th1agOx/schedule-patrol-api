package br.com.schedule.application.usecase.agendamento;

import br.com.schedule.application.exception.RecursoNaoEncontradoException;
import br.com.schedule.application.response.AgendamentoRondaResponse;
import br.com.schedule.application.response.PeriodoResponse;
import br.com.schedule.domain.model.AgendamentoRonda;
import br.com.schedule.domain.model.StatusAgendamento;
import br.com.schedule.domain.repository.AgendamentoRondaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static br.com.schedule.application.ApplicationTestFixtures.FIM;
import static br.com.schedule.application.ApplicationTestFixtures.INICIO;
import static br.com.schedule.application.ApplicationTestFixtures.agendamento;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BuscarAgendamentoPorIdUseCaseTest {

    @Mock
    private AgendamentoRondaRepository agendamentoRepository;

    private BuscarAgendamentoPorIdUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new BuscarAgendamentoPorIdUseCase(agendamentoRepository);
    }

    @Test
    void shouldFindAgendamentoById() {
        AgendamentoRonda agendamento = agendamento("agendamento-1", StatusAgendamento.AGENDADA);
        when(agendamentoRepository.buscarPorId("agendamento-1")).thenReturn(Optional.of(agendamento));

        AgendamentoRondaResponse response = useCase.executar("agendamento-1");

        assertThat(response.id()).isEqualTo("agendamento-1");
        assertThat(response.idRonda()).isEqualTo("ronda-1");
        assertThat(response.responsavel()).isEqualTo("Maria");
        assertThat(response.periodoPrevisto()).isEqualTo(new PeriodoResponse(INICIO, FIM));
        assertThat(response.status()).isEqualTo(StatusAgendamento.AGENDADA);
        assertThat(response.motivoCancelamento()).isNull();
    }

    @Test
    void shouldThrowWhenAgendamentoNotFound() {
        when(agendamentoRepository.buscarPorId("inexistente")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.executar("inexistente"))
                .isExactlyInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessageContaining("Agendamento")
                .hasMessageContaining("inexistente");
    }
}
