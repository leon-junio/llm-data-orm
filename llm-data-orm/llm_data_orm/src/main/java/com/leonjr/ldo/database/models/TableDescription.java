package com.leonjr.ldo.database.models;

import java.util.List;

import com.google.gson.Gson;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class TableDescription {
    @NotBlank(message = "Table name cannot be blank")
    private String name;
    private List<ColumnDescription> columns;

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Table: ").append(name).append("\n");
        if (columns != null) {
            columns.forEach(column -> sb.append(column.toString()).append("\n"));
        }
        return sb.toString();
    }

    /**
     * Convert the object to JSON string
     * 
     * @return JSON string representation of the object
     */
    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

}
