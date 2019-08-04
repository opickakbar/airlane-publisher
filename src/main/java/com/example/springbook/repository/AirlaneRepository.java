package com.example.springbook.repository;

import org.springframework.data.repository.CrudRepository;

import com.example.springbook.model.Airlane;

public interface AirlaneRepository extends CrudRepository<Airlane, Integer> {

}
