package com.example.nearby.listViewSearch;

import java.util.List;

public class DataList {
    List<String> photo, name, id,address;

    public DataList(List<String> id, List<String> photo, List<String> name, List<String> address) {
        this.id = id;
        this.photo = photo;
        this.address = address;
        this.name = name;
    }
}
