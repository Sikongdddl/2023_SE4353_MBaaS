package com.tiger.baas.repository;

import com.tiger.baas.entity.MetaData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MetaDataRepo extends JpaRepository<MetaData,Long> {
    List<MetaData> findByDatabaseid(String database_id);
    MetaData findByFieldname(String fieldname);
}
