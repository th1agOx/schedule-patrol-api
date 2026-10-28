package br.com.schedule.application.service;

import br.com.schedule.application.dto.PontoDTO;
import br.com.schedule.application.dto.request.RondaRequestDTO;
import br.com.schedule.application.dto.response.RondaResponseDTO;
import br.com.schedule.application.exception.RecursoNaoEncontradoException;
import br.com.schedule.domain.exception.PontoDuplicadoException;
import br.com.schedule.domain.exception.RondaComAgendamentosAtivosException;
import br.com.schedule.domain.model.Ronda;
import br.com.schedule.domain.repository.RondaRepository;
import br.com.schedule.domain.service.PoliticaDeExclusaoDeRonda;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static br.com.schedule.application.ApplicationTestFixtures.ronda;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RondaServiceTest {

    @Mock
    private RondaRepository rondaRepository;

    @Mock
    private PoliticaDeExclusaoDeRonda politicaDeExclusao;

    private RondaService service;

    @BeforeEach
    void setUp() {
        service = new RondaService(rondaRepository, politicaDeExclusao);
    }

    @Test
    void shouldListAllWhenFilterIsNull() {
        when(rondaRepository.listarTodas()).thenReturn(List.of(ronda("ronda-1"), ronda("ronda-2", false)));

        List<RondaResponseDTO> responses = service.listar(null);

        assertThat(responses).extracting(RondaResponseDTO::id)
                .containsExactly("ronda-1", "ronda-2");
        verify(rondaRepository).listarTodas();
        verify(rondaRepository, never()).listarPorAtiva(anyBoolean());
    }

    @Test
    void shouldListByActiveWhenFilterIsProvided() {
        when(rondaRepository.listarPorAtiva(false)).thenReturn(List.of(ronda("ronda-2", false)));

        List<RondaResponseDTO> responses = service.listar(false);

        assertThat(responses).extracting(RondaResponseDTO::id).containsExactly("ronda-2");
        assertThat(responses).extracting(RondaResponseDTO::ativa).containsExactly(false);
        verify(rondaRepository).listarPorAtiva(false);
        verify(rondaRepository, never()).listarTodas();
    }

    @Test
    void shouldFindById() {
        when(rondaRepository.buscarPorId("ronda-1")).thenReturn(Optional.of(ronda("ronda-1")));

        RondaResponseDTO response = service.buscarPorId("ronda-1");

        assertThat(response.id()).isEqualTo("ronda-1");
        assertThat(response.nome()).isEqualTo("Ronda Norte");
        assertThat(response.descricao()).isEqualTo("Inspeção diária");
        assertThat(response.ativa()).isTrue();
        assertThat(response.pontos()).containsExactly(new PontoDTO("P-01", "Portão principal"));
    }

    @Test
    void shouldThrowWhenRondaNotFound() {
        when(rondaRepository.buscarPorId("inexistente")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorId("inexistente"))
                .isExactlyInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessageContaining("Ronda")
                .hasMessageContaining("inexistente");
    }

    @Test
    void shouldCreateAndPersistRonda() {
        RondaRequestDTO request = new RondaRequestDTO("Ronda Norte", "Inspeção diária",
                List.of(new PontoDTO("P-01", "Portão principal")));
        when(rondaRepository.salvar(any(Ronda.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RondaResponseDTO response = service.criar(request);

        assertThat(response.id()).isNotBlank();
        assertThat(response.nome()).isEqualTo("Ronda Norte");
        assertThat(response.descricao()).isEqualTo("Inspeção diária");
        assertThat(response.ativa()).isTrue();
        assertThat(response.pontos()).containsExactly(new PontoDTO("P-01", "Portão principal"));

        ArgumentCaptor<Ronda> captor = ArgumentCaptor.forClass(Ronda.class);
        verify(rondaRepository).salvar(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(response.id());
        assertThat(captor.getValue().isAtiva()).isTrue();
    }

    @Test
    void shouldNormalizeBlankDescription() {
        RondaRequestDTO request = new RondaRequestDTO("Ronda Norte", "   ",
                List.of(new PontoDTO("P-01", "Portão principal")));
        when(rondaRepository.salvar(any(Ronda.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(service.criar(request).descricao()).isNull();
    }

    @Test
    void shouldPropagateDuplicatedPoint() {
        RondaRequestDTO request = new RondaRequestDTO("Ronda Norte", null,
                List.of(new PontoDTO("P-01", "Portão principal"),
                        new PontoDTO("p-01", "Portão secundário")));

        assertThatThrownBy(() -> service.criar(request))
                .isExactlyInstanceOf(PontoDuplicadoException.class);

        verify(rondaRepository, never()).salvar(any());
    }

    @Test
    void shouldUpdateRonda() {
        when(rondaRepository.buscarPorId("ronda-1")).thenReturn(Optional.of(ronda("ronda-1")));
        when(rondaRepository.salvar(any(Ronda.class))).thenAnswer(invocation -> invocation.getArgument(0));
        RondaRequestDTO request = new RondaRequestDTO("Ronda Sul", "Nova descrição",
                List.of(new PontoDTO("P-02", "Casa de válvulas")));

        RondaResponseDTO response = service.atualizar("ronda-1", request);

        assertThat(response.id()).isEqualTo("ronda-1");
        assertThat(response.nome()).isEqualTo("Ronda Sul");
        assertThat(response.descricao()).isEqualTo("Nova descrição");
        assertThat(response.pontos()).containsExactly(new PontoDTO("P-02", "Casa de válvulas"));
    }

    @Test
    void shouldActivateRonda() {
        when(rondaRepository.buscarPorId("ronda-1")).thenReturn(Optional.of(ronda("ronda-1", false)));
        when(rondaRepository.salvar(any(Ronda.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(service.ativar("ronda-1").ativa()).isTrue();
    }

    @Test
    void shouldDeactivateRonda() {
        when(rondaRepository.buscarPorId("ronda-1")).thenReturn(Optional.of(ronda("ronda-1", true)));
        when(rondaRepository.salvar(any(Ronda.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(service.desativar("ronda-1").ativa()).isFalse();
    }

    @Test
    void shouldDeleteWhenPolicyAllows() {
        when(rondaRepository.buscarPorId("ronda-1")).thenReturn(Optional.of(ronda("ronda-1")));

        service.excluir("ronda-1");

        verify(politicaDeExclusao).verificarExclusao("ronda-1");
        verify(rondaRepository).excluir("ronda-1");
    }

    @Test
    void shouldNotDeleteWhenPolicyBlocks() {
        when(rondaRepository.buscarPorId("ronda-1")).thenReturn(Optional.of(ronda("ronda-1")));
        doThrow(new RondaComAgendamentosAtivosException("ronda-1"))
                .when(politicaDeExclusao).verificarExclusao("ronda-1");

        assertThatThrownBy(() -> service.excluir("ronda-1"))
                .isExactlyInstanceOf(RondaComAgendamentosAtivosException.class);

        verify(rondaRepository, never()).excluir(anyString());
    }

    @Test
    void shouldThrowWhenDeletingInexistentRonda() {
        when(rondaRepository.buscarPorId("inexistente")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.excluir("inexistente"))
                .isExactlyInstanceOf(RecursoNaoEncontradoException.class);

        verifyNoInteractions(politicaDeExclusao);
        verify(rondaRepository, never()).excluir(anyString());
    }
}
