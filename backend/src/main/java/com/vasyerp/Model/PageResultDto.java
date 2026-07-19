package com.vasyerp.Model;

import org.springframework.data.domain.Page;

import java.util.List;

public class PageResultDto<T> {
    private List<T> content;
    private long totalElements;
    private int totalPages;
    private int number;
    private int size;

    public static <T> PageResultDto<T> from(Page<T> page) {
        PageResultDto<T> dto = new PageResultDto<>();
        dto.content = page.getContent();
        dto.totalElements = page.getTotalElements();
        dto.totalPages = page.getTotalPages();
        dto.number = page.getNumber();
        dto.size = page.getSize();
        return dto;
    }

    public List<T> getContent() { return content; }
    public long getTotalElements() { return totalElements; }
    public int getTotalPages() { return totalPages; }
    public int getNumber() { return number; }
    public int getSize() { return size; }
}