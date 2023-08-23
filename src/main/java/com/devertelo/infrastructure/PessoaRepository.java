package com.devertelo.infrastructure;

import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.repository.JpaRepository;
import io.micronaut.data.jpa.repository.JpaSpecificationExecutor;
import io.micronaut.data.jpa.repository.criteria.Specification;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.transaction.annotation.Transactional;

import java.util.UUID;


@Repository
public interface PessoaRepository extends JpaRepository<PessoaEntity, UUID>, JpaSpecificationExecutor<PessoaEntity> {

    @Transactional
    default Page<PessoaEntity> findByTerm(String term) {

        Specification<PessoaEntity> specification = Specifications.apelidoLike(term)
                .or(Specifications.nameLike(term))
                .or(Specifications.stackLike(term));

        return findAll(specification, Pageable.from(0, 50));
    }

    class Specifications {
        public static Specification<PessoaEntity> apelidoLike(String apelido) {
            return (root, query, criteriaBuilder)
                    -> criteriaBuilder.like(root.get("apelido"), "%" + apelido + "%");
        }

        public static Specification<PessoaEntity> nameLike(String name) {
            return (root, query, criteriaBuilder)
                    -> criteriaBuilder.like(root.get("nome"), "%" + name + "%");
        }

        public static Specification<PessoaEntity> stackLike(String stack) {
            return (root, query, criteriaBuilder)
                    -> criteriaBuilder.like(root.get("stack"), "%" + stack + "%");
        }
    }
}
