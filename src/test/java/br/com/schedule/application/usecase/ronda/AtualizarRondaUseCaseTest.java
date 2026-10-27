package br.com.schedule.application.usecase.ronda;

import br.com.schedule.application.command.AtualizarRondaCommand;
import br.com.schedule.application.command.PontoCommand;
import br.com.schedule.application.exception.RecursoNaoEncontradoException;
import br.com.schedule.application.response.PontoResponse;
import br.com.schedule.application.response.RondaResponse;
import br.com.schedule.domain.exception.NomeObrigatorioException;
import br.com.schedule.domain.model.Ronda;
import br.com.schedule.domain.repository.RondaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static br.com.schedule.application.ApplicationTestFixtures.ronda;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AtualizarRondaUseCaseTest {

    @Mock
    private RondaRepository rondaRepository;

    private AtualizarRondaUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new AtualizarRondaUseCase(rondaRepository);
    }

    @Test
    void shouldUpdateRonda() {
        Ronda ronda = ronda("ronda-1");
        when(rondaRepository.buscarPorId("ronda-1")).thenReturn(Optional.of(ronda));
        when(rondaRepository.salvar(any(Ronda.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AtualizarRondaCommand command = new AtualizarRondaCommand("Ronda Sul", "Nova descrição",
                List.of(new PontoCommand("P-02", "Casa de válvulas")));

        RondaResponse response = useCase.executar("ronda-1", command);

        assertThat(response.nome()).isEqualTo("Ronda Sul");
        assertThat(response.descricao()).isEqualTo("Nova descrição");
        assertThat(response.pontos()).containsExactly(new PontoResponse("P-02", "Casa de válvulas"));
        verify(rondaRepository).salvar(ronda);
    }

    @Test
    void shouldThrowWhenRondaNotFound() {
        when(rondaRepository.buscarPorId("inexistente")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.executar("inexistente", command()))
                .isExactlyInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessageContaining("Ronda")
                .hasMessageContaining("inexistente");

        verify(rondaRepository, never()).salvar(any());
    }

    @Test
    void shouldPropagateDomainValidation() {
        Ronda ronda = ronda("ronda-1");
        when(rondaRepository.buscarPorId("ronda-1")).thenReturn(Optional.of(ronda));

        AtualizarRondaCommand command = new AtualizarRondaCommand("   ", null,
                List.of(new PontoCommand("P-01", "Portão principal")));

        assertThatThrownBy(() -> useCase.executar("ronda-1", command))
                .isExactlyInstanceOf(NomeObrigatorioException.class);

        verify(rondaRepository, never()).salvar(any());
    }

    private static AtualizarRondaCommand command() {
        return new AtualizarRondaCommand("Ronda Sul", "Nova descrição",
                List.of(new PontoCommand("P-02", "Casa de válvulas")));
    }
}
