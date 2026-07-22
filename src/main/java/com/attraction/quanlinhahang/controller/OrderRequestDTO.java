package com.attraction.quanlinhahang.controller;

import java.util.Map;

public class OrderRequestDTO {
    private Map<Long, Integer> items;
    private Map<Long, String> notes;

    public Map<Long, Integer> getItems() { return items; }
    public void setItems(Map<Long, Integer> items) { this.items = items; }

    public Map<Long, String> getNotes() { return notes; }
    public void setNotes(Map<Long, String> notes) { this.notes = notes; }
}
