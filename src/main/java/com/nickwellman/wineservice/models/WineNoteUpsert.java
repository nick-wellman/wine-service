package com.nickwellman.wineservice.models;

import lombok.Data;

@Data
public class WineNoteUpsert {
    private String id;
    private String note;
}
