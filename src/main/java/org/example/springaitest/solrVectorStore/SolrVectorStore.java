package org.example.springaitest.solrVectorStore;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingOptionsBuilder;
import org.springframework.ai.vectorstore.AbstractVectorStoreBuilder;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionConverter;
import org.springframework.ai.vectorstore.observation.AbstractObservationVectorStore;
import org.springframework.ai.vectorstore.observation.VectorStoreObservationContext;
import org.springframework.util.Assert;
import com.google.common.primitives.Floats;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("NullableProblems")
public class SolrVectorStore extends AbstractObservationVectorStore {
    private final SolrClient solrClient;
    private final FilterExpressionConverter filterExpressionConverter;

    private final String collectionName;
    protected SolrVectorStore(Builder builder) {
        super(builder);
        this.collectionName = builder.collection;
        Assert.notNull(builder.solrClient, "SolrClient must not be null");
        this.filterExpressionConverter = builder.filterExConverter;
        this.solrClient = builder.solrClient;
    }

    public static Builder builder(SolrClient solrClient, EmbeddingModel embeddingModel) {
        return new Builder(solrClient, embeddingModel);
    }

    @Override
    public void doAdd(List<Document> documents) {
            List<float[]> embeddings = this.embeddingModel.embed(documents, EmbeddingOptionsBuilder.builder().build(), this.batchingStrategy);

            for (Document document : documents) {
                SolrInputDocument doc = new SolrInputDocument();
                doc.addField("id", document.getId());
                doc.addField("doi", document.getMetadata().get("doi"));
                doc.addField("vector", Floats.asList(embeddings.get(documents.indexOf(document))));
                doc.addField("content", document.getText());
                try {
                    solrClient.add(collectionName, doc);
                } catch (SolrServerException | IOException e) {
                    throw new RuntimeException(e);
                }
            }


    }

    @Override
    public void doDelete(@NotNull List<String> idList) {
        for (String id : idList) {
            try {
                solrClient.deleteById(collectionName, id);
            } catch (SolrServerException | IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

    @Override
    public void doDelete(@NotNull Filter.Expression filterExpression) {

            try {
                this.solrClient.deleteByQuery(collectionName, getSolrQueryString(filterExpression));
            } catch (Exception var3) {
                throw new IllegalStateException("Failed to delete documents by filter", var3);
            }

    }


    @Override
    public List<Document> doSimilaritySearch(SearchRequest searchRequest) {
        Assert.notNull(searchRequest, "The search request must not be null.");

        try {
            float threshold = (float) searchRequest.getSimilarityThreshold();
            float[] vectors = this.embeddingModel.embed(searchRequest.getQuery());
            String solrQueryString = this.getSolrQueryString(searchRequest.getFilterExpression());
            String similarityQuery = getSimilarityQuery(searchRequest, threshold, vectors);
            SolrQuery query = new SolrQuery();
            query.setQuery(similarityQuery);
            query.addFilterQuery(solrQueryString);

            QueryResponse response = this.solrClient.query(collectionName, query);
            return toDocument(response.getBeans(SolrSearchDocument.class));
        } catch (IOException | SolrServerException var6) {
            throw new RuntimeException(var6);
        }
    }

    private static String getSimilarityQuery(SearchRequest searchRequest, float threshold, float[] vectors) {
        return "{!vectorSimilarity f=vector minReturn=%s topK=%s}%s"
                .formatted(threshold, searchRequest.getTopK(), Floats.asList(vectors).toString());
    }

    private String getSolrQueryString(Filter.Expression filterExpression) {
        return Objects.isNull(filterExpression) ? "*" : this.filterExpressionConverter.convertExpression(filterExpression);
    }


    private List<Document> toDocument(List<SolrSearchDocument> document) {
        return document.stream().map(doc -> {
            String id = doc.id();
            String text = doc.content().get(0);
            ArrayList<Float> vectors = (ArrayList<Float>) doc.vector();
            String doi = doc.doi();
            return Document.builder()
                    .id(id)
                    .text(text)
                    .metadata("vector", vectors)
                    .metadata("doi", doi)
                    .build();
        }).collect(Collectors.toList());
    }
    @NotNull
    @Override
    public VectorStoreObservationContext.Builder createObservationContextBuilder(@NotNull String operationName) {
        String solrDataBaseSystem = "solr";
        return VectorStoreObservationContext
                .builder(solrDataBaseSystem, operationName)
                .collectionName(collectionName)
                .dimensions(this.embeddingModel.dimensions())
                .similarityMetric("cosine");
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> getNativeClient() {
        T client = (T) this.solrClient;
        return Optional.of(client);
    }

    public static class Builder extends AbstractVectorStoreBuilder<Builder> {
        private final SolrClient solrClient;
        private String collection = "core";
        private final FilterExpressionConverter filterExConverter = new SolrAiSearchFilterExpressionConverter();

        public Builder(SolrClient solrClient, EmbeddingModel embeddingModel) {
            super(embeddingModel);
            Assert.notNull(solrClient, "SolrClient must not be null");
            this.solrClient = solrClient;
        }

        public Builder collection(String collection) {
            Assert.notNull(collection, "collection must not be null");
            this.collection = collection;
            return this;
        }

        public SolrVectorStore build() {
            return new SolrVectorStore(this);
        }
    }
}

