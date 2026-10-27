package br.com.schedule.application.usecase.agendamento;

import br.com.schedule.application.exception.RecursoNaoEncontradoException;
import br.com.schedule.application.response.AgendamentoRondaResponse;
import br.com.schedule.domain.exception.TransicaoDeStatusInvalidaException;
import br.com.schedule.domain.model.AgendamentoRonda;
import br.com.schedule.domain.model.StatusAgendamento;
import br.com.schedule.domain.repository.AgendamentoRondaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static br.com.schedule.application.ApplicationTestFixtures.agendamento;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConcluirAgendamentoUseCaseTest {

    @Mock
    private AgendamentoRondaRepository agendamentoRepository;

    private ConcluirAgendamentoUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ConcluirAgendamentoUseCase(agendamentoRepository);
    }

    @Test
    void shouldCompleteAgendamento() {
        AgendamentoRonda agendamento = agendamento("agendamento-1", StatusAgendamento.EM_ANDAMENTO);
        when(agendamentoRepository.buscarPorId("agendamento-1")).thenReturn(Optional.of(agendamento));
        when(agendamentoRepository.salvar(any(AgendamentoRonda.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AgendamentoRondaResponse response = useCase.executar("agendamento-1");

        assertThat(response.status()).isEqualTo(StatusAgendamento.CONCLUIDA);
        verify(agendamentoRepository).salvar(agendamento);
    }

    @Test
    void shouldThrowWhenAgendamentoNotFound() {
        when(agendamentoRepository.buscarPorId("inexistente")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.executar("inexistente"))
                .isExactlyInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessageContaining("Agendamento")
                .hasMessageContaining("inexistente");

        verify(agendamentoRepository, never()).salvar(any());
    }

    @Test
    void shouldNotCompleteWhenStatusIsNotEmAndamento() {
        AgendamentoRonda agendamento = agendamento("agendamento-1", StatusAgendamento.AGENDADA);
        when(agendamentoRepository.buscarPorId("agendamento-1")).thenReturn(Optional.of(agendamento));

        assertThatThrownBy(() -> useCase.executar("agendamento-1"))
                .isExactlyInstanceOf(TransicaoDeStatusInvalidaException.class);

        verify(agendamentoRepository, never()).salvar(any());
    }
}
