package br.com.schedule.infrastructure.config;

import br.com.schedule.application.usecase.agendamento.AgendarRondaUseCase;
import br.com.schedule.application.usecase.agendamento.BuscarAgendamentoPorIdUseCase;
import br.com.schedule.application.usecase.agendamento.CancelarAgendamentoUseCase;
import br.com.schedule.application.usecase.agendamento.ConcluirAgendamentoUseCase;
import br.com.schedule.application.usecase.agendamento.IniciarAgendamentoUseCase;
import br.com.schedule.application.usecase.agendamento.ListarAgendamentosUseCase;
import br.com.schedule.application.usecase.agendamento.ReagendarRondaUseCase;
import br.com.schedule.application.usecase.ronda.AtivarRondaUseCase;
import br.com.schedule.application.usecase.ronda.AtualizarRondaUseCase;
import br.com.schedule.application.usecase.ronda.BuscarRondaPorIdUseCase;
import br.com.schedule.application.usecase.ronda.CriarRondaUseCase;
import br.com.schedule.application.usecase.ronda.DesativarRondaUseCase;
import br.com.schedule.application.usecase.ronda.ExcluirRondaUseCase;
import br.com.schedule.application.usecase.ronda.ListarRondasUseCase;
import br.com.schedule.domain.repository.AgendamentoRondaRepository;
import br.com.schedule.domain.repository.RondaRepository;
import br.com.schedule.domain.service.PoliticaDeExclusaoDeRonda;
import br.com.schedule.domain.service.VerificadorDeConflitoDeAgenda;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration(proxyBeanMethods = false)
public class UseCaseConfig {

    @Bean
    public CriarRondaUseCase criarRondaUseCase(RondaRepository rondaRepository) {
        return new CriarRondaUseCase(rondaRepository);
    }

    @Bean
    public AtualizarRondaUseCase atualizarRondaUseCase(RondaRepository rondaRepository) {
        return new AtualizarRondaUseCase(rondaRepository);
    }

    @Bean
    public AtivarRondaUseCase ativarRondaUseCase(RondaRepository rondaRepository) {
        return new AtivarRondaUseCase(rondaRepository);
    }

    @Bean
    public DesativarRondaUseCase desativarRondaUseCase(RondaRepository rondaRepository) {
        return new DesativarRondaUseCase(rondaRepository);
    }

    @Bean
    public ExcluirRondaUseCase excluirRondaUseCase(RondaRepository rondaRepository,
            PoliticaDeExclusaoDeRonda politicaDeExclusaoDeRonda) {
        return new ExcluirRondaUseCase(rondaRepository, politicaDeExclusaoDeRonda);
    }

    @Bean
    public BuscarRondaPorIdUseCase buscarRondaPorIdUseCase(RondaRepository rondaRepository) {
        return new BuscarRondaPorIdUseCase(rondaRepository);
    }

    @Bean
    public ListarRondasUseCase listarRondasUseCase(RondaRepository rondaRepository) {
        return new ListarRondasUseCase(rondaRepository);
    }

    @Bean
    public AgendarRondaUseCase agendarRondaUseCase(RondaRepository rondaRepository,
            AgendamentoRondaRepository agendamentoRepository,
            VerificadorDeConflitoDeAgenda verificadorDeConflitoDeAgenda,
            Clock clock) {
        return new AgendarRondaUseCase(rondaRepository, agendamentoRepository,
                verificadorDeConflitoDeAgenda, clock);
    }

    @Bean
    public ReagendarRondaUseCase reagendarRondaUseCase(
            AgendamentoRondaRepository agendamentoRepository,
            VerificadorDeConflitoDeAgenda verificadorDeConflitoDeAgenda,
            Clock clock) {
        return new ReagendarRondaUseCase(agendamentoRepository, verificadorDeConflitoDeAgenda, clock);
    }

    @Bean
    public IniciarAgendamentoUseCase iniciarAgendamentoUseCase(
            AgendamentoRondaRepository agendamentoRepository) {
        return new IniciarAgendamentoUseCase(agendamentoRepository);
    }

    @Bean
    public ConcluirAgendamentoUseCase concluirAgendamentoUseCase(
            AgendamentoRondaRepository agendamentoRepository) {
        return new ConcluirAgendamentoUseCase(agendamentoRepository);
    }

    @Bean
    public CancelarAgendamentoUseCase cancelarAgendamentoUseCase(
            AgendamentoRondaRepository agendamentoRepository) {
        return new CancelarAgendamentoUseCase(agendamentoRepository);
    }

    @Bean
    public BuscarAgendamentoPorIdUseCase buscarAgendamentoPorIdUseCase(
            AgendamentoRondaRepository agendamentoRepository) {
        return new BuscarAgendamentoPorIdUseCase(agendamentoRepository);
    }

    @Bean
    public ListarAgendamentosUseCase listarAgendamentosUseCase(
            AgendamentoRondaRepository agendamentoRepository) {
        return new ListarAgendamentosUseCase(agendamentoRepository);
    }
}
