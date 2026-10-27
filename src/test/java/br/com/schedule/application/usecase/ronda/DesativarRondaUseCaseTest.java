package br.com.schedule.application.usecase.ronda;

import br.com.schedule.application.exception.RecursoNaoEncontradoException;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DesativarRondaUseCaseTest {

    @Mock
    private RondaRepository rondaRepository;

    private DesativarRondaUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new DesativarRondaUseCase(rondaRepository);
    }

    @Test
    void shouldDeactivateRonda() {
        Ronda ronda = ronda("ronda-1");
        when(rondaRepository.buscarPorId("ronda-1")).thenReturn(Optional.of(ronda));
        when(rondaRepository.salvar(any(Ronda.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RondaResponse response = useCase.executar("ronda-1");

        assertThat(response.ativa()).isFalse();
        assertThat(ronda.isAtiva()).isFalse();
        verify(rondaRepository).salvar(ronda);
    }

    @Test
    void shouldThrowWhenRondaNotFound() {
        when(rondaRepository.buscarPorId("inexistente")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.executar("inexistente"))
                .isExactlyInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessageContaining("inexistente");

        verify(rondaRepository, never()).salvar(any());
    }
}
