package com.leonjr.ldo.confs.enums;

public enum DatabaseType {
    POSTGRES("org.postgresql.Driver"),
    MYSQL("com.mysql.cj.jdbc.Driver");

    private String driverClassName;

    DatabaseType(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    public String getDriverClassName() {
        return driverClassName;
    }

}
