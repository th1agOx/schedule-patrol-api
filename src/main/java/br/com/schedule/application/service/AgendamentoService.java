package br.com.schedule.application.service;

import br.com.schedule.application.dto.request.AgendamentoRequestDTO;
import br.com.schedule.application.dto.response.AgendamentoResponseDTO;
import br.com.schedule.application.dto.request.CancelamentoRequestDTO;
import br.com.schedule.application.dto.request.ReagendamentoRequestDTO;
import br.com.schedule.application.exception.RecursoNaoEncontradoException;
import br.com.schedule.application.mapper.AgendamentoDTOMapper;
import br.com.schedule.domain.model.AgendamentoRonda;
import br.com.schedule.domain.model.Periodo;
import br.com.schedule.domain.model.Ronda;
import br.com.schedule.domain.model.StatusAgendamento;
import br.com.schedule.domain.repository.AgendamentoRondaRepository;
import br.com.schedule.domain.repository.RondaRepository;
import br.com.schedule.domain.service.VerificadorDeConflitoDeAgenda;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.List;
import java.util.Optional;

/**
 * Orquestra o ciclo de vida do agendamento: busca, chama o comportamento do domínio, salva e responde.
 * O relógio é lido UMA vez por operação (clock.instant()) e passado ao domínio.
 *
 * Camada 2: cada método de mudança de estado é o ponto onde o evento será publicado no Kafka.
 */
@Service
public class AgendamentoService {

    private final RondaRepository rondaRepository;
    private final AgendamentoRondaRepository agendamentoRepository;
    private final VerificadorDeConflitoDeAgenda verificadorDeConflito;
    private final Clock clock;

    public AgendamentoService(RondaRepository rondaRepository,
            AgendamentoRondaRepository agendamentoRepository,
            VerificadorDeConflitoDeAgenda verificadorDeConflito,
            Clock clock) {
        this.rondaRepository = rondaRepository;
        this.agendamentoRepository = agendamentoRepository;
        this.verificadorDeConflito = verificadorDeConflito;
        this.clock = clock;
    }

    public List<AgendamentoResponseDTO> listar(StatusAgendamento status, String responsavel, String idRonda) {
        return agendamentoRepository.listar(status, responsavel, idRonda).stream()
                .map(AgendamentoDTOMapper::paraResponse)
                .toList();
    }

    public AgendamentoResponseDTO buscarPorId(String id) {
        return AgendamentoDTOMapper.paraResponse(buscar(id));
    }

    public AgendamentoResponseDTO agendar(AgendamentoRequestDTO request) {
        Ronda ronda = Optional.ofNullable(request.idRonda())
                .flatMap(rondaRepository::buscarPorId)
                .orElseThrow(() -> RecursoNaoEncontradoException.ronda(request.idRonda()));

        Periodo periodo = AgendamentoDTOMapper.paraPeriodo(request.periodoPrevisto());
        AgendamentoRonda agendamento = AgendamentoRonda.agendar(
                ronda, request.responsavel(), periodo, clock.instant());

        verificadorDeConflito.verificar(agendamento.getResponsavel(), agendamento.getPeriodoPrevisto());

        AgendamentoRonda salvo = agendamentoRepository.salvar(agendamento);

        return AgendamentoDTOMapper.paraResponse(salvo);
    }

    public AgendamentoResponseDTO iniciar(String id) {
        AgendamentoRonda agendamento = buscar(id);
        agendamento.iniciar();
        return AgendamentoDTOMapper.paraResponse(agendamentoRepository.salvar(agendamento));
    }

    public AgendamentoResponseDTO concluir(String id) {
        AgendamentoRonda agendamento = buscar(id);
        agendamento.concluir();
        return AgendamentoDTOMapper.paraResponse(agendamentoRepository.salvar(agendamento));
    }

    public AgendamentoResponseDTO cancelar(String id, CancelamentoRequestDTO request) {
        AgendamentoRonda agendamento = buscar(id);
        agendamento.cancelar(request.motivo());

        AgendamentoRonda salvo = agendamentoRepository.salvar(agendamento);

        return AgendamentoDTOMapper.paraResponse(salvo);
    }

    public AgendamentoResponseDTO reagendar(String id, ReagendamentoRequestDTO request) {
        AgendamentoRonda agendamento = buscar(id);
        Periodo periodo = AgendamentoDTOMapper.paraPeriodo(request.periodoPrevisto());

        agendamento.reagendar(periodo, clock.instant());
        verificadorDeConflito.verificar(agendamento.getResponsavel(), agendamento.getPeriodoPrevisto(),
                agendamento.getId());

        return AgendamentoDTOMapper.paraResponse(agendamentoRepository.salvar(agendamento));
    }

    private AgendamentoRonda buscar(String id) {
        return Optional.ofNullable(id)
                .flatMap(agendamentoRepository::buscarPorId)
                .orElseThrow(() -> RecursoNaoEncontradoException.agendamento(id));
    }
}
