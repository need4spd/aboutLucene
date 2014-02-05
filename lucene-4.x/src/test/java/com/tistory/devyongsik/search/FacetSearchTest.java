package com.tistory.devyongsik.search;

import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.facet.index.FacetFields;
import org.apache.lucene.facet.params.FacetSearchParams;
import org.apache.lucene.facet.search.CountFacetRequest;
import org.apache.lucene.facet.search.FacetResult;
import org.apache.lucene.facet.search.FacetResultNode;
import org.apache.lucene.facet.search.FacetsCollector;
import org.apache.lucene.facet.taxonomy.CategoryPath;
import org.apache.lucene.facet.taxonomy.TaxonomyReader;
import org.apache.lucene.facet.taxonomy.TaxonomyWriter;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyReader;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyWriter;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.FieldInfo;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FacetSearchTest {

    private String[] ids = {"1", "2", "3", "4", "5", "6"};
    private String[] titles = {"lucene in action", "about lucene", "java programming", "hadoop in action", "programming ruby", "hadoop definition guide"};
    private String[] categories = {"lucene", "lucene", "java", "hadoop", "ruby", "hadoop"};

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

            CategoryPath cp = new CategoryPath("category22", categories[i]);
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

        Query q = new MatchAllDocsQuery();

        TopScoreDocCollector tdc = TopScoreDocCollector.create(1, true);
        FacetSearchParams facetSearchParams = new FacetSearchParams(new CountFacetRequest(new CategoryPath("category22"), 10));
        FacetsCollector facetsCollector = FacetsCollector.create(facetSearchParams, indexSearcher.getIndexReader(), taxonomyReader);

        indexSearcher.search(q, MultiCollector.wrap(tdc, facetsCollector));
        List<FacetResult> facetResults = facetsCollector.getFacetResults();

        for (FacetResult fr : facetResults)
        {
            for ( FacetResultNode sr : fr.getFacetResultNode().subResults)
            {
                System.out.println(sr.label + ", " + sr.value);
            }
        }
    }
}
