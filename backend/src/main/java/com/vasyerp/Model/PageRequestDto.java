package com.vasyerp.Model;

public class PageRequestDto {
    private int page = 0;
    private int size = 10;
    private String search;

    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }
    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
    public String getSearch() { return search; }
    public void setSearch(String search) { this.search = search; }
}