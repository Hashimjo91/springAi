package org.example.springaitest.solrVectorStore;

import org.apache.solr.client.solrj.beans.Field;

import java.util.List;

public class SolrSearchDocument {
    @Field
    private String id;
    @Field
    private List<String> content;
    @Field
    private String doi;
    @Field
    private List<Float> vector;

    @Field
    private Float score;

    public SolrSearchDocument() {
    }

    public SolrSearchDocument(String id, List<String> content, String doi, List<Float> vector, Float score) {
        this.id = id;
        this.content = content;
        this.doi = doi;
        this.vector = vector;
        this.score = score;
    }

    public String id() {
        return this.id;
    }

    public List<String> content() {
        return this.content;
    }

    public String doi() {return doi;}

    public List<Float> vector() {
        return this.vector;
    }

    public Float score() {
        return score;
    }
}