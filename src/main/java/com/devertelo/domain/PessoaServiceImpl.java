package com.devertelo.domain;

import com.devertelo.application.exceptions.AlreadyExistsException;
import com.devertelo.controller.Pessoa;
import com.devertelo.infrastructure.PessoaEntity;
import com.devertelo.infrastructure.PessoaRepository;
import io.micronaut.context.annotation.Bean;
import org.hibernate.exception.ConstraintViolationException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Bean
public class PessoaServiceImpl implements PessoaService {

    private final PessoaRepository pessoaRepository;

    public PessoaServiceImpl(PessoaRepository pessoaRepository) {
        this.pessoaRepository = pessoaRepository;
    }

    public Pessoa create(Pessoa pessoa) {
        var entity = dtoToEntity(pessoa);
        try {
            var entitySaved = pessoaRepository.save(entity);
            return entityToDTO(entitySaved);
        } catch (ConstraintViolationException exception) {
            throw new AlreadyExistsException();
        }
    }

    @Override
    public Optional<Pessoa> getById(UUID id) {
        var entity = pessoaRepository.findById(id);
        return entity.map(this::entityToDTO);
    }

    public List<Pessoa> getAll(String term) {
        var entities = pessoaRepository.findByTerm(term);
        return entities.getContent().stream()
                .map(this::entityToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Long count() {
        return pessoaRepository.count();
    }

    private PessoaEntity dtoToEntity(Pessoa pessoa) {
        var stack = pessoa.stack() != null ? String.join(";", pessoa.stack()) : null;
        return new PessoaEntity(pessoa.apelido(), pessoa.nome(), pessoa.nascimento(), stack);
    }

    private Pessoa entityToDTO(PessoaEntity entity) {
        var stacks = entity.getStack() != null ? Arrays.stream(entity.getStack().split(";")).toList() : null;
        return new Pessoa(entity.getId(), entity.getApelido(), entity.getNome(), entity.getNascimento(), stacks);
    }
}
