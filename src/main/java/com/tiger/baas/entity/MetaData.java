package com.tiger.baas.entity;

import javax.persistence.*;

@Entity
@Table(name="metadata")
public class MetaData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long uuid;

    private String databaseid;

    private String fieldname;

    private String fieldtype;

    private String tablebelong;

    private String primarykey;
    public long getUuid() {
        return uuid;
    }

    public void setUuid(long uuid) {
        this.uuid = uuid;
    }

    public String getDatabaseid() {
        return databaseid;
    }

    public void setDatabaseid(String databaseid) {
        this.databaseid = databaseid;
    }

    public String getFieldname() {
        return fieldname;
    }

    public void setFieldname(String fieldname) {
        this.fieldname = fieldname;
    }

    public String getFieldtype() {
        return fieldtype;
    }

    public void setFieldtype(String fieldtype) {
        this.fieldtype = fieldtype;
    }

    public String getTablebelong() {
        return tablebelong;
    }

    public void setTablebelong(String tablebelong) {
        this.tablebelong = tablebelong;
    }

    public String getPrimarykey() {
        return primarykey;
    }

    public void setPrimarykey(String primarykey) {
        this.primarykey = primarykey;
    }
}
