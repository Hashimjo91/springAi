package org.example.springaitest.solrVectorStore;

public enum SimilarityFunction {
    l2_norm,
    dot_product,
    cosine;

    private SimilarityFunction() {
    }
}
