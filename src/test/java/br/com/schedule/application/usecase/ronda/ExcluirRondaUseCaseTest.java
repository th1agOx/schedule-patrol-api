package br.com.schedule.application.usecase.ronda;

import br.com.schedule.application.exception.RecursoNaoEncontradoException;
import br.com.schedule.domain.exception.RondaComAgendamentosAtivosException;
import br.com.schedule.domain.model.Ronda;
import br.com.schedule.domain.repository.RondaRepository;
import br.com.schedule.domain.service.PoliticaDeExclusaoDeRonda;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static br.com.schedule.application.ApplicationTestFixtures.ronda;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExcluirRondaUseCaseTest {

    @Mock
    private RondaRepository rondaRepository;

    @Mock
    private PoliticaDeExclusaoDeRonda politicaDeExclusao;

    private ExcluirRondaUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ExcluirRondaUseCase(rondaRepository, politicaDeExclusao);
    }

    @Test
    void shouldDeleteRondaAfterPolicyCheck() {
        Ronda ronda = ronda("ronda-1");
        when(rondaRepository.buscarPorId("ronda-1")).thenReturn(Optional.of(ronda));

        useCase.executar("ronda-1");

        InOrder inOrder = inOrder(politicaDeExclusao, rondaRepository);
        inOrder.verify(politicaDeExclusao).verificarExclusao("ronda-1");
        inOrder.verify(rondaRepository).excluir("ronda-1");
    }

    @Test
    void shouldNotDeleteWhenPolicyRejects() {
        Ronda ronda = ronda("ronda-1");
        when(rondaRepository.buscarPorId("ronda-1")).thenReturn(Optional.of(ronda));
        doThrow(new RondaComAgendamentosAtivosException("ronda-1"))
                .when(politicaDeExclusao).verificarExclusao("ronda-1");

        assertThatThrownBy(() -> useCase.executar("ronda-1"))
                .isExactlyInstanceOf(RondaComAgendamentosAtivosException.class);

        verify(rondaRepository, never()).excluir(any());
    }

    @Test
    void shouldThrowWhenRondaNotFound() {
        when(rondaRepository.buscarPorId("inexistente")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.executar("inexistente"))
                .isExactlyInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessageContaining("inexistente");

        verifyNoInteractions(politicaDeExclusao);
        verify(rondaRepository, never()).excluir(any());
    }
}
