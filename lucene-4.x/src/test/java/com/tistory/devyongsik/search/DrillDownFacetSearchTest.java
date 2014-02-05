package com.tistory.devyongsik.search;

import com.google.common.collect.Lists;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.facet.index.FacetFields;
import org.apache.lucene.facet.params.FacetSearchParams;
import org.apache.lucene.facet.search.*;
import org.apache.lucene.facet.taxonomy.CategoryPath;
import org.apache.lucene.facet.taxonomy.TaxonomyReader;
import org.apache.lucene.facet.taxonomy.TaxonomyWriter;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyReader;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyWriter;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.FieldInfo;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.complexPhrase.ComplexPhraseQueryParser;
import org.apache.lucene.facet.search.DrillDownQuery;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DrillDownFacetSearchTest {
    private String[] ids = {"1", "2", "3", "4", "5", "6"};
    private String[] titles = {"lucene in action", "about lucene", "java programming", "hadoop in action", "programming ruby", "hadoop definition guide"};
    private String[] categories = {"java/lucene", "java/lucene", "java/java", "java/hadoop", "ruby/ruby", "ruby/hadoop"};

    private Directory indexDirectory = new RAMDirectory();
    private Directory taxonomyDirectory = new RAMDirectory();

    private IndexWriter getWriter() throws IOException {
        IndexWriterConfig conf = new IndexWriterConfig(Version.LUCENE_46, new WhitespaceAnalyzer(Version.LUCENE_46));

        IndexWriter indexWriter = new IndexWriter(indexDirectory, conf);

        return indexWriter;
    }

    private TaxonomyWriter getTaxonomyWriter() throws IOException {
        TaxonomyWriter taxonomyWriter = new DirectoryTaxonomyWriter(taxonomyDirectory, IndexWriterConfig.OpenMode.CREATE);

        return taxonomyWriter;
    }

    @Before
    public void init() throws IOException {

        IndexWriter indexWriter = getWriter();
        TaxonomyWriter taxonomyWriter = getTaxonomyWriter();
        FacetFields facetFields = new FacetFields(taxonomyWriter);

        for(int i = 0; i < ids.length; i++) {
            List<CategoryPath> categoryPathList = new ArrayList<>();
            Document doc = new Document();

            FieldType fieldType = new FieldType();
            fieldType.setIndexed(true);
            fieldType.setStored(true);
            fieldType.setTokenized(false);
            fieldType.setIndexOptions(FieldInfo.IndexOptions.DOCS_AND_FREQS);
            fieldType.setStoreTermVectors(true);

            doc.add(new Field("ids", ids[i], fieldType));
            doc.add(new Field("title", titles[i], fieldType));
            doc.add(new Field("category", categories[i], fieldType));

            CategoryPath cp = new CategoryPath("category/" + categories[i], '/');
            categoryPathList.add(cp);
            //taxonomyWriter.addCategory(cp); //not sure if necessary

            facetFields.addFields(doc, categoryPathList);
            indexWriter.addDocument(doc);
        }

        indexWriter.commit();
        indexWriter.close();

        taxonomyWriter.commit();
        taxonomyWriter.close();
    }

    @Test
    public void search() throws IOException {
        IndexSearcher indexSearcher = new IndexSearcher(DirectoryReader.open(indexDirectory));
        TaxonomyReader taxonomyReader = new DirectoryTaxonomyReader(taxonomyDirectory);

        CategoryPath drillDownCategoryPath = new CategoryPath("category" + "/" + "java", '/');
        CategoryPath drillDownCategoryPath2 = new CategoryPath("category" + "/" + "ruby", '/');

        TopScoreDocCollector tdc = TopScoreDocCollector.create(1, true);

        List<FacetRequest> countFacetRequests = Lists.newArrayList();
        countFacetRequests.add(new CountFacetRequest(new CategoryPath("category"), 10));
        countFacetRequests.add(new CountFacetRequest(drillDownCategoryPath, 10));
        countFacetRequests.add(new CountFacetRequest(drillDownCategoryPath2, 10));


        FacetSearchParams facetSearchParams = new FacetSearchParams(countFacetRequests);
        FacetsCollector facetsCollector = FacetsCollector.create(facetSearchParams, indexSearcher.getIndexReader(), taxonomyReader);

        DrillDownQuery q = new DrillDownQuery(facetSearchParams.indexingParams, new MatchAllDocsQuery());

        indexSearcher.search(q, MultiCollector.wrap(tdc, facetsCollector));
        List<FacetResult> facetResults = facetsCollector.getFacetResults();

        for (FacetResult facetResult : facetResults) {
            FacetResultNode resultNode = facetResult.getFacetResultNode();

            if (resultNode.subResults.size() > 0) {
                int numSubResults = resultNode.subResults.size();
                String facetName = resultNode.label.toString();

                System.out.println("## " + facetName + ": " + numSubResults);

                for (FacetResultNode node : resultNode.subResults) {
                    String label = node.label.toString();
                    Integer count = (int) node.value;

                    System.out.println(label + ": " + count);
                }
            }
        }
    }
}
