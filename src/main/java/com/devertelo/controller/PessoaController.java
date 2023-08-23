package com.devertelo.controller;

import com.devertelo.application.exceptions.AlreadyExistsException;
import com.devertelo.domain.PessoaService;
import io.lettuce.core.api.StatefulRedisConnection;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;
import io.micronaut.http.uri.UriBuilder;
import io.micronaut.serde.ObjectMapper;
import io.micronaut.validation.Validated;
import io.micronaut.validation.validator.Validator;
import jakarta.validation.Valid;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Controller
@Validated
public class PessoaController {

    private final Validator validator;
    private final PessoaService pessoaService;
    private final StatefulRedisConnection<String, String> connection;

    private final ObjectMapper objectMapper;


    public PessoaController(Validator validator, PessoaService pessoaService, StatefulRedisConnection<String, String> connection, ObjectMapper objectMapper) {
        this.validator = validator;
        this.pessoaService = pessoaService;
        this.connection = connection;
        this.objectMapper = objectMapper;
    }

    @Post("/pessoas")
    public HttpResponse<Pessoa> post(HttpRequest httpRequest, @Body @Valid Pessoa pessoa) {
        try {
            var redis = connection.sync();
            var cache = redis.get(pessoa.apelido());
            if (cache != null) {
                throw new AlreadyExistsException();
            }
            var violations = validator.validate(pessoa);
            if (!violations.isEmpty()) {
                return HttpResponse.unprocessableEntity();
            }
            var response = pessoaService.create(pessoa);

            var value = objectMapper.writeValueAsString(response);
            redis.set(response.apelido(), value);
            redis.set(response.id().toString(), value);

            var location = UriBuilder.of(httpRequest.getUri()).path(response.id().toString()).build();
            return HttpResponse.created(response, location);

        } catch (AlreadyExistsException exception) {
            return HttpResponse.unprocessableEntity();
        } catch (IOException e) {
            return HttpResponse.serverError();
        }
    }

    @Get("/pessoas/{id}")
    public HttpResponse<Pessoa> get(@PathVariable UUID id) {
        var redis = connection.sync();
        var cacheString = redis.get(id.toString());
        if (cacheString != null) {
            try {
                return HttpResponse.ok(objectMapper.readValue(cacheString, Pessoa.class));
            } catch (IOException e) {
                return HttpResponse.serverError();
            }
        }
        var pessoa = pessoaService.getById(id);

        return pessoa.map(HttpResponse::ok)
                .orElse(HttpResponse.badRequest());
    }

    @Get("/pessoas")
    public HttpResponse<List<Pessoa>> get(@QueryValue String t) {
        if (t == null) {
            return HttpResponse.badRequest();
        }

        return HttpResponse.ok(pessoaService.getAll(t));
    }

    @Get("/contagem-pessoas")
    public HttpResponse<Long> get() {
        return HttpResponse.ok(pessoaService.count());
    }
}
