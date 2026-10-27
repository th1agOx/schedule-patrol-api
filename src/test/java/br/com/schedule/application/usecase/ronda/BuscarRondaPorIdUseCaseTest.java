package br.com.schedule.application.usecase.ronda;

import br.com.schedule.application.exception.RecursoNaoEncontradoException;
import br.com.schedule.application.response.PontoResponse;
import br.com.schedule.application.response.RondaResponse;
import br.com.schedule.domain.model.Ronda;
import br.com.schedule.domain.repository.RondaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static br.com.schedule.application.ApplicationTestFixtures.ronda;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BuscarRondaPorIdUseCaseTest {

    @Mock
    private RondaRepository rondaRepository;

    private BuscarRondaPorIdUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new BuscarRondaPorIdUseCase(rondaRepository);
    }

    @Test
    void shouldFindRondaById() {
        Ronda ronda = ronda("ronda-1");
        when(rondaRepository.buscarPorId("ronda-1")).thenReturn(Optional.of(ronda));

        RondaResponse response = useCase.executar("ronda-1");

        assertThat(response.id()).isEqualTo("ronda-1");
        assertThat(response.nome()).isEqualTo("Ronda Norte");
        assertThat(response.ativa()).isTrue();
        assertThat(response.pontos()).containsExactly(new PontoResponse("P-01", "Portão principal"));
    }

    @Test
    void shouldThrowWhenRondaNotFound() {
        when(rondaRepository.buscarPorId("inexistente")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.executar("inexistente"))
                .isExactlyInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessageContaining("Ronda")
                .hasMessageContaining("inexistente");
    }
}
