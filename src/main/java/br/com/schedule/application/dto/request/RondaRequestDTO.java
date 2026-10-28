package br.com.schedule.application.dto.request;

import java.util.List;

import br.com.schedule.application.dto.PontoDTO;

// criar e atualizar uma ronda os campos são os mesmos 
public record RondaRequestDTO(String nome, String descricao, List<PontoDTO> pontos) {
}
