package com.univ.tracedinclient;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class HelloRepository {

    private final JdbcTemplate jdbcTemplate;

    public void hello() {
        jdbcTemplate.execute("create table hello (id int, name varchar(100))");
    }

}
