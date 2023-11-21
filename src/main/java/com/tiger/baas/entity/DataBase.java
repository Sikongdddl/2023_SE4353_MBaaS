package com.tiger.baas.entity;

import javax.persistence.*;

@Table(name="example_db")
@Entity
public class DataBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long uuid;

    private String uname;

    private String databaseid;

    private String tablename;

    public long getUuid() {
        return uuid;
    }

    public void setUuid(long uuid) {
        this.uuid = uuid;
    }

    public String getUname() {
        return uname;
    }

    public void setUname(String uname) {
        this.uname = uname;
    }

    public String getDatabaseid() {
        return databaseid;
    }

    public void setDatabaseid(String databaseid) {
        this.databaseid = databaseid;
    }

    public String getTablename() {
        return tablename;
    }

    public void setTablename(String tablename) {
        this.tablename = tablename;
    }
}
