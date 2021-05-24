package com.example.sketch_pad;

// Модель/сущность для работы со списком записей
class Sketch{
    private int id;
    private String date_time;
    private String label_sketch;
    private String text_sketch;

    public Sketch(int id, String date_time, String label_sketch, String text_sketch) {
        this.id = id;
        this.date_time = date_time;
        this.label_sketch = label_sketch;
        this.text_sketch = text_sketch;
    }

    public int getId() {
        return id;
    }

    public String getDate_time() {
        return date_time;
    }

    public String getLabel_sketch() {
        return label_sketch;
    }

    public String getText_sketch() {
        return text_sketch;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setDate_time(String date_time) {
        this.date_time = date_time;
    }

    public void setLabel_sketch(String label_sketch) {
        this.label_sketch = label_sketch;
    }

    public void setText_sketch(String text_sketch) {
        this.text_sketch = text_sketch;
    }

    @Override
    public String toString() {
        return "Sketch{" +
                "id=" + id +
                ", date_time='" + date_time + '\'' +
                ", label_sketch='" + label_sketch + '\'' +
                ", text_sketch='" + text_sketch + '\'' +
                '}';
    }
}