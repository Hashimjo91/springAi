package org.example.springaitest.solrVectorStore;


public class SolrSearchVectorStoreOptions {
    private String indexName = "ms-marco";
    private int dimensions = 1024;
    private SimilarityFunction similarity;

    public SolrSearchVectorStoreOptions() {
        this.similarity = SimilarityFunction.cosine;
    }

    public String getIndexName() {
        return this.indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    public int getDimensions() {
        return this.dimensions;
    }

    public void setDimensions(int dims) {
        this.dimensions = dims;
    }

    public SimilarityFunction getSimilarity() {
        return this.similarity;
    }

    public void setSimilarity(SimilarityFunction similarity) {
        this.similarity = similarity;
    }
}


