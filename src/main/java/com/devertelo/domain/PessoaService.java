package com.devertelo.domain;

import com.devertelo.controller.Pessoa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PessoaService {

    Pessoa create(Pessoa pessoa);

    List<Pessoa> getAll(String term);

    Optional<Pessoa> getById(UUID id);

    Long count();

}
