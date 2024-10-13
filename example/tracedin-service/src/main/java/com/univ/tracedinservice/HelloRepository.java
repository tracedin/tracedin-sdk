package com.univ.tracedinservice;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class HelloRepository {

    private final JdbcTemplate jdbcTemplate;

    public void createTable() {
        jdbcTemplate.execute("create table hello (id int, name varchar(100))");
    }

    public void insertData() {
        jdbcTemplate.execute("insert into hello values (1, 'Hello')");
    }



}
