package com.tiger.baas.repository;

import com.tiger.baas.entity.MetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
public interface MetaDataRepo extends JpaRepository<MetaData,Long> {

    List<MetaData> findByDatabaseid(String database_id);

    @Transactional
    void deleteByTablebelong(String tablebelong);

    MetaData findByFieldname(String fieldname);
}
